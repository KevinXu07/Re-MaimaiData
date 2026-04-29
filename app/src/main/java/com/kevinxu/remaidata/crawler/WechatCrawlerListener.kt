package com.kevinxu.remaidata.crawler

interface WechatCrawlerListener {
    fun onMessageReceived(logString: String)

    fun onStartAuth()

    fun onFinishUpdate()

    fun onError(e: Exception)
}