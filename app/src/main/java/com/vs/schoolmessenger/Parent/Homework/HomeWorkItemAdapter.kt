package com.vs.schoolmessenger.Parent.Homework

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.CommonScreens.ImageSliderAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.WaveformSeekBar
import com.vs.schoolmessenger.Utils.fetchVimeoThumbnail
import me.relex.circleindicator.CircleIndicator
import kotlin.math.max

class HomeWorkItemAdapter (
    private var itemList: List<HomeWorkList>?,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var currentlyPlayingHolder: DataViewHolder? = null // Track currently playing holder

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.shimmer_view_small_list, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.homeword_report_item, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position], position, this)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private lateinit var mediaPlayer: MediaPlayer
        private var isPrepared = false
        private var isPlayingVoice = false
        private var lastPosition: Int = 0
        private val handler = Handler(Looper.getMainLooper())

        // Progress updater for audio playback
        private val progressUpdater = object : Runnable {
            override fun run() {
                if (isPrepared && mediaPlayer.isPlaying) {
                    // Update SeekBar progress
                    val normalizedPower = max(1f, (1f + 160) / 160)
                    waveformSeekBar.updateWithLevel(normalizedPower)

                    // Update elapsed time
                    lblStartDuration.text = formatTime(mediaPlayer.currentPosition)
                    handler.postDelayed(this, 100)
                }
            }
        }

        private var isTextExpanded = false
        private val imgNewImage: ImageView = itemView.findViewById(R.id.imgNewImage)


        // Image
        private val rlaImageReport: RelativeLayout = itemView.findViewById(R.id.rlaImageReport)
        private val lblDateImage: TextView = itemView.findViewById(R.id.lblDateImage)
        private val lblTitleImage: TextView = itemView.findViewById(R.id.lblTitleImage)
        private val lblContentImage: TextView = itemView.findViewById(R.id.lblContentImage)
        private val viewpager: ViewPager = itemView.findViewById(R.id.viewpager)
        private val indicator: CircleIndicator = itemView.findViewById(R.id.indicator)
        private val tvSeeMoreImage: TextView = itemView.findViewById(R.id.tvSeeMoreImage)


        //Voice
        private val rlaVoiceReport: RelativeLayout = itemView.findViewById(R.id.rlaVoiceReport)
        private val imgVoicePlay: ImageView = itemView.findViewById(R.id.imgVoicePlay)
        private val waveformSeekBar: WaveformSeekBar = itemView.findViewById(R.id.waveformSeekBar)
        private val lblStartDuration: TextView = itemView.findViewById(R.id.lblStartDuration)
        private val lblEndDuration: TextView = itemView.findViewById(R.id.lblEndDuration)
        private val lblTitleVoice: TextView = itemView.findViewById(R.id.lblTitleVoice)
        private val lblVoiceContent: TextView = itemView.findViewById(R.id.lblVoiceContent)
        private val tvSeeMoreVoice: TextView = itemView.findViewById(R.id.tvSeeMoreVoice)
        private val lblDateVoice: TextView = itemView.findViewById(R.id.lblDateVoice)
        private val isVoiceProgress: ProgressBar = itemView.findViewById(R.id.isVoiceProgress)


        //Text
        private val rlaReportText: RelativeLayout = itemView.findViewById(R.id.rlaReportText)
        private val lblTitleText: TextView = itemView.findViewById(R.id.lblTitleText)
        private val lblDateText: TextView = itemView.findViewById(R.id.lblDateText)
        private val lblContentText: TextView = itemView.findViewById(R.id.lblContentText)
        private val tvSeeMoreText: TextView = itemView.findViewById(R.id.lblSeeMoreText)


        //Video
        private val rlaReportVideo: RelativeLayout = itemView.findViewById(R.id.rlaReportVideo)
        private val lblDateVideo: TextView = itemView.findViewById(R.id.lblDateVideo)
        private val imgVimeoThumbnail: ImageView = itemView.findViewById(R.id.imgVimeoThumbnail)
        private val imgVideoPlay: ImageView = itemView.findViewById(R.id.imgVideoPlay)
        private val customProgressBar: ProgressBar = itemView.findViewById(R.id.customProgressBar)
        private val lblTitleVideo: TextView = itemView.findViewById(R.id.lblTitleVideo)
        private val lblVideoContent: TextView = itemView.findViewById(R.id.lblVideoContent)
        private val tvSeeMoreVideo: TextView = itemView.findViewById(R.id.tvSeeMoreVideo)


        // Pdf
        private val rlaPdf: RelativeLayout = itemView.findViewById(R.id.rlaPdf)
        private val lblDatePdf: TextView = itemView.findViewById(R.id.lblDatePdf)
        private val lblTitlePdf: TextView = itemView.findViewById(R.id.lblTitlePdf)
        private val lblPdfContent: TextView = itemView.findViewById(R.id.lblPdfContent)
        private val tvSeeMorePdf: TextView = itemView.findViewById(R.id.tvSeeMorePdf)
        private val webPdf: WebView = itemView.findViewById(R.id.webPdf)
        private val loadingBar: ProgressBar = itemView.findViewById(R.id.loadingBar)


        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            data: HomeWorkList,
            position: Int,
            adapter: HomeWorkItemAdapter
        ) {

            when (data.isType) {
                "isText" -> {
                    rlaReportText.visibility = View.VISIBLE
                    lblTitleText.text = data.isTitle
                    lblDateText.text = data.date
                    lblContentText.text = data.isDescription
                    rlaVoiceReport.visibility = View.GONE
                    rlaReportVideo.visibility = View.GONE
                    rlaImageReport.visibility = View.GONE
                    rlaPdf.visibility = View.GONE
                    isSeeMoreVisibility(lblContentText, tvSeeMoreText)
                }

                "isVoice" -> {
                    rlaVoiceReport.visibility = View.VISIBLE
                    lblTitleVoice.text = data.isTitle
                    lblVoiceContent.text = data.isDescription
                    lblDateVoice.text = data.date
                    rlaReportText.visibility = View.GONE
                    rlaReportVideo.visibility = View.GONE
                    rlaImageReport.visibility = View.GONE
                    rlaPdf.visibility = View.GONE
                    isSeeMoreVisibility(lblVoiceContent, tvSeeMoreVoice)


                    getAudioDuration(data.isLink) { duration ->
                        lblEndDuration.text =
                            formatTime(duration) // Update the TextView with formatted duration
                    }

                    imgVoicePlay.setOnClickListener {
                        isVoiceProgress.visibility= View.VISIBLE

                        if (adapter.currentlyPlayingHolder != null && adapter.currentlyPlayingHolder != this) {
                            adapter.currentlyPlayingHolder?.stopAudioPlayback()
                        }

                        if (isPlayingVoice) {
                            pauseAudio()
                        } else {
                            if (!isPrepared) {
                                initializeMediaPlayer(data.isLink)
                            } else {
                                resumeAudio()
                            }
                        }
                        adapter.currentlyPlayingHolder = this
                    }
                }

                "isVideo" -> {
                    rlaReportVideo.visibility = View.VISIBLE
                    lblDateVideo.text = data.date
                    lblTitleVideo.text = data.isTitle
                    lblVideoContent.text = data.isDescription
                    rlaReportText.visibility = View.GONE
                    rlaVoiceReport.visibility = View.GONE
                    rlaImageReport.visibility = View.GONE
                    rlaPdf.visibility = View.GONE
                    isSeeMoreVisibility(lblVideoContent, tvSeeMoreVideo)
                    isGetTheThumbnail(data.isVideoId)

                }

                "isPDF" -> {
                    rlaPdf.visibility = View.VISIBLE
                    rlaVoiceReport.visibility = View.GONE
                    rlaReportVideo.visibility = View.GONE
                    rlaImageReport.visibility = View.GONE
                    rlaReportText.visibility = View.GONE

                    lblDatePdf.text = data.date
                    lblTitlePdf.text = data.isTitle
                    lblPdfContent.text = data.isDescription

                    isSeeMoreVisibility(lblPdfContent, tvSeeMorePdf)


                    loadingBar.visibility = View.VISIBLE

                    webPdf.apply {
                        settings.javaScriptEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.domStorageEnabled = true


                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(
                                view: WebView?,
                                url: String?,
                                favicon: Bitmap?
                            ) {
                                loadingBar.visibility = View.VISIBLE
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                loadingBar.visibility = View.GONE
                            }
                        }

                        webChromeClient = WebChromeClient()

                        // Use Google Drive viewer to load the PDF
                        loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=${data.isLink}")


                        setOnTouchListener { _, event ->
                            if (event.action == MotionEvent.ACTION_UP) {
//                                listener.onItemPDFClick(data) // Trigger your custom listener
                            }
                            false // Let the WebView handle the touch event as well
                        }
                    }
                }

                "isImage" -> {
                    rlaImageReport.visibility = View.VISIBLE
                    lblDateImage.text = data.date
                    lblTitleImage.text = data.isTitle
                    lblContentImage.text = data.isDescription
                    rlaReportText.visibility = View.GONE
                    rlaVoiceReport.visibility = View.GONE
                    rlaReportVideo.visibility = View.GONE
                    rlaPdf.visibility = View.GONE
                    isSeeMoreVisibility(lblContentImage, tvSeeMoreImage)


                    val imageUrls = listOf(
                        "https://picsum.photos/600/400?random=1", // Random Image 1
                        "https://picsum.photos/600/400?random=2", // Random Image 2
                        "https://picsum.photos/600/400?random=3", // Random Image 3
                        "https://picsum.photos/600/400?random=4", // Random Image 4
                        "https://picsum.photos/600/400?random=5"  // Random Image 5
                    )

                    // Set up the adapter
                    val viewPagerAdapter = ImageSliderAdapter(context, imageUrls)
                    viewpager.adapter = viewPagerAdapter
                    indicator.setViewPager(viewpager)
                }
            }

            imgVideoPlay.setOnClickListener {
//                listener.onItemVideoClick(data)
            }

            tvSeeMoreImage.setOnClickListener {
                isSeeMoreExpanded(tvSeeMoreImage, lblContentImage)
            }
            tvSeeMoreVideo.setOnClickListener {
                isSeeMoreExpanded(tvSeeMoreVideo, lblVideoContent)
            }
            tvSeeMoreText.setOnClickListener {
                isSeeMoreExpanded(tvSeeMoreText, lblContentText)
            }
            tvSeeMoreVoice.setOnClickListener {
                isSeeMoreExpanded(tvSeeMoreVoice, lblVoiceContent)
            }
            tvSeeMorePdf.setOnClickListener {
                isSeeMoreExpanded(tvSeeMorePdf, lblPdfContent)
            }

        }

        private fun isGetTheThumbnail(isVideId: String) {
            fetchVimeoThumbnail("https://vimeo.com/${isVideId}") { thumbnailUrl ->
                if (thumbnailUrl != null) {
                    Glide.with(imgVimeoThumbnail.context)
                        .load(thumbnailUrl)
                        .into(imgVimeoThumbnail)
                    imgVideoPlay.visibility = View.VISIBLE
                    customProgressBar.visibility = View.GONE
                }
            }
        }

        private fun isSeeMoreExpanded(tvSeeMore: TextView, lblContent: TextView) {

            if (isTextExpanded) {
                isTextExpanded = false
                lblContent.maxLines = 3
                lblContent.ellipsize = TextUtils.TruncateAt.END
                tvSeeMore.text = R.string.SeeMore.toString()
            } else {
                isTextExpanded = true
                lblContent.maxLines = Integer.MAX_VALUE
                lblContent.ellipsize = null
                tvSeeMore.text = R.string.SeeLess.toString()
            }
        }

        private fun isSeeMoreVisibility(lblContent: TextView, tvSeeMore: TextView) {
            lblContent.post {
                if (lblContent.lineCount > 3) {
                    tvSeeMore.visibility = View.VISIBLE
                    lblContent.maxLines = 3
                    lblContent.ellipsize = TextUtils.TruncateAt.END
                }
            }
        }

        // isVoice
        private fun initializeMediaPlayer(audioUrl: String) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                prepareAsync()
                setOnPreparedListener {
                    isPrepared = true
                    startAudioProgressUpdate()
                    isVoiceProgress.visibility= View.GONE
                    start()
                    isPlayingVoice = true
                    updatePlayPauseIcon(isPlaying = true)
                }
                setOnCompletionListener {
                    resetPlaybackState()
                }
            }
        }

        // Pause audio playback
        private fun pauseAudio() {
            isVoiceProgress.visibility= View.GONE
            mediaPlayer.pause()
            lastPosition = mediaPlayer.currentPosition
            isPlayingVoice = false
            updatePlayPauseIcon(isPlaying = false)
            waveformSeekBar.updateWithLevel(0f)
        }

        // Resume audio playback
        private fun resumeAudio() {
            isVoiceProgress.visibility= View.GONE
            mediaPlayer.seekTo(lastPosition)
            mediaPlayer.start()
            isPlayingVoice = true
            startAudioProgressUpdate()
            updatePlayPauseIcon(isPlaying = true)
        }

        // Stop audio playback
        private fun stopAudioPlayback() {
            isVoiceProgress.visibility= View.GONE
            if (::mediaPlayer.isInitialized) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.reset()
                resetPlaybackState()
            }
        }

        // Update the play/pause icon
        private fun updatePlayPauseIcon(isPlaying: Boolean) {
            val icon = if (isPlaying) R.drawable.pause_icon else R.drawable.video_play
            imgVoicePlay.setImageDrawable(ContextCompat.getDrawable(context, icon))
        }

        // Reset playback state
        private fun resetPlaybackState() {
            stopAudioProgressUpdate()
            isPrepared = false
            isPlayingVoice = false
            lastPosition = 0
            waveformSeekBar.updateWithLevel(0f)
            updatePlayPauseIcon(isPlaying = false)
        }

        // Start updating audio progress
        private fun startAudioProgressUpdate() {
            handler.post(progressUpdater)
        }

        // Stop updating audio progress
        private fun stopAudioProgressUpdate() {
            handler.removeCallbacks(progressUpdater)
        }

        // Format milliseconds to "mm:ss"
        private fun formatTime(milliseconds: Int): String {
            val seconds = (milliseconds / 1000) % 60
            val minutes = (milliseconds / (1000 * 60)) % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

        // Get audio duration asynchronously
        private fun getAudioDuration(audioUrl: String, callback: (Int) -> Unit) {
            val tempMediaPlayer = MediaPlayer()
            try {
                tempMediaPlayer.setDataSource(audioUrl)
                tempMediaPlayer.prepareAsync()
                tempMediaPlayer.setOnPreparedListener {
                    callback(tempMediaPlayer.duration)
                    tempMediaPlayer.release()
                }
                tempMediaPlayer.setOnErrorListener { mp, _, _ ->
                    mp.release()
                    false
                }
            } catch (e: Exception) {
                tempMediaPlayer.release()
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer() // Start shimmer effect
        }
    }
}