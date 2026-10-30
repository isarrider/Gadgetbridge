package nodomain.freeyourgadget.gadgetbridge.service.devices.polar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import nodomain.freeyourgadget.gadgetbridge.model.WeatherSpec;
import nodomain.freeyourgadget.gadgetbridge.proto.polar.PolarProto;
import nodomain.freeyourgadget.gadgetbridge.proto.polar.PolarProto.PbWeatherIcon.PbStandardWeatherIcon;
import nodomain.freeyourgadget.gadgetbridge.proto.polar.PolarProto.PbWeatherIcon.PbStandardWeatherIcon.PbPrecipitation;

/**
 * Builds the forecast file the watch shows on its weather view. Polar Flow downloads it ready made
 * from Polar's server, so the layout is known but the units are assumed: degrees Celsius and
 * meters per second, like everywhere else in Polar's files.
 */
public final class PolarWeather {
    public static final String PATH = "/U/0/WEATHER/FORECAST.BPB";

    private static final int DAYS = 4;

    private PolarWeather() {
    }

    @NonNull
    public static PolarProto.PbWeatherForecast forecast(@Nullable final WeatherSpec spec, final ZoneId zone) {
        final PolarProto.PbWeatherForecast.Builder forecast = PolarProto.PbWeatherForecast.newBuilder();
        if (spec == null) {
            return forecast.setErrorCode(PolarProto.PbWeatherForecast.PbErrorCode.LOCATION_NOT_AVAILABLE_ERROR).build();
        }
        if (spec.getLocation() != null) {
            forecast.setLocation(PolarProto.PbOneLineText.newBuilder().setText(spec.getLocation()));
        }
        final LocalDateTime issued = LocalDateTime.ofInstant(Instant.ofEpochSecond(spec.getTimestamp()), ZoneOffset.UTC);
        forecast.setForecastTime(PolarProto.PbSystemDateTime.newBuilder()
                .setDate(date(issued.toLocalDate()))
                .setTime(time(issued.toLocalTime()))
                .setTrusted(true));
        if (spec.getLatitude() != 0 || spec.getLongitude() != 0) {
            forecast.setMobilePhoneCoordinates(PolarProto.PbMobilePhoneCoordinates.newBuilder()
                    .setLatitude(spec.getLatitude())
                    .setLongitude(spec.getLongitude()));
        }

        // Hourly values by local day; the current conditions stand in for the running hour
        final Map<LocalDate, List<PolarProto.PbHourlyWeatherForecast>> hourly = new TreeMap<>();
        final LocalDateTime now = LocalDateTime.ofInstant(Instant.ofEpochSecond(spec.getTimestamp()), zone);
        hourly.computeIfAbsent(now.toLocalDate(), d -> new ArrayList<>()).add(hour(now.getHour(),
                spec.getCurrentConditionCode(), spec.getCurrentTemp(), spec.getFeelsLikeTemp(), spec.getCurrentHumidity(),
                spec.getWindSpeed(), spec.getWindDirection(), spec.getPressure(), isNight(spec, now.toLocalTime(), zone)));
        for (final WeatherSpec.Hourly h : spec.getHourly()) {
            final LocalDateTime at = LocalDateTime.ofInstant(Instant.ofEpochSecond(h.getTimestamp()), zone);
            if (!at.isAfter(now.withMinute(59))) {
                continue;
            }
            hourly.computeIfAbsent(at.toLocalDate(), d -> new ArrayList<>()).add(hour(at.getHour(),
                    h.getConditionCode(), h.getTemp(), 0, h.getHumidity(), h.getWindSpeed(), h.getWindDirection(), 0,
                    isNight(spec, at.toLocalTime(), zone)));
        }

        for (int i = 0; i < DAYS; i++) {
            final LocalDate day = now.toLocalDate().plusDays(i);
            final PolarProto.PbDailyWeatherForecast.Builder daily = PolarProto.PbDailyWeatherForecast.newBuilder().setDate(date(day));
            final List<PolarProto.PbHourlyWeatherForecast> hours = hourly.get(day);
            if (hours != null) {
                daily.setHourlyWeatherForecasts(PolarProto.PbDailyWeatherForecast.PbHourlyWeatherForecasts.newBuilder()
                        .addAllHourlyWeatherForecasts(hours));
                daily.setThreeHourWeatherForecasts(blocks(hours, 3));
                daily.setSixHourWeatherForecasts(blocks(hours, 6));
            } else {
                // Days beyond the hourly forecast: the whole day in six hour blocks of the daily values
                final WeatherSpec.Daily d = dailyFor(spec, i);
                if (d == null) {
                    break;
                }
                final PolarProto.PbDailyWeatherForecast.PbMultihourWeatherForecasts.Builder blocks =
                        PolarProto.PbDailyWeatherForecast.PbMultihourWeatherForecasts.newBuilder();
                for (int h = 0; h < 24; h += 6) {
                    blocks.addMultihourWeatherForecasts(PolarProto.PbMultihourWeatherForecast.newBuilder()
                            .setHour(h)
                            .setLength(6)
                            .setWeatherIcon(icon(d.getConditionCode(), h < 6))
                            .setMinTemperature(celsius(d.getMinTemp()))
                            .setMaxTemperature(celsius(d.getMaxTemp())));
                }
                daily.setSixHourWeatherForecasts(blocks);
            }
            forecast.addDailyForecasts(daily);
        }
        return forecast.build();
    }

