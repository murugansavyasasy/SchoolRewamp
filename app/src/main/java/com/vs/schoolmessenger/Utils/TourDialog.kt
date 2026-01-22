package com.vs.schoolmessenger.Utils

import android.app.Dialog
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.vs.schoolmessenger.R

class TourDialog (
    private val onFinish: () -> Unit
) : DialogFragment() {

    private var step = 0
    private lateinit var images: ArrayList<Int>

    companion object {
        private const val KEY_IMAGES = "key_images"

        fun newInstance(
            images: ArrayList<Int>,
            onFinish: () -> Unit
        ): TourDialog {
            val fragment = TourDialog(onFinish)
            fragment.arguments = Bundle().apply {
                putIntegerArrayList(KEY_IMAGES, images)
            }
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.tour_xml)
        dialog.setCancelable(false)

        images = requireArguments().getIntegerArrayList(KEY_IMAGES) ?: arrayListOf()

        val img = dialog.findViewById<ImageView>(R.id.imgTour)
        val skip = dialog.findViewById<TextView>(R.id.btnSkip)
        val next = dialog.findViewById<TextView>(R.id.btnNext)

        img.setImageResource(images[step])

        next.setOnClickListener {
            step++
            if (step < images.size) {
                img.setImageResource(images[step])
            } else {
                dismiss()
                onFinish()
            }
        }

        skip.setOnClickListener {
            dismiss()
            onFinish()
        }

        return dialog
    }
}
