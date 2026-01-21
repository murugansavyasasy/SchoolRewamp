package com.vs.schoolmessenger.School.DailyCollection

import android.app.Dialog
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.vs.schoolmessenger.R

class DailyCollectionTourDialog(
    private val onFinish: () -> Unit
) : DialogFragment() {

    private var step = 0
    private val images = listOf(
        R.drawable.daily_collection_tour_1,
        R.drawable.daily_collection_tour_2
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.tour_xml)
        dialog.setCancelable(false)

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
