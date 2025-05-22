package com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetDateWiseHomeworkData
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetHomeworkDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import me.relex.circleindicator.CircleIndicator

class HomeWorkItemAdapter(
    private var GetHomeworkData: GetDateWiseHomeworkData?,
    private var HomeDetails: List<GetHomeworkDetails>?,
    private var context: Context,
    private var isLoading: Boolean,

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
            DataViewHolder.ShimmerViewHolder(view)
        } else {

            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.homework_school_reportitem, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(GetHomeworkData, HomeDetails!![position], position, this)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else HomeDetails?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private var isTextExpanded = false


        private val lblDateImage: TextView = itemView.findViewById(R.id.lblDateImage)
        private val lblTitleImage: TextView = itemView.findViewById(R.id.lblTitleImage)
        private val lblContentImage: TextView = itemView.findViewById(R.id.lblContentImage)
        private val tvSeeMoreImage: TextView = itemView.findViewById(R.id.tvSeeMoreImage)
        private val RcyImgPdf: RecyclerView = itemView.findViewById(R.id.rcyImgPDF)
        private val DotIndicator: CircleIndicator =itemView.findViewById(R.id.indicator)
        private val lblSubjectName: TextView = itemView.findViewById(R.id.LblHWSubjectName)
        private val rlaSelectText: RelativeLayout = itemView.findViewById(R.id.rlaSelectText)

        var mHomeworkImgPDFAdapter: HomeworkImgPDFAdapter? = null


        private fun getRecyclerView(): RecyclerView {
            return RcyImgPdf
        }

        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            data: GetDateWiseHomeworkData?,
            homeworkData: GetHomeworkDetails,
            position: Int,
            adapter: HomeWorkItemAdapter,

            ) {
            val homeworkImgPdf = getRecyclerView()
            lblTitleImage.text = homeworkData.title
            lblContentImage.text = homeworkData.description
            lblDateImage.text = Constant.convertDateTimeFormat(data!!.date)
            lblSubjectName.text = homeworkData.subject_name
            rlaSelectText.visibility = View.GONE

            isSeeMoreVisibility(lblContentImage, tvSeeMoreImage)
            tvSeeMoreImage.setOnClickListener {
                isSeeMoreExpanded(tvSeeMoreImage, lblContentImage)
            }

            if (homeworkData.file_path.size > 0) {
                RcyImgPdf.visibility = View.VISIBLE
            } else {
                RcyImgPdf.visibility = View.GONE
            }
            mHomeworkImgPDFAdapter =

                HomeworkImgPDFAdapter("",null, context, Constant.isShimmerViewShow)
            homeworkImgPdf.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            homeworkImgPdf.adapter = mHomeworkImgPDFAdapter

                mHomeworkImgPDFAdapter =
                    HomeworkImgPDFAdapter(
                        homeworkData.subject_name,
                        homeworkData.file_path,
                        context,
                        Constant.isShimmerViewDisable,
                    )
                homeworkImgPdf.adapter = mHomeworkImgPDFAdapter

        }

        private fun isSeeMoreExpanded(tvSeeMore: TextView, lblContent: TextView) {

            if (isTextExpanded) {
                isTextExpanded = false
                lblContent.maxLines = 3
                lblContent.ellipsize = TextUtils.TruncateAt.END
                tvSeeMore.text = itemView.context.getString(R.string.SeeMore)
            } else {
                isTextExpanded = true
                lblContent.maxLines = Integer.MAX_VALUE
                lblContent.ellipsize = null
                tvSeeMore.text = itemView.context.getString(R.string.SeeLess)
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


        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val shimmerLayout: ShimmerFrameLayout =
                itemView.findViewById(R.id.shimmer_view_container)

            init {
                shimmerLayout.startShimmer() // Start shimmer effect
            }
        }
    }
}