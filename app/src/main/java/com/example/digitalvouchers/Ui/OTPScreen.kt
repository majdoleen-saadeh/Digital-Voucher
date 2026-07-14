package com.example.digitalvouchers.Ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.digitalvouchers.Api.networkapi
import com.example.digitalvouchers.Api.networkapi.mobileNumber
import com.example.digitalvouchers.Data.IPayOrderRequest
import com.example.digitalvouchers.Data.IPayOrderResponse
import com.example.digitalvouchers.Data.SavedVoucherItem
import com.example.digitalvouchers.Data.SavedVoucherRequest
import com.example.digitalvouchers.Data.SavedVoucherResponse
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.IntentFilter
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.util.regex.Pattern
import com.example.digitalvouchers.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.digitalvouchers.Data.RequestIPayOtpRequest
import com.example.digitalvouchers.Data.RequestIPayOtpResponse

class OTPScreen : AppCompatActivity() {

    // متغيرات الشاشة asdasdas
    private lateinit var loadingSpinner: ImageView
    private var countDownTimer: CountDownTimer? = null
    private lateinit var otpFields: Array<EditText>
    private var transactionId: String = ""

    private var failedAttempts = 0
    private var lastEnteredOtp = ""
    private lateinit var tvErrorMsg: TextView

    private val MASK_DELAY_MS: Long = 1000
    private val RESEND_TIMER_MS: Long = 60000

