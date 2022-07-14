package com.aladakatti.simple_quote.data

class QuoteRepository private constructor(val quoteDAO: FakeQuoteDAO){

    fun addQuote(quote: Quote){
        quoteDAO.addQuotes(quote)
    }

    fun getQuote() = quoteDAO.getQuotes()

    companion object {
        @Volatile
        private var instance: QuoteRepository ?= null

        fun getInstance(quoteDAO: FakeQuoteDAO) =
            instance ?: synchronized(this) {
                instance ?: QuoteRepository(quoteDAO).also { instance = it }
            }
    }

}