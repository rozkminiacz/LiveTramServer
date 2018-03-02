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


class Repository : Closeable {
    val db = DBMaker.heapDB().make()

    init {
        db.treeMap("tram-data").create()
    }

    fun update(data: List<MinifiedTramData>) : Flowable<Boolean> {
        db.transaction {
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

    fun retrieve(): Single<List<MinifiedTramData>> {
        var list: List<String> = listOf()
        db.transaction {
            list = it.values.asIterable().filterNotNull().toList()
            true
        }

        println(list)

//        val bTreeMap: BTreeMap<String, String> = db.treeMap("tram-data", Serializer.STRING, Serializer.STRING).createOrOpen()

        return list.mapNotNull { it.fromJson() }.toSingle()

    }


    fun String.fromJson(): MinifiedTramData? {
        return Gson().fromJson(this, MinifiedTramData::class.java)
    }
}

private fun DB.transaction(function: (BTreeMap<String, String>) -> Boolean) {
    Flowable.just(function)
            .map { treeMap("tram-data", Serializer.STRING, Serializer.STRING).open() }
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
