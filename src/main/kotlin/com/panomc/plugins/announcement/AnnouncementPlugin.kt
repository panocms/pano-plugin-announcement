package com.panomc.plugins.announcement

import com.panomc.platform.api.PanoPlugin

class AnnouncementPlugin : PanoPlugin() {
    companion object {
        internal lateinit var INSTANCE: AnnouncementPlugin
    }

    override suspend fun onLoad() {
        INSTANCE = this
    }

    override suspend fun onEnable() {
        logger.info("Started!")
    }

    override suspend fun onDisable() {
    }
}

