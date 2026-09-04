package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Adapter

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model.getCoScholasticDataValues
import com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.SpinnerMarkUploadAdapter

class CoScholasticListAdapter(
    private val coScholastic: List<getCoScholasticDataValues>,
    private var isEntryType: Boolean,
    private val context: Context,
) : RecyclerView.Adapter<CoScholasticListAdapter.SubjectViewHolder>() {

    companion object {
        private const val TAG = "CoScholasticAdapter"
        private const val DEFAULT_ITEM =
            "\uD83D\uDCC4\u00A0\u00A0COLUMNS FROM UPLOADED IMAGE"
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SubjectViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.map_co_scholastic_item,
                parent,
                false
            )

        return SubjectViewHolder(view)
    }

    override fun getItemCount(): Int {
        return coScholastic.size
    }

    override fun onBindViewHolder(
        holder: SubjectViewHolder,
        position: Int
    ) {
        holder.bind(coScholastic[position])
    }

    fun getFinalList(): List<getCoScholasticDataValues> = coScholastic


    inner class SubjectViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val cardSubject: CardView =
            itemView.findViewById(R.id.cardSubject)

        private val lnrEntireHeader: LinearLayout =
            itemView.findViewById(R.id.lnrEntireHeader)

        private val headerRow: RelativeLayout =
            itemView.findViewById(R.id.headerRow)

        private val imgCheck: ImageView =
            itemView.findViewById(R.id.imgCheck)

        private val subjectName: TextView =
            itemView.findViewById(R.id.subjectName)

        private val lblClear: ImageView =
            itemView.findViewById(R.id.lblClear)

        private val lnrFlexContainer: LinearLayout =
            itemView.findViewById(R.id.lnrFlexContainer)

        private val spinnerContainer: RelativeLayout =
            itemView.findViewById(R.id.spinnerContainer)

        private val isSpinnerColumn: Spinner =
            itemView.findViewById(R.id.isSpinnerColumn)

        private val lblHint: TextView =
            itemView.findViewById(R.id.lblHint)


        fun bind(item: getCoScholasticDataValues) {

            subjectName.text =
                "${item.name ?: ""} (Max: ${item.type ?: ""} marks)"

            if (isEntryType) {

                bindEntryType(item)

            } else {

                bindManualType(item)

            }
        }

        private fun bindEntryType(
            item: getCoScholasticDataValues
        ) {


            lnrFlexContainer.visibility =
                View.GONE

            spinnerContainer.visibility =
                View.GONE

            lblHint.visibility =
                View.GONE

            lblClear.visibility =
                View.GONE


            val hasSelection =
                !item.selectedValue.isNullOrEmpty()

            item.isSelected =
                hasSelection


            setCheckIcon(
                item.isSelected
            )

            applyItemColor(item)


            subjectName.setOnClickListener {


                if (item.isSelected) {
                    return@setOnClickListener
                }

                openSpinner()
            }

            imgCheck.setOnClickListener {

                if (item.isSelected) {
                    return@setOnClickListener
                }

                openSpinner()
            }


            val defaultItems =
                listOf(DEFAULT_ITEM)

            val fullList =
                defaultItems + item.activities


            val spinnerAdapter =
                SpinnerMarkUploadAdapter(
                    context,
                    fullList
                )

            isSpinnerColumn.adapter =
                spinnerAdapter


            /*
             * Restore previous spinner selection.
             */
            if (!item.selectedValue.isNullOrEmpty()) {

                val selectedPosition =
                    fullList.indexOf(item.selectedValue)

                if (selectedPosition >= 0) {

                    spinnerAdapter.selectedPosition =
                        selectedPosition

                    isSpinnerColumn.setSelection(
                        selectedPosition,
                        false
                    )
                }

            } else {

                spinnerAdapter.selectedPosition =
                    -1

                isSpinnerColumn.setSelection(
                    0,
                    false
                )
            }



            lblClear.visibility =
                if (hasSelection) {
                    View.VISIBLE
                } else {
                    View.GONE
                }


            lblClear.setOnClickListener {

                clearSelection(
                    item,
                    spinnerAdapter
                )
            }


            if (hasSelection) {

                lnrFlexContainer.visibility =
                    View.VISIBLE

                spinnerContainer.visibility =
                    View.GONE

                lblHint.visibility =
                    View.VISIBLE

                lblClear.visibility =
                    View.VISIBLE

                setMappedHint(
                    lblHint,
                    item.selectedValue
                )

            } else {

                lnrFlexContainer.visibility =
                    View.GONE

                spinnerContainer.visibility =
                    View.GONE

                lblHint.visibility =
                    View.GONE

                lblClear.visibility =
                    View.GONE
            }



            isSpinnerColumn.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {

                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {

                        if (position == 0) {

                            isSpinnerColumn.setSelection(
                                if (
                                    spinnerAdapter.selectedPosition == -1
                                ) {
                                    0
                                } else {
                                    spinnerAdapter.selectedPosition
                                },
                                false
                            )

                            return
                        }



                        spinnerAdapter.selectedPosition =
                            position


                        item.selectedValue =
                            fullList[position]

                        item.isSelected =
                            true


                        spinnerAdapter.notifyDataSetChanged()

                        spinnerContainer.visibility =
                            View.GONE


                        lnrFlexContainer.visibility =
                            View.VISIBLE

                        lblHint.visibility =
                            View.VISIBLE

                        setMappedHint(
                            lblHint,
                            item.selectedValue
                        )


                        lblClear.visibility =
                            View.VISIBLE


                        setCheckIcon(true)

                        applyItemColor(item)

                    }


                    override fun onNothingSelected(
                        parent: AdapterView<*>
                    ) {
                    }
                }
        }


        private fun openSpinner() {

            lblHint.visibility =
                View.GONE

            lnrFlexContainer.visibility =
                View.VISIBLE

            spinnerContainer.visibility =
                View.VISIBLE



            isSpinnerColumn.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {

                    override fun onGlobalLayout() {

                        isSpinnerColumn.viewTreeObserver
                            .removeOnGlobalLayoutListener(this)

                        isSpinnerColumn.performClick()
                    }
                }
            )
        }


        private fun clearSelection(
            item: getCoScholasticDataValues,
            spinnerAdapter: SpinnerMarkUploadAdapter
        ) {

            item.selectedValue =
                null

            item.isSelected =
                false


            spinnerAdapter.selectedPosition =
                -1

            spinnerAdapter.notifyDataSetChanged()

            isSpinnerColumn.setSelection(
                0,
                false
            )


            lblClear.visibility =
                View.GONE

            lblHint.visibility =
                View.GONE


            spinnerContainer.visibility =
                View.GONE


            lnrFlexContainer.visibility =
                View.GONE


            setCheckIcon(false)

            applyItemColor(item)

        }

        private fun bindManualType(
            item: getCoScholasticDataValues
        ) {

            lnrFlexContainer.visibility =
                View.GONE

            spinnerContainer.visibility =
                View.GONE

            lblHint.visibility =
                View.GONE

            lblClear.visibility =
                View.GONE



            setCheckIcon(
                item.isSelected
            )


            applyItemColor(item)


            subjectName.setOnClickListener {

                item.isSelected =
                    !item.isSelected

                item.selectedValue =
                    null

                setCheckIcon(
                    item.isSelected
                )

                applyItemColor(item)

            }


            imgCheck.setOnClickListener {

                item.isSelected =
                    !item.isSelected

                item.selectedValue =
                    null

                setCheckIcon(
                    item.isSelected
                )

                applyItemColor(item)

            }
        }


        private fun setCheckIcon(
            selected: Boolean
        ) {

            imgCheck.setImageResource(
                if (selected) {
                    R.drawable.ic_checkbox_checked2
                } else {
                    R.drawable.ic_checkbox_unchecked2
                }
            )

            imgCheck.clearColorFilter()
        }


        private fun applyItemColor(
            item: getCoScholasticDataValues
        ) {

            val bg =
                lnrEntireHeader.background
                        as? GradientDrawable
                    ?: return


            bg.mutate()


            if (item.isSelected) {


                bg.setStroke(
                    context.dp(2),
                    ContextCompat.getColor(
                        context,
                        R.color.dark_green_3
                    )
                )

                bg.setColor(
                    ContextCompat.getColor(
                        context,
                        R.color.light_pale_green_1
                    )
                )


                cardSubject.cardElevation =
                    0f

            } else {


                bg.setStroke(
                    context.dp(2),
                    ContextCompat.getColor(
                        context,
                        android.R.color.white
                    )
                )

                bg.setColor(
                    ContextCompat.getColor(
                        context,
                        android.R.color.white
                    )
                )

                cardSubject.cardElevation =
                    context.dp(4).toFloat()
            }
        }


        private fun setMappedHint(
            textView: TextView,
            selected: String?
        ) {

            val prefix =
                "Mapped to: "

            val value =
                selected ?: ""

            textView.text =
                "$prefix$value"
        }
    }


    private fun Context.dp(
        value: Int
    ): Int {

        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}
