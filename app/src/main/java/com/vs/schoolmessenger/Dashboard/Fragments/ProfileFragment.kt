package com.vs.schoolmessenger.Dashboard.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
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
            binding.rytHeader.setBackgroundResource(com.vs.schoolmessenger.R.drawable.gradient_theme_parent)
            setupStudentDetails()

        }
        else {
            binding.rytEdit.visibility = View.GONE
            binding.lblSchoolName.visibility = View.GONE
            binding.lblRegisterNo.visibility = View.GONE
            binding.lnrStandardDetails.visibility = View.GONE
            binding.rytHeader.setBackgroundResource(com.vs.schoolmessenger.R.drawable.gradient_theme_school)
            setupStaffDetails()

        }
        return binding.root
    }

    private fun setupStaffDetails() {
        val profileUrl: String? = staffDetails!!.staff_profile
        val name: String? = staffDetails!!.name
        val school_name: String? = staffDetails!!.school_name
        val school_city: String? = staffDetails!!.school_address
        val school_logo: String? = staffDetails!!.school_logo
        val contact_no: String? = staffDetails!!.mobile_no
        val email: String? = staffDetails!!.email
        val staff_address: String? = staffDetails!!.address
        val blood_group: String? = staffDetails!!.blood_group

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
        binding.lblName.setText(name)
    }

    private fun setupStudentDetails() {
        val profileUrl: String? = childDetails!!.profile
        val name: String? = childDetails!!.name
        val school_name: String? = childDetails!!.school_name
        val school_city: String? = childDetails!!.school_city
        val school_logo: String? = childDetails!!.school_logo_url
        val standard: String? = childDetails!!.standard_name
        val section: String? = childDetails!!.section_name
        val roll_no: String? = childDetails!!.roll_number
        val student_address: String? = childDetails!!.student_address
        val contact_no: String? = childDetails!!.secondary_mobile
        val whatsapp_no: String? = childDetails!!.whatsapp_number
        val email: String? = childDetails!!.email
        val blood_group: String? = childDetails!!.blood_group
        val father_name: String? = childDetails!!.father_name
        val father_occupation: String? = childDetails!!.father_occupation
        val mother_name: String? = childDetails!!.mother_name
        val mother_occupation: String? = childDetails!!.mother_occupation

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

        binding.lblName.setText(name)
        binding.lblSchoolName.setText(school_name)
        binding.lblRegisterNo.setText(roll_no)
        binding.lblStandard.setText("Standard : "+standard)
        binding.lblSection.setText("Section : "+section)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }
}