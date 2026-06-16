package com.vs.schoolmessenger.Dashboard.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ProfileBinding

class ProfileFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: ProfileBinding
    var userDetails: UserDetails? = null
    var staffDetails: StaffDetails? = null
    var childDetails: ChildDetails? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = ProfileBinding.inflate(layoutInflater)
        userDetails = SharedPreference.getUserDetails(requireActivity())
        staffDetails = userDetails!!.staff_details[0]
        childDetails = SharedPreference.getChildDetails(requireActivity())

        if (Constant.isParentChoose) {
            binding.rytEdit.visibility = View.VISIBLE
            binding.rytHeader.setBackgroundResource(R.drawable.gradient_theme_school)
            setupStudentDetails()

        } else {
            binding.rytEdit.visibility = View.GONE
            binding.lblSchoolName.visibility = View.GONE
            binding.lblRegisterNo.visibility = View.GONE
            binding.lnrStandardDetails.visibility = View.GONE
            binding.rytHeader.setBackgroundResource(R.drawable.gradient_theme_school)
            setupStaffDetails()

        }
        return binding.root
    }

    private fun setupStaffDetails() {

        val profileUrl: String = staffDetails!!.staff_profile
        val name: String = staffDetails!!.name
        staffDetails!!.school_name
        staffDetails!!.school_address
        staffDetails!!.school_logo
        staffDetails!!.mobile_no
        staffDetails!!.email
        staffDetails!!.address
        staffDetails!!.blood_group

        val defaultProfileRes = R.drawable.default_profile
        if (!profileUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(profileUrl)
                .placeholder(defaultProfileRes) // While loading
                .error(defaultProfileRes)       // If failed to load
                .centerCrop()  // Crop to fit circle efficiently
                .dontAnimate()  // Skip fade-in for snappier lists
                .priority(Priority.HIGH)  // Prioritize over other loads
                .into(binding.imgStudentProfile)
        } else {
            Glide.with(this)
                .load(defaultProfileRes)
                .centerCrop()  // Crop to fit circle efficiently
                .dontAnimate()  // Skip fade-in for snappier lists
                .priority(Priority.HIGH)  // Prioritize over other loads
                .into(binding.imgStudentProfile)
        }
        binding.lblName.text = name
    }

    private fun setupStudentDetails() {

        val profileUrl: String = childDetails!!.profile
        val name: String = childDetails!!.name
        val school_name: String = childDetails!!.school_name
        childDetails!!.school_city
        childDetails!!.school_logo_url
        val standard: String = childDetails!!.standard_name
        val section: String = childDetails!!.section_name
        val roll_no: String = childDetails!!.roll_number
        childDetails!!.student_address
        childDetails!!.secondary_mobile
        childDetails!!.whatsapp_number
        childDetails!!.email
        childDetails!!.blood_group
        childDetails!!.father_name
        childDetails!!.father_occupation
        childDetails!!.mother_name
        childDetails!!.mother_occupation

        val defaultProfileRes = R.drawable.default_profile
        if (!profileUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(profileUrl)
                .placeholder(defaultProfileRes) // While loading
                .error(defaultProfileRes)       // If failed to load
                .into(binding.imgStudentProfile)
        } else {
            Glide.with(this)
                .load(defaultProfileRes)
                .into(binding.imgStudentProfile)
        }

        binding.lblName.text = name
        binding.lblSchoolName.text = school_name
        binding.lblRegisterNo.text = roll_no
        binding.lblStandard.text = "${getString(R.string.Standard)} : " + standard
        binding.lblSection.text = "${getString(R.string.Section)} : " + section
    }

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }
}