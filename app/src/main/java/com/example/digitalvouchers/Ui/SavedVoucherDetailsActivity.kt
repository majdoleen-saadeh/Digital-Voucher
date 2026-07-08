package com.example.digitalvouchers.Ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.digitalvouchers.R
import org.json.JSONObject
import androidx.activity.enableEdgeToEdge
import android.graphics.Canvas
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import android.net.Uri

class SavedVoucherDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.savedvoucherdetails)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnClose = findViewById<ImageView>(R.id.btnClose)
        val ivProviderLogo = findViewById<ImageView>(R.id.ivDetailsProviderLogo)
        val tvProviderName = findViewById<TextView>(R.id.tvDetailsProviderName)
        val tvDisplayText = findViewById<TextView>(R.id.tvDetailsDisplayText)
        val tvPinCode = findViewById<TextView>(R.id.tvPinCode)
        val btnCopyPin = findViewById<RelativeLayout>(R.id.btnCopyPin)
        val txtRedeemTitle = findViewById<TextView>(R.id.txtRedeemTitle)

        val accordionHeader = findViewById<View>(R.id.accordion_header)
        val accordionContent = findViewById<LinearLayout>(R.id.accordion_content)
        val arrowIcon = findViewById<ImageView>(R.id.arrow_icon)
        val btnShare = findViewById<RelativeLayout>(R.id.btnShare)

        val providerName = intent.getStringExtra("providerName").orEmpty()
        val displayText = intent.getStringExtra("displayText").orEmpty()
        val providerImgUrl = intent.getStringExtra("providerImgUrl").orEmpty()
        val productLogoUrl = intent.getStringExtra("productLogoUrl").orEmpty()
        val reciptParams = intent.getStringExtra("reciptParams").orEmpty()
        val redeemTitle = intent.getStringExtra("redeemTitle").orEmpty()
        val howToRedeem = intent.getStringArrayListExtra("howToRedeem") ?: arrayListOf()

        tvProviderName.text = providerName
        tvDisplayText.text = displayText
        if (redeemTitle.isNotBlank()) txtRedeemTitle.text = redeemTitle

        Glide.with(this)
            .load(productLogoUrl)
            .into(ivProviderLogo)

        val pin = extractPin(reciptParams)
        tvPinCode.text = pin

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

        // الهاو تو ريديم بتضل مفتوحة
        accordionContent.visibility = View.VISIBLE
        arrowIcon.rotation = 180f

        btnBack.setOnClickListener { finish() }
        btnClose.setOnClickListener { finish() }

        btnCopyPin.setOnClickListener {
            copyToClipboard(pin)
        }

        accordionHeader.setOnClickListener {
            val isExpanded = accordionContent.visibility == View.VISIBLE
            accordionContent.visibility = if (isExpanded) View.GONE else View.VISIBLE
            arrowIcon.rotation = if (isExpanded) 0f else 180f
        }

        btnShare.setOnClickListener {
            shareVoucher(providerName, displayText, pin)
        }
    }

    private fun extractPin(reciptParams: String): String {
        return try {
            JSONObject(reciptParams).optString("pin", "")
        } catch (e: Exception) {
            ""
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("voucher_pin", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
    }

    private fun shareVoucher(providerName: String, displayText: String, pin: String) {
        // تحديد الـ ScrollView كـ ScrollView وليس كـ View عام
        val scrollContainer = findViewById<android.widget.ScrollView>(R.id.scrollContainer)
        val bitmap = captureScreen(scrollContainer)

        if (bitmap != null) {
            shareScreenshot(this, bitmap)
        } else {
            Toast.makeText(this, "error while taking the screen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun captureScreen(scrollView: android.widget.ScrollView): Bitmap? {
        return try {
            val childView = scrollView.getChildAt(0) ?: return null

            // 1. تحديد الـ Views اللي بدنا نخفيها مؤقتاً أثناء التصوير
            val headerIcons = findViewById<RelativeLayout>(R.id.headerIcons) // رح نعمل ID لهذا الـ RelativeLayout

            // 2. إخفاء العناصر قبل أخذ اللقطة
            // ملاحظة: تأكدي إنك حطيتي ID للـ RelativeLayout اللي فيه السهم والاكس
            headerIcons.visibility = View.INVISIBLE

            val bitmap = Bitmap.createBitmap(childView.width, childView.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            canvas.drawColor(android.graphics.Color.WHITE)
            childView.draw(canvas)

            // 3. إظهار العناصر مرة ثانية فوراً بعد التصوير
            headerIcons.visibility = View.VISIBLE

            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun shareScreenshot(context: Context, bitmap: Bitmap) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "voucher_screenshot.png")
            val fileOutputStream = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Share Voucher Image"))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "error while sharing", Toast.LENGTH_SHORT).show()
        }
    }
}