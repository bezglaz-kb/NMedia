package ru.netology.nmedia.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import java.time.Instant

@SuppressLint("NewApi")
private val emptyPost = Post(
    id = 0L,
    author = "",
    authorAvatar = "",
    content = "",
    published = Instant.now(),
    likedByMe = false,
    likes = 0
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImpl(
        dao = AppDb.getInstance(
            context = application
        ).postDao
    )

    val data: LiveData<FeedModel> = repository.data
        .map { FeedModel(posts = it, empty = it.isEmpty()) }
        .catch { it.printStackTrace() }
        .asLiveData(Dispatchers.Default)

    val newerCount = data.switchMap {
        repository.getNewer(it.posts.firstOrNull()?.id ?: 0)
            .catch { _state.postValue(FeedModelState(error = true)) }
            .asLiveData(Dispatchers.Default)
    }
    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state

    val edited = MutableLiveData(emptyPost)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _errorEvent = SingleLiveEvent<String>()
    val errorEvent: LiveData<String>
        get() = _errorEvent

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = FeedModelState(loading = true)

            try {
                repository.getAll()
                _state.value = FeedModelState()
            } catch (_: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = FeedModelState(refreshing = true)

            try {
                repository.getAll()
                _state.value = FeedModelState()
            } catch (_: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            edited.value?.let {
                try {
                    repository.save(it, photo.value?.file)
                    _postCreated.value = Unit
                } catch (_: Exception) {
                    _state.value = FeedModelState(error = true)
                }
            }
            edited.value = emptyPost
        }
    }

//    fun save() {
//        android.util.Log.d("DEBUG_SAVE", "3. ViewModel.save(), edited = ${edited.value}")
//        viewModelScope.launch {
//            edited.value?.let {
//                try {
//                    android.util.Log.d("DEBUG_SAVE", "4. Отправляем в репозиторий")
//                    repository.save(it, photo.value?.file)
//                    android.util.Log.d("DEBUG_SAVE", "5. Успешно сохранено!")
//                    _postCreated.value = Unit
//                    edited.value = emptyPost
//                    _photo.value = null
//                } catch (e: Exception) {
//                    android.util.Log.e("DEBUG_SAVE", "Ошибка при сохранении", e)
//                    _state.value = FeedModelState(error = true)
//                }
//            }
//        }
//    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.likeById(id)
            } catch (_: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

fun removeById(id: Long) {
    viewModelScope.launch {
        try {
            repository.removeById(id)
        } catch (_: Exception) {
            _state.value = FeedModelState(error = true)
        }
    }
}

    fun edit(post: Post) {
        edited.value = post
    }

    fun cancelEdit() {
        edited.value = emptyPost
    }

    fun shareById(id: Long) {
        viewModelScope.launch {
            repository.shareById(id)
        }
    }

    fun showNewPosts() = viewModelScope.launch {
        repository.showAll()
    }

    private val _photo = MutableLiveData<PhotoModel>(null)
    val photo: LiveData<PhotoModel?>
        get() = _photo
    fun changePhoto (uri: Uri, file: File) {
        _photo.value = PhotoModel(uri, file)

    }

    fun removePhoto() {
        _photo.value = null
    }
}