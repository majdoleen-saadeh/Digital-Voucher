package com.example.digitalvouchers.Data

data class TransactionDetails(
    val id: String? = null,
    val mobileNumber: String? = null,
    val iPayCustomerID: String? = null,
    val targetIdentifier: String? = null,
    val countryIso2: String? = null,
    val countryIso3: String? = null,
    val countryName: String? = null,
    val countryFlagUrl: String? = null,
    val providerCode: String? = null,
    val providerName: String? = null,
    val providerImgUrl: String? = null,
    val productSku: String? = null,
    val productDisplayText: String? = null,
    val serviceCode: String? = null,
    val amount: String? = null,
    val currency: String? = null,
    val billingRef: String? = null,
    val status: String? = null,
    val statusMessage: String? = null,
    val statusMsg: String? = null,
    val reciptParams: String? = null,
    val descriptionMarkdown: String? = null,
    val readMoreMarkdown: String? = null,
    val terms: TransactionTerms? = null,
    val howToRedeem: List<String>? = null,
    val redeemTitle: String? = null,
    val productLogoUrl: String? = null,
    val isPlaceHolder: Boolean? = null,
    val dateTime: String? = null
)

data class TransactionTerms(
    val info: List<String>? = null,
    val important: List<String>? = null
)