/*  Copyright (C) 2026 sbutnari

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
package nodomain.freeyourgadget.gadgetbridge.service.devices.oneplus

import nodomain.freeyourgadget.gadgetbridge.model.WeatherSpec
import nodomain.freeyourgadget.gadgetbridge.service.devices.oneplus.OnePlusProtocol.Proto
import kotlin.math.roundToInt

/**
 * Builders for the watch's weather service (9). The watch asks and the phone answers:
 *  - 9.1 city list: reply with [cityList]
 *  - 9.2 weather of a city (request {1: location key}): reply with [weatherDetail]
 *  - 9.5 phone location: reply with [location]
 * Field layouts come from the vendor app's protobuf classes. Temperatures are degrees Celsius.
 */
object OnePlusWeather {
    const val CMD_CITY_LIST = 1
    const val CMD_DETAIL = 2
    const val CMD_LOCATION = 5

    /** Location key used for the one city Gadgetbridge offers. */
    const val CITY_KEY = "gadgetbridge"

    private fun celsius(kelvin: Int): Int = (kelvin - 273.15).roundToInt()

    /**
     * Vendor weather icon id for an OpenWeatherMap condition code. The numbering looks like the AccuWeather icon set
     * (1 sunny ... 7 cloudy, 12 showers, 15 thunderstorm, 18 rain, 22 snow); not yet verified on the watch.
     */
    fun weatherId(owm: Int): Int = when (owm) {
        800 -> 1
        801 -> 2
        802 -> 3
        803 -> 6
        804 -> 7
        in 200..232 -> 15
        in 300..321 -> 12
        500, 501 -> 12
        in 502..504 -> 18
        511 -> 26
        in 520..531 -> 12
        600, 601, 602, in 620..622 -> 22
        611, 612, 613 -> 25
        615, 616 -> 29
        701, 741 -> 11
        711, 721, 731, 751, 761, 762 -> 5
        771, 781 -> 32
        else -> 7
    }

    private fun name(spec: WeatherSpec) = spec.location ?: "Gadgetbridge"

    /** 9.1 reply: the one city, marked as the local city. */
    fun cityList(spec: WeatherSpec): ByteArray {
        val city = Proto.encode(
            1 to CITY_KEY,
            2 to name(spec),
            3 to name(spec),
            8 to "",
            9 to "C",
            10 to weatherId(spec.currentConditionCode).toString(),
            11 to celsius(spec.currentTemp).toString(),
            12 to 1,
        )
        return Proto.encode(1 to 0, 2 to city, 3 to 0, 4 to "en")
    }

    /** 9.2 reply for [key]: current conditions, hourly and daily forecast. */
    fun weatherDetail(key: String, spec: WeatherSpec): ByteArray {
        val fields = ArrayList<Pair<Int, Any>>()
        fields.add(1 to name(spec).let { CITY_KEY })
        fields.add(2 to name(spec))
        fields.add(3 to java.util.TimeZone.getDefault().id)
        fields.add(4 to weatherId(spec.currentConditionCode))
        fields.add(5 to celsius(spec.currentTemp))
        fields.add(6 to celsius(spec.feelsLikeTemp.takeIf { it > 0 } ?: spec.currentTemp).toString())
        fields.add(7 to spec.windSpeed.roundToInt().toString())
        fields.add(8 to spec.windDirection)
        fields.add(9 to spec.windSpeed.roundToInt().toString())
        fields.add(10 to spec.uvIndex.roundToInt())
        fields.add(12 to spec.currentHumidity.toString())
        if (spec.visibility > 0) fields.add(13 to (spec.visibility / 1000f).roundToInt().toString())
        if (spec.pressure > 0) fields.add(14 to spec.pressure.roundToInt().toString())
        fields.add(15 to spec.precipProbability.toString())
        if (spec.sunRise > 0) fields.add(16 to spec.sunRise * 1000L)
        if (spec.sunSet > 0) fields.add(17 to spec.sunSet * 1000L)
        fields.add(18 to System.currentTimeMillis())
        for (h in spec.hourly.take(24)) {
            fields.add(30 to Proto.encode(
                1 to h.timestamp * 1000L,
                2 to celsius(h.temp),
                3 to weatherId(h.conditionCode),
                4 to h.precipProbability.toString(),
            ))
        }
        // today first, then the forecast days
        fields.add(31 to dayEntry(System.currentTimeMillis(), spec.currentConditionCode, spec.todayMaxTemp, spec.todayMinTemp, spec.sunRise, spec.sunSet))
        var day = System.currentTimeMillis()
        for (d in spec.forecasts.take(6)) {
            day += 86_400_000L
            fields.add(31 to dayEntry(day, d.conditionCode, d.maxTemp, d.minTemp, d.sunRise, d.sunSet))
        }
        return Proto.encode(1 to 0, 2 to key, 3 to Proto.encode(*fields.toTypedArray()), 4 to "en")
    }

    private fun dayEntry(millis: Long, owm: Int, maxK: Int, minK: Int, sunrise: Int, sunset: Int): ByteArray {
        val f = ArrayList<Pair<Int, Any>>()
        val id = weatherId(owm)
        f.add(1 to id)
        f.add(2 to id)
        f.add(3 to celsius(maxK))
        f.add(4 to celsius(minK))
        f.add(5 to millis)
        if (sunrise > 0) f.add(6 to sunrise * 1000L)
        if (sunset > 0) f.add(7 to sunset * 1000L)
        return Proto.encode(*f.toTypedArray())
    }

    /** 9.5 reply: the phone's position (taken from the weather location, as Gadgetbridge has no live fix here). */
    fun location(spec: WeatherSpec?): ByteArray {
        if (spec == null || (spec.latitude == 0f && spec.longitude == 0f)) {
            return Proto.encode(7 to 0)
        }
        return Proto.encode(
            1 to spec.latitude.toDouble(),
            2 to spec.longitude.toDouble(),
            4 to name(spec),
            7 to 1,
        )
    }
}
