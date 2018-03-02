package me.rozkmin.livetram

import io.reactivex.Single
import retrofit2.http.GET

interface NetworkService {
    @GET("/internetservice/geoserviceDispatcher/services/stopinfo/stops?left=-648000000&bottom=-324000000&right=648000000&top=324000000")
    fun getStops(): Single<StopResponse>

    @GET("/internetservice/geoserviceDispatcher/services/vehicleinfo/vehicles?positionType=CORRECTED&colorType=ROUTE_BASED")
    fun getTrams(): Single<TramResponse>
}

class TramResponse(val lastUpdate: Double, val vehicles: List<TramLocationData> = listOf())
class StopResponse(val stops: List<StopData> = listOf())

class TramLocationData(
        val id: String = "",
        val category: String = "",
        val color: String = "",
        val tripId: String = "",
        val name: String = "",
        val path: List<Path> = listOf(),
        private val longitude: Int = 0,
        private val latitude: Int = 0,
        val heading: Int = 0,
        val isDeleted: Boolean = false) {

    fun getLongitude(): Double {
        return longitude / 3600000.0
    }

    fun getLatitude(): Double {
        return latitude / 3600000.0
    }

    inner class Path(val length: Double = 0.0,
                     val y1: Int = 0,
                     val y2: Int = 0,
                     val x2: Int = 0,
                     val angle: Int = 0,
                     val x1: Int = 0) {

        override fun toString(): String {
            return "Path{" +
                    "length=" + length +
                    ", y1=" + y1 +
                    ", y2=" + y2 +
                    ", x2=" + x2 +
                    ", angle=" + angle +
                    ", x1=" + x1 +
                    '}'.toString()
        }
    }
}

data class StopData(val category: String, val id: String, val latitude: Long, val longitude: Long, val name: String, val shortName: String) {
    fun getLatDouble(): Double = latitude.toDouble() / 3600000.0
    fun getLonDouble(): Double = longitude.toDouble() / 3600000.0
}