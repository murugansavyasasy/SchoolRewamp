package com.vs.schoolmessenger.Dashboard.Settings.Faq.Model

data class FrequentlyModelResponse (
    val status: Boolean,
    val message: String,
    val data: List<FaqItem>
)