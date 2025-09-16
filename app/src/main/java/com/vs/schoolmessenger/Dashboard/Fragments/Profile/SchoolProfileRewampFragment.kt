package com.vs.schoolmessenger.Dashboard.Fragments.Profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Dashboard.Fragments.Model.ProfileField
import com.vs.schoolmessenger.Dashboard.Fragments.Model.ProfileItem
import com.vs.schoolmessenger.Dashboard.Fragments.Profile.Listener.DocumentClickListener
import com.vs.schoolmessenger.Dashboard.Fragments.Profile.ProfileRewampFragmentAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ProfileFragmentBinding
import kotlin.collections.iterator

class SchoolProfileRewampFragment : Fragment(), View.OnClickListener, DocumentClickListener {
    private lateinit var binding: ProfileFragmentBinding
    private lateinit var appViewModel: App
    private var isAccessToken: String? = null
    var userDetails: UserDetails? = null
    var staffDetails: StaffDetails? = null
    private var isStaffDetails: StaffDetails? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = ProfileFragmentBinding.inflate(layoutInflater)
        isStaffDetails = SharedPreference.getStaffDetails(requireContext())
        isAccessToken = isStaffDetails!!.access_token

        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel.init()

        fetchProfileData()

        binding.btnupdateprofile.visibility = View.GONE
        binding.btnupdateprofile.setOnClickListener(this)

        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())

        appViewModel.isSchoolprofilelist?.observe(viewLifecycleOwner) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                val items = mutableListOf<ProfileItem>()

                val sectionsMap = response.data.firstOrNull() ?: emptyMap()

                var photoUrl: String? = null

                for ((sectionName, fields) in sectionsMap) {
                    items.add(ProfileItem.Header(sectionName))

                    fields.forEach { field ->
                        items.add(ProfileItem.Field(field))

                        if (sectionName.equals("PhotoPath", ignoreCase = true) &&
                            field.node.equals("photoPath", ignoreCase = true)
                        ) {
                            photoUrl = field.value
                            Log.d("Profile Image Details", "Photo URL -> $photoUrl")
                        }
                    }
                }

                binding.recyclerview.adapter = ProfileRewampFragmentAdapter(items, requireContext(),this)
                binding.recyclerview.visibility = View.VISIBLE
                binding.lytNoDataFound.visibility = View.GONE


                val defaultProfileRes = R.drawable.default_profile
                if (!photoUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(photoUrl)
                        .placeholder(defaultProfileRes)
                        .error(defaultProfileRes)
                        .into(binding.imgProfile)
                } else {
                    Glide.with(this)
                        .load(defaultProfileRes)
                        .into(binding.imgProfile)
                }

            } else {
                binding.recyclerview.visibility = View.GONE
                binding.lytNoDataFound.visibility = View.VISIBLE
            }
        }


        return binding.root
    }


    private fun fetchProfileData() {
        appViewModel!!.isSchoolprofilelist(isAccessToken!!)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }

    override fun onDocumentClicked(
        field: ProfileField,
        position: Int
    ) {
        TODO("Not yet implemented")
    }
}