    /** The daily forecast {@code offset} days from today; the spec's forecasts start tomorrow. */
    @Nullable
    private static WeatherSpec.Daily dailyFor(final WeatherSpec spec, final int offset) {
        if (offset == 0) {
            final WeatherSpec.Daily today = new WeatherSpec.Daily();
            today.setMinTemp(spec.getTodayMinTemp());
            today.setMaxTemp(spec.getTodayMaxTemp());
            today.setConditionCode(spec.getCurrentConditionCode());
            return today;
        }
        return offset - 1 < spec.getForecasts().size() ? spec.getForecasts().get(offset - 1) : null;
    }

    private static PolarProto.PbHourlyWeatherForecast hour(final int hour, final int condition, final int kelvin,
                                                           final int feelsLikeKelvin, final int humidity, final float windKmh,
                                                           final int windDirection, final float pressure, final boolean night) {
        final PolarProto.PbHourlyWeatherForecast.Builder builder = PolarProto.PbHourlyWeatherForecast.newBuilder()
                .setHour(hour)
                .setWeatherIcon(icon(condition, night))
                .setTemperature(celsius(kelvin))
                .setFeelsLikeTemperature(celsius(feelsLikeKelvin != 0 ? feelsLikeKelvin : kelvin))
                .setHumidity(humidity)
                .setWindSpeed(windKmh / 3.6f)
                .setWindDirection(windDirection);
        if (pressure > 0) {
            builder.setSeaLevelPressure(pressure);
        }
        return builder.build();
    }

    /** Hours grouped into blocks of {@code length} hours, starting at multiples of it. */
    private static PolarProto.PbDailyWeatherForecast.PbMultihourWeatherForecasts blocks(
            final List<PolarProto.PbHourlyWeatherForecast> hours, final int length) {
        final Map<Integer, List<PolarProto.PbHourlyWeatherForecast>> byBlock = new TreeMap<>();
        for (final PolarProto.PbHourlyWeatherForecast hour : hours) {
            byBlock.computeIfAbsent(hour.getHour() / length * length, k -> new ArrayList<>()).add(hour);
        }
        final PolarProto.PbDailyWeatherForecast.PbMultihourWeatherForecasts.Builder blocks =
                PolarProto.PbDailyWeatherForecast.PbMultihourWeatherForecasts.newBuilder();
        for (final Map.Entry<Integer, List<PolarProto.PbHourlyWeatherForecast>> entry : byBlock.entrySet()) {
            final List<PolarProto.PbHourlyWeatherForecast> block = entry.getValue();
            float min = Float.MAX_VALUE;
            float max = -Float.MAX_VALUE;
            for (final PolarProto.PbHourlyWeatherForecast hour : block) {
                min = Math.min(min, hour.getTemperature());
                max = Math.max(max, hour.getTemperature());
            }
            blocks.addMultihourWeatherForecasts(PolarProto.PbMultihourWeatherForecast.newBuilder()
                    .setHour(entry.getKey())
                    .setLength(length)
                    .setWeatherIcon(block.get(block.size() / 2).getWeatherIcon())
                    .setMinTemperature(min)
                    .setMaxTemperature(max));
        }
        return blocks.build();
    }

    private static boolean isNight(final WeatherSpec spec, final LocalTime time, final ZoneId zone) {
        if (spec.getSunRise() == 0 || spec.getSunSet() == 0) {
            return time.getHour() < 6 || time.getHour() >= 21;
        }
        final LocalTime rise = LocalTime.ofInstant(Instant.ofEpochSecond(spec.getSunRise()), zone);
        final LocalTime set = LocalTime.ofInstant(Instant.ofEpochSecond(spec.getSunSet()), zone);
        return time.isBefore(rise) || !time.isBefore(set);
    }

