package com.ott.platform.feature.details.impl

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val detailsModule = module {
    viewModel { (contentId: String) ->
        DetailsViewModel(
            contentId = contentId,
            getChannelDetails = get(),
            toggleWatchlist = get(),
            watchlistRepository = get(),
        )
    }
}
