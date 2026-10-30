/*  Copyright (C) 2026 Gadgetbridge contributors

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.service.devices.polar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;

import nodomain.freeyourgadget.gadgetbridge.model.WeatherSpec;
import nodomain.freeyourgadget.gadgetbridge.proto.polar.PolarProto;
import nodomain.freeyourgadget.gadgetbridge.proto.polar.PolarProto.PbWeatherIcon.PbStandardWeatherIcon;

public class PolarWeatherTest {
    private static final ZoneOffset ZONE = ZoneOffset.ofHours(1);

    @Test
    public void missingWeatherTellsTheWatchThereIsNoLocation() {
        assertEquals(PolarProto.PbWeatherForecast.PbErrorCode.LOCATION_NOT_AVAILABLE_ERROR,
                PolarWeather.forecast(null, ZONE).getErrorCode());
    }

    @Test
    public void conditionsMapToIconParts() {
        final PolarProto.PbWeatherIcon rain = PolarWeather.icon(502, false);
        assertEquals(PbStandardWeatherIcon.PbPrecipitation.PbPrecipitationType.RAIN,
                rain.getStandardWeatherIcon().getPrecipitation().getPrecipitationType());
        assertEquals(PbStandardWeatherIcon.PbPrecipitation.PbPrecipitationVolume.HEAVY_PRECIPITATION,
                rain.getStandardWeatherIcon().getPrecipitation().getPrecipitationVolume());

        final PolarProto.PbWeatherIcon clearNight = PolarWeather.icon(800, true);
        assertEquals(PbStandardWeatherIcon.PbCloudiness.NO_CLOUDS, clearNight.getStandardWeatherIcon().getCloudiness());
        assertEquals(PbStandardWeatherIcon.PbTimeOfDay.NIGHT, clearNight.getStandardWeatherIcon().getTimeOfDay());
        assertFalse(clearNight.getStandardWeatherIcon().hasPrecipitation());

        assertTrue(PolarWeather.icon(211, false).getStandardWeatherIcon().getThunder());
        assertEquals(PolarProto.PbWeatherIcon.PbWeatherIconType.MIST_ICON, PolarWeather.icon(741, false).getIconType());
    }

    @Test
    public void forecastHasHourlyBlocksTodayAndDailyValuesLater() {
        final long noon = LocalDate.of(2026, 10, 30).atTime(12, 20).toEpochSecond(ZONE);
        final WeatherSpec spec = new WeatherSpec();
        spec.setTimestamp((int) noon);
        spec.setLocation("Wien");
        spec.setCurrentTemp(283); // 9.85 °C
        spec.setCurrentConditionCode(800);
        spec.setWindSpeed(36f); // km/h
        spec.setTodayMinTemp(278);
        spec.setTodayMaxTemp(285);
        for (int h = 1; h <= 3; h++) {
            final WeatherSpec.Hourly hourly = new WeatherSpec.Hourly();
            hourly.setTimestamp((int) (noon + h * 3600L));
            hourly.setTemp(283 + h);
            hourly.setConditionCode(500);
            spec.getHourly().add(hourly);
        }
        final WeatherSpec.Daily tomorrow = new WeatherSpec.Daily();
        tomorrow.setMinTemp(275);
        tomorrow.setMaxTemp(280);
        tomorrow.setConditionCode(600);
        spec.getForecasts().add(tomorrow);

        final PolarProto.PbWeatherForecast forecast = PolarWeather.forecast(spec, ZONE);

        assertEquals("Wien", forecast.getLocation().getText());
        assertEquals(2, forecast.getDailyForecastsCount());
        final PolarProto.PbDailyWeatherForecast today = forecast.getDailyForecasts(0);
        assertEquals(30, today.getDate().getDay());
        assertEquals(4, today.getHourlyWeatherForecasts().getHourlyWeatherForecastsCount());
        final PolarProto.PbHourlyWeatherForecast now = today.getHourlyWeatherForecasts().getHourlyWeatherForecasts(0);
        assertEquals(12, now.getHour());
        assertEquals(9.85f, now.getTemperature(), 0.01f);
        assertEquals(10f, now.getWindSpeed(), 0.01f);
        // 12-14 and 15 in three hour blocks
        assertEquals(2, today.getThreeHourWeatherForecasts().getMultihourWeatherForecastsCount());
        assertEquals(12, today.getThreeHourWeatherForecasts().getMultihourWeatherForecasts(0).getHour());

        final PolarProto.PbDailyWeatherForecast next = forecast.getDailyForecasts(1);
        assertEquals(4, next.getSixHourWeatherForecasts().getMultihourWeatherForecastsCount());
        assertEquals(1.85f, next.getSixHourWeatherForecasts().getMultihourWeatherForecasts(0).getMinTemperature(), 0.01f);
    }
}
