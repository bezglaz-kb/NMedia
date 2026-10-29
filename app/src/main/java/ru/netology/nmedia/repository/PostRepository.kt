package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    interface Callback<T> {
        fun onSuccess(data: T)
        fun onError(e: Exception)
    }
    fun getAllAsync(callback: Callback<List<Post>>)
    fun likeByIdAsync(id: Long, likedByMe: Boolean, callback: Callback<Post>)
    fun removeByIdAsync(id: Long, callback: Callback<Unit>)
    fun saveAsync(post: Post, callback: Callback<Post>)
    fun shareById(id: Long)
}