    /** Maps an OpenWeatherMap condition code to the watch's icon parts. */
    static PolarProto.PbWeatherIcon icon(final int condition, final boolean night) {
        final PolarProto.PbWeatherIcon.Builder icon = PolarProto.PbWeatherIcon.newBuilder();
        if (condition >= 700 && condition < 800) {
            return icon.setIconType(PolarProto.PbWeatherIcon.PbWeatherIconType.MIST_ICON).build();
        }
        final PbStandardWeatherIcon.Builder standard = PbStandardWeatherIcon.newBuilder()
                .setTimeOfDay(night ? PbStandardWeatherIcon.PbTimeOfDay.NIGHT : PbStandardWeatherIcon.PbTimeOfDay.DAY)
                .setThunder(condition >= 200 && condition < 300);
        if (condition == 800) {
            standard.setCloudiness(PbStandardWeatherIcon.PbCloudiness.NO_CLOUDS);
        } else if (condition == 801) {
            standard.setCloudiness(PbStandardWeatherIcon.PbCloudiness.FEW_CLOUDS);
        } else if (condition == 802) {
            standard.setCloudiness(PbStandardWeatherIcon.PbCloudiness.SCATTERED_CLOUDS);
        } else if (condition == 803) {
            standard.setCloudiness(PbStandardWeatherIcon.PbCloudiness.BROKEN_CLOUDS);
        } else {
            standard.setCloudiness(PbStandardWeatherIcon.PbCloudiness.OVERCAST_CLOUDS);
            final PbPrecipitation precipitation = precipitation(condition);
            if (precipitation != null) {
                standard.setPrecipitation(precipitation);
            }
        }
        return icon.setIconType(PolarProto.PbWeatherIcon.PbWeatherIconType.STANDARD_ICON)
                .setStandardWeatherIcon(standard)
                .build();
    }

    @Nullable
    private static PbPrecipitation precipitation(final int condition) {
        final PbPrecipitation.PbPrecipitationType type;
        PbPrecipitation.PbPrecipitationVolume volume = PbPrecipitation.PbPrecipitationVolume.MODERATE_PRECIPITATION;
        if (condition >= 200 && condition < 300) {
            type = PbPrecipitation.PbPrecipitationType.RAIN;
            if (condition == 200 || condition == 210 || condition == 230) {
                volume = PbPrecipitation.PbPrecipitationVolume.LIGHT_PRECIPITATION;
            } else if (condition == 202 || condition == 212) {
                volume = PbPrecipitation.PbPrecipitationVolume.HEAVY_PRECIPITATION;
            }
        } else if (condition >= 300 && condition < 400) {
            type = PbPrecipitation.PbPrecipitationType.DRIZZLE;
            volume = PbPrecipitation.PbPrecipitationVolume.LIGHT_PRECIPITATION;
        } else if (condition == 511) {
            type = PbPrecipitation.PbPrecipitationType.SLEET;
        } else if (condition >= 500 && condition < 600) {
            type = PbPrecipitation.PbPrecipitationType.RAIN;
            if (condition == 500 || condition == 520) {
                volume = PbPrecipitation.PbPrecipitationVolume.LIGHT_PRECIPITATION;
            } else if (condition >= 502 && condition != 521) {
                volume = PbPrecipitation.PbPrecipitationVolume.HEAVY_PRECIPITATION;
            }
        } else if (condition >= 611 && condition <= 616) {
            type = PbPrecipitation.PbPrecipitationType.SLEET;
        } else if (condition >= 600 && condition < 700) {
            type = PbPrecipitation.PbPrecipitationType.SNOW;
            if (condition == 600 || condition == 620) {
                volume = PbPrecipitation.PbPrecipitationVolume.LIGHT_PRECIPITATION;
            } else if (condition == 602 || condition == 622) {
                volume = PbPrecipitation.PbPrecipitationVolume.HEAVY_PRECIPITATION;
            }
        } else {
            return null;
        }
        return PbPrecipitation.newBuilder().setPrecipitationType(type).setPrecipitationVolume(volume).build();
    }

    private static float celsius(final int kelvin) {
        return kelvin - 273.15f;
    }

    private static PolarProto.PbDate date(final LocalDate date) {
        return PolarProto.PbDate.newBuilder().setYear(date.getYear()).setMonth(date.getMonthValue()).setDay(date.getDayOfMonth()).build();
    }

    private static PolarProto.PbTime time(final LocalTime time) {
        return PolarProto.PbTime.newBuilder().setHour(time.getHour()).setMinute(time.getMinute()).setSeconds(time.getSecond()).setMillis(0).build();
    }
}
