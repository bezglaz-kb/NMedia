package ru.netology.nmedia.repository


import android.annotation.SuppressLint
import retrofit2.Call
import retrofit2.Callback
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post
import java.io.IOException

class PostRepositoryRoomImpl : PostRepository {
    @SuppressLint("NewApi")

    override fun getAllAsync(callback: PostRepository.Callback<List<Post>>) {
        PostApi.service.getAll().enqueue(object : Callback<List<Post>> {
            override fun onFailure(call: retrofit2.Call<List<Post>>, t: Throwable) {
                callback.onError(RuntimeException(t))
            }

            override fun onResponse(call: retrofit2.Call<List<Post>>, response: retrofit2.Response<List<Post>>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        callback.onSuccess(body)
                    } else {
                        callback.onError(RuntimeException("Body is empty"))
                    }
                } else {
                    callback.onError(RuntimeException("Ошибка сервера: ${response.code()}"))
                }
            }
        })
    }

    override fun saveAsync(post: Post, callback: PostRepository.Callback<Post>) {
        PostApi.service.save(post).enqueue(object : Callback<Post> {
            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onError(IOException(t))
            }

            override fun onResponse(call: retrofit2.Call<Post>, response: retrofit2.Response<Post>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        callback.onSuccess(body)
                    } else {
                        callback.onError(RuntimeException("Body is empty"))
                    }
                } else {
                    callback.onError(RuntimeException("Ошибка сервера при сохранении: ${response.code()}"))
                }
            }
        })
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.Callback<Unit>) {
        PostApi.service.deleteById(id).enqueue(object : Callback<Unit> {

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                callback.onError(IOException(t))
            }

            override fun onResponse(call: retrofit2.Call<Unit>, response: retrofit2.Response<Unit>) {
                if (response.isSuccessful) {
                    callback.onSuccess(Unit)
                } else {
                    callback.onError(RuntimeException("Ошибка сервера при удалении: ${response.code()}"))
                }
            }
        })
    }

    override fun likeByIdAsync(id: Long, likedByMe: Boolean, callback: PostRepository.Callback<Post>) {
        val call = if (likedByMe) {
            PostApi.service.unlikeById(id)
        } else {
            PostApi.service.likeById(id)
        }

        call.enqueue(object : Callback<Post> {
            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onError(IOException(t))
            }

            override fun onResponse(call: retrofit2.Call<Post>, response: retrofit2.Response<Post>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        callback.onSuccess(body) // Успех 2xx — отдаем обновленный пост дальше
                    } else {
                        callback.onError(RuntimeException("Тело ответа пустое"))
                    }
                } else {
                    callback.onError(RuntimeException("Ошибка при обработке лайка: ${response.code()}"))
                }
            }
        })
    }



    override fun shareById(id: Long) {
        // TODO
    }
}
