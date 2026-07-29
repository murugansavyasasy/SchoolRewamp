package com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableViewDetails

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.UserDetails
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkListener
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTable.ExamSubjectAdapter
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewampModel.ExamTimetable
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewampModel.ExamTimetableActivity
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewampModel.ExamTimetableRubric
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewampModel.ExamTimetableSubject
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableViewDetails.Adapter.ExamTimeTableActivityWise
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.ExamTimeTableViewDetailsBinding

class ExamTimeTableViewDetails : BaseActivity<ExamTimeTableViewDetailsBinding>(), View.OnClickListener,
    ExamMarkListener{
    override fun getViewBinding(): ExamTimeTableViewDetailsBinding {
        return ExamTimeTableViewDetailsBinding.inflate(layoutInflater)
    }

    private var isAccessToken: String? = null
    lateinit var mAdapter: ExamSubjectAdapter

    private var appViewModel: App? = null

    private var msg_id: Int = -1
    private var headerId: String? = null
    private var receiverId: String? = null
    private var menu_name: String? = null
    private var fromNotification: Boolean = false
    var userDetails: UserDetails? = null

    private var examTimetable: ArrayList<ExamTimetable> = arrayListOf()
    private var activityData: ArrayList<ExamTimetableActivity> = arrayListOf()


    override fun setupViews() {
        super.setupViews()
        isToolBarPrimaryParent(
            mainViewId = R.id.main,
            statusBarBgView = binding.statusBarBackground
        )

        appViewModel = ViewModelProvider(this).get(App::class.java)
        appViewModel?.init()

        userDetails = SharedPreference.getUserDetails(this)
        fromNotification = intent.getBooleanExtra(Constant.fromNotification, false)



        examTimetable =
            intent.getParcelableArrayListExtra<ExamTimetable>("reciever_exam_data")
                ?: arrayListOf()



        Log.d("SelectedExamTimetable",examTimetable.toString())
        Log.d("SelectedSubjectList",activityData.toString())
        Log.d("SelectedActivity",activityData.toString())


        if (fromNotification) {
            Constant.isParentChoose = true
            msg_id = intent.getIntExtra(Constant.msg_id, -1)
            headerId = intent.getStringExtra(Constant.header_id)
            receiverId = intent.getStringExtra(Constant.receiverid)
            menu_name = intent.getStringExtra(Constant.menu_name)

            Log.d(
                "NoticeBoard_EXTRAS",
                "Raw extras - headerId: $headerId, receiverId: $receiverId, menu_name: $menu_name"
            )

            val matchedChild = userDetails?.child_details?.find { it.child_id == receiverId }
            SharedPreference.putChildDetails(this, matchedChild!!)
            Constant.isSelectedMenuName = menu_name!!
        }

        val isChildDetails = SharedPreference.getChildDetails(this)
        isAccessToken = isChildDetails?.access_token


        binding.toolbarLayout.imgSearchToolBar.visibility= View.GONE


//        binding.root.post {
//            val finalName =
//                Constant.isSelectedMenuName?.takeIf { it.isNotEmpty() } ?: menu_name ?: ""
//            Log.d("NoticeBoard_HeaderFinal", "Setting headerview text: $finalName")
//
//            binding.lblHeaderTitle.text = if (activityData.size == 1) {
//                getString(R.string.activity)+" ("+activityData.size+") "
//            } else {
//                getString(R.string.activity_2)+" ("+activityData.size+") "
//            }
//        }


        binding.toolbarLayout.apply {
            imgBack.setOnClickListener(this@ExamTimeTableViewDetails)
            lblStudentName.text = isChildDetails!!.name
            lblStudentSection.text =
                "${isChildDetails.standard_name} - ${isChildDetails.section_name}"
        }

        isLoadExamTimeTableActivity()


    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }


    private fun showScheduleBottomSheet(rubric: ExamTimetableRubric) {

        val details = rubric.schedulingDetails ?: return

        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_exam_rubric, null)

        dialog.setContentView(view)

        val txtTitle = view.findViewById<TextView>(R.id.txtTitle)
        val lblDate = view.findViewById<TextView>(R.id.lblDate)
        val lblTime = view.findViewById<TextView>(R.id.lblTime)
        val lblSession = view.findViewById<TextView>(R.id.lblSession)
        val lblVenue = view.findViewById<TextView>(R.id.lblVenue)
        val lblSyllabus = view.findViewById<TextView>(R.id.lblSyllabus)
        val lblTotalMarks = view.findViewById<TextView>(R.id.lblTotalMarks)
        val lblPassMarks = view.findViewById<TextView>(R.id.lblPassMarks)
        val imgClose = view.findViewById<ImageView>(R.id.imgClose)

        txtTitle.text = rubric.rubricName ?: ""

        lblDate.text =
            if (details.date.isNullOrEmpty()) "-" else Constant.formatDate33(details.date)

        lblTime.text =
            "${details.startTime ?: "-"} - ${details.endTime ?: "-"}"

        lblSession.text =
            if (details.session.isNullOrEmpty()) "-" else details.session

        lblVenue.text =
            if (details.venue.isNullOrEmpty()) "-" else details.venue

        lblSyllabus.text =
            if (details.syllabus.isNullOrEmpty()) "-" else details.syllabus
        lblTotalMarks.text =rubric.max_mark

        lblPassMarks.text =rubric.pass_mark



        imgClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun isLoadExamTimeTableActivity() {

        val data = examTimetable.getOrNull(0)?.subjects ?: emptyList()
        Log.d("data", data.toString())

        if (data.isEmpty()) {
            binding.lytList.visibility = View.VISIBLE
            binding.rcExamTimeTableAct.visibility = View.GONE
        } else {
            binding.lytList.visibility = View.GONE
            binding.rcExamTimeTableAct.visibility = View.VISIBLE

            binding.rcExamTimeTableAct.layoutManager = LinearLayoutManager(this)

            mAdapter = ExamSubjectAdapter(
                ArrayList(data),
                this,
                this
            )

            binding.rcExamTimeTableAct.adapter = mAdapter
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }



    override fun onSearchResultEmpty(isEmpty: Boolean) {
    }

    override fun onExamSelected(examid: String, examName: String,type: String) {
    }

    override fun onRubricClick(rubric: ExamTimetableRubric) {
        showScheduleBottomSheet(rubric)

    }
}
