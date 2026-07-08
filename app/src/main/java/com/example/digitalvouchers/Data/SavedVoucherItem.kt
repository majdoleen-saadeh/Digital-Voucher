package com.example.digitalvouchers.Data

data class SavedVoucherItem(
    val id: String,
    val mobileNumber: String,
    val countryIso2: String,
    val countryIso3: String,
    val countryName: String,
    val countryFlagUrl: String,
    val providerCode: String,
    val providerName: String,
    val providerImgUrl: String,
    val productSku: String,
    val productDisplayText: String,
    val serviceCode: String,
    val amount: String,
    val currency: String,
    val dateTime: String,
    val billingRef: String,
    val reciptParams: String,
    val descriptionMarkdown: String,
    val readMoreMarkdown: String,
    val terms: ProviderTerms,
    val displayText: String,
    val redeemTitle: String,
    val howToRedeem: List<String>,
    val productLogoUrl: String,
    val isPlaceHolder: Boolean
)
