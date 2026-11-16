package ru.netology.nmedia.repository


import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import java.io.IOException

class PostRepositoryImpl(
    private val dao: PostDao,
) : PostRepository {
    override val data: LiveData<List<Post>> = dao.getData().map {
        it.map(PostEntity::toPost)
    }

    override suspend fun getAll() {
        val posts = PostApi.service.getAll()
        dao.insert(posts.map(PostEntity::fromPost))
    }

    override suspend fun save(post: Post): Post {
        val savedPost = PostApi.service.save(post)
        dao.save(PostEntity.fromPost(savedPost))
        return savedPost
    }

    override suspend fun removeById(id: Long) {
        // 1. Сохраняем копию поста на случай ошибки сети
        val backup = dao.getById(id)

        try {
            dao.removeById(id)
            PostApi.service.removeById(id)
        } catch (e: Exception) {
            backup?.let { dao.save(it) }
            throw e
        }
    }

    override suspend fun likeById(id: Long) {
        val post = dao.getById(id) ?: return
        val wasLiked = post.likedByMe

        try {
            dao.likeById(id)

            val response = if (wasLiked) {
                PostApi.service.dislikeById(id)
            } else {
                PostApi.service.likeById(id)
            }

            dao.save(PostEntity.fromPost(response))
        } catch (e: IOException) {
            dao.likeById(id)
            throw e
        }
    }

    override suspend fun shareById(id: Long) {
        dao.shareById(id)
    }
}
