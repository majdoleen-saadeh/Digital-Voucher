package com.example.digitalvouchers.Data

data class Products(
    val skuCode: String,
    val providerCode: String,
    val countryIso: String,
    val displayText: String,
    val sendValue: Double,
    val sendCurrencyIso: String,
    val receiveCurrencyIso: String,
    val receiveValue: String,
    val sendValueMax: Double,
    val descriptionMarkdown: String,
    val readMoreMarkdown: String,
    val settingDefinitions: List<Any?>,
    val terms: Terms,
    val howToRedeem: List<String>,
    val redeemTitle: String,
    val validityDays: String,
    val classification: Long,
    val logo: String,
    val isPlaceHolder: Boolean)


data class Terms(
    val info: List<String>,
    val important: List<String>,
)
