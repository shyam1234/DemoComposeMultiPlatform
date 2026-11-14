package com.ott.platform.feature.player.impl

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val playerFeatureModule = module {
    viewModel { (contentId: String) ->
        PlayerViewModel(
            contentId = contentId,
            getChannelDetails = get(),
            playerController = get(),
        )
    }
}
