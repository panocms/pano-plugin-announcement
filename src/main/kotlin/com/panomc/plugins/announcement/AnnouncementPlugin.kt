package com.panomc.plugins.announcement

import com.panomc.platform.api.PanoPlugin

class AnnouncementPlugin : PanoPlugin() {
    override suspend fun onStart() {
        logger.info("Started!")
    }
}

