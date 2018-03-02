package me.rozkmin.livetram

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import retrofit2.Converter
import java.io.Closeable
import java.util.concurrent.TimeUnit

class Client(networkModule: NetworkModule = NetworkModule(), private val tramDataConverter: TramDataConverter = TramDataConverter()) : Closeable {

    private val service = networkModule.provideNetworkService()
    private val tramSubject = PublishSubject.create<List<MinifiedTramData>>()
    private val stopsSubject = PublishSubject.create<List<StopData>>()

    private val repository = Repository()

    private var stops: List<StopData> = listOf()
        set(value) {
            field = value
            stopsSubject.onNext(value)
        }

    override fun close() {
        compositeDisposable.clear()
        repository.close()
    }

    var compositeDisposable = CompositeDisposable()

    fun start() {
        compositeDisposable.add(Flowable.interval(0, 1, TimeUnit.MINUTES)
                .flatMapSingle { service.getStops() }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map { it.stops }
                .doOnNext { this.stops = stops }
                .doOnNext { println("stops: ${it.size}") }
                .doOnError { println(it.message) }
                .subscribe())

        compositeDisposable.add(Flowable.interval(0, System.getenv("REFRESH").toLong(), TimeUnit.MILLISECONDS)
                .flatMapSingle { service.getTrams() }
                .map { it.vehicles.filter { !it.isDeleted }.map { tramDataConverter.convert(it) } }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext { tramSubject.onNext(it) }
                .doOnNext { }
                .doOnNext { println("vehicles: ${it.size}") }
                .flatMap { repository.update(it) }
                .doOnNext{ println("saved? $it")}
                .doOnError { println(it.message) }
                .subscribe())
    }

    fun provideLatestData() = repository.retrieve().cache()
    fun provideStops() = stopsSubject.toFlowable(BackpressureStrategy.BUFFER).firstElement().cache()
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