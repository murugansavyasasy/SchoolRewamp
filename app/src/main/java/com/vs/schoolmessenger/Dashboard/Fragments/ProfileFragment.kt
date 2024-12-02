package com.vs.schoolmessenger.Dashboard.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.vs.schoolmessenger.databinding.ProfileBinding

class ProfileFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: ProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = ProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }
}