    private var isVerifying = false // مشان امنعه يرسل اكتر من ريكويست
    private lateinit var btnResendOtp: TextView
    private lateinit var tvTimer: TextView
    private var isTimerFinished = false
    private var productSku: String = ""
    private var serviceCode: String = ""
    private var iPayCustomerId: String = ""
//بيقرأو رسالة ال sms وبدوروا عال 4 ديجيتس فيها
    private val smsConsentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val message = result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
            message?.let { extractAndFillOtp(it) }
        }
    }

    private val smsVerificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                val extras = intent.extras
                val status: Status? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    extras?.getParcelable(SmsRetriever.EXTRA_STATUS, Status::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    extras?.get(SmsRetriever.EXTRA_STATUS) as? Status
                }

                when (status?.statusCode) {
                    CommonStatusCodes.SUCCESS -> {

                        val consentIntent: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            extras?.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT, Intent::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            extras?.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT)
                        }

                        try {
                            consentIntent?.let { smsConsentLauncher.launch(it) }
                        } catch (e: ActivityNotFoundException) {
                            e.printStackTrace()
                        }
                    }
                    CommonStatusCodes.TIMEOUT -> {

                    }
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sending_otp_screen)

        // بناخد الـ transactionId من الشاشة اللي قبل
        transactionId = getTransactionIdFromIntent()

        val ivFlag = findViewById<ImageView>(R.id.VCountryLogo)
        val tvTotalPay = findViewById<TextView>(R.id.TotalPay)
        val tvNumber = findViewById<TextView>(R.id.number)
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        productSku = intent.getStringExtra("PRODUCT_SKU") ?: ""
        serviceCode = intent.getStringExtra("SERVICE_CODE") ?: ""
        iPayCustomerId = intent.getStringExtra("IPAY_CUSTOMER_ID") ?: ""

        btnResendOtp = findViewById(R.id.btnResendOtp)
        tvTimer = findViewById(R.id.tvTimer)
        tvErrorMsg = findViewById(R.id.tvErrorMsg)
        loadingSpinner = findViewById(R.id.loadingSpinner)

        Glide.with(this)
            .asGif()
            .load(R.raw.keybs_spinner)
            .into(loadingSpinner)

        val rootView = findViewById<View>(R.id.otpscreen)
        rootView.setOnClickListener {
            imm.hideSoftInputFromWindow(rootView.windowToken, 0)
            currentFocus?.clearFocus()
        }

        otpFields = arrayOf(
            findViewById(R.id.etDigit1),
            findViewById(R.id.etDigit2),
            findViewById(R.id.etDigit3),
            findViewById(R.id.etDigit4)
        )

        tvNumber.text = mobileNumber

        val totalPay = intent.getStringExtra("TOTAL_PAY")
        if (totalPay != null) {
            tvTotalPay.text = totalPay
        } else {
            tvTotalPay.text = ""
        }

        val flagUrl = intent.getStringExtra("COUNTRY_FLAG_URL")
        if (flagUrl != null && flagUrl.isNotEmpty()) {
            loadSvg(ivFlag, flagUrl)
        }

        setupOtpFields()

        btnBack.setOnClickListener {
            finish()
        }
        btnResendOtp.setOnClickListener {
            // إظهار السبينر وإخفاء العناصر
            btnResendOtp.visibility = View.GONE
            tvTimer.visibility = View.GONE
            tvErrorMsg.visibility = View.GONE
            loadingSpinner.visibility = View.VISIBLE

            // الآن نستخدم البيانات الحقيقية التي استقبلناها من الـ Intent
            val request = RequestIPayOtpRequest(
                mobileNumber = mobileNumber,
                serviceCode = serviceCode,
                iPayCustomerID = iPayCustomerId,
                targetNumber = "",
                productSku = productSku,
                saveRecharge = "1",
                billAmount = "0", // أو القيمة التي تم تمريرها
                settingsData = ""
            )

            networkapi.apiService.requestiPayOtp(request).enqueue(object : Callback<RequestIPayOtpResponse> {
                override fun onResponse(call: Call<RequestIPayOtpResponse>, response: Response<RequestIPayOtpResponse>) {
                    loadingSpinner.visibility = View.GONE

                    if (response.isSuccessful && response.body()?.status == "SUCCESS") {
                        // تحديث الـ transactionId الجديد
                        response.body()?.transactionId?.let { newId ->
                            transactionId = newId.toString()
                        }

                        Toast.makeText(this@OTPScreen, "OTP Resent Successfully", Toast.LENGTH_SHORT).show()

                        tvTimer.visibility = View.VISIBLE
                        clearAllOtpFields()
                        startResendTimer(btnResendOtp, tvTimer)
                        SmsRetriever.getClient(this@OTPScreen).startSmsUserConsent(null)
                    } else {
                        btnResendOtp.visibility = View.VISIBLE
                        Toast.makeText(this@OTPScreen, "Failed to resend", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RequestIPayOtpResponse>, t: Throwable) {
                    loadingSpinner.visibility = View.GONE
                    btnResendOtp.visibility = View.VISIBLE
                    Toast.makeText(this@OTPScreen, "Network Error", Toast.LENGTH_SHORT).show()
                }
            })
        }

//        //يرجع يبعت طلب otp  جديد
//        btnResendOtp.setOnClickListener {
//            // إخفاء الأزرار وإظهار السبينر
//            btnResendOtp.visibility = View.GONE
//            tvTimer.visibility = View.GONE
//            tvErrorMsg.visibility = View.GONE
//            loadingSpinner.visibility = View.VISIBLE
//
//            //حاليا مؤقت لسا ما ربطناه بال api
//            Handler(Looper.getMainLooper()).postDelayed({
//                loadingSpinner.visibility = View.GONE
//                tvTimer.visibility = View.VISIBLE
//                failedAttempts = 0
//                lastEnteredOtp = ""
//                clearAllOtpFields()
//                startResendTimer(btnResendOtp, tvTimer)
//                Toast.makeText(this@OTPScreen, "OTP sent successfully", Toast.LENGTH_SHORT).show()
//
//                // إعادة بدء مراقبة الرسائل عند طلب كود جديد
//                SmsRetriever.getClient(this@OTPScreen).startSmsUserConsent(null)
//            }, 1500)

      //  }

        btnResendOtp.visibility = View.GONE
        tvTimer.visibility = View.VISIBLE
        startResendTimer(btnResendOtp, tvTimer)

        // تشغيل خدمة مراقبة الرسائل للقراءة التلقائية أول ما تفتح الشاشة
        SmsRetriever.getClient(this).startSmsUserConsent(null)
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)

        ContextCompat.registerReceiver(
            this,
            smsVerificationReceiver,
            intentFilter,
            SmsRetriever.SEND_PERMISSION,
            null,
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(smsVerificationReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    // بتاخد الرقم وبتحطلي اياه محل ال otp
    private fun extractAndFillOtp(message: String) {
        val pattern = Pattern.compile("(\\d{4})")
        val matcher = pattern.matcher(message)

        if (matcher.find()) {
            val otp = matcher.group(0)

            if (otp != null && otp.length == 4) {
                for (i in 0..3) {
                    otpFields[i].text.clear()
                    otpFields[i].setText(otp[i].toString()) // تعبئة الرقم

                    // نغطيه طوالي
                    otpFields[i].transformationMethod = RedDotTransformationMethod()
                    otpFields[i].setTextColor(Color.parseColor("#bd0612"))
                }

                // الفوكس للاخر
                otpFields[3].requestFocus()
                otpFields[3].setSelection(1)

                Handler(Looper.getMainLooper()).postDelayed({
                    checkOtpCompletion()
                }, 500)// يستنى نص ثانية ويتحقق
            }
        }
    }
    // بتفرغلي خانات ال otp
    private fun clearAllOtpFields() {
        for (field in otpFields) {
            field.text.clear()
            field.transformationMethod = null
            field.setTextColor(Color.BLACK)
        }
        otpFields[0].requestFocus()
    }

    // بتعمل هاندل لكل انواع الترانزاكشن id
    private fun getTransactionIdFromIntent(): String {
        val idAsString = intent.getStringExtra("TRANSACTION_ID")
        if (idAsString != null) {
            return idAsString
        }

        val idAsLong = intent.getLongExtra("TRANSACTION_ID", -1L)
        if (idAsLong != -1L) {
            return idAsLong.toString()
        }

        val idAsInt = intent.getIntExtra("TRANSACTION_ID", -1)
        if (idAsInt != -1) {
            return idAsInt.toString()
        }

        return ""
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    private fun startResendTimer(btnResend: TextView, timerText: TextView) {
        isTimerFinished = false
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(RESEND_TIMER_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                timerText.text = "00: $seconds"
            }

            override fun onFinish() {
                isTimerFinished = true
                if (tvErrorMsg.visibility != View.VISIBLE) {
                    timerText.visibility = View.GONE
                    btnResend.visibility = View.VISIBLE
                }
            }
        }
        countDownTimer?.start()
    }

    private fun setupOtpFields() {
        for (index in otpFields.indices) {
            val field = otpFields[index]

            field.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    focusFirstEmptyOrCurrent(field)
                }
            }

            field.setOnClickListener {
                field.setSelection(field.text.length)
            }

            field.addTextChangedListener(digitWatcher(field, index))

            field.setOnKeyListener { _, keyCode, event ->
                handleBackspace(index, keyCode, event)
            }
        }
    }

    private fun focusFirstEmptyOrCurrent(current: EditText) {
        if (current.text.isEmpty()) {
            var target: EditText = current
            for (field in otpFields) {
                if (field.text.isEmpty()) {
                    target = field
                    break
                }
            }
            if (current != target) {
                target.requestFocus()
                return
            }
        }
        current.post {
            current.setSelection(current.text.length)
        }
    }

    private fun hideErrorAndRestoreTimer() {
        if (tvErrorMsg.visibility == View.VISIBLE) {
            tvErrorMsg.visibility = View.GONE
            if (isTimerFinished) {
                btnResendOtp.visibility = View.VISIBLE
                tvTimer.visibility = View.GONE
            } else {
                btnResendOtp.visibility = View.GONE
                tvTimer.visibility = View.VISIBLE
            }
        }
    }

    private fun digitWatcher(field: EditText, index: Int) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            field.isSelected = !s.isNullOrEmpty()
            if (s?.length == 1 && count == 1 && before == 0 && field.isFocused) {
                hideErrorAndRestoreTimer()
                onDigitEntered(field, index)
            } else if (s.isNullOrEmpty()) {
                field.setTextColor(Color.BLACK)
            }
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    private fun onDigitEntered(field: EditText, index: Int) {
        field.setTextColor(Color.BLACK)

        Handler(Looper.getMainLooper()).postDelayed({
            field.transformationMethod = RedDotTransformationMethod()
            field.setTextColor(Color.parseColor("#bd0612"))
            field.setSelection(field.text.length)
        }, MASK_DELAY_MS)

        if (index < otpFields.size - 1) {
            otpFields[index + 1].requestFocus()
        } else {
            checkOtpCompletion()
        }
    }

    private fun handleBackspace(index: Int, keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode != KeyEvent.KEYCODE_DEL || event.action != KeyEvent.ACTION_DOWN) {
            return false
        }

        hideErrorAndRestoreTimer()

        val current = otpFields[index]
        if (current.text.isNotEmpty()) {
            clearField(current)
            return true
        }

        if (index > 0) {
            val previous = otpFields[index - 1]
            previous.requestFocus()
            clearField(previous)
            return true
        }

        return false
    }

    private fun clearField(field: EditText) {
        field.text.clear()
        field.transformationMethod = null
        field.setTextColor(Color.BLACK)
    }

    // بتجمع الأربع أرقام، ولو كملوا بترسل طلب التحقق للسيرفر
    private fun checkOtpCompletion() {
        val enteredOTP = otpFields.joinToString("") { it.text.toString() }

        if (enteredOTP.length != 4) return
        if (isVerifying) return
        if (transactionId.isEmpty()) return

        isVerifying = true
        loadingSpinner.visibility = View.VISIBLE
        sendOtpToServer(enteredOTP)
    }

    private fun sendOtpToServer(enteredOTP: String) {
        val request = IPayOrderRequest(
            otp = enteredOTP,
            transactionId = transactionId
        )

        networkapi.apiService.iPayOrder(request).enqueue(object : Callback<IPayOrderResponse> {
            override fun onResponse(call: Call<IPayOrderResponse>, response: Response<IPayOrderResponse>) {
                if (!response.isSuccessful) {
                    loadingSpinner.visibility = View.GONE

                    // هندلة الترانزاكشن إيرور عرض ديالوج الفشل
                    showSummaryDialogForFailure("Transaction Error")
                    isVerifying = false
                    return
                }

                val apiResponse = response.body()

                if (apiResponse?.status == "SUCCESS") {
                    onOtpVerifiedSuccessfully()
                } else {
                    onOtpVerificationFailed(enteredOTP, apiResponse?.message)
                }
            }

            override fun onFailure(call: Call<IPayOrderResponse>, t: Throwable) {
                loadingSpinner.visibility = View.GONE

                // عرض الديالوج إذا انقطع الإنترنت أو فشل الاتصال
                showSummaryDialogForFailure("Network Error: ${t.message}")
                isVerifying = false
            }
        })
    }

    private fun onOtpVerifiedSuccessfully() {
        tvErrorMsg.visibility = View.GONE
        // لا نخفي السبينر هنا لأنه سيبقى يلف أثناء جلب بيانات الفاوتشر
        failedAttempts = 0
        lastEnteredOtp = ""

        // بنستنى ثانيتين لحتى السيرفر يخلص إنشاء الفاوتشر قبل ما نجيب تفاصيله
        Handler(Looper.getMainLooper()).postDelayed({
            fetchTransactionAndShowSuccess()
        }, 2000)
    }

    private fun onOtpVerificationFailed(enteredOTP: String, serverMessage: String?) {
        loadingSpinner.visibility = View.GONE // إخفاء السبينر فوراً

        if (enteredOTP != lastEnteredOtp) {
            failedAttempts++
            lastEnteredOtp = enteredOTP
        }

        if (failedAttempts >= 3) {
            tvErrorMsg.visibility = View.GONE
            failedAttempts = 0
            lastEnteredOtp = ""
            clearAllOtpFields()

            // استخدام ديلوج الـ Summary بعد المحاولة الثالثة
            val finalMessage = "Maximum attempts reached. Transaction failed."
            showSummaryDialogForFailure(finalMessage)
        } else {
            // نظهر رسالة الخطأ ونخفي العداد والزر مؤقتاً
            tvErrorMsg.visibility = View.VISIBLE
            tvTimer.visibility = View.GONE
            btnResendOtp.visibility = View.GONE

            // عرض المسج الحقيقي الجاي من السيرفر، ولو مافي نعرض نص بديل
            tvErrorMsg.text =  "Invalid OTP"

            clearAllOtpFields()


            // عشان العداد يضل يكمل شغل بالخلفية بدون ما يوقف
            isVerifying = false
        }
    }

    // دالة جديدة لعرض ديالوج الفشل والرجوع للخلف
    private fun showSummaryDialogForFailure(message: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_summary, null)
        val tvMessage = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val btnAction = dialogView.findViewById<Button>(R.id.btnDialogAction)
        val dialogGif = dialogView.findViewById<ImageView>(R.id.dialogGif)

        // استخدام الـ Cross GIF ليدل على الفشل
        Glide.with(this)
            .asGif()
            .load(R.raw.keybs_cross)
            .into(dialogGif)

        tvMessage.text = message

        val customDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnAction.setOnClickListener {
            customDialog.dismiss()
            finish() // الرجوع للشاشة السابقة مباشرة
        }

        customDialog.show()
    }

    //نجيب الفاوتشر ديتيلز
    private fun fetchTransactionAndShowSuccess() {
        val request = SavedVoucherRequest(
            mobileNumber = networkapi.mobileNumber,
            serviceCode = networkapi.serviceCode,
            iPayCustomerID = networkapi.iPayCustomerID
        )

        networkapi.apiService.getSavedVouchers(request).enqueue(object : Callback<SavedVoucherResponse> {
            override fun onResponse(call: Call<SavedVoucherResponse>, response: Response<SavedVoucherResponse>) {
                loadingSpinner.visibility = View.GONE // إخفاء السبينر نهائياً هنا
                var matchedVoucher: SavedVoucherItem? = null

                if (response.isSuccessful) {
                    val allVouchers = response.body()?.items ?: emptyList()

                    for (voucher in allVouchers) {
                        Log.d("OTPScreen", "voucher billingRef=${voucher.billingRef} vs transactionId=$transactionId")
                    }

                    for (voucher in allVouchers) {
                        if (voucher.billingRef == transactionId) {
                            matchedVoucher = voucher
                            break
                        }
                    }

                    if (matchedVoucher == null && allVouchers.isNotEmpty()) {
                        matchedVoucher = allVouchers.first()
                    }
                }

                if (matchedVoucher == null) {
                    Log.e("OTPScreen", "getSavedVouchers: no voucher found at all, code=${response.code()}")
                }

                showDialog(true, "Transaction Successful!", matchedVoucher)
            }

            override fun onFailure(call: Call<SavedVoucherResponse>, t: Throwable) {
                loadingSpinner.visibility = View.GONE // إخفاء عند الفشل
                Log.e("OTPScreen", "getSavedVouchers network failure: ${t.message}", t)
                showDialog(true, "Transaction Successful!", null)
            }
        })
    }

    // كلاس بسيط عشان يخفي الأرقام وتصير نقاط حمراء بعد ما اليوزر يكتبها
    class RedDotTransformationMethod : PasswordTransformationMethod() {
        override fun getTransformation(source: CharSequence, view: View): CharSequence {
            return PasswordCharSequence(source)
        }

        private class PasswordCharSequence(private val source: CharSequence) : CharSequence {
            override val length: Int = source.length
            override fun get(index: Int): Char = '●'
            override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
                return source.subSequence(startIndex, endIndex)
            }
        }
    }

    private fun showDialog(isSuccess: Boolean, message: String, voucher: SavedVoucherItem? = null) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_voucher)

        val dialogGif = dialog.findViewById<ImageView>(R.id.dialogGif)
        val dialogMessage = dialog.findViewById<TextView>(R.id.dialogMessage)
        val btnDialogAction = dialog.findViewById<Button>(R.id.btnDialogAction)

        dialogMessage.text = message

        if (isSuccess) {
            Glide.with(this).asGif().load(R.raw.keybs_tick).into(dialogGif)
        } else {
            Glide.with(this).asGif().load(R.raw.keybs_cross).into(dialogGif)
        }

        btnDialogAction.setOnClickListener {
            dialog.dismiss()
            handleDialogButtonClick(isSuccess, voucher)
        }

        dialog.show()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    // بعد ما اليوزر يدوس عالزر بالديالوج، وين لازم يروح
    private fun handleDialogButtonClick(isSuccess: Boolean, voucher: SavedVoucherItem?) {
        if (!isSuccess) {
            finish()
            return
        }

        if (voucher == null) {
            Toast.makeText(this, "server delay the voucher is in the saved voucher", Toast.LENGTH_LONG).show()
            finish() // منع الرجوع للرئيسية والاكتفاء بالرجوع للشاشة السابقة
            return
        }

        goToVoucherDetailsScreen(voucher)
        finish()
    }

    private fun goToVoucherDetailsScreen(voucher: SavedVoucherItem) {
        var displayText = voucher.productDisplayText
        if (displayText.isBlank()) {
            displayText = voucher.displayText
        }

        val intent = Intent(this, SavedVoucherDetailsActivity::class.java)
        intent.putExtra("providerName", voucher.providerName)
        intent.putExtra("displayText", displayText)
        intent.putExtra("providerImgUrl", voucher.providerImgUrl)
        intent.putExtra("productLogoUrl", voucher.productLogoUrl)
        intent.putExtra("reciptParams", voucher.reciptParams)
        intent.putExtra("redeemTitle", voucher.redeemTitle)
        intent.putStringArrayListExtra("howToRedeem", ArrayList(voucher.howToRedeem))

        startActivity(intent)
    }

//
//    private fun goToMainScreen() {
//        val intent = Intent(this, MainActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        startActivity(intent)
//    }
}