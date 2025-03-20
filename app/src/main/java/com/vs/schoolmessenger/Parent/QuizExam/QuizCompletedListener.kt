package com.vs.schoolmessenger.Parent.QuizExam

interface QuizCompletedListener

{
    fun onItemClick(data: QuizCompletedData, holder: QuizCompletedAdapter.DataViewHolder)
}