package com.vs.schoolmessenger.School.NoticeBoard
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.CreateNoticeBoardBinding

class CreateNoticeBoard : BaseActivity<CreateNoticeBoardBinding>(),NoticeClickListener,
    View.OnClickListener {

    lateinit var mAdapter: SchoolNoticeBoardAdapter
    private lateinit var noticeBoardList: List<NoticeData>

    override fun getViewBinding(): CreateNoticeBoardBinding {
        return CreateNoticeBoardBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbar()
        binding.imgBack.setOnClickListener(this)
        binding.btnCreate.setOnClickListener(this)
        binding.btnHistory.setOnClickListener(this)

        Glide.with(this)
            .load("https://s3.ap-south-1.amazonaws.com/schoolchimes-files-india/27-11-2024/File_vc_-5346401391795845263.png")
            .into(binding.imgPick1)

        Glide.with(this)
            .load("https://s3.ap-south-1.amazonaws.com/schoolchimes-files-india/27-11-2024/File_vc_-5346401391801142838.png")
            .into(binding.imgPick2)

        Constant.editTextCounter(this,binding.txtDesc,500,binding.lbTextCount)

    }


    override fun onResume() {
        super.onResume()
    }


    override fun onPause() {
        super.onPause()
        Constant.stopDelay()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
            R.id.btnCreate -> {
                isBackRoundChange(binding.btnCreate)
                binding.rytHistory.visibility = View.GONE
                binding.noticeboardCreate.visibility = View.VISIBLE

            }
            R.id.btnHistory -> {
                isBackRoundChange(binding.btnHistory)
                binding.rytHistory.visibility = View.VISIBLE
                binding.noticeboardCreate.visibility = View.GONE
                loadData()
            }
        }
    }

    private fun loadData() {
        noticeBoardList = listOf(
            NoticeData(
                "Annual Day celebrartions", "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024"
            ),
            NoticeData(
                "Annual Day celebrartions", "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024"
            ),
            NoticeData(
                "Annual Day celebrartions", "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024"
            ),
            NoticeData(
                "Annual Day celebrartions", "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024"
            ),
            NoticeData(
                "Annual Day celebrartions", "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024"
            ),
            NoticeData(
                "Annual Day celebrartions", "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024"
            ),
            NoticeData(
                "Annual Day celebrartions", "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024"
            ),
            NoticeData(
                "Annual Day celebrartions", "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024"
            ),
            NoticeData(
                "Annual Day celebrartions", "If you're working in a collaborative environment, stashing and pulling is often the safest option, as it allows you to integrate your work with the latest changes without losing progress.",
                "15 Nov 2024"
            )
        )

        mAdapter = SchoolNoticeBoardAdapter(null, this, this, Constant.isShimmerViewShow)
        binding.recycleStudents.layoutManager = LinearLayoutManager(this)
        binding.recycleStudents.adapter = mAdapter

        Constant.executeAfterDelay {
            // Once data is loaded, stop shimmer and pass the actual data
            mAdapter =
                SchoolNoticeBoardAdapter(noticeBoardList, this, this, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.recycleStudents.adapter = mAdapter
        }

    }

    private fun isBackRoundChange(isClickingId: TextView) {

        if (isClickingId == binding.btnCreate) {
            binding.btnHistory.background = null
            binding.btnHistory.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))
        }

        if (isClickingId == binding.btnHistory) {
            binding.btnCreate.background = null
            binding.btnCreate.setTextColor(ContextCompat.getColor(this, R.color.dark_blue))

        }

        isClickingId.background = ContextCompat.getDrawable(this, R.drawable.bg_blue)
        isClickingId.setTextColor(ContextCompat.getColor(this, R.color.white))

    }

    override fun onItemClick(data: NoticeData) {
//        startActivity(Intent(this, ImageViewActivity::class.java))

    }
}