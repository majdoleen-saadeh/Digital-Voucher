package com.example.digitalvouchers.Data

import com.google.gson.annotations.SerializedName

data class CheckTransactionResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("transaction") val transaction: TransactionDetails? = null
)