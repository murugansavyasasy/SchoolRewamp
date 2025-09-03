package com.vs.schoolmessenger.School.MessageFromManagement

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.HandlerCompat.postDelayed
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Auth.MobilePasswordSignIn.StaffDetails
import com.vs.schoolmessenger.Parent.Attachment.Adapter.AttachmentFilePathAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Repository.App
import com.vs.schoolmessenger.School.MessageFromManagement.Adapter.AttachmentMediaAdapter
import com.vs.schoolmessenger.School.MessageFromManagement.Adapter.MessageFromStaffAdapter
import com.vs.schoolmessenger.School.MessageFromManagement.Model.GetMessagesStaffData
import com.vs.schoolmessenger.School.QuizExam.Adapter.AddQuestion.PickQuestionAdapter
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.SharedPreference
import com.vs.schoolmessenger.databinding.MessageFromManagementBinding
import me.relex.circleindicator.CircleIndicator2


class MessageFromManagement : BaseActivity<MessageFromManagementBinding>(),
    View.OnClickListener,MsgStaffListener {

    private var isAccessToken: String? = null
    private var isStaffDetails: StaffDetails? = null
    private var appViewModel: App? = null
    private lateinit var adapter2: AttachmentFilePathAdapter
    private lateinit var adapter: MessageFromStaffAdapter
    var mediaPlayer: MediaPlayer? = MediaPlayer()
    var mediaFileLengthInMilliseconds = 0
    private lateinit var runnable: Runnable
    private var handler: Handler = Handler()




    override fun getViewBinding(): MessageFromManagementBinding {
        return MessageFromManagementBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        setupToolbarBlueWhite()
        binding.toolbarLayout.imgBack.setOnClickListener(this)
        appViewModel = ViewModelProvider(this)[App::class.java]
        appViewModel!!.init()
        isStaffDetails = SharedPreference.getStaffDetails(this)
        isAccessToken = isStaffDetails!!.access_token
        binding.toolbarLayout.lblParentToolBar.text = Constant.isSchoolMenuName
        binding.toolbarLayout.lblSchoolName.visibility = View.GONE
        isGetMessageFromStaff()

        appViewModel?.isGetMessageStaff?.observe(this) { response ->
            if (response != null) {
                if (response.status) {
                    binding.rcMessageStaff.visibility = View.VISIBLE
                    binding.lytList.visibility = View.GONE
                    isLoadMsgStaff(response.data)
                }
                else {
                    binding.rlaMessageFFromStaff.visibility = View.VISIBLE
                    binding.rcMessageStaff.visibility = View.GONE
                    ErrorMessage(response.message)
                }
            } else {
                binding.rlaMessageFFromStaff.visibility = View.VISIBLE
                binding.rcMessageStaff.visibility = View.GONE
                ErrorMessage(getString(R.string.Something_went_wrong_Please_try_again))
            }
        }

    }

    private fun isLoadMsgStaff(data: List<GetMessagesStaffData>) {

        if (data.isNotEmpty()) {
            adapter = MessageFromStaffAdapter(data,this, this, Constant.isShimmerViewDisable)
            binding.rcMessageStaff.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
            binding.rcMessageStaff.adapter = adapter
            binding.rcMessageStaff.isNestedScrollingEnabled = false
            binding.rcMessageStaff.visibility = View.VISIBLE
            binding.lytList.visibility = View.GONE
        } else {
            binding.rcMessageStaff.visibility = View.GONE
            binding.lytList.visibility = View.VISIBLE
            binding.txtNoData.text = getString(R.string.no_data_found)
        }
    }



    fun isGetMessageFromStaff(){
        adapter = MessageFromStaffAdapter(null,this, this, Constant.isShimmerViewShow)
        binding.rcMessageStaff.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        binding.rcMessageStaff.adapter = adapter
        binding.rcMessageStaff.isNestedScrollingEnabled = false
        appViewModel?.isGetMessageStaff(isAccessToken ?: "")
    }




    fun ErrorMessage(errorMessage: String) {
        binding.lytList.visibility = View.VISIBLE
        binding.txtNoData.text = errorMessage
    }

    fun showResumeListDialog(
        activity: Activity,
        data: GetMessagesStaffData
    ) {
        if (activity.isFinishing || activity.isDestroyed) return

        val dialogView = LayoutInflater.from(activity).inflate(R.layout.msg_from_staff_preview, null)
        val builder = AlertDialog.Builder(activity).setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (!activity.isFinishing && !activity.isDestroyed) {
            alertDialog.show()
        }

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rcAttachement)
        val indicator = dialogView.findViewById<CircleIndicator2>(R.id.indicator)
        val lblSentTime = dialogView.findViewById<TextView>(R.id.lblSentTime)
        val lblSendBy = dialogView.findViewById<TextView>(R.id.lblSendBy)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        val tvDescription = dialogView.findViewById<TextView>(R.id.tvDescription)
        val imgBack = dialogView.findViewById<ImageView>(R.id.imgBack)
        val rlaAudioDetails = dialogView.findViewById<RelativeLayout>(R.id.rlaAudioDetails)

        // FIX: use dialogView.findViewById instead of findViewById
        val lblRecentTotalDuration: TextView = dialogView.findViewById(R.id.lblRecentTotalDuration)
        val lblEmgRecentduration: TextView = dialogView.findViewById(R.id.lblEmgRecentduration)
        val imgRecentEmgplaypause: ImageView = dialogView.findViewById(R.id.imgRecentEmgplaypause)
        val recentseekbar: SeekBar = dialogView.findViewById(R.id.recentseekbar)
        val recentSeekbarlayout: RelativeLayout = dialogView.findViewById(R.id.recentSeekbarlayout)

        tvTitle.text = data.title
        lblSendBy.text = "Sent by Santhosh Kumar"
        lblSentTime.text = "Sent at ${Constant.isFormatDate(data.date.toString())} ${data.time}"

        if (!data.content.isNullOrEmpty()) {
            lblRecentTotalDuration.visibility = View.VISIBLE
            lblRecentTotalDuration.text = data.duration.toString()
            recentSeekbarlayout.visibility = View.VISIBLE

            // MediaPlayer play/pause handling
            imgRecentEmgplaypause.setOnClickListener {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                    imgRecentEmgplaypause.setImageResource(R.drawable.play_icon_voice)
                } else {
                    try {
                        if (mediaPlayer == null) mediaPlayer = MediaPlayer()
                        mediaPlayer?.reset()
                        mediaPlayer?.setDataSource(data.content)
                        mediaPlayer?.prepare()
                        mediaPlayer?.start()

                        imgRecentEmgplaypause.setImageResource(R.drawable.pause_icon)

                        // Start SeekBar updates
                        updateSeekBar(mediaPlayer!!, recentseekbar, lblEmgRecentduration, lblRecentTotalDuration)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            recentseekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mediaPlayer?.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        } else {
            recentSeekbarlayout.visibility = View.GONE
        }

        // Attachment handling
        when (data.type) {
            Constant.TEXT -> {
                rlaAudioDetails.visibility=View.GONE
                recyclerView.visibility = View.GONE
                indicator.visibility = View.GONE
                tvDescription.text = data.content
            }
            Constant.VOICE ->{ tvDescription.text = data.description
                rlaAudioDetails.visibility=View.VISIBLE
                recyclerView.visibility = View.GONE
                indicator.visibility = View.GONE

            }
            Constant.ATTACHMENT_ -> {
                rlaAudioDetails.visibility=View.GONE
                recyclerView.visibility = View.VISIBLE
                indicator.visibility = View.VISIBLE
                tvDescription.text = data.description
            }
        }

        if (data.file_size.isNullOrEmpty()) {
            indicator.visibility = View.GONE
            recyclerView.visibility = View.GONE
        } else {
            recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = AttachmentMediaAdapter(
                data.file_path,
                activity,
                Constant.isShimmerViewDisable
            )
            indicator.attachToRecyclerView(recyclerView)
        }

        imgBack.setOnClickListener { alertDialog.dismiss() }
    }


//    fun showResumeListDialog(
//        activity: Activity,
//        data: GetMessagesStaffData
//    ) {
//        if (activity.isFinishing || activity.isDestroyed) return
//
//        val dialogView = LayoutInflater.from(activity).inflate(R.layout.msg_from_staff_preview, null)
//        val builder = AlertDialog.Builder(activity)
//        builder.setView(dialogView)
//        val alertDialog = builder.create()
//        alertDialog.setCancelable(false)
//        alertDialog.setCanceledOnTouchOutside(false)
//        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//
//        if (!activity.isFinishing && !activity.isDestroyed) {
//            alertDialog.show()
//        }
//
//        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rcAttachement)
//        val indicator = dialogView.findViewById<CircleIndicator2>(R.id.indicator)
//        val lblSentTime = dialogView.findViewById<TextView>(R.id.lblSentTime)
//        val lblSendBy = dialogView.findViewById<TextView>(R.id.lblSendBy)
//        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
//        val tvDescription = dialogView.findViewById<TextView>(R.id.tvDescription)
//        val imgBack = dialogView.findViewById<ImageView>(R.id.imgBack)
//
//        var lblRecentTotalDuration: TextView = findViewById(R.id.lblRecentTotalDuration)
//        var lblEmgRecentduration: TextView = findViewById(R.id.lblEmgRecentduration)
//        var imgRecentEmgplaypause: ImageView = findViewById(R.id.imgRecentEmgplaypause)
//        var recentseekbar: SeekBar = findViewById(R.id.recentseekbar)
//        var recentSeekbarlayout: RelativeLayout = findViewById(R.id.recentSeekbarlayout)
//
//
//
//
//
//        tvTitle.text=data.title
//        lblSendBy.text="Sent by Santhosh Kumar"
//        lblSentTime.text = "Sent at"+" "+Constant.isFormatDate(data.date.toString())+" "+data.time
//
//        if (data.content!!.isNotEmpty()) {
//
//            lblRecentTotalDuration.visibility = View.VISIBLE
//            lblRecentTotalDuration.text = data.duration.toString()
//            recentSeekbarlayout.visibility = View.VISIBLE
//            imgRecentEmgplaypause.setOnClickListener {
//                if (mediaPlayer!!.isPlaying) {
//                    mediaPlayer!!.seekTo(mediaPlayer!!.currentPosition)
//                    mediaPlayer!!.pause()
//                    imgRecentEmgplaypause.setImageResource(R.drawable.play_icon_voice)
//                } else {
//                    mediaFileLengthInMilliseconds = mediaPlayer!!.duration
//                    imgRecentEmgplaypause.setImageResource(R.drawable.pause_icon)
//                    mediaPlayer!!.reset()
//                    mediaPlayer!!.setDataSource(data.content)
//                    mediaPlayer!!.prepare()
//                    mediaPlayer!!.start()
//                    primarySeekBarProgressUpdater(mediaFileLengthInMilliseconds)
//                }
//                initializeSeekBar()
//            }
//
//            recentseekbar.setOnSeekBarChangeListener(object :
//                SeekBar.OnSeekBarChangeListener {
//                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
//                    if (b) {
//                        mediaPlayer!!.seekTo(i * 1000)
//                    }
//                    mediaPlayer!!.setOnCompletionListener {
//                        imgRecentEmgplaypause.setImageResource(R.drawable.play_icon_voice)
//                        mediaPlayer!!.seekTo(0)
//                    }
//                }
//
//                override fun onStartTrackingTouch(seekBar: SeekBar) {
//                }
//
//                override fun onStopTrackingTouch(seekBar: SeekBar) {
//                }
//            })
//        } else {
//            recentSeekbarlayout!!.visibility = View.GONE
//        }
//
//
//        when (data.type) {
//            Constant.TEXT -> {
//                recyclerView.visibility=View.GONE
//                indicator.visibility = View.GONE
//                tvDescription.text=data.content
//            }
//
//            Constant.VOICE -> {
//                tvDescription.text=data.description
//            }
//
//            Constant.ATTACHMENT_ -> {
//                recyclerView.visibility=View.VISIBLE
//                indicator.visibility = View.VISIBLE
//                tvDescription.text=data.description
//
//            }
//        }
//
//        if (data.file_size.isNullOrEmpty()) {
//            indicator.visibility = View.GONE
//            recyclerView.visibility=View.GONE
//        }
//        else{
//            indicator.visibility = View.VISIBLE
//            recyclerView.visibility=View.VISIBLE
//
//            recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
//            recyclerView.adapter = AttachmentMediaAdapter(
//                data.file_path,
//                activity,
//                Constant.isShimmerViewDisable
//            )
//            indicator.attachToRecyclerView(recyclerView)
//        }
//
//
//        imgBack.setOnClickListener {
//            alertDialog.dismiss()
//        }
//
//
//    }

    private fun CircleIndicator2.attachToRecyclerView(recyclerView: RecyclerView) {
        val adapter = recyclerView.adapter ?: return
        this.createIndicators(adapter.itemCount, 0)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = rv.layoutManager as? LinearLayoutManager ?: return
                val firstVisible = layoutManager.findFirstVisibleItemPosition()
                this@attachToRecyclerView.animatePageSelected(firstVisible)
            }
        })

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                this@attachToRecyclerView.createIndicators(adapter.itemCount, 0)
            }
        })
    }



    override fun onClick(p0: View?) {
        when (p0?.id) {

        }
    }

    override fun onStaffClick(data: GetMessagesStaffData) {
        Log.d("SelectedData",data.toString())
        showResumeListDialog(this,data)
    }

    fun milliSecondsToTimer(milliseconds: Long): String {
        var finalTimerString = ""
        var secondsString = ""
        var minutesString = ""

        // Convert total duration into time
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000).toInt()
        // Add hours if there
        if (hours > 0) {
            finalTimerString = "$hours:"
        }

        // Prepending 0 to Minutes if it is one digit
        minutesString = if (minutes < 10) {
            "0$minutes"
        } else {
            "" + minutes
        }

        // Prepending 0 to seconds if it is one digit
        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }
        finalTimerString = "$finalTimerString$minutesString:$secondsString"

        // return timer string
        return finalTimerString
    }


    private fun updateSeekBar(
        mediaPlayer: MediaPlayer,
        seekBar: SeekBar,
        lblCurrent: TextView,
        lblTotal: TextView
    ) {
        seekBar.max = mediaPlayer.duration
        lblTotal.text = milliSecondsToTimer(mediaPlayer.duration.toLong())

        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                if (mediaPlayer.isPlaying) {
                    seekBar.progress = mediaPlayer.currentPosition
                    lblCurrent.text = milliSecondsToTimer(mediaPlayer.currentPosition.toLong())
                    handler.postDelayed(this, 1000)
                }
            }
        })
    }

//
//    private fun primarySeekBarProgressUpdater(fileLength: Int,) {
//        val iProgress = (mediaPlayer!!.currentPosition.toFloat() / fileLength * 100).toInt()
//        recentseekbar.setProgress(iProgress) // This math construction give a percentage of "was playing"/"song length"
//        if (mediaPlayer!!.isPlaying) {
//            val notification = Runnable {
//                lblEmgRecentduration.text = milliSecondsToTimer(
//                    mediaPlayer!!.currentPosition.toLong()
//                )
//                primarySeekBarProgressUpdater(fileLength)
//            }
//            postDelayed(notification, 1000)
//        }
//    }
//
//
//    private fun initializeSeekBar() {
//        recentseekbar.max = mediaPlayer!!.seconds
//
//        runnable = Runnable {
//            recentseekbar.progress = mediaPlayer!!.currentSeconds
//            postDelayed(runnable, 1000)
//        }
//        postDelayed(runnable, 1000)
//    }



    val MediaPlayer.seconds: Int
        get() {
            return this.duration / 1000
        }

    // Creating an extension property to get media player current position in seconds
    val MediaPlayer.currentSeconds: Int
        get() {
            return this.currentPosition / 1000
        }

}