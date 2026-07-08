package com.example.digitalvouchers.Data

data class CategoryItem( val categoryId: String,
                         val categoryName: String,
                         val imgSelected: String,
                         val imgNotSelected: String,
                         val providers: List<ProviderItem>)