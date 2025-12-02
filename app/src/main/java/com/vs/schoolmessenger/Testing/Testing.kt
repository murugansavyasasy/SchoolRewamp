package com.vs.schoolmessenger.Testing

import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.CalendarAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.CalendarDate
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.HomeWorkParentData
import com.vs.schoolmessenger.databinding.ParentHomeworkActivityBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Testing : BaseActivity<ParentHomeworkActivityBinding>(), View.OnClickListener {

    override fun getViewBinding(): ParentHomeworkActivityBinding {
        return ParentHomeworkActivityBinding.inflate(layoutInflater)
    }


    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()

    }

    override fun onClick(v: View?) {

    }
}

