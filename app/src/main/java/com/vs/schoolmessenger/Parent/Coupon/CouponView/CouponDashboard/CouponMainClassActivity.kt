package com.vs.schoolmessenger.Parent.Coupon.CouponView.CouponDashboard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.Coupon.CouponController.CategoryController
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummary.Summary
import com.vs.schoolmessenger.Parent.Coupon.CouponView.Adapter.CouponMenuAdapter
import com.vs.schoolmessenger.Parent.Coupon.CouponView.Adapter.CouponSummaryAdapter
import com.vs.schoolmessenger.R
import kotlin.Int
import kotlin.collections.ArrayList
import kotlin.collections.MutableList


class CouponMainClassActivity : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null
    private var recyclerView1: RecyclerView? = null
    private var adapter1: CouponSummaryAdapter? = null
    private var btnHome: ImageView? = null
    private var editSearch: EditText? = null
    private var btnTicket: ImageView? = null
    private var adapter: CouponMenuAdapter? = null
    private var categoryController: CategoryController? = null
    private var homeBackground: RelativeLayout? = null
    private var ticketBackground: RelativeLayout? = null
    private var relative_layout: RelativeLayout? = null
    private var totalcoins: TextView? = null
    private var usedcoins: TextView? = null
    private var availablecoins: TextView? = null
    private var pointsRemaining = 0
    private var pointPerCoupon = 0

    var isProgressBar: ProgressBar? = null
    var lblNoRecord: TextView? = null
    var text_view: TextView? = null
    var lnrPoints: LinearLayout? = null
    var blueColor: Int = 0
    var defaultColor: Int = 0
    private var originalSummaryList: MutableList<Summary> = ArrayList<Summary>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.coupon_dashboard)
        recyclerView = findViewById<RecyclerView?>(R.id.recyclerview1)
        recyclerView1 = findViewById<RecyclerView?>(R.id.recyclerView)
        btnHome = findViewById<ImageView?>(R.id.btnHome)
        btnTicket = findViewById<ImageView?>(R.id.btnTicket)
        totalcoins = findViewById<TextView?>(R.id.totalcoins)
        usedcoins = findViewById<TextView?>(R.id.usedcoins)
        availablecoins = findViewById<TextView?>(R.id.availablecoins)
        homeBackground = findViewById<RelativeLayout?>(R.id.homeBackground)
        ticketBackground = findViewById<RelativeLayout?>(R.id.ticketBackground)
        relative_layout = findViewById<RelativeLayout?>(R.id.relative_layout)
        editSearch = findViewById<EditText?>(R.id.editSearch)
        isProgressBar = findViewById<ProgressBar?>(R.id.isProgressBar)
        lblNoRecord = findViewById<TextView?>(R.id.lblNoRecord)
        text_view = findViewById<TextView?>(R.id.text_view)
        lnrPoints = findViewById<LinearLayout?>(R.id.lnrPoints)

        blueColor = ContextCompat.getColor(this, R.color.gnt_blue)
        defaultColor = ContextCompat.getColor(this, R.color.gnt_gray)

        btnHome!!.setImageDrawable(getDrawable(R.drawable.homeimage))
        btnTicket!!.setImageDrawable(getDrawable(R.drawable.ticketimage))

        homeBackground!!.setBackgroundResource(R.drawable.bg_selected)
        ticketBackground!!.setBackgroundColor(Color.TRANSPARENT)

        btnHome!!.setOnClickListener(View.OnClickListener { view: View? ->
            homeBackground!!.setBackgroundResource(R.drawable.bg_selected)
            ticketBackground!!.setBackgroundColor(Color.TRANSPARENT)

            btnHome!!.setImageDrawable(getDrawable(R.drawable.homeimage))
            btnTicket!!.setImageDrawable(getDrawable(R.drawable.ticketimage))
            val intent = Intent(this@CouponMainClassActivity, CouponMainClassActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        })

        btnTicket!!.setOnClickListener(View.OnClickListener { view: View? ->
//            ticketBackground!!.setBackgroundResource(R.drawable.bg_selected)
//            homeBackground!!.setBackgroundColor(Color.TRANSPARENT)
//
//            btnHome!!.setImageDrawable(getDrawable(R.drawable.home_gray))
//            btnTicket!!.setImageDrawable(getDrawable(R.drawable.ticket_blue))
//            val intent: Intent =
//                Intent(this@CouponMainClassActivity, TicketCouponViewActivity::class.java)
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//            startActivity(intent)
        })

        relative_layout!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                onBackPressed()
            }
        })

        recyclerView!!.setLayoutManager(
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        )
        recyclerView1!!.setLayoutManager(GridLayoutManager(this, 2))


