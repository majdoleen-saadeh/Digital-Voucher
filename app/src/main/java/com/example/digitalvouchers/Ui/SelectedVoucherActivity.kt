package com.example.digitalvouchers.Ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.digitalvouchers.Api.networkapi
import com.example.digitalvouchers.Data.Products
import com.example.digitalvouchers.Data.ProductsRequest
import com.example.digitalvouchers.Data.ProductsResponse
import com.example.digitalvouchers.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectedVoucherActivity : AppCompatActivity() {

    private lateinit var rvSelectedVouchers: RecyclerView
    private lateinit var productsAdapter: ProductsAdapter
    private lateinit var tvProductName: TextView
    private lateinit var btnBack: ImageView
    private lateinit var accordionHeader: View
    private lateinit var accordionContent: LinearLayout
    private lateinit var arrowIcon: ImageView

    private var countryFlagUrl: String = ""
    private var providerCode: String = ""
    private var countryIso: String = ""
    private var providerName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.selectedvoucher)

        val mainView = findViewById<View>(R.id.scrollContainer)
        mainView?.let { v ->
            ViewCompat.setOnApplyWindowInsetsListener(v) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        providerCode = intent.getStringExtra("providerCode").toString()
        countryIso = intent.getStringExtra("countryIso").toString()
        providerName = intent.getStringExtra("providerName").toString()
        countryFlagUrl = intent.getStringExtra("countryFlagUrl").toString()

        rvSelectedVouchers = findViewById(R.id.rvSelectedVouchers)
        tvProductName = findViewById(R.id.productname)
        btnBack = findViewById(R.id.btnBack)
        accordionHeader = findViewById(R.id.accordion_header)
        accordionContent = findViewById(R.id.accordion_content)
        arrowIcon = findViewById(R.id.arrow_icon)

        tvProductName.text = providerName

        productsAdapter = ProductsAdapter()

        productsAdapter.setOnItemClickListener { selectedProduct ->
            navigateToReviewScreen(selectedProduct)
        }

        rvSelectedVouchers.layoutManager = GridLayoutManager(this, 2)
        rvSelectedVouchers.adapter = productsAdapter
        rvSelectedVouchers.isNestedScrollingEnabled = false

        btnBack.setOnClickListener { finish() }

        accordionHeader.setOnClickListener {
            val isExpanded = accordionContent.visibility == View.VISIBLE
            accordionContent.visibility = if (isExpanded) View.GONE else View.VISIBLE
            arrowIcon.rotation = if (isExpanded) 0f else 180f
        }

        fetchProducts()
    }

    private fun selectProduct(product: Products) {
        val howToRedeem = product.howToRedeem ?: arrayListOf()
        accordionContent.removeAllViews()

        howToRedeem.forEach { step ->
            val cleanText = step.replace("\r\n", "\n").trim()

            if (cleanText.isNotEmpty()) {
                val stepView = layoutInflater.inflate(R.layout.item_how_to_redeem, accordionContent, false)
                val tvText = stepView.findViewById<TextView>(R.id.tvRedeemStepText)
                tvText.text = cleanText
                accordionContent.addView(stepView)
            }
        }
    }

    private fun navigateToReviewScreen(product: Products) {
        val intent = Intent(this, ReviewVoucherActivity::class.java).apply {
            putExtra("COMPANY_NAME", providerName)
            putExtra("VALIDITY", product.validityDays)
            putExtra("VOUCHER_NAME", product.displayText)
            putExtra("VOUCHER_AMOUNT", "${product.receiveCurrencyIso} ${product.receiveValue}")
            putExtra("TOTAL_PAY", "${product.sendCurrencyIso} ${product.sendValue}")
            putExtra("COUNTRY_ISO", product.countryIso)
            putExtra("COUNTRY_FLAG_URL", countryFlagUrl)
            putExtra("PRODUCT_SKU", product.skuCode)
        }
        startActivity(intent)
    }

    private fun fetchProducts() {
        val requestBody = ProductsRequest(
            mobileNumber = networkapi.mobileNumber,
            serviceCode = "INT_VOUCHER",
            countryCode = countryIso,
            providerCode = providerCode
        )

        networkapi.apiService.getProducts(requestBody)
            .enqueue(object : Callback<ProductsResponse> {
                override fun onResponse(
                    call: Call<ProductsResponse>,
                    response: Response<ProductsResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val productsData = response.body()!!
                        if (productsData.status == "SUCCESS") {
                            productsAdapter.setProducts(productsData.items)
                            if (!productsData.items.isNullOrEmpty()) {
                                selectProduct(productsData.items[0])
                            }
                        } else {
                            showError("error: ${productsData.status}")
                        }
                    } else {
                        showError("error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ProductsResponse>, t: Throwable) {
                    showError("error: ${t.message}")
                }
            })
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}