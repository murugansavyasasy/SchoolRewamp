package com.vs.schoolmessenger.Auth.Country

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.vs.schoolmessenger.R
import de.hdodenhof.circleimageview.CircleImageView

class CountryListAdapter(
    private val context: Context,
    private val countryList: List<Country>,
    private val onItemSelected: (Country) -> Unit
) : RecyclerView.Adapter<CountryListAdapter.CountryViewHolder>() {

    private var selectedPosition = -1
    private var isAllCountryVisible = true
    private var filteredList = countryList.toMutableList()

    inner class CountryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.lblCountryName)
        private val lblAllCountry: TextView = itemView.findViewById(R.id.lblAllCountry)
        private val imgCountryLogo: CircleImageView = itemView.findViewById(R.id.imgCountryLogo)
        private val rytCountry: RelativeLayout = itemView.findViewById(R.id.rytCountry)
        private val lnrRadio: LinearLayout = itemView.findViewById(R.id.lnrRadio)

        init {
            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                onItemSelected(filteredList[selectedPosition])
            }
        }

        fun bind(pos : Int,country: Country, isSelected: Boolean) {

                if (pos == 3) {
                    lblAllCountry.visibility = View.VISIBLE
                    rytCountry.visibility = View.GONE
                    isAllCountryVisible = false
                } else {
                    lblAllCountry.visibility = View.GONE
                    rytCountry.visibility = View.VISIBLE
                }

            nameText.text = country.name

            Glide.with(context)
                .load(country.flag_url)
                .placeholder(R.drawable.school_sample)
                .error(R.drawable.school_sample)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("GlideError", "Image load failed", e)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .into(imgCountryLogo)

            if(isSelected){
                lnrRadio.visibility = View.VISIBLE
                rytCountry.background = ContextCompat.getDrawable(context, R.drawable.edittext_background_focused)
            }
            else{
                lnrRadio.visibility = View.GONE
                rytCountry.background = ContextCompat.getDrawable(context, R.drawable.login_bg_rect_grey)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.country_list_item, parent, false)
        return CountryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        holder.bind(position,filteredList[position], position == selectedPosition)

    }

    override fun getItemCount(): Int = filteredList.size

    fun filter(query: String) {

        isAllCountryVisible = true

        filteredList = if (query.isEmpty()) {
            countryList.toMutableList()
        } else {
            countryList.filter {
                it.name.contains(query, ignoreCase = true)
            }.toMutableList()
        }

        // Reset selection on filter
        selectedPosition = -1
        notifyDataSetChanged()
    }
}
