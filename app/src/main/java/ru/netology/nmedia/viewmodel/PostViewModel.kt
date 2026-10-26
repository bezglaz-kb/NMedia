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
        _data.value = FeedModel(loading = true)

        repository.getAllAsync(object : PostRepository.Callback<List<Post>> {
            override fun onSuccess(data: List<Post>) {
                _data.postValue(FeedModel(posts = data, empty = data.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
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

        repository.likeByIdAsync(post.id, post.likedByMe, object : PostRepository.Callback<Post> {
            override fun onSuccess(data: Post) {
                val updatedPosts = _data.value?.posts?.map {
                    if (it.id == data.id) data else it
                }.orEmpty()
                _data.postValue(FeedModel(posts = updatedPosts, empty = updatedPosts.isEmpty()))
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
            }
        })
    }

    fun removeById(id: Long) {
        repository.removeByIdAsync(id, object : PostRepository.Callback<Unit> {
            override fun onSuccess(data: Unit) {
                val updatedPosts = _data.value?.posts?.filter { it.id != id }.orEmpty()
                _data.postValue(FeedModel(posts = updatedPosts, empty = updatedPosts.isEmpty()))
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
            }
        })
    }
    fun save(content: String) {
        val postToSave = edited.value ?: return
        val trimmed = content.trim()

        repository.saveAsync(postToSave.copy(content = trimmed), object : PostRepository.Callback<Post> {
            override fun onSuccess(data: Post) {
                load()
                _postCreated.postValue(Unit)
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
            }
        })
        edited.value = emptyPost
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun cancelEdit() {
        edited.value = emptyPost
    }

    fun shareById(id: Long) = repository.shareById(id)
}