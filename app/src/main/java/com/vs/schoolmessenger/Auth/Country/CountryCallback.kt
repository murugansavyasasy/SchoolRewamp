package com.vs.schoolmessenger.Auth.Country

import androidx.recyclerview.widget.DiffUtil

class CountryCallback : DiffUtil.ItemCallback<CountryItem>() {
    override fun areItemsTheSame(oldItem: CountryItem, newItem: CountryItem): Boolean =
        oldItem.iconResource == newItem.iconResource

    override fun areContentsTheSame(oldItem: CountryItem, newItem: CountryItem): Boolean =
        oldItem == newItem
}
