package com.vs.schoolmessenger.CommonScreens
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.R

class ImageSliderAdapter(
    private val context: Context,
    private val imageUrls: List<String>
) : PagerAdapter() {

    override fun getCount(): Int = imageUrls.size

    override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater = LayoutInflater.from(container.context)
        val view = layoutInflater.inflate(R.layout.image_slider_view_list, container, false)

        val imageView: ImageView = view.findViewById(R.id.sliderImage)
        Glide.with(view.context)
            .load(imageUrls[position])
            .into(imageView)

       imageView.setOnClickListener {
           container.context.startActivity(Intent(container.context, ImageViewActivity::class.java))

       }

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }
}