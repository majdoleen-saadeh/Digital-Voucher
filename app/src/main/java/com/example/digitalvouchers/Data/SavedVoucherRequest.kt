package com.example.digitalvouchers.Data

data class SavedVoucherRequest(
    val mobileNumber: String,
    val serviceCode: String,
    val iPayCustomerID: String
)
