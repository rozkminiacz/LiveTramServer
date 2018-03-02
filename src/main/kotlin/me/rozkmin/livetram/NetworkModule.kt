package me.rozkmin.livetram

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class NetworkModule {

    companion object {
        const val API_URL = "http://www.ttss.krakow.pl"
        val TAG = NetworkModule::class.java.simpleName
    }

    private fun provideApiUrl(): String = API_URL

    private fun provideRetrofitClient(baseUrl: String, gson: Gson, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .build()
    }

    fun provideNetworkService(
            retrofit: Retrofit = provideRetrofitClient(
                    baseUrl = provideApiUrl(),
                    gson = provideGson(),
                    okHttpClient = provideOkHttpClient())
    ): NetworkService =
            retrofit.create(NetworkService::class.java)

    private fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
                .build()
    }

    private fun provideGson(): Gson = GsonBuilder()
            .setLenient()
            .serializeNulls()
            .create()

}