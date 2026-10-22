package ru.netology.nmedia.repository


import android.annotation.SuppressLint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.dto.Post
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

class PostRepositoryRoomImpl : PostRepository {
    @SuppressLint("NewApi")
    private companion object {
        const val BASE_URL = "http://10.0.2.2:9999"
        val jsonType = "application/json".toMediaType()
        val gson = com.google.gson.GsonBuilder()
            .registerTypeAdapter(java.time.Instant::class.java, com.google.gson.JsonSerializer<java.time.Instant> { src, _, _ ->
                com.google.gson.JsonPrimitive(src.toString())
            })
            .registerTypeAdapter(java.time.Instant::class.java, com.google.gson.JsonDeserializer<java.time.Instant> { json, _, _ ->
                java.time.Instant.parse(json.asString)
            })
            .create()
        val postsType: Type = object : TypeToken<List<Post>>() {}.type
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun getData(): List<Post> {
       val call: Call = client.newCall(
            Request.Builder()
                .url("$BASE_URL/api/posts")
                .build()
        )

        val response = call.execute()
        val responseText = response.body?.string()
        return gson.fromJson<List<Post>>(responseText, postsType)
    }

    override fun save(post: Post):Post {
        val call: Call = client.newCall(
            Request.Builder()
                .url("$BASE_URL/api/posts")
                .post(gson.toJson(post).toRequestBody(jsonType))
                .build()
        )

        val response = call.execute()
        val responseText = response.body?.string()
        return gson.fromJson(responseText, Post::class.java)
    }

    override fun likeById(id: Long, likedByMe: Boolean): Post {
        val requestBuilder = Request.Builder()
            .url("$BASE_URL/api/posts/$id/likes")

        val request = if (likedByMe) {
            requestBuilder.delete().build()
        } else {
            requestBuilder.post("".toRequestBody(null)).build()
        }

        val call = client.newCall(request)
        val response = call.execute()
        val responseText = response.body?.string() ?: ""
        return gson.fromJson(responseText, Post::class.java)
    }

    override fun shareById(id: Long) {
       // dao.shareById(id)
    }

    override fun removeById(id: Long) {
        val call: Call = client.newCall(
            Request.Builder()
                .url("$BASE_URL/api/posts/$id")
                .delete()
                .build()
        )

       call.execute()
    }
}
