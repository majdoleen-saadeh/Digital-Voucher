package com.example.digitalvouchers.Ui

import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.caverock.androidsvg.SVG
import com.example.digitalvouchers.Api.networkapi
import com.example.digitalvouchers.Data.Country
import com.example.digitalvouchers.Data.CountryRequest
import com.example.digitalvouchers.Data.CountryResponse
import com.example.digitalvouchers.Data.DeleteBillRequest
import com.example.digitalvouchers.Data.DeleteBillResponse
import com.example.digitalvouchers.Data.ProviderItem
import com.example.digitalvouchers.Data.ProvidersResponse
import com.example.digitalvouchers.Data.SavedVoucherItem
import com.example.digitalvouchers.Data.SavedVoucherRequest
import com.example.digitalvouchers.Data.SavedVoucherResponse
import com.example.digitalvouchers.Data.providersRequest
import com.example.digitalvouchers.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivityDebug"
    }
    private lateinit var btnBuyNew: View
    private lateinit var btnSavedVouchers: View
    private lateinit var ivBuyNewIcon: ImageView
    private lateinit var ivSavedIcon: ImageView
    private lateinit var tvBuyNew: TextView
    private lateinit var tvSaved: TextView

    // تعريف الـ ViewPager والـ Views المنفصلة
    private lateinit var viewPager: ViewPager2
    private lateinit var viewBuyNew: View
    private lateinit var viewSaved: View

    private lateinit var rvVouchers: RecyclerView
    private lateinit var tvSelectedCountryName: TextView
    private lateinit var SelectedCountryFlag: ImageView
    private lateinit var countryList: List<Country>
    private lateinit var selectedCountry: Country

    private lateinit var ProviderAdapter: ProviderAdapter
    private lateinit var CategoryAdapter: CategoryAdapter
    private lateinit var SavedVoucherAdapter: SavedVoucherAdapter
    private var savedVouchersLoaded = false

    private lateinit var etSearch: EditText
    private var allProviders: List<ProviderItem> = emptyList()
    private var selectedCategory: String? = null
    private lateinit var rvCategories: RecyclerView
    private lateinit var searchCountryRow: View
    private lateinit var layoutTabsContainer: android.widget.LinearLayout
    private lateinit var rvSavedVouchers: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "starting the app")
        setupGradientStatusBar()
        setContentView(R.layout.activity_main)

        Glide.get(this).registry
            .register(SVG::class.java, PictureDrawable::class.java, SvgToDrawable())
            .append(InputStream::class.java, SVG::class.java, StreamToSvg())

        //main activity elements
        btnBuyNew = findViewById(R.id.btnBuyNew)
        btnSavedVouchers = findViewById(R.id.btnSavedVouchers)
        ivBuyNewIcon = findViewById(R.id.ivBuyNewIcon)
        ivSavedIcon = findViewById(R.id.ivSavedIcon)
        tvBuyNew = findViewById(R.id.tvBuyNew)
        tvSaved = findViewById(R.id.tvSaved)
        layoutTabsContainer = findViewById(R.id.Tabs)
        viewPager = findViewById(R.id.viewPager)
        try {
            // ... binding views ...
            Log.d(TAG, "onCreate: views bound successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during binding", e)
        }
        //inflate for both of the screen
        viewBuyNew = layoutInflater.inflate(R.layout.layout_buy_new, null)
        viewSaved = layoutInflater.inflate(R.layout.layout_saved, null)

        //buy new element
        rvVouchers = viewBuyNew.findViewById(R.id.rvVouchers)
        etSearch = viewBuyNew.findViewById(R.id.etSearch)
        searchCountryRow = viewBuyNew.findViewById(R.id.searchCountryRow)
        rvCategories = viewBuyNew.findViewById(R.id.rvCategories)
        tvSelectedCountryName = viewBuyNew.findViewById(R.id.tvSelectedCountryName)
        SelectedCountryFlag = viewBuyNew.findViewById(R.id.SelectedCountryFlag)

        //saved element
        rvSavedVouchers = viewSaved.findViewById(R.id.rvSavedVouchers)

        //  إعداد الـ ViewPager2 باستخدام Adapter مخصص يعرض الـ Views
        viewPager.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            val views = listOf(viewBuyNew, viewSaved)

            inner class ViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = views[viewType]
                (view.parent as? ViewGroup)?.removeView(view)
                view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                return ViewPagerViewHolder(view)
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
            override fun getItemCount() = views.size
            override fun getItemViewType(position: Int) = position
        }
        rvCategories.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        //ما بدي يصير تضارب بالسحب عند الكاتيجوري
        rvCategories.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: android.view.MotionEvent): Boolean {
                when (e.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        // وقفت الفيو بيجر هون
                        rv.parent.requestDisallowInterceptTouchEvent(true)
                    }
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                        //بنرجع شغل الفيو بيجر لما اشيل اصبعي عن الكاتيجوري
                        rv.parent.requestDisallowInterceptTouchEvent(false)
                    }
                }

                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: android.view.MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        ProviderAdapter = ProviderAdapter(emptyList())
        ProviderAdapter.onProviderClick = { clickedProvider ->
            val intent = Intent(this, SelectedVoucherActivity::class.java)
            intent.putExtra("providerCode", clickedProvider.providerCode)
            intent.putExtra("countryIso", clickedProvider.countryIso)
            intent.putExtra("providerName", clickedProvider.name)
            intent.putExtra("countryFlagUrl", clickedProvider.flagUrl)
            startActivity(intent)
        }

        rvVouchers.layoutManager = GridLayoutManager(this, 3)
        rvVouchers.adapter = ProviderAdapter

        // الحالة الابتدائية للتطبيق
        layoutTabsContainer.isSelected = true
        setTabSelected(isBuyNewSelected = true)

        SavedVoucherAdapter = SavedVoucherAdapter()
        SavedVoucherAdapter.setOnItemClickListener { savedVoucher ->
            navigateToVoucherDetails(savedVoucher)
        }
        SavedVoucherAdapter.setOnDeleteClickListener { voucher, position ->
            deleteSavedVoucher(voucher, position)
        }
        rvSavedVouchers.layoutManager = LinearLayoutManager(this)
        rvSavedVouchers.adapter = SavedVoucherAdapter

        rvCategories.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        Log.d(TAG, " calling fetchCountriesFromServer")
        fetchCountriesFromServer()
        setupSwipeToDelete()

        CategoryAdapter = CategoryAdapter(emptyList()) { clickedCategory ->
            if (selectedCategory != clickedCategory.categoryId) {
                selectedCategory = clickedCategory.categoryId
                CategoryAdapter.setSelected(clickedCategory.categoryId)
                Filters()
            }
        }
        rvCategories.adapter = CategoryAdapter

        //view pager transaction between the pages
        btnBuyNew.setOnClickListener {
            Log.d(TAG, "btnBuyNew clicked")
            viewPager.currentItem = 0
        }

        btnSavedVouchers.setOnClickListener {
            Log.d(TAG, "btnSavedVouchers clicked")
            viewPager.currentItem = 1
        }

        // مستمع لمزامنة التابات مع السحب ولإخفاء الكيبورد أثناء السحب
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d(TAG, "onPageSelected: position=$position")
                if (position == 0) {
                    layoutTabsContainer.isSelected = true
                    setTabSelected(isBuyNewSelected = true)
                    fetchProviderCategories(
                        if (::selectedCountry.isInitialized) selectedCountry.countryIso else "all"
                    )
                } else {
                    layoutTabsContainer.isSelected = false
                    setTabSelected(isBuyNewSelected = false)
                    if (!savedVouchersLoaded) {
                        Log.d(TAG, " loading saved vouchers for the first time")
                        fetchSavedVouchers()
                        savedVouchersLoaded = true
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    etSearch.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(etSearch.windowToken, 0)
                }
            }
        })

        tvSelectedCountryName.setOnClickListener {
            if (::countryList.isInitialized) {
                val sheet = CountryScreen.newInstance(ArrayList(countryList))
                sheet.onCountrySelected = { country ->
                    selectedCountry = country
                    tvSelectedCountryName.text = formatCountryName(country.name)
                    loadSvg(SelectedCountryFlag, country.flagUrl)
                    selectedCategory = null
                    CategoryAdapter.setSelected(null)
                    fetchProviderCategories(country.countryIso)
                }
                sheet.show(supportFragmentManager, "CountrySheet")
            }
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Filters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        Log.d(TAG, "onCreate: finished")
    }
    private fun setTabSelected(isBuyNewSelected: Boolean) {
        Log.d(TAG, "setTabSelected: isBuyNewSelected=$isBuyNewSelected")

        btnBuyNew.isSelected = isBuyNewSelected
        tvBuyNew.isSelected = isBuyNewSelected
        ivBuyNewIcon.visibility = if (isBuyNewSelected) View.VISIBLE else View.GONE

        btnSavedVouchers.isSelected = !isBuyNewSelected
        tvSaved.isSelected = !isBuyNewSelected
        ivSavedIcon.visibility = if (!isBuyNewSelected) View.VISIBLE else View.GONE
    }

    private fun Filters() {
        val searchText = etSearch.text.toString().trim()
        val filtered = allProviders.filter { provider ->
            val matchesCategory = selectedCategory == null || provider.categoryId == selectedCategory
            val matchesSearch = searchText.isEmpty() || provider.name.contains(searchText, ignoreCase = true)
            matchesCategory && matchesSearch
        }
        ProviderAdapter.updateData(filtered)
    }

    private fun fetchProviderCategories(countryCode: String) {
        Log.d(TAG, "fetchProviderCategories: countryCode=$countryCode")
        val requestBody = providersRequest(
            mobileNumber = networkapi.mobileNumber,
            serviceCode = "INT_VOUCHER",
            countryCode = countryCode
        )

        networkapi.apiService.getProvidersCategories(requestBody)
            .enqueue(object : Callback<ProvidersResponse> {
                override fun onResponse(call: Call<ProvidersResponse>, response: Response<ProvidersResponse>) {
                    Log.d(TAG, "fetchProviderCategories onResponse: code=${response.code()} successful=${response.isSuccessful}")
                    if (response.isSuccessful && response.body() != null) {
                        val providersData = response.body()!!
                        Log.d("TESTAPI", "providersData: ${providersData}")

                        if (providersData.status == "SUCCESS") {
                            allProviders = providersData.items.flatMap { categoryItem ->
                                categoryItem.providers.onEach { provider ->
                                    provider.categoryId = categoryItem.categoryId
                                }
                            }
                            CategoryAdapter.updateData(providersData.items)
                            ProviderAdapter.updateData(allProviders)
                            Log.d("TAG", "Providers loaded: ${allProviders.size}")

                            if (selectedCategory == null) {
                                val firstCategory = providersData.items.firstOrNull()
                                if (firstCategory != null) {
                                    selectedCategory = firstCategory.categoryId
                                    CategoryAdapter.setSelected(firstCategory.categoryId)
                                    Filters()
                                }
                            }
                        } else {
                            showError("error: ${providersData.status}")
                        }
                    } else {
                        showError("error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ProvidersResponse>, t: Throwable) {
                    Log.e(TAG, "fetchProviderCategories onFailure: ${t.javaClass.simpleName} - ${t.message}", t)
                    showError("error ${t.message}")
                }
            })
    }

    private fun formatCountryName(name: String): String {
        return name.replace(" ", "\n")
    }

    private fun fetchCountriesFromServer() {
        Log.d(TAG, "fetchCountriesFromServer: sending request, baseUrl=${networkapi.apiService}")
        val requestBody = CountryRequest(
            serviceCode = networkapi.serviceCode,
            mobileNumber = networkapi.mobileNumber
        )

        networkapi.apiService.getCountries(requestBody)
            .enqueue(object : Callback<CountryResponse> {
                override fun onResponse(call: Call<CountryResponse>, response: Response<CountryResponse>) {
                    Log.d(TAG, "fetchCountriesFromServer onResponse: code=${response.code()} successful=${response.isSuccessful}")
                    if (response.isSuccessful && response.body() != null) {
                        val countryData = response.body()!!
                        Log.d("TESTAPI", "countryData: ${countryData}")

                        if (countryData.status == "SUCCESS") {
                            countryList = countryData.items
                            if (countryList.isNotEmpty()) {
                                selectedCountry = countryList[0]
                                tvSelectedCountryName.text = formatCountryName(selectedCountry.name)
                                loadSvg(SelectedCountryFlag, selectedCountry.flagUrl)
                                fetchProviderCategories(selectedCountry.countryIso)
                            }
                        } else {
                            showError("error: ${countryData.status}")
                        }
                    }
                }

                override fun onFailure(call: Call<CountryResponse>, t: Throwable) {
                    Log.e(TAG, "fetchCountriesFromServer onFailure: ${t.javaClass.simpleName} - ${t.message}", t)
                    showError("error: ${t.message}")
                }
            })
    }

    private fun setupSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 2f

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun onChildDraw(
                c: android.graphics.Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val holder = viewHolder as SavedVoucherAdapter.SavedVoucherViewHolder
                    val position = holder.adapterPosition

                    val isCurrentlyOpen = position == SavedVoucherAdapter.openPosition
                    val baseX = if (isCurrentlyOpen) -holder.maxSwipeDx else 0f

                    var targetX = baseX + dX
                    targetX = targetX.coerceIn(-holder.maxSwipeDx, 0f)

                    holder.viewForeground.translationX = targetX
                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                val holder = viewHolder as SavedVoucherAdapter.SavedVoucherViewHolder
                val position = holder.adapterPosition
                if (position == RecyclerView.NO_POSITION) return

                val currentX = holder.viewForeground.translationX
                val shouldOpen = currentX < -holder.maxSwipeDx / 2

                holder.viewForeground.animate()
                    .translationX(if (shouldOpen) -holder.maxSwipeDx else 0f)
                    .setDuration(200)
                    .start()

                if (shouldOpen) {
                    SavedVoucherAdapter.setOpenPosition(position)
                } else {
                    if (position == SavedVoucherAdapter.openPosition) {
                        SavedVoucherAdapter.swipeClose()
                    }
                }
            }
        }

        ItemTouchHelper(swipeCallback).attachToRecyclerView(rvSavedVouchers)
    }

    private fun fetchSavedVouchers() {
        Log.d(TAG, "fetchSavedVouchers: sending request")
        val requestBody = SavedVoucherRequest(
            mobileNumber = networkapi.mobileNumber,
            serviceCode = networkapi.serviceCode,
            iPayCustomerID = networkapi.iPayCustomerID
        )

        networkapi.apiService.getSavedVouchers(requestBody)
            .enqueue(object : Callback<SavedVoucherResponse> {
                override fun onResponse(call: Call<SavedVoucherResponse>, response: Response<SavedVoucherResponse>) {
                    Log.d(TAG, "fetchSavedVouchers onResponse: code=${response.code()} successful=${response.isSuccessful}")
                    if (response.isSuccessful && response.body() != null) {
                        val savedVouchersData = response.body()!!
                        if (savedVouchersData.status == "SUCCESS") {
                            SavedVoucherAdapter.setVouchers(savedVouchersData.items)
                        } else {
                            showError("error: ${savedVouchersData.status}")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.d("SAVEDVOUCHERS", "code=${response.code()} body=$errorBody")
                        showError("error: ${response.code()} - $errorBody")
                    }
                }

                override fun onFailure(call: Call<SavedVoucherResponse>, t: Throwable) {
                    Log.e(TAG, "fetchSavedVouchers onFailure: ${t.javaClass.simpleName} - ${t.message}", t)
                    showError("error: ${t.message}")
                }
            })
    }

    private fun deleteSavedVoucher(voucher: SavedVoucherItem, position: Int) {
        val billId = voucher.id.toIntOrNull()
        if (billId == null) {
            showError("unavailable id")
            return
        }

        val requestBody = DeleteBillRequest(id = billId)

        networkapi.apiService.deleteBill(requestBody)
            .enqueue(object : Callback<DeleteBillResponse> {
                override fun onResponse(call: Call<DeleteBillResponse>, response: Response<DeleteBillResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val deleteResult = response.body()!!
                        if (deleteResult.status == "SUCCESS") {
                            SavedVoucherAdapter.removeItem(position)

                            // إعداد الديالوج
                            val dialogView = layoutInflater.inflate(R.layout.dialog_summary, null)
                            val tvMessage = dialogView.findViewById<TextView>(R.id.dialogMessage)
                            val btnAction = dialogView.findViewById<Button>(R.id.btnDialogAction)
                            val dialogGif = dialogView.findViewById<ImageView>(R.id.dialogGif)


                            Glide.with(this@MainActivity)
                                .asGif()
                                .load(R.raw.keybs_summary)
                                .into(dialogGif)

                            tvMessage.text = deleteResult.message

                            val customDialog = androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
                                .setView(dialogView)
                                .setCancelable(false)
                                .create()

                            customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

                            btnAction.setOnClickListener {// اضل بنفس الصفحة
                                customDialog.dismiss()
                            }

                            customDialog.show()
                        } else {
                            showError("error: ${deleteResult.status}")
                        }
                    } else {
                        showError("error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<DeleteBillResponse>, t: Throwable) {
                    showError("error: ${t.message}")
                }
            })
    }
    private fun setupGradientStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
            val background: Drawable? = ContextCompat.getDrawable(this, R.drawable.gradient_status)
            window.setBackgroundDrawable(background)
        }
    }

    private fun navigateToVoucherDetails(voucher: SavedVoucherItem) {
        val intent = Intent(this, SavedVoucherDetailsActivity::class.java).apply {
            putExtra("providerName", voucher.providerName)
            putExtra("displayText", voucher.productDisplayText.ifBlank { voucher.displayText })
            putExtra("providerImgUrl", voucher.providerImgUrl)
            putExtra("productLogoUrl", voucher.productLogoUrl)
            putExtra("reciptParams", voucher.reciptParams)
            putExtra("redeemTitle", voucher.redeemTitle)
            putStringArrayListExtra("howToRedeem", ArrayList(voucher.howToRedeem))
        }
        startActivity(intent)
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}