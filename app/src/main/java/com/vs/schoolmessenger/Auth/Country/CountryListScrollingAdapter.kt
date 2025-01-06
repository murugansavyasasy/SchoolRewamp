package com.vs.schoolmessenger.Auth.Country

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R

class CountryListScrollingAdapter(private val items: List<CountryItem>) :
    RecyclerView.Adapter<CountryListScrollingAdapter.FeatureItemViewHolder>() {

    val itemList = mutableListOf<CountryItem>()
    private var isFlagLoadPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.iscountry_item, parent, false)
        return FeatureItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeatureItemViewHolder, position: Int) {
        holder.bind(items)
    }

    override fun getItemCount(): Int = itemList.size

    fun submitList(newList: List<CountryItem>) {
        itemList.clear()
        itemList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class FeatureItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val lnrSixthPositionCountry: LinearLayout =
            view.findViewById(R.id.lnrSixthPositionCountry)
        private val lnrFifthPositionCountry: LinearLayout =
            view.findViewById(R.id.lnrFifthPositionCountry)
        private val lnrFourthPositionCountry: LinearLayout =
            view.findViewById(R.id.lnrFourthPositionCountry)
        private val lnrThirdPositionCountry: LinearLayout =
            view.findViewById(R.id.lnrThirdPositionCountry)
        private val lnrSecondPositionCountry: LinearLayout =
            view.findViewById(R.id.lnrSecondPositionCountry)
        private val lnrFirstPositionCountry: LinearLayout =
            view.findViewById(R.id.lnrFirstPositionCountry)

        fun bind(isItem: List<CountryItem>) {
            if (isFlagLoadPosition == 0) {
                isFlagLoadPosition++
                lnrFirstPositionCountry.setBackgroundResource(isItem[0].iconResource)
                lnrSecondPositionCountry.setBackgroundResource(isItem[1].iconResource)
                lnrThirdPositionCountry.setBackgroundResource(isItem[2].iconResource)
                lnrFourthPositionCountry.setBackgroundResource(isItem[3].iconResource)
                lnrFifthPositionCountry.setBackgroundResource(isItem[4].iconResource)
                lnrSixthPositionCountry.setBackgroundResource(isItem[5].iconResource)

            }
            else if (isFlagLoadPosition == 1) {
                isFlagLoadPosition--
                lnrFirstPositionCountry.setBackgroundResource(isItem[6].iconResource)
                lnrSecondPositionCountry.setBackgroundResource(isItem[7].iconResource)
                lnrThirdPositionCountry.setBackgroundResource(isItem[8].iconResource)
                lnrFourthPositionCountry.setBackgroundResource(isItem[9].iconResource)
                lnrFifthPositionCountry.setBackgroundResource(isItem[10].iconResource)
                lnrSixthPositionCountry.setBackgroundResource(isItem[11].iconResource)
            }
        }
    }
}


