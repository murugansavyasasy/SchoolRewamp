package com.vs.schoolmessenger.Dashboard.Settings.Faq.Model

data class FaqItem (
    val id: String,
    val question: String,
    val answer: List<String>
)