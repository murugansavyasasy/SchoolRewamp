package com.vs.schoolmessenger.Auth.Country

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.MobileNumber
import com.vs.schoolmessenger.Auth.TermsConditions.TermsAndConditions
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Repository.RestClient
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.Utils.ToastManager
import com.vs.schoolmessenger.databinding.CountryListScreenBinding

class CountryScreen : BaseActivity<CountryListScreenBinding>(), View.OnClickListener,
    CountryClickListener {

    override fun getViewBinding(): CountryListScreenBinding {
        return CountryListScreenBinding.inflate(layoutInflater)
    }

    var authViewModel: Auth? = null
    private var isAgree = false
    var isCountrySelected: Boolean? = false

    private lateinit var mAdapter: CountryListAdapter


    override fun setupViews() {
        super.setupViews()
        binding.rytBack.setOnClickListener(this)
        binding.btnContinue.setOnClickListener(this)
//        setupToolbar()
        isToolBarPrimaryTheme()

        Log.d("CountryScreen", "onCreate triggered")

        authViewModel = ViewModelProvider(this).get(Auth::class.java)
        authViewModel!!.init()

        binding.lblTermsConditions.setOnClickListener {
            startActivity(Intent(this, TermsAndConditions::class.java))
        }

        binding.termsCheckbox.setOnCheckedChangeListener { _, isChecked ->
            isAgree = isChecked
        }

        isCountry()

        authViewModel!!.isCountryList?.observe(this) { response ->
            if (response != null) {
                val status = response.status
                response.message
                if (status) {
                    val isCountryList = response.data


//                    isLoadCountry(isCountryList)

                    loadCountry(isCountryList)

                }
            }
        }


        binding.txtCountry.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                mAdapter.filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadCountry(countryList: List<Country>) {
        val updatedList = countryList.toMutableList()
        updatedList.add(3, Country(0, "", 0, 0, "", "", "", ""))
        mAdapter = CountryListAdapter(this, updatedList) { selectedCountry ->
            isCountrySelected = true
            Constant.country_details = selectedCountry

//            Toast.makeText(this, "Selected: ${selectedCountry.name}", Toast.LENGTH_SHORT).show()
        }
        binding.recycleCountry.layoutManager = LinearLayoutManager(this)
        binding.recycleCountry.adapter = mAdapter


    }

    private fun isCountry() {
        authViewModel!!.isCountryList()
    }


//    private fun isLoadCountry(countryList: List<Country>) {
//
//        val isCountryList =
//            listOf(Country(-0, "Select Your Country", -0, -0, "", "", "", "")) + countryList
//        val adapter = CountrySpinnerAdapter(this@CountryScreen, isCountryList)
//        binding.isSpinner.adapter = adapter
//
//        if (isCountryList.isNotEmpty()) {
//            Constant.country_details = isCountryList[0]
//        }
//
//        binding.isSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                if (position != 0) {
//                    isCountrySelected = true
//                    Constant.country_details = isCountryList[position]
//                    Log.d(
//                        "SelectedCountry",
//                        "ID: ${Constant.country_details!!.id}, Name: ${Constant.country_details!!.name}"
//                    )
//                } else {
//                    isCountrySelected = false
//                }
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {}
//        }
//    }


    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btnContinue -> {
                if (isCountrySelected == true) {
                    if (isAgree) {
                    ToastManager.cancelToast()
                    SharedPreference.putCountryId(
                        this,
                        Constant.country_details!!.id
                    )
                    SharedPreference.putBaseUrl(this, Constant.country_details!!.base_url)
                    RestClient.changeApiBaseUrl(Constant.country_details!!.base_url)
                    val intent = Intent(this@CountryScreen, MobileNumber::class.java)
                    startActivity(intent)
                     }
                    else {
                        ToastManager.showToast(this, R.string.AgreeTermsConditions)
                    }
                } else {
                    ToastManager.showToast(this, R.string.lblChoosecountry)
                }
            }

            R.id.rytBack -> {
                onBackPressed()
            }

        }
    }

    override fun onItemClick(data: Country) {

    }
}
