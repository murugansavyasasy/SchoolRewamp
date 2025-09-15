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
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Dashboard.Fragments.Model.ProfileField
import com.vs.schoolmessenger.Dashboard.Fragments.Model.ProfileItem
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ProfileFragmentBinding
import kotlin.collections.iterator

class ParentProfileRewampFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: ProfileFragmentBinding
    private lateinit var appViewModel: App
    private var isAccessToken: String? = null

    private var isChildDetails: ChildDetails? = null


    private var originalData: List<Map<String, List<ProfileField>>> = emptyList()

    private var adapter: ProfileRewampFragmentAdapter? = null

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
                originalData = response.data.map { section ->
                    section.mapValues { entry ->
                        entry.value.map { field ->
                            field.copy()
                        }
                    }
                }


                val sectionsMap = response.data.firstOrNull() ?: emptyMap()
                var photoUrl: String? = null

                for ((sectionName, fields) in sectionsMap) {
                    items.add(ProfileItem.Header(sectionName))
                    fields.forEach { field ->
                        items.add(ProfileItem.Field(field))

                        if (sectionName.equals("PhotoPath", ignoreCase = true) && field.node.equals(
                                "photoPath",
                                ignoreCase = true
                            )
                        ) {
                            photoUrl = field.value
                        }
                    }
                }

                adapter = ProfileRewampFragmentAdapter(items, requireContext())
                binding.recyclerview.adapter = adapter
                binding.recyclerview.visibility = View.VISIBLE
                binding.lytNoDataFound.visibility = View.GONE

                val defaultProfileRes = R.drawable.default_profile
                if (!photoUrl.isNullOrEmpty()) {
                    Glide.with(this).load(photoUrl).placeholder(defaultProfileRes)
                        .error(defaultProfileRes).into(binding.imgProfile)
                } else {
                    Glide.with(this).load(defaultProfileRes).into(binding.imgProfile)
                }
            } else {
                binding.recyclerview.visibility = View.GONE
                binding.lytNoDataFound.visibility = View.VISIBLE
            }
        }

        appViewModel.ispresubmission?.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.status) {
                    Constant.showDataValidation(
                        resources.getString(R.string.success), response.message, requireActivity()
                    )
                } else {
                    Constant.showDataValidation(
                        resources.getString(R.string.fail), response.message, requireActivity()
                    )
                }
            }
        }

        return binding.root
    }

    private fun fetchProfileData() {
        appViewModel.isParentprofilelist(isAccessToken!!)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnupdateprofile -> {
                isUpdateProfile()
            }
        }
    }

    private fun isUpdateProfile() {
        val changedData = JsonObject()

        for (section in originalData) {
            val originalFields = section.values.firstOrNull() ?: continue

            for (original in originalFields) {
                val current = adapter?.getUpdatedField(original.node)
                if (current != null && current.value != original.originalValue) {
                    changedData.addProperty(current.node, current.value ?: "")
                }
            }
        }

        Log.d("UpdatePayload", changedData.toString())
        appViewModel.ispresubmission(isAccessToken!!, changedData)
    }

}
