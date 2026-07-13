package com.example.digitalvouchers.Api

import com.example.digitalvouchers.Data.CheckTransactionRequest
import com.example.digitalvouchers.Data.CheckTransactionResponse
import com.example.digitalvouchers.Data.CountryRequest
import com.example.digitalvouchers.Data.CountryResponse
import com.example.digitalvouchers.Data.DeleteBillRequest
import com.example.digitalvouchers.Data.DeleteBillResponse
import com.example.digitalvouchers.Data.IPayOrderRequest
import com.example.digitalvouchers.Data.IPayOrderResponse
import com.example.digitalvouchers.Data.OtpResponse
import com.example.digitalvouchers.Data.ProvidersResponse
import com.example.digitalvouchers.Data.providersRequest
import com.example.digitalvouchers.Data.ProductsRequest
import com.example.digitalvouchers.Data.ProductsResponse
import com.example.digitalvouchers.Data.RequestIPayOtpRequest
import com.example.digitalvouchers.Data.RequestIPayOtpResponse
import com.example.digitalvouchers.Data.SavedVoucherRequest
import com.example.digitalvouchers.Data.SavedVoucherResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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
    @POST("api/requestiPayOtp")
    fun requestiPayOtp(
        @Body request: RequestIPayOtpRequest
    ): Call<RequestIPayOtpResponse>

    @POST("api/iPayOrder")
    fun iPayOrder(
        @Body request: IPayOrderRequest
    ): Call<IPayOrderResponse>

    @POST("api/checkTransaction")
    fun checkTransaction(
        @Body request: CheckTransactionRequest
    ): Call<CheckTransactionResponse>

    @GET("traffic/ex/access-mgmt/v1/otp")
    fun getOtpIpay(
        @Query("mobile") mobileNumber: String
    ): Call<OtpResponse>



}