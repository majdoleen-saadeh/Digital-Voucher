package com.example.digitalvouchers.Api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object networkapi {
    private const val BASE_URL = "https://staging.keybs.ai/"

    // service code : INT_VOUCHER
    const val serviceCode: String = "INT_VOUCHER"
    const val secretKey: String = "9a339dc9f40a3677ed170af9e6b4022fcf39acd286e901bf883f57b37e8b0c63"
    const val mobileNumber: String = "55555015"
    const val iPayCustomerID: String = "625562213"

    private val client = OkHttpClient.Builder().addInterceptor { chain ->
        val newRequest = chain.request().newBuilder()
            .header("Authorization", "Bearer $secretKey")
            .build()
        chain.proceed(newRequest)
    }.build()

    val apiService: ApiServices by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServices::class.java)
    }
}