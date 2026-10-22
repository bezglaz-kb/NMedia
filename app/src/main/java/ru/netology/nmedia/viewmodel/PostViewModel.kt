package ru.netology.nmedia.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryRoomImpl
import ru.netology.nmedia.util.SingleLiveEvent
import java.time.Instant
import kotlin.concurrent.thread

@SuppressLint("NewApi")
private val emptyPost = Post(
    id = 0L,
    author = "",
    content = "",
    published = Instant.now(),
    likedByMe = false,
    likes = 0
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryRoomImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(emptyPost)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        load()
    }

    fun load() {
        thread {
            _data.postValue(FeedModel(loading = true))

            val state = try {
                val posts = repository.getData()
                FeedModel(posts = posts, empty = posts.isEmpty())
            } catch (_: Exception) {
                FeedModel(error = true)
            }

            _data.postValue(state)
        }
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long) {
        val post = data.value?.posts?.find { it.id == id } ?: return

        thread {
            try {
                repository.likeById(post.id, post.likedByMe)
                load()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun shareById(id: Long) = repository.shareById(id)
    fun removeById(id: Long) {
        thread {
            try {
                repository.removeById(id)
                load()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun save(content: String) {
        val postToSave = edited.value ?: return
        val trimmed = content.trim()

        thread {
            try {
                repository.save(postToSave.copy(content = trimmed))
                load()
                _postCreated.postValue(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        edited.value = emptyPost
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun cancelEdit() {
        edited.value = emptyPost
    }
}