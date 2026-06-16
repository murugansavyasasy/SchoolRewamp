package com.vs.schoolmessenger.Parent.FeeDetails

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.FeeDetails.Model.FeeInvoiceResponse
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant.isFormatDate
import com.vs.schoolmessenger.Utils.ShimmerUtil

class FeeReceiptAdapter(
    private var originalList: List<FeeInvoiceResponse.InvoiceData>?,
    private var listener: InvoiceClickListener,
    private var context: Context,
    private var isLoading: Boolean,
    private var filterResultListener: FeeDetails.OnFilterResultListener? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), android.widget.Filterable {

    private var filteredList: List<FeeInvoiceResponse.InvoiceData>? = originalList
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.fee_receipt_list_items)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fee_receipt_list_items, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            filteredList?.get(position)?.let {
                holder.bind(it, listener)
            }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 6 else filteredList?.size ?: 0
    }

    override fun getFilter(): android.widget.Filter {
        return object : android.widget.Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                val results = if (query.isEmpty()) {
                    originalList ?: listOf()
                } else {
                    originalList?.filter {
                        it.invoice_no?.lowercase()?.contains(query) == true ||
                                it.invoice_date?.lowercase()?.contains(query) == true ||
                                it.invoice_amount?.lowercase()?.contains(query) == true
                    } ?: listOf()
                }
                return FilterResults().apply { values = results }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<FeeInvoiceResponse.InvoiceData> ?: listOf()
                notifyDataSetChanged()
                filterResultListener?.onFilterResult(filteredList.isNullOrEmpty())
            }
        }
    }


    fun getCurrentList(): List<FeeInvoiceResponse.InvoiceData> = filteredList ?: listOf()

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblInvoiceNo: TextView = itemView.findViewById(R.id.lblInvoiceNo)
        private val lblInvoiceDate: TextView = itemView.findViewById(R.id.lblInvoiceDate)
        private val lblInvoiceAmount: TextView = itemView.findViewById(R.id.lblInvoiceAmount)
        private val lblInvoiceTime: TextView = itemView.findViewById(R.id.lblInvoiceTime)
        private val rytView: RelativeLayout = itemView.findViewById(R.id.rytView)
        private val rytViewInvoice: RelativeLayout = itemView.findViewById(R.id.rytViewInvoice)
        private val imgPdf: ImageView = itemView.findViewById(R.id.imgPdf)

        @SuppressLint("SetTextI18n")
        fun bind(data: FeeInvoiceResponse.InvoiceData, listener: InvoiceClickListener) {

            lblInvoiceNo.text =
                "${context.getString(R.string.receipt_No)} : ${data.invoice_no ?: "-"}"
            val dateTime = data.invoice_date?.split(" ") ?: listOf()
            val rawDate = dateTime.getOrNull(0) ?: "-"
            val time = dateTime.drop(1).joinToString(" ")

            val formattedDate = isFormatDate(rawDate)
            lblInvoiceDate.text = formattedDate
            lblInvoiceTime.text = time
            lblInvoiceAmount.text =
                "${context.getString(R.string.paid_Amount)} : ${data.invoice_amount ?: "-"}"
            imgPdf.setImageResource(R.drawable.pdf_icon)

            rytView.setOnClickListener { listener.onItemClick(data, this@DataViewHolder) }

            rytViewInvoice.setOnClickListener {
                val invoiceId = data.id ?: return@setOnClickListener
                if (context is FeeDetails) {
                    (context as FeeDetails).viewInvoice(invoiceId)
                }
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }

    fun setData(newList: List<FeeInvoiceResponse.InvoiceData>, loading: Boolean = false) {
        this.originalList = newList
        this.filteredList = newList
        this.isLoading = loading
        notifyDataSetChanged()
    }

    fun showShimmer() {
        isLoading = true
        notifyDataSetChanged()
    }

    fun hideShimmer() {
        isLoading = false
        notifyDataSetChanged()
    }
}
