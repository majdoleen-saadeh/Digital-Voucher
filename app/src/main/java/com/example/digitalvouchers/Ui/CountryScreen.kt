package com.example.digitalvouchers.Ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.digitalvouchers.Data.Country
import com.example.digitalvouchers.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CountryScreen : BottomSheetDialogFragment() {

    private lateinit var rvcountry: RecyclerView
    private lateinit var countryAdapter: CountryAdapter
    private lateinit var etCountrySearch: EditText

    // Callback
    var onCountrySelected: ((Country) -> Unit)? = null

    companion object {
        fun newInstance(countryList: ArrayList<Country>): CountryScreen {
            val fragment = CountryScreen()
            val args = Bundle()
            args.putSerializable("countryList", countryList)
            fragment.arguments = args
            return fragment
        }
    }
//بننشئ الفيو عنا
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.countryscreen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvcountry = view.findViewById(R.id.rvcountry)
        etCountrySearch = view.findViewById(R.id.etCountrySearch)
//بنحول الداتا عنا
        val countries = arguments?.getSerializable("countryList") as? ArrayList<Country> ?: arrayListOf()

        rvcountry.layoutManager = LinearLayoutManager(requireContext())
//بنعطي الدول للادابتر
        countryAdapter = CountryAdapter(countries) { selectedCountry ->
            onCountrySelected?.invoke(selectedCountry)
            dismiss()
        }
        rvcountry.adapter = countryAdapter

        etCountrySearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                countryAdapter.filter(s.toString())//بناخد النص وبنعطيه للادابتر يفلتلره
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}

//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.countryscreen)
//
//        rvcountry = findViewById(R.id.rvcountry)
//        etCountrySearch = findViewById(R.id.etCountrySearch)
//
//        rvcountry.layoutManager = LinearLayoutManager(this)
//
//        val receivedCountries =
//            intent.getSerializableExtra("countryList") as? ArrayList<Country> ?: arrayListOf()
//
//        CountryAdapter = CountryAdapter(receivedCountries)
//        rvcountry.adapter = CountryAdapter
//
//
//        etCountrySearch.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                CountryAdapter.filter(s.toString())
//            }
//            override fun afterTextChanged(s: Editable?) {}
//        })}
