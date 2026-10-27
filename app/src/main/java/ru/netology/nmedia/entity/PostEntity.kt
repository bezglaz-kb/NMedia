package ru.netology.nmedia.entity

import android.annotation.SuppressLint
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post
import java.time.Instant

@Entity()
data class PostEntity(
    @ColumnInfo()
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val shares: Int = 0,
    val views: Int = 0,
    val video: String? = null
) {

    @SuppressLint("NewApi")
    fun toPost(): Post = Post(
        id = this.id,
        author = this.author,
        content = this.content,
        published = java.time.Instant.ofEpochMilli(published),
        likedByMe = this.likedByMe,
        likes = this.likes,
//        shares = this.shares,
//        views = this.views,
//        video = this.video
    )

    companion object {
        @SuppressLint("NewApi")
        fun fromPost(post: Post): PostEntity = with(post) {
            PostEntity(
                id = post.id,
                author = post.author,
                content = post.content,
                published = post.published.toEpochMilli(),
                likedByMe = post.likedByMe,
                likes = post.likes,
//                shares = post.shares,
//                views = post.views,
//                video = post.video
            )
        }
    }
}