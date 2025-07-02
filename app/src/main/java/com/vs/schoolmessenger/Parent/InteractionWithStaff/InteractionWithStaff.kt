package com.vs.schoolmessenger.Parent.InteractionWithStaff

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Utils.OnDateSelectedListener
import com.vs.schoolmessenger.databinding.AssignmentParentBinding
import com.vs.schoolmessenger.databinding.IntectionWithStaffBinding

class InteractionWithStaff : BaseActivity<IntectionWithStaffBinding>() {

    override fun getViewBinding(): IntectionWithStaffBinding {
        return IntectionWithStaffBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.Btn.setOnClickListener {
            showCustomDatePickerDialog(supportFragmentManager, object : OnDateSelectedListener {
                override fun onDateSelected(date: String) {
                    binding.SelDate.text = date
// Handle selected date (e.g., update UI)
                    Toast.makeText(this@InteractionWithStaff, "Selected: $date", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        }
    }


}