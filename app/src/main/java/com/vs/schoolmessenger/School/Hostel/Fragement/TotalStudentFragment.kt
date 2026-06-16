package com.vs.schoolmessenger.School.Hostel.Fragement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Hostel.BottomSheet
import com.vs.schoolmessenger.databinding.TotalStudentBinding


class TotalStudentFragment : Fragment(), View.OnClickListener {

    private var _binding: TotalStudentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = TotalStudentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.imgClose -> {
                (parentFragment as? BottomSheet)?.closeSheet()
            }
        }
    }
}