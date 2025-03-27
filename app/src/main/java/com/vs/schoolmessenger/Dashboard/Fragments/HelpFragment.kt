package com.vs.schoolmessenger.Dashboard.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.vs.schoolmessenger.Auth.TermsConditions.TermsAndConditions
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.HelpFragmentBinding

class HelpFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: HelpFragmentBinding // Automatically generated binding class

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HelpFragmentBinding.inflate(layoutInflater)

        Constant.loadWebView(
            this.requireContext(),
            binding.webView,
            "https://www.schoolchimes.com/vs_web/help_line/"
        )

        return binding.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }
}