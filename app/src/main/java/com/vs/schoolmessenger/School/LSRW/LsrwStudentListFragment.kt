package com.vs.schoolmessenger.School.LSRW

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.LsrwStudentlistFragmentBinding

class LsrwStudentListFragment : Fragment() {

    private var _binding: LsrwStudentlistFragmentBinding? = null
    private val binding get() = _binding!!

    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null

    private lateinit var submittedStudentlistAdapter: SubmittedStudentlistAdapter
    private var id: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getString(Constant.id_)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LsrwStudentlistFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appViewModel = ViewModelProvider(this)[App::class.java].apply { init() }
        isStaffDetails = SharedPreference.getStaffDetails(requireContext())
        isAccessToken = isStaffDetails?.access_token

        setupAdapter()

        appViewModel?.islsrwStudentlist?.observe(viewLifecycleOwner) { response ->
            if (response?.status == true && !response.data.isNullOrEmpty()) {
                binding.rcystudentlist.visibility = View.VISIBLE
                binding.nomessage.visibility = View.GONE
                binding.txtNoData.visibility = View.GONE
                submittedStudentlistAdapter.updateList(response.data)
            } else {
                binding.rcystudentlist.visibility = View.GONE
                binding.nomessage.visibility = View.VISIBLE
                binding.txtNoData.visibility = View.VISIBLE
                binding.txtNoData.text = response?.message ?: getString(R.string.no_data_found)
            }
        }

        Log.d("FragmentCheck", "Access Token: $isAccessToken")
        Log.d("FragmentCheck", "ID: $id")
        if (!isAccessToken.isNullOrEmpty() && !id.isNullOrEmpty()) {
            Log.d("FragmentCheck", "Calling API now...")
            appViewModel?.islsrwStudentlist(isAccessToken!!, id!!)
        } else {
            Log.e("FragmentCheck", "API not called. AccessToken or ID missing")
        }

    }

    private fun setupAdapter() {
        submittedStudentlistAdapter = SubmittedStudentlistAdapter(emptyList(), requireContext())
        binding.rcystudentlist.layoutManager = LinearLayoutManager(requireContext())
        binding.rcystudentlist.isNestedScrollingEnabled = false
        binding.rcystudentlist.adapter = submittedStudentlistAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(id: String) = LsrwStudentListFragment().apply {
            arguments = Bundle().apply { putString(Constant.id_, id) }
        }
    }
}
