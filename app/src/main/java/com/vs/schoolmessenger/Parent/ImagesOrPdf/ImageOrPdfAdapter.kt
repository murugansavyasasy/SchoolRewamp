package com.vs.schoolmessenger.Parent.ImagesOrPdf

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
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
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.CommonScreens.ImageSliderAdapter
import com.vs.schoolmessenger.R
import me.relex.circleindicator.CircleIndicator

class ImageOrPdfAdapter(
    private var itemList: List<ImageOrPdfData>?,
    private var listener: ImageOrPdfClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

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
            holder.bind(itemList!![position], position, listener, this)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

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
            data: ImageOrPdfData,
            position: Int,
            listener: ImageOrPdfClickListener,
            adapter: ImageOrPdfAdapter
        ) {

            when (data.isType) {
                "isPDF" -> {
                    rlaPdf.visibility = View.VISIBLE
                    rlaImageReport.visibility = View.GONE
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
                                listener.onItemPDFClick(data) // Trigger your custom listener
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


            tvSeeMoreImage.setOnClickListener {
                isSeeMoreExpanded(tvSeeMoreImage, lblContentImage)
            }
            tvSeeMorePdf.setOnClickListener {
                isSeeMoreExpanded(tvSeeMorePdf, lblPdfContent)
            }

        }

        private fun isSeeMoreExpanded(tvSeeMore: TextView, lblContent: TextView) {

            if (isTextExpanded) {
                isTextExpanded = false
                lblContent.maxLines = 3
                lblContent.ellipsize = TextUtils.TruncateAt.END
                tvSeeMore.text = context.getString(R.string.SeeMore)
            } else {
                isTextExpanded = true
                lblContent.maxLines = Integer.MAX_VALUE
                lblContent.ellipsize = null
                tvSeeMore.text = context.getString(R.string.SeeLess)
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
    }


    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer() // Start shimmer effect
        }
    }

}