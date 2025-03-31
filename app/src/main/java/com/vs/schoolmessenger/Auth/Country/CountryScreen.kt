package com.vs.schoolmessenger.Auth.Country

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.Login
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.MobileNumber
import com.vs.schoolmessenger.Auth.TermsConditions.TermsAndConditions
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.Auth
import com.vs.schoolmessenger.Repository.RestClient
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.CountryListScreenBinding

class CountryScreen : BaseActivity<CountryListScreenBinding>(), View.OnClickListener {

    override fun getViewBinding(): CountryListScreenBinding {
        return CountryListScreenBinding.inflate(layoutInflater)
    }

    var authViewModel: Auth? = null
    private var isAgree = false

    override fun setupViews() {
        super.setupViews()
        binding.btnArrowNext.setOnClickListener(this)
        setupToolbar()
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

        val isCountryList = listOf(Country(-0, "Select Country", -0, -0, "", "", "", "")) + countryList
        val adapter = CountrySpinnerAdapter(this@CountryScreen, isCountryList)
        binding.isSpineer.adapter = adapter

        if (isCountryList.isNotEmpty()) {
            Constant.country_details = isCountryList[0]
        }

        // Handle selection event
        binding.isSpineer.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) {
                    Constant.country_details = isCountryList[position]

                    Log.d(
                        "SelectedCountry",
                        "ID: ${Constant.country_details!!.id}, Name: ${Constant.country_details!!.name}"
                    )

                    Toast.makeText(
                        this@CountryScreen,
                        "Selected: ${Constant.country_details!!.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnArrowNext -> {
                if (Constant.country_details!!.id != -0) {
                    if (isAgree) {
                        SharedPreference.putCountryId(
                            this,
                            Constant.country_details!!.id.toString()
                        )
                        RestClient.changeApiBaseUrl(Constant.country_details!!.base_url)
                        val intent = Intent(this@CountryScreen, MobileNumber::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, R.string.AgreeTermsConditions, Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(this, "Select Country", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
