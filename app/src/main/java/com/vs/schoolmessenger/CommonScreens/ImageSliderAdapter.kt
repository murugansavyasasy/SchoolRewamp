package com.vs.schoolmessenger.CommonScreens
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)

        progressBar.visibility = View.VISIBLE // Show ProgressBar before loading

        Glide.with(view.context)
            .load(imageUrls[position])
            .listener(object : RequestListener<Drawable> {


                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE // Hide ProgressBar on success
                    return false // Allow Glide to display the image
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE // Hide ProgressBar on failure
                    return false // Allow Glide to handle the error
                }
            })
            .into(imageView)


        imageView.setOnClickListener {
            container.context.startActivity(
                Intent(
                    container.context,
                    ImageViewActivity::class.java
                )
            )

        }

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }
}