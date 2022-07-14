package com.aladakatti.simple_quote.ui.quotes

import androidx.lifecycle.ViewModel
import com.aladakatti.simple_quote.data.Quote
import com.aladakatti.simple_quote.data.QuoteRepository

class QuotesViewModel(private val quoteRepository: QuoteRepository)
    : ViewModel(){

        fun addQuote(quote: Quote) = quoteRepository.addQuote(quote)

        fun getQuotes() = quoteRepository.getQuote()

}