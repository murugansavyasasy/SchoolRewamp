package com.vs.schoolmessenger.School.LSRW.Adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LSRW.LsrwReportAndStatics
import com.vs.schoolmessenger.School.LSRW.Model.Overview
import com.vs.schoolmessenger.Utils.Constant

class LsRwDashboardAdapter(
    private var itemList: List<Overview>,
    private val context: Context,
    private val onDashboardClick: (Overview) -> Unit,
    private val onCompletedClick: (Overview) -> Unit
) : RecyclerView.Adapter<LsRwDashboardAdapter.HeaderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lsrw_dashboard_viewitem, parent, false)
        return HeaderViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }


    fun updateList(newList: List<Overview>) {
        itemList = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = itemList.size


    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)
        private val txtCount: TextView = itemView.findViewById(R.id.txtCount)
        private val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        private val txtSubTitle: TextView = itemView.findViewById(R.id.txtSubTitle)
        private val root_linearlayout: LinearLayout = itemView.findViewById(R.id.root_linearlayout)

        fun bind(item: Overview) {
            txtCount.text = item.value
            txtTitle.text = item.title
            txtSubTitle.text = item.subtitle

            if (item.title == Constant.Active_Tasks) {
                imgIcon.setImageResource(R.drawable.exampadsvg)
            } else if (item.title == Constant.Avg_Performance) {
                imgIcon.setImageResource(R.drawable.graphsvgformat)
            } else if (item.title == Constant.Completed_Tasks){
                imgIcon.setImageResource(R.drawable.correcticonsvg)
            } else {
                imgIcon.setImageResource(R.drawable.questionmark)
            }

            root_linearlayout.setOnClickListener {



                if (item.title == Constant.Active_Tasks) {
                    if (item.value == Constant.zero) {
                        showNoDataPopup(context.getString(R.string.no_active_tasks_available))
                    } else {
                        onDashboardClick(item)
                    }

                } else if (item.title == Constant.Avg_Performance) {
                    if (item.value == Constant.zero) {
                        showNoDataPopup(context.getString(R.string.no_performance_data_available))
                    } else {
                        val intent = Intent(context, LsrwReportAndStatics::class.java)
                        context.startActivity(intent)
                    }

                } else if (item.title == Constant.Completed_Tasks) {
                    if (item.value == Constant.zero) {
                        showNoDataPopup(context.getString(R.string.no_completed_tasks_available))
                    } else {
                        onCompletedClick(item)
                    }
                }
            }
        }

        private fun showNoDataPopup(message: String) {
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.info))
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.permission_ok)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

    }
}