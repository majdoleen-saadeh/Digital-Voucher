package com.example.digitalvouchers.Api

import com.example.digitalvouchers.Data.CountryRequest
import com.example.digitalvouchers.Data.CountryResponse
import com.example.digitalvouchers.Data.DeleteBillRequest
import com.example.digitalvouchers.Data.DeleteBillResponse
import com.example.digitalvouchers.Data.ProvidersResponse
import com.example.digitalvouchers.Data.providersRequest
import com.example.digitalvouchers.Data.ProductsRequest
import com.example.digitalvouchers.Data.ProductsResponse
import com.example.digitalvouchers.Data.SavedVoucherRequest
import com.example.digitalvouchers.Data.SavedVoucherResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiServices {
    @POST("api/countries")
    fun getCountries(
        @Body request: CountryRequest
    ): Call<CountryResponse>

    @POST("api/providersCategory")
    fun getProvidersCategories(
        @Body request: providersRequest
    ): Call<ProvidersResponse>

    @POST("api/products")
    fun getProducts(
        @Body request: ProductsRequest
    ): Call<ProductsResponse>

    @POST("api/getSavedBillsSDK")
    fun getSavedVouchers(
        @Body request: SavedVoucherRequest
    ): Call<SavedVoucherResponse>

    @POST("api/deleteBillSDK")
    fun deleteBill(
        @Body request: DeleteBillRequest
    ): Call<DeleteBillResponse>
}