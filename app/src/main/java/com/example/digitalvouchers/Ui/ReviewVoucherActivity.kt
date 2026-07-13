package com.example.digitalvouchers.Ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.digitalvouchers.R
import androidx.activity.enableEdgeToEdge
import android.view.View
import com.example.digitalvouchers.Api.ApiServices
import com.example.digitalvouchers.Api.networkapi
import com.example.digitalvouchers.Data.RequestIPayOtpRequest
import com.example.digitalvouchers.Data.RequestIPayOtpResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        val countryFlagUrl = intent.getStringExtra("COUNTRY_FLAG_URL").toString()
        val validity = intent.getStringExtra("VALIDITY").toString()
        val productSku = intent.getStringExtra("PRODUCT_SKU").toString()

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

        btnBack.setOnClickListener { finish() }

        btnProceed.setOnClickListener { //الرسالة اللي بدنا نرسلها للسيرفر فيها كل المعلومات
            val request = RequestIPayOtpRequest(
                mobileNumber = networkapi.mobileNumber,
                serviceCode = networkapi.serviceCode,
                iPayCustomerID = networkapi.iPayCustomerID,
                targetNumber = "",
                productSku = productSku,
                saveRecharge = "1",
                billAmount = "0",
                settingsData = ""
            )

            // هون بنتصل بال api
            networkapi.apiService.requestiPayOtp(request).enqueue(object : Callback<RequestIPayOtpResponse> {
                override fun onResponse(
                    call: Call<RequestIPayOtpResponse>,
                    response: Response<RequestIPayOtpResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "SUCCESS") {// شرط التحقق اذا تم الاتصال
                        val transactionId = response.body()?.transactionId // الترنزاكشن id

                        // اذا تم ارسال ال otp بفتح شاشته
                        val intent = Intent(this@ReviewVoucherActivity, OTPScreen::class.java).apply {
                            putExtra("COUNTRY_FLAG_URL", countryFlagUrl)
                            putExtra("TOTAL_PAY", txtTotalPay.text.toString())
                            putExtra("TRANSACTION_ID", transactionId ?: -1)// الترانزاكشن اللي اجى من الشاشة
                            putExtra("PRODUCT_SKU", productSku)
                            putExtra("SERVICE_CODE", networkapi.serviceCode)
                            putExtra("IPAY_CUSTOMER_ID", networkapi.iPayCustomerID)
                        }
                        startActivity(intent)
                    } else {
                        // حالة الفشل
                        Toast.makeText(this@ReviewVoucherActivity, "Failed to request OTP. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RequestIPayOtpResponse>, t: Throwable) {
                    //في حال خلل في الشبكة
                    Toast.makeText(this@ReviewVoucherActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}