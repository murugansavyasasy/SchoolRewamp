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
        isToolBarPrimaryTheme1(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )
        binding.termsCheckbox.buttonTintList = null


        Log.d("CountryScreen", "onCreate triggered")

        authViewModel = ViewModelProvider(this).get(Auth::class.java)
        authViewModel!!.init()

        binding.lblTermsConditions.setOnClickListener {
            val intent = Intent(this, TermsAndConditions::class.java)
            intent.putExtra("screen_name", "isTerms")
            startActivity(intent)
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
        Constant.hideLoading(this)
        val updatedList = countryList.toMutableList()
        if (updatedList.isNotEmpty()){
            binding.rytSearch.visibility= View.VISIBLE
        }else{
            binding.rytSearch.visibility= View.GONE
        }
        updatedList.add(3, Country(0, "", 0, 0, "", "", "", ""))
        mAdapter = CountryListAdapter(this, updatedList, this) { selectedCountry ->
            isCountrySelected = true
            Constant.country_details = selectedCountry
        }
        binding.recycleCountry.layoutManager = LinearLayoutManager(this)
        binding.recycleCountry.adapter = mAdapter


    }

    private fun isCountry() {
        Constant.showLoading(this)
        authViewModel!!.isCountryList()
    }


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
                        SharedPreference.putReportingUrl(
                            this,
                            Constant.country_details!!.reporting_url
                        )
                        val intent = Intent(this@CountryScreen, MobileNumber::class.java)
                        startActivity(intent)
                    } else {
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

    override fun onEmptyStateChanged(isEmpty: Boolean) {
        if (isEmpty) {
            binding.lytList.visibility = View.VISIBLE
            binding.recycleCountry.visibility = View.GONE
            binding.lblPopular.visibility = View.GONE
            binding.txtNoData.text = getString(R.string.no_country_found)
        } else {
            binding.recycleCountry.visibility = View.VISIBLE
            binding.lblPopular.visibility = View.VISIBLE
            binding.lytList.visibility = View.GONE
        }
    }
}