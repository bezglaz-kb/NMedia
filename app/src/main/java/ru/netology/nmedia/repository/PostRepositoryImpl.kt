package ru.netology.nmedia.repository

import androidx.lifecycle.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.Dispatcher
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.File
import java.io.IOException
import kotlin.concurrent.thread

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {
    override val data = dao.getData()
        .map { posts -> posts.map { it.toPost() } }

    override suspend fun getAll() {
        val posts = PostApi.service.getAll()
        dao.insert(posts.map(PostEntity::fromPost))
    }

    override suspend fun save(post: Post, image: File?): Post {
        try {
            val media = image?.let {
                upload(it)
            }
            val postWithAttachment = media?.let {
                post.copy(attachment = Attachment(it.id, AttachmentType.IMAGE))
            } ?: post

            val savedPost = PostApi.service.save(postWithAttachment)
            dao.save(PostEntity.fromPost(savedPost))
            return savedPost
        } catch (e: IOException) {
            e.printStackTrace()
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    private suspend fun upload(file: File): Media =
        PostApi.service.upload(
            MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())
        )


    override suspend fun removeById(id: Long) {
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

    override fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000)
            val response = PostApi.service.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.map { PostEntity.fromPost(it, hidden = true) })
            emit(body.size)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun showAll() {
        dao.showAll()
    }
}
