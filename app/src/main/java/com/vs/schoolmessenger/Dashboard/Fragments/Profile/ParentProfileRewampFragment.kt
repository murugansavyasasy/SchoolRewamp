package com.vs.schoolmessenger.Dashboard.Fragments.Profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.Fragments.Model.ProfileItem
import com.vs.schoolmessenger.Dashboard.Fragments.Profile.ProfileRewampFragmentAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ProfileFragmentBinding
import kotlin.collections.iterator

class ParentProfileRewampFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: ProfileFragmentBinding
    private lateinit var appViewModel: App
    private var isAccessToken: String? = null

    private var isChildDetails: ChildDetails? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = ProfileFragmentBinding.inflate(layoutInflater)
        isChildDetails = SharedPreference.getChildDetails(requireContext())
        isAccessToken = isChildDetails?.access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel.init()

        fetchProfileData()

        binding.btnupdateprofile.setOnClickListener(this)

        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())

        appViewModel.isParentprofilelist?.observe(viewLifecycleOwner) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                val items = mutableListOf<ProfileItem>()

                val sectionsMap = response.data.firstOrNull() ?: emptyMap()


                for ((sectionName, fields) in sectionsMap) {

                    items.add(ProfileItem.Header(sectionName))


                    fields.forEach { field ->
                        items.add(ProfileItem.Field(field))
                    }
                }

                binding.recyclerview.adapter = ProfileRewampFragmentAdapter(items, requireContext())
                binding.recyclerview.visibility = View.VISIBLE
                binding.lytNoDataFound.visibility = View.GONE
            } else {
                binding.recyclerview.visibility = View.GONE
                binding.lytNoDataFound.visibility = View.VISIBLE
            }
        }


        val profileUrl: String? = isChildDetails!!.profile
        val defaultProfileRes = R.drawable.default_profile
        if (!profileUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(profileUrl)
                .placeholder(defaultProfileRes)
                .error(defaultProfileRes)
                    .into(binding.imgProfile)
        } else {
            Glide.with(this)
                .load(defaultProfileRes)
                .into(binding.imgProfile)
        }

        return binding.root
    }


    private fun fetchProfileData() {
        appViewModel!!.isParentprofilelist(isAccessToken!!)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }
}