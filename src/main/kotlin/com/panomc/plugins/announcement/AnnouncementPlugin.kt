package com.panomc.plugins.announcement

import com.panomc.platform.api.PanoPlugin

class AnnouncementPlugin : PanoPlugin() {
    override suspend fun onEnable() {
        logger.info("Started!")
    }

    override suspend fun onDisable() {
    }
}

