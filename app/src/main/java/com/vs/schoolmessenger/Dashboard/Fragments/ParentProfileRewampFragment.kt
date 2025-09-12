package com.vs.schoolmessenger.Dashboard.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.Fragments.Model.ProfileItem
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ProfileFragmentBinding


class ParentProfileRewampFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: ProfileFragmentBinding
    private lateinit var appViewModel: App
    private var isAccessToken: String? = null
    var userDetails: UserDetails? = null
    var staffDetails: StaffDetails? = null
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