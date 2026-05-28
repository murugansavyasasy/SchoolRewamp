package com.vs.schoolmessenger.Dashboard.Settings.WhatsNew

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Dashboard.Settings.WhatsNew.Model.WhatsNewUpdateData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class WhatsNewAdapter(
    private var itemList: List<WhatsNewUpdateData>?,
    private val context: Context,
    private var isLoading: Boolean,
    private val recyclerView: RecyclerView //  we'll need this to scroll on click
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.whatsnew_recyclerview)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.whatsnew_recyclerview, parent, false)
            DataViewHolder(view, recyclerView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && !isLoading) {
            val totalCount = itemList?.size ?: 0
            itemList?.get(position)?.let { holder.bind(it, totalCount, position, context) }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 3 else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val recyclerView: RecyclerView) :
        RecyclerView.ViewHolder(itemView) {

        private val titleText = itemView.findViewById<TextView>(R.id.menu_name)
        private val descText = itemView.findViewById<TextView>(R.id.description_value)
        private val bannerImage = itemView.findViewById<ImageView>(R.id.banner_image)
        private val btnLearnMore = itemView.findViewById<TextView>(R.id.btnLearnMore)
        private val explore_linearlayout =
            itemView.findViewById<LinearLayout>(R.id.explore_linearlayout)
        private val swipeMoreLayout = itemView.findViewById<LinearLayout>(R.id.swipeeformore)
        private val swipeMoreLeft = itemView.findViewById<TextView>(R.id.swipemore)
        private val swipeMoreRight = itemView.findViewById<TextView>(R.id.swipemoredata)

        fun bind(data: WhatsNewUpdateData, totalCount: Int, position: Int, context: Context) {
            titleText.text = data.name
            descText.text = data.description

            Glide.with(itemView.context)
                .load(data.downloadable_image)
                .fitCenter()
                .into(bannerImage)

            if (data.app_redirect_link.isNullOrEmpty()) {
                explore_linearlayout.visibility = View.GONE
            } else {
                explore_linearlayout.visibility = View.VISIBLE
            }

            swipeMoreLayout.visibility = if (totalCount > 1) View.VISIBLE else View.GONE

            if (totalCount > 1) {
                when {
                    position == 0 -> {
                        swipeMoreLeft.visibility = View.GONE
                        swipeMoreRight.visibility = View.VISIBLE
                    }

                    position == 1 -> { // second item special case
                        swipeMoreLeft.visibility = View.VISIBLE
                        if (totalCount > 2) {
                            swipeMoreRight.visibility = View.VISIBLE
                        } else {
                            swipeMoreRight.visibility = View.GONE
                        }
                    }

                    position == totalCount - 1 -> { // last item
                        swipeMoreLeft.visibility = View.VISIBLE
                        swipeMoreRight.visibility = View.GONE
                    }

                    else -> { // middle items (like 3rd, 4th)
                        swipeMoreLeft.visibility = View.VISIBLE
                        swipeMoreRight.visibility = View.VISIBLE
                    }
                }
            }


            swipeMoreRight.setOnClickListener {
                val nextPos = adapterPosition + 1
                if (nextPos < totalCount) {
                    recyclerView.smoothScrollToPosition(nextPos)
                } else {
                    Toast.makeText(
                        itemView.context,
                        context.getString(R.string.no_more_updates), Toast.LENGTH_SHORT
                    ).show()
                }
            }

            swipeMoreLeft.setOnClickListener {
                val prevPos = adapterPosition - 1
                if (prevPos >= 0) {
                    recyclerView.smoothScrollToPosition(prevPos)
                } else {
                    Toast.makeText(
                        itemView.context,
                        context.getString(R.string.you_re_at_the_start), Toast.LENGTH_SHORT
                    ).show()
                }
            }

            btnLearnMore.setOnClickListener {
                val redirectUrl = data.app_redirect_link
                if (!redirectUrl.isNullOrEmpty()) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl))
                        itemView.context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            itemView.context,
                            context.getString(R.string.unable_to_open_link), Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                } else {
                    Toast.makeText(
                        itemView.context,
                        context.getString(R.string.no_redirect_link_available),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