//        editSearch!!.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                filter(s.toString())
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//            }
//        })
    }

//    private fun loadInitialData() {
////        categoryController = CategoryController(this)
////
////        categoryController.fetchCoinDetails(object : PointsCouponCallback() {
////            public override fun onSuccess(pointsData: PointsData?) {
////                if (pointsData != null) {
////                    lnrPoints!!.setVisibility(View.VISIBLE)
////                    totalcoins!!.setText("" + pointsData.getPointsEarned())
////                    usedcoins!!.setText("Used: " + pointsData.getPointsSpent())
////                    availablecoins!!.setText("Available: " + pointsData.getPointsRemaining())
////
////                    pointsRemaining = pointsData.getPointsRemaining()
////                    pointPerCoupon = pointsData.getPointsPerCoupon()
////                } else {
////                    lnrPoints!!.setVisibility(View.GONE)
////                    Toast.makeText(
////                        this@CouponMainClassActivity,
////                        "No point data available",
////                        Toast.LENGTH_SHORT
////                    ).show()
////                }
////            }
////
////
////            public override fun onFailure(errorMessage: String?) {
////                lnrPoints!!.setVisibility(View.GONE)
////                Toast.makeText(this@CouponMainClassActivity, errorMessage, Toast.LENGTH_SHORT)
////                    .show()
////            }
//        })

