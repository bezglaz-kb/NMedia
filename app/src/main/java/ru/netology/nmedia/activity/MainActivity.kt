package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.adapter.PostListener
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: PostViewModel by viewModels()
    val postContract = registerForActivityResult(NewPostContract) { result ->
        if (result == null) {
            viewModel.cancelEdit()
            return@registerForActivityResult
        }
        viewModel.save(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        val adapter = PostAdapter(
            object : PostListener {
                override fun onLike(post: Post) {
                    viewModel.likeById(post.id)
                }

                override fun onShare(post: Post) {
                    viewModel.shareById(post.id)
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, post.content)
                    }

                    val chooser =
                        Intent.createChooser(intent, getString(R.string.chooser_share_post))
                    startActivity(chooser)
                }

                override fun onRemove(post: Post) {
                    viewModel.removeById(post.id)
                }

                override fun onEdit(post: Post) {
                    viewModel.edit(post)
                    postContract.launch(post.content)
                }

                override fun onPlayVideo(post: Post) {
                    val videoUrl = post.video ?: return
                    val intent = Intent(Intent.ACTION_VIEW, videoUrl.toUri())
                    startActivity(intent)
                }
            }
        )

        binding.list.adapter = adapter

        viewModel.data.observe(this) { posts ->
            adapter.submitList(posts)
        }

        binding.add.setOnClickListener {
            postContract.launch(null)
        }
    }
}