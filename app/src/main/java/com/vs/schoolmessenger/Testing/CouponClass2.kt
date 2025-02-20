package com.vs.schoolmessenger.Testing

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.databinding.DemoCoupon2Binding
import com.vs.schoolmessenger.databinding.DemoCouponBinding



class CouponClass2 : AppCompatActivity() {

//    private lateinit var binding: DemoCoupon2Binding
//    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = DemoCoupon2Binding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Initialize Bottom Sheet Behavior
//        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
//
//        // Set the initial state to half the screen height
//        bottomSheetBehavior.peekHeight = resources.displayMetrics.heightPixels / 2
//
//        // Allow dragging to full height with some gap at the top
//        bottomSheetBehavior.expandedOffset = 100  // 100dp gap at the top
//
//        // Enable full and half-height behavior
//        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
//
//        // Optional: Listen for Bottom Sheet State Changes
//        bottomSheetBehavior.addBottomSheetCallback(object :
//            BottomSheetBehavior.BottomSheetCallback() {
//            override fun onStateChanged(bottomSheet: View, newState: Int) {
//                when (newState) {
//                    BottomSheetBehavior.STATE_EXPANDED -> {
//                        binding.titleText.text = "Expanded Mode"
//                    }
//                    BottomSheetBehavior.STATE_COLLAPSED -> {
//                        binding.titleText.text = "Swipe up for more details"
//                    }
//                    BottomSheetBehavior.STATE_DRAGGING -> {
//                        // Can handle UI changes while dragging if needed
//                    }
//                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
//                        binding.titleText.text = "Half Expanded Mode"
//                    }
//                }
//            }
//
//            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                // Optional: Handle slide offset changes if needed
//            }
//        })
//    }
}
