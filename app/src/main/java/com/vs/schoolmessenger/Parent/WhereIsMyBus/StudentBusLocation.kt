package com.vs.schoolmessenger.Parent.WhereIsMyBus

import android.view.View
import com.google.android.gms.maps.model.LatLng
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Parent.FeeDetails.FeeReceiptAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.StudentBustLocationBinding
import io.socket.client.IO
import org.json.JSONObject

class StudentBusLocation : BaseActivity<StudentBustLocationBinding>(), View.OnClickListener{

    override fun getViewBinding(): StudentBustLocationBinding {
        return StudentBustLocationBinding.inflate(layoutInflater)
    }

    lateinit var mAdapter: FeeReceiptAdapter
    override fun setupViews() {
        super.setupViews()
        setUpGradientParent()
        binding.imgBack.setOnClickListener(this)

        val socket = IO.socket("http://YOUR_SERVER_IP:3000")
        socket.connect()
        socket.on("busLocation_bus_101") { args ->
            val data = args[0] as JSONObject
            val lat = data.getDouble("lat")
            val lng = data.getDouble("lng")

            runOnUiThread {
                val latLng = LatLng(lat, lng)
            }
        }
    }
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> onBackPressed()
        }
    }
}