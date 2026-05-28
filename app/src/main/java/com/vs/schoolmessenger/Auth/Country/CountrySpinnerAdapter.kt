package com.vs.schoolmessenger.Auth.Country

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.R

class CountrySpinnerAdapter(
    private val context: Context,
    private val countryList: List<Country>
) : ArrayAdapter<Country>(context, R.layout.item_country_spinner, countryList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_country_spinner, parent, false)
        val country = countryList[position]

        val flagImageView: ImageView = view.findViewById(R.id.flagImageView)
        val countryNameTextView: TextView = view.findViewById(R.id.countryNameTextView)


        if (position == 0) {
            Glide.with(context)
                .load(R.drawable.select_country)
                .into(flagImageView)
        } else {
            Glide.with(context)
                .load(country.flag_url)
                .into(flagImageView)
        }

        countryNameTextView.text = country.name

        return view
    }
}
