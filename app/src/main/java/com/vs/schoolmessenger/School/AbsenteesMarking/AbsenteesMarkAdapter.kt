package com.vs.schoolmessenger.School.AbsenteesMarking
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R

class AbsenteesMarkAdapter(
    private var itemList: List<StudentData>?,
    private var listener: AbsenteesClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var isTextExpanded = false
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.shimmer_view_small_list, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.attendance_student_list, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position],position,listener)

        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val lblName: TextView = itemView.findViewById(R.id.lblName)
        private val lblRollNo: TextView = itemView.findViewById(R.id.lblRollNo)
        private val lnrPresent: LinearLayout = itemView.findViewById(R.id.lnrPresent)
        private val lnrAbsent: RelativeLayout = itemView.findViewById(R.id.lnrAbsent)

        fun bind(data: StudentData, position: Int, listener: AbsenteesClickListener) {
            lblName.text = data.Name
            lblRollNo.text = data.RollNo

            lnrPresent.setOnClickListener {
                lnrAbsent.visibility = View.VISIBLE
                lnrPresent.visibility = View.GONE
                listener.onItemClick(data)
            }

            lnrAbsent.setOnClickListener{
                lnrAbsent.visibility = View.GONE
                lnrPresent.visibility = View.VISIBLE
                listener.onItemClick(data)
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout = itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer() // Start shimmer effect
        }
    }
}