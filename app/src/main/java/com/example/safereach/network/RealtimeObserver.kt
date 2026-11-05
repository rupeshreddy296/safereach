package com.example.safereach.network

import android.util.Log
import okhttp3.*
import okio.BufferedSource
import java.io.IOException
import java.util.concurrent.TimeUnit

class RealtimeObserver(private val onEvent: (String, String) -> Unit) {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // infinite timeout for stream
        .build()

    private var call: Call? = null

    fun start(baseUrl: String) {
        val request = Request.Builder()
            .url("${baseUrl}api/events/stream.php")
            .header("Accept", "text/event-stream")
            .build()

        call = client.newCall(request)
        call?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("RealtimeObserver", "Stream failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val source = response.body?.source() ?: return
                try {
                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: break
                        if (line.startsWith("event: ")) {
                            val eventType = line.substring(7)
                            val dataLine = source.readUtf8Line() ?: ""
                            if (dataLine.startsWith("data: ")) {
                                val data = dataLine.substring(6)
                                onEvent(eventType, data)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("RealtimeObserver", "Error reading stream: ${e.message}")
                }
            }
        })
    }

    fun stop() {
        call?.cancel()
    }
}
