package com.vs.schoolmessenger.Parent.QuizExam


import android.graphics.Color
import com.vs.schoolmessenger.databinding.QuizBinding


import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.ChildDetails
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificateListener
import com.vs.schoolmessenger.Parent.CertificateRequest.CertificateRequestAdapter
import com.vs.schoolmessenger.Parent.LSRW.LSRWAdapter
import com.vs.schoolmessenger.Parent.LSRW.LSRWClickListener
import com.vs.schoolmessenger.Parent.LSRW.LSRWData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.CertificateRequestParentBinding
import com.vs.schoolmessenger.databinding.LsrwBinding
import com.vs.schoolmessenger.databinding.QuizExamBinding

class Quiz : BaseActivity<QuizBinding>(), View.OnClickListener {

    private lateinit var adapter: QuizUpcomingAdapter
    private val quizupcominglist = mutableListOf<QuizUpcomingData>()
    private lateinit var adapter1: QuizCompletedAdapter
    private val quizcompletedlist = mutableListOf<QuizCompletedData>()


    private var appViewModel: App? = null
    private var isAccessToken: String? = null
    private var isChildDetails: ChildDetails? = null


    override fun getViewBinding(): QuizBinding {
        return QuizBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryTheme()

        // Toolbar setup
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        binding.toolbarLayout.lblLeftSideBar.setOnClickListener(this)
        binding.toolbarLayout.lblRightSideBar.setOnClickListener(this)
        binding.toolbarLayout.lblParentToolBar.text = Constant.isParentMenuName
        binding.toolbarLayout.rytSearch.visibility = View.GONE
        binding.toolbarLayout.lnrParent.visibility = View.VISIBLE
        binding.toolbarLayout.lblLeftSideBar.text = "Completed"
        binding.toolbarLayout.lblRightSideBar.text = "Upcomming"
        isChildDetails = SharedPreference.getChildDetails(this)
        binding.toolbarLayout.lblStudentName.text = isChildDetails?.name ?: ""
        binding.toolbarLayout.lblStudentSection.text =
            isChildDetails?.standard_name + " - " + isChildDetails?.section_name

        isUpcoming()
        setupRecyclerView()
        loadHardcodedData()

        setupRecyclerView1()
        loadHardcodedData1()

    }


    private fun setupRecyclerView() {
        adapter = QuizUpcomingAdapter(quizupcominglist, object : QuizUpcomingListener {
            override fun onItemClick(
                data: QuizUpcomingData,
                holder: QuizUpcomingAdapter.DataViewHolder
            ) {
                // Handle item click
            }
        }, this, false)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun loadHardcodedData() {
        quizupcominglist.apply {
            add(
                QuizUpcomingData(
                    "Online Quiz",
                    "Play Quiz games to improve the learning in a funnier way it will reduce the stress and improve the brain activities ",
                    "Subject : Tamil",
                    "15 Questions"
                )
            )
            add(
                QuizUpcomingData(
                    "Online Quiz",
                    "Play Quiz games to improve the learning in a funnier way it will reduce the stress and improve the brain activities ",
                    "Subject : Tamil",
                    "15 Questions"
                )
            )
            add(
                QuizUpcomingData(
                    "Online Quiz",
                    "Play Quiz games to improve the learning in a funnier way it will reduce the stress and improve the brain activities ",
                    "Subject : Tamil",
                    "15 Questions"
                )
            )
            add(
                QuizUpcomingData(
                    "Online Quiz",
                    "Play Quiz games to improve the learning in a funnier way it will reduce the stress and improve the brain activities ",
                    "Subject : Tamil",
                    "15 Questions"
                )
            )
            add(
                QuizUpcomingData(
                    "Online Quiz",
                    "Play Quiz games to improve the learning in a funnier way it will reduce the stress and improve the brain activities ",
                    "Subject : Tamil",
                    "15 Questions"
                )
            )
        }
        adapter.notifyDataSetChanged()
    }


    private fun setupRecyclerView1() {
        adapter1 = QuizCompletedAdapter(quizcompletedlist, object : QuizCompletedListener {
            override fun onItemClick(
                data: QuizCompletedData,
                holder: QuizCompletedAdapter.DataViewHolder
            ) {
                // Handle item click
            }
        }, this, false)

        binding.recyclerView1.layoutManager = LinearLayoutManager(this)
        binding.recyclerView1.adapter = adapter1
    }

    private fun loadHardcodedData1() {
        quizcompletedlist.apply {
            add(
                QuizCompletedData(
                    "What is the capital of Germany",
                    "Berlin",
                    "Munich",
                    "Frankurt",
                    "Hamburg"
                )
            )
            add(
                QuizCompletedData(
                    "What is the capital of Germany",
                    "Berlin ",
                    "Munich",
                    "Frankurt",
                    "Hamburg"
                )
            )
            add(
                QuizCompletedData(
                    "What is the capital of Germany",
                    "Berlin",
                    "Munich",
                    "1Frankurt",
                    "Hamburg"
                )
            )
            add(
                QuizCompletedData(
                    "What is the capital of Germany",
                    "Berlin ",
                    "Munich",
                    "Frankurt",
                    "Hamburg"
                )
            )
            add(
                QuizCompletedData(
                    "What is the capital of Germany",
                    "Berlin",
                    "Munich",
                    "Frankurt",
                    "Hamburg"
                )
            )
        }
        adapter1.notifyDataSetChanged()
    }


    override fun onClick(v: View?) {
        if (v == null) return
        when (v.id) {
            R.id.lblLeftSideBar ->{

                binding.toolbarLayout.lblRightSideBar.setBackgroundResource(R.drawable.bg_light_green)
                binding.toolbarLayout.lblRightSideBar.setTextColor(Color.BLACK)
                binding.toolbarLayout.lblLeftSideBar.setBackgroundResource(R.drawable.white_radious)
                binding.recyclerView.visibility = View.VISIBLE
                isCompleted()
            }
            R.id.lblRightSideBar ->{
                binding.toolbarLayout.lblRightSideBar.setBackgroundResource(R.drawable.white_radious)
                binding.toolbarLayout.lblRightSideBar.setTextColor(Color.BLACK)
                binding.toolbarLayout.lblLeftSideBar.setBackgroundResource(R.drawable.bg_light_green)
                binding.recyclerView.visibility = View.VISIBLE
                isUpcoming()
            }

            R.id.imgBack -> onBackPressed()

        }
    }

    fun isUpcoming() {

        binding.correctanswers.visibility = View.GONE
        binding.incorrectanswers.visibility = View.GONE
        binding.recyclerView1.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    fun isCompleted() {

        binding.correctanswers.visibility = View.VISIBLE
        binding.incorrectanswers.visibility = View.VISIBLE
        binding.recyclerView1.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE


    }
}