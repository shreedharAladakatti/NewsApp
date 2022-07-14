package com.aladakatti.simple_quote.utilities

import com.aladakatti.simple_quote.data.FakeDatabase
import com.aladakatti.simple_quote.data.QuoteRepository
import com.aladakatti.simple_quote.ui.quotes.QuotesViewModelFactory

object InjectorUtils {

    fun provideQuotesViewModelFactory() : QuotesViewModelFactory {
        val quoteRepository = QuoteRepository.getInstance(FakeDatabase.getInstance().quoteDao)
        return QuotesViewModelFactory(quoteRepository)
    }

}
