package me.rozkmin.livetram

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import retrofit2.Converter
import java.util.concurrent.TimeUnit

class Client(networkModule: NetworkModule = NetworkModule(), val tramDataConverter: TramDataConverter = TramDataConverter()) {

    private val service = networkModule.provideNetworkService()
    private val tramSubject = PublishSubject.create<List<MinifiedTramData>>()
    private val stopsSubject = PublishSubject.create<List<StopData>>()

    private var stops: List<StopData> = listOf()
        set(value) {
            field = value
            stopsSubject.onNext(value)
        }

    fun start() {
        Flowable.interval(0, 1, TimeUnit.MINUTES)
                .flatMapSingle { service.getStops() }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map { it.stops }
                .doOnNext { this.stops = stops }
                .doOnNext { println("stops: ${it.size}") }
                .subscribe()

        Flowable.interval(0, 1500, TimeUnit.MILLISECONDS)
                .flatMapSingle { service.getTrams() }
                .map { it.vehicles.filter { !it.isDeleted }.map { tramDataConverter.convert(it) } }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext { tramSubject.onNext(it) }
                .subscribe()
    }

    fun provideLatestData() = tramSubject.toFlowable(BackpressureStrategy.LATEST).firstElement()
    fun provideStops() = stopsSubject.toFlowable(BackpressureStrategy.LATEST).firstElement()
}

data class MinifiedTramData(val id: String, val lat: Double, val lon: Double, val name: String, val angle: Float)

class TramDataConverter() : Converter<TramLocationData, MinifiedTramData> {
    override fun convert(value: TramLocationData): MinifiedTramData {
        return value.let {
            MinifiedTramData(
                    it.id,
                    it.getLatitude(),
                    it.getLongitude(),
                    it.name,
                    it.path.firstOrNull()?.angle?.toFloat() ?: 0f)
        }
    }
}