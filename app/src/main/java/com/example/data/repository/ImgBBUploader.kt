package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

object ImgBBUploader {
    private const val API_KEY = "4ac5668ba66709b8e8c1c641a57dcca9"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun uploadImageUri(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.use { it.readBytes() } ?: throw Exception("Cannot read image")

        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", base64)
            .build()

        val request = Request.Builder()
            .url("https://api.imgbb.com/1/upload?key=$API_KEY")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response from image server")

        val json = JSONObject(responseBody)
        if (json.optBoolean("success")) {
            json.getJSONObject("data").getString("url")
        } else {
            val errorMsg = json.optJSONObject("error")?.optString("message") ?: "Image upload failed"
            throw Exception(errorMsg)
        }
    }
}
