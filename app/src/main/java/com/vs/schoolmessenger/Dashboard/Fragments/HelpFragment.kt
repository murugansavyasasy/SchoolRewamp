package com.vs.schoolmessenger.Dashboard.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.ChildHomeFragmentBinding
import com.vs.schoolmessenger.databinding.HelpFragmentBinding

class HelpFragment : Fragment() {
    private lateinit var binding: HelpFragmentBinding // Automatically generated binding class

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HelpFragmentBinding.inflate(layoutInflater)


        return binding.root
    }
}