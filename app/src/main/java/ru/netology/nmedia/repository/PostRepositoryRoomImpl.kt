package ru.netology.nmedia.repository


import android.annotation.SuppressLint
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import ru.netology.nmedia.dto.Post
import java.io.IOException
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

class PostRepositoryRoomImpl : PostRepository {
    @SuppressLint("NewApi")
    private companion object {
        const val BASE_URL = "http://10.0.2.2:9999"
        val jsonType = "application/json".toMediaType()
        val gson = com.google.gson.GsonBuilder()
            .registerTypeAdapter(
                java.time.Instant::class.java,
                com.google.gson.JsonSerializer<java.time.Instant> { src, _, _ ->
                    com.google.gson.JsonPrimitive(src.toString())
                })
            .registerTypeAdapter(
                java.time.Instant::class.java,
                com.google.gson.JsonDeserializer<java.time.Instant> { json, _, _ ->
                    java.time.Instant.parse(json.asString)
                })
            .create()
        val postsType: Type = object : TypeToken<List<Post>>() {}.type
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun getAllAsync(callback: PostRepository.Callback<List<Post>>) {
        val request = Request.Builder()
            .url("$BASE_URL/api/posts")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException("Ошибка сервера: ${response.code}"))
                        return
                    }

                    val responseText = response.body?.string()
                    try {
                        val posts = gson.fromJson<List<Post>>(responseText, postsType)
                        callback.onSuccess(posts)
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
            }
        })
    }

    override fun saveAsync(post: Post, callback: PostRepository.Callback<Post>) {
        val request = Request.Builder()
            .url("$BASE_URL/api/posts")
            .post(gson.toJson(post).toRequestBody(jsonType))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException("Ошибка сервера при сохранении: ${response.code}"))
                        return
                    }
                    val responseText = response.body?.string()
                    try {
                        val savedPost = gson.fromJson(responseText, Post::class.java)
                        callback.onSuccess(savedPost)
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
            }
        })
    }

    override fun likeByIdAsync(id: Long, likedByMe: Boolean, callback: PostRepository.Callback<Post>) {
        val requestBuilder = Request.Builder()
            .url("$BASE_URL/api/posts/$id/likes")

        val request = if (likedByMe) {
            requestBuilder.delete().build()
        } else {
            requestBuilder.post("".toRequestBody(null)).build()
        }

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException("Ошибка сервера при обработке лайка: ${response.code}"))
                        return
                    }
                    val responseText = response.body?.string() ?: ""
                    try {
                        val likedPost = gson.fromJson(responseText, Post::class.java)
                        callback.onSuccess(likedPost)
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
            }
        })
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.Callback<Unit>) {
        val request = Request.Builder()
            .url("$BASE_URL/api/posts/$id")
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException("Ошибка сервера при удалении: ${response.code}"))
                        return
                    }
                    callback.onSuccess(Unit)
                }
            }
        })
    }

    override fun shareById(id: Long) {
        // TODO
    }
}
