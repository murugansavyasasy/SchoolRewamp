package com.vs.schoolmessenger.Parent.QuizExam

interface QuizUpcomingListener {
    fun onItemClick(data: QuizUpcomingData, holder: QuizUpcomingAdapter.DataViewHolder)
}