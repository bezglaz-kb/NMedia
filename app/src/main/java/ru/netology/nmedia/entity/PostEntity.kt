package ru.netology.nmedia.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity()
data class PostEntity(
    @ColumnInfo()
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val shares: Int = 0,
    val views: Int = 0,
    val video: String? = null
) {

    fun toPost(): Post = Post(
        id = this.id,
        author = this.author,
        content = this.content,
        published = this.published,
        likedByMe = this.likedByMe,
        likes = this.likes,
        shares = this.shares,
        views = this.views,
        video = this.video
    )

    companion object {
        fun fromPost(post: Post): PostEntity = with(post) {
            PostEntity(
                id = post.id,
                author = post.author,
                content = post.content,
                published = post.published,
                likedByMe = post.likedByMe,
                likes = post.likes,
                shares = post.shares,
                views = post.views,
                video = post.video
            )
        }
    }
}