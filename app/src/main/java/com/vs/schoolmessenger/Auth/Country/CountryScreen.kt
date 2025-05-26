package com.vs.schoolmessenger.Auth.Country

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
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

class CountryScreen : BaseActivity<CountryListScreenBinding>(), View.OnClickListener {

    override fun getViewBinding(): CountryListScreenBinding {
        return CountryListScreenBinding.inflate(layoutInflater)
    }

    var authViewModel: Auth? = null
    private var isAgree = false
    var isCountrySelected: Boolean? = false


    override fun setupViews() {
        super.setupViews()
        binding.btnArrowNext.setOnClickListener(this)
        setupToolbar()
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
                val message = response.message
                if (status) {
                    val isCountryList = response.data
                    isLoadCountry(isCountryList)
                }
            }
        }
    }

    private fun isCountry() {
        authViewModel!!.isCountryList()
    }

    private fun isLoadCountry(countryList: List<Country>) {

        val isCountryList =
            listOf(Country(-0, "Select Your Country", -0, -0, "", "", "", "")) + countryList
        val adapter = CountrySpinnerAdapter(this@CountryScreen, isCountryList)
        binding.isSpinner.adapter = adapter

        if (isCountryList.isNotEmpty()) {
            Constant.country_details = isCountryList[0]
        }

        binding.isSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) {
                    isCountrySelected = true
                    Constant.country_details = isCountryList[position]
                    Log.d(
                        "SelectedCountry",
                        "ID: ${Constant.country_details!!.id}, Name: ${Constant.country_details!!.name}"
                    )
                } else {
                    isCountrySelected = false
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnArrowNext -> {
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
                    } else {
                        ToastManager.showToast(this, R.string.AgreeTermsConditions)
                    }
                } else {
                    ToastManager.showToast(this, R.string.lblChoosecountry)
                }
            }
        }
    }
}
