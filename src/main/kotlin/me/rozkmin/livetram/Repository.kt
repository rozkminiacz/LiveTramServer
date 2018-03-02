package me.rozkmin.livetram

import com.google.gson.Gson
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.rxkotlin.toSingle
import org.mapdb.BTreeMap
import org.mapdb.DB
import org.mapdb.DBMaker
import org.mapdb.Serializer
import java.io.Closeable
import kotlin.reflect.KClass


class Repository : Closeable {
    val db = DBMaker.heapDB().make()

    init {
        db.treeMap("tram-data").create()
        db.treeMap("stops-data").create()
    }

    fun updateStops(data : List<StopData>) : Flowable<Boolean>{
        db.transaction("stops-data") {
            it.apply {
                data.forEach { put(it.id, it.toJson()) }
            }
            true
        }
        return Flowable.just(true)
    }

    fun update(data: List<MinifiedTramData>) : Flowable<Boolean> {
        db.transaction("tram-data") {
            it.apply {
                data.forEach { put(it.id, it.toJson()) }
            }
            true
        }
        return Flowable.just(true)

    }


    override fun close() {
        db.close()
    }

    fun retrieveStops() : Single<List<StopData>>{
        var list: List<String> = listOf()
        db.transaction("stops-data"){
            list = it.values.filterNotNull().toList()
            true
        }
        return list.mapNotNull { it.fromJson(StopData::class) as StopData }.toSingle()
    }

    fun retrieve(): Single<List<MinifiedTramData>> {
        var list: List<String> = listOf()
        db.transaction("tram-data") {
            list = it.values.asIterable().filterNotNull().toList()
            true
        }

        println(list)

//        val bTreeMap: BTreeMap<String, String> = db.treeMap("tram-data", Serializer.STRING, Serializer.STRING).createOrOpen()

        return list.mapNotNull { it.fromJson(MinifiedTramData::class) as MinifiedTramData }.toSingle()

    }


    fun String.fromJson(kClass: KClass<*>) =
        Gson().fromJson(this, kClass.javaObjectType)

}

private fun DB.transaction(name : String, function: (BTreeMap<String, String>) -> Boolean) {
    Flowable.just(function)
            .map { treeMap(name, Serializer.STRING, Serializer.STRING).open() }
            .map { function.invoke(it) }
            .doOnComplete {
                commit()
            }
            .subscribe()

//    treeMap("tram-data", Serializer.STRING, Serializer.STRING).createOrOpen().apply {
//        function.invoke(this)
//    }
//    commit()
//    close()
}
