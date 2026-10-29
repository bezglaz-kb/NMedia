package ru.netology.nmedia.dto

import java.time.Instant

data class Post(
    val id: Long,
    val author: String,
    val authorAvatar: String?,
    val content: String,
    val published: Instant,
    val likedByMe: Boolean,
    val likes: Int = 0,
)