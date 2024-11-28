package com.vs.schoolmessenger.Parent.Video

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.VimeoVideoPlay
import com.vs.schoolmessenger.databinding.ParentSideVideoBinding

class ParentVideo : BaseActivity<ParentSideVideoBinding>(), VideoOnItemClickListener,
    View.OnClickListener {
    private lateinit var items: List<VideoData>
    private lateinit var isVideoListAdapter: VideoListAdapter
    private var isVideoData: MutableList<VideoData> = mutableListOf()

    override fun getViewBinding(): ParentSideVideoBinding {
        return ParentSideVideoBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarWhiteTheme()

        binding.imgBack.setOnClickListener(this)

        items = listOf(
            VideoData(
                "https://vimeo.com/76979871", "76979871",
                "Text Message",
                "Come to school",
                "Mon 12 Mar, 2020"
            ),
            VideoData(
                "https://vimeo.com/22439234", "22439234",
                "Voice Message",
                "Big Content is a content marketing format that requires far greater time and effort to produce than the more common formats (i.e. blog posts). Big Content is often based on a novel or original idea and is differentiated in its form, frequency or duration.",
                "Sun 17 Apr, 2023"
            ),
            VideoData(
                "https://vimeo.com/259411563", "259411563",
                "Assignment Message",
                "Complete the Assignment",
                "Fri 2 Jun, 2044"
            ),
            VideoData(
                "https://vimeo.com/216098214", "216098214",
                "Image Message",
                "Drawing the Image",
                "Wen 22 Jan, 2021"
            ),
            VideoData(
                "https://vimeo.com/90509568", "90509568",
                "Notice Board Message",
                "Follow the NoticeBoard",
                "Tus 31 Dec, 2077",
            )
        )

        isVideoData.addAll(items)


        binding.txtVideoMenu.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }
        })


        isVideoListAdapter = VideoListAdapter(isVideoData, this, this)
        binding.rcyVideo.layoutManager = LinearLayoutManager(this)
        binding.rcyVideo.adapter = isVideoListAdapter

    }

    private fun filter(text: String) {
        isVideoData.clear()
        if (text.isEmpty()) {
            isVideoData.addAll(items)  // If search is empty, show all items
        } else {
            for (item in items) {
                if (item.title.toLowerCase().contains(text.toLowerCase())) {
                    isVideoData.add(item)  // Add the matching GridItem to filteredList
                }
            }
        }
        isVideoListAdapter.notifyDataSetChanged()
    }


    override fun onItemClick(videoData: VideoData) {
        val intent = Intent(this, VimeoVideoPlay::class.java)
        val videoUrl = "https://vimeo.com/${videoData.videoId}"
        intent.putExtra("VIDEO_URL", videoUrl)
        intent.putExtra("VIDEO_TITLE", videoData.title)
        startActivity(intent)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgBack -> {
                onBackPressed()
            }
        }
    }
}
