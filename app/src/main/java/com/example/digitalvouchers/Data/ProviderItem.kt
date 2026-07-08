package com.example.digitalvouchers.Data


data class ProviderItem(
    val providerCode: String,
    val countryIso: String,
    val name: String,
    val logoUrl: String,
    val flagUrl: String,
    val countryName: String,
    val terms: ProviderTerms,
    val validationRegex: String = "",
    val settingDefinitions: List<String> = emptyList(),
    val howToRedeem: List<String> = emptyList(),
    val redeemTitle: String = "",
    var categoryId: String = ""
)