//        val categoryClickListener: CouponMenuAdapter.OnCategoryClickListener =
//            CouponMenuAdapter.OnCategoryClickListener { category ->
//                if (category.getId() === -1) {
//                    text_view!!.setText("All Coupons")
//                    isProgressBar!!.setVisibility(View.VISIBLE)
//                    categoryController.fetchCouponSummary(object : CouponSummaryCallback() {
//                        public override fun onSuccess(campaigns: MutableList<Summary?>) {
//                            originalSummaryList = ArrayList<Summary>(campaigns)
//                            isProgressBar!!.setVisibility(View.GONE)
//                            if (campaigns.isEmpty()) {
//                                lblNoRecord!!.setVisibility(View.VISIBLE)
//                                recyclerView1!!.setVisibility(View.GONE)
//                            } else {
//                                lblNoRecord!!.setVisibility(View.GONE)
//                                recyclerView1!!.setVisibility(View.VISIBLE)
//                            }
//
//                            adapter1 = CouponSummaryAdapter(
//                                this@CouponMainClassActivity,
//                                campaigns,
//                                pointsRemaining,
//                                pointPerCoupon
//                            )
//                            recyclerView1!!.setAdapter(adapter1)
//                        }
//
//                        public override fun onFailure(errorMessage: String?) {
//                            Toast.makeText(
//                                this@CouponMainClassActivity,
//                                errorMessage,
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    })
//                } else {
//                    isProgressBar!!.setVisibility(View.VISIBLE)
//                    text_view.setText(category.getCategoryName() + " " + "Coupons")
//                    val categoryId: String? = String.valueOf(category.getId())
//                    categoryController.fetchCategoryCouponSummary(
//                        categoryId,
//                        object : CategoryCouponSummaryCallback() {
//                            public override fun onSuccess(campaigns: MutableList<Summary?>) {
//                                originalSummaryList = ArrayList<Summary>(campaigns)
//                                isProgressBar!!.setVisibility(View.GONE)
//                                if (campaigns.isEmpty()) {
//                                    lblNoRecord!!.setVisibility(View.VISIBLE)
//                                    recyclerView1!!.setVisibility(View.GONE)
//                                } else {
//                                    lblNoRecord!!.setVisibility(View.GONE)
//                                    recyclerView1!!.setVisibility(View.VISIBLE)
//                                }
//                                adapter1 = CouponSummaryAdapter(
//                                    this@CouponMainClassActivity,
//                                    campaigns,
//                                    pointsRemaining,
//                                    pointPerCoupon
//                                )
//                                recyclerView1!!.setAdapter(adapter1)
//                            }
//
//                            public override fun onFailure(errorMessage: kotlin.String?) {
//                                Toast.makeText(
//                                    this@CouponMainClassActivity,
//                                    errorMessage,
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        })
//                }
//            }
//        isProgressBar!!.setVisibility(View.VISIBLE)
//        categoryController.fetchCategories(object : CategoryCallback() {
//            public override fun onSuccess(categories: MutableList<Category?>) {
//                val hardcodedCategory: Category = Category()
//                hardcodedCategory.setId(-1)
//                hardcodedCategory.setCategoryName("All")
//                text_view!!.setText("All Coupons")
//                isProgressBar!!.setVisibility(View.GONE)
//                hardcodedCategory.setDrawableResId(R.drawable.allimage)
//                categories.add(0, hardcodedCategory)
//
//                adapter =
//                    CouponMenuAdapter(this@CouponMainClassActivity, categories, 0, { category ->
//                        if (category.getId() === -1) {
//                            text_view!!.setText("All Coupons")
//                            categoryController.fetchCouponSummary(object : CouponSummaryCallback() {
//                                public override fun onSuccess(campaigns: MutableList<Summary?>) {
//                                    originalSummaryList = ArrayList<Summary>(campaigns)
//                                    if (campaigns.isEmpty()) {
//                                        lblNoRecord!!.setVisibility(View.VISIBLE)
//                                        recyclerView1!!.setVisibility(View.GONE)
//                                    } else {
//                                        lblNoRecord!!.setVisibility(View.GONE)
//                                        recyclerView1!!.setVisibility(View.VISIBLE)
//                                    }
//                                    adapter1 = CouponSummaryAdapter(
//                                        this@CouponMainClassActivity,
//                                        campaigns,
//                                        pointsRemaining,
//                                        pointPerCoupon
//                                    )
//                                    recyclerView1!!.setAdapter(adapter1)
//                                }
//
//                                public override fun onFailure(errorMessage: kotlin.String?) {
//                                    Toast.makeText(
//                                        this@CouponMainClassActivity,
//                                        errorMessage,
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                }
//                            })
//                        } else {
//                            text_view.setText(category.getCategoryName() + " " + "Coupons")
//                            isProgressBar!!.setVisibility(View.VISIBLE)
//                            val categoryId: kotlin.String? = String.valueOf(category.getId())
//                            categoryController.fetchCategoryCouponSummary(
//                                categoryId,
//                                object : CategoryCouponSummaryCallback() {
//                                    public override fun onSuccess(campaigns: MutableList<Summary?>) {
//                                        isProgressBar!!.setVisibility(View.GONE)
//                                        if (campaigns.isEmpty()) {
//                                            lblNoRecord!!.setVisibility(View.VISIBLE)
//                                            recyclerView1!!.setVisibility(View.GONE)
//                                        } else {
//                                            lblNoRecord!!.setVisibility(View.GONE)
//                                            recyclerView1!!.setVisibility(View.VISIBLE)
//                                        }
//                                        originalSummaryList = ArrayList<Summary>(campaigns)
//
//                                        adapter1 = CouponSummaryAdapter(
//                                            this@CouponMainClassActivity,
//                                            campaigns,
//                                            pointsRemaining,
//                                            pointPerCoupon
//                                        )
//                                        recyclerView1!!.setAdapter(adapter1)
//                                    }
//
//                                    public override fun onFailure(errorMessage: kotlin.String?) {
//                                        Toast.makeText(
//                                            this@CouponMainClassActivity,
//                                            errorMessage,
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    }
//                                })
//                        }
//                    })
//
//                recyclerView!!.setAdapter(adapter)
//                if (!categories.isEmpty()) {
//                    categoryClickListener.onCategoryClick(categories.get(0))
//                }
//            }
//
//            public override fun onFailure(errorMessage: kotlin.String?) {
//                Toast.makeText(this@CouponMainClassActivity, errorMessage, Toast.LENGTH_SHORT)
//                    .show()
//            }
//        })
//    }

//    private fun filter(text: kotlin.String) {
//        val filteredList: MutableList<Summary?> = ArrayList<Summary?>()
//        if (text.isEmpty()) {
//            filteredList.addAll(originalSummaryList)
//        } else {
//            val searchText = text.lowercase(Locale.getDefault()).trim { it <= ' ' }
//            for (item in originalSummaryList) {
//                if (item.getMerchantName().toLowerCase()
//                        .contains(searchText) || item.getCategoryName().toLowerCase()
//                        .contains(searchText)
//                ) {
//                    filteredList.add(item)
//                }
//            }
//        }
//
//        if (adapter1 != null) {
//            adapter1.updateList(filteredList)
//        }
//    }

    override fun onResume() {
        super.onResume()
//        loadInitialData()
    }
}
