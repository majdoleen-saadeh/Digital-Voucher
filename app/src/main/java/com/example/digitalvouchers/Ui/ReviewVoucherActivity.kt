package com.example.digitalvouchers.Ui

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.digitalvouchers.R
import androidx.activity.enableEdgeToEdge
import android.view.View

class ReviewVoucherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.reviewvoucher)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val validityDays=findViewById<TextView>(R.id.validity)

        val txtCompanyName = findViewById<TextView>(R.id.CompanyName)
        val txtVoucherName = findViewById<TextView>(R.id.VoucherName)
        val txtVoucherAmount = findViewById<TextView>(R.id.VoucherAmount)
        val txtTotalPay = findViewById<TextView>(R.id.TotalPay)
        val btnProceed = findViewById<Button>(R.id.Proceed)
        val VCountryLogo = findViewById<ImageView>(R.id.VCountryLogo)
        val validityName =findViewById<TextView>(R.id.validityName)
        val companyName = intent.getStringExtra("COMPANY_NAME").toString()
        val voucherName = intent.getStringExtra("VOUCHER_NAME").toString()
        val voucherAmount = intent.getStringExtra("VOUCHER_AMOUNT").toString()
        val totalPay = intent.getStringExtra("TOTAL_PAY").toString()
       // val voucherLogo = intent.getStringExtra("VOUCHER_LOGO").toString()
        val countryFlagUrl = intent.getStringExtra("COUNTRY_FLAG_URL").toString()
        val validity = intent.getStringExtra("VALIDITY").toString()



        txtCompanyName.text = companyName
        txtVoucherName.text = voucherName
        txtVoucherAmount.text = voucherAmount
        loadSvg(VCountryLogo, countryFlagUrl)
        txtTotalPay.text = totalPay

        if (validity.isNullOrBlank() || validity == "null") {

            validityName.visibility = View.GONE
            validityDays.visibility = View.GONE
        } else {

            validityName.visibility = View.VISIBLE
            validityDays.visibility = View.VISIBLE
            validityDays.text = validity
        }

//       Glide.with(this)
//         .load(voucherLogo)
//         .into(VCountryLogo)

        btnBack.setOnClickListener { finish() }


        btnProceed.setOnClickListener {

        }
    }}