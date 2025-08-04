package com.vs.schoolmessenger.Dashboard.School

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ScrollItem

class AutoScrollAdapterWithDots(
    private val items: List<ScrollItem>,
    private val onPositionChanged: (Int) -> Unit
) : RecyclerView.Adapter<AutoScrollAdapterWithDots.ViewHolder>() {

    private var onItemClickListener: ((ScrollItem, Int) -> Unit)? = null
    private var lastNotifiedPosition = -1

    fun setOnItemClickListener(listener: (ScrollItem, Int) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.resent_using_app_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (items.isEmpty()) return

        val actualPosition = position % items.size
        val item = items[actualPosition]

        holder.bind(item, actualPosition)

        if (actualPosition != lastNotifiedPosition) {
            onPositionChanged(actualPosition)
            lastNotifiedPosition = actualPosition
        }

        holder.itemView.setOnClickListener {
            it.bounceAnimation()
            onItemClickListener?.invoke(item, actualPosition)
        }
    }

    override fun getItemCount(): Int {
        return if (items.isNotEmpty()) items.size * 10000 else 0
    }

    fun getMiddlePosition(): Int {
        return if (items.isNotEmpty()) (itemCount / 2) - ((itemCount / 2) % items.size) else 0
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        private val itemText: TextView = itemView.findViewById(R.id.itemText)

        fun bind(item: ScrollItem, position: Int) {
            itemImage.setImageResource(item.imageRes)
            itemText.text = item.text

            try {
                val color = Color.parseColor(item.backgroundColor)
                val drawable = GradientDrawable()
                drawable.shape = GradientDrawable.RECTANGLE
                drawable.setColor(color)
                drawable.cornerRadius = 24f
                itemImage.background = drawable
            } catch (e: Exception) {
            }
        }
    }
}

fun View.bounceAnimation() {
    if (animation != null) return

    animate()
        .scaleX(0.95f)
        .scaleY(0.95f)
        .setDuration(100)
        .withEndAction {
            animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(100)
                .start()
        }
        .start()
}
