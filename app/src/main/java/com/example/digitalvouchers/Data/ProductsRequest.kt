package com.example.digitalvouchers.Data

data class ProductsRequest(
    val mobileNumber: String,
    val serviceCode: String,
    val countryCode: String,
    val providerCode: String
)
