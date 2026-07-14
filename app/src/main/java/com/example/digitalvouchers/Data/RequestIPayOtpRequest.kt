package com.example.digitalvouchers.Data
data class RequestIPayOtpRequest(
    val mobileNumber: String,
    val serviceCode: String,
    val iPayCustomerID: String,
    val targetNumber: String,
    val productSku: String,
    val saveRecharge: String,
    val billAmount: String,
    val settingsData: String
)