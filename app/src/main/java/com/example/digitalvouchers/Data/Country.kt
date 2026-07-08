package com.example.digitalvouchers.Data

import java.io.Serializable

data class Country(
    val name : String,
    val flagUrl : String,
    val prefix : String,
    val minimumLength : String,
    val maximumLength : String,
    val countryIso : String
): Serializable //لانه الاندرويد ما بفهم غير البايتس فهون عملية التحويل للبايتس