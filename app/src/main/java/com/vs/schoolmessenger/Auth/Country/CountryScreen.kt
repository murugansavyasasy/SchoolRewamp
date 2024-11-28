package com.vs.schoolmessenger.Auth.Country

import android.content.Intent
import android.view.View
import android.widget.Toast
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.Login
import com.vs.schoolmessenger.Auth.TermsConditions.TermsAndConditions
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.CountryListScreenBinding

class CountryScreen : BaseActivity<CountryListScreenBinding>(), View.OnClickListener {

    override fun getViewBinding(): CountryListScreenBinding {
        return CountryListScreenBinding.inflate(layoutInflater)
    }

    private var isAgree = false

    override fun setupViews() {
        super.setupViews()
        // Access a specific view using its ID
        binding.btnArrowNext.setOnClickListener(this)
        setupToolbar()

        binding.lblTermsConditions.setOnClickListener {
            startActivity(Intent(this, TermsAndConditions::class.java))
        }

        binding.termsCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                isAgree = true
            } else {
                isAgree = false
            }
        }
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.btnArrowNext -> {
                if (isAgree) {
                    val intent = Intent(this@CountryScreen, Login::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, R.string.AgreeTermsConditions, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}