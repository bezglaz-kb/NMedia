package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.viewmodel.PostViewMode

class MainActivity : AppCompatActivity() {
    private val viewModel: PostViewMode by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left + v.paddingLeft, systemBars.top + v.paddingTop, systemBars.right + v.paddingRight, systemBars.bottom + v.paddingBottom)
            insets
        }
        viewModel.data.observe(this) {post ->
        with(binding) {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            avatar.setImageResource(R.drawable.post_avatar_drawable)
            countLikes.text = formatCount(post.likes)
            countShares.text = formatCount(post.shares)
            countViews.text = formatCount(post.views)
            like.setImageResource(if (post.likedByMe) R.drawable.ic_liked_24 else R.drawable.ic_like_24)
        }
    }

        binding.like.setOnClickListener {
           viewModel.like()
        }
        binding.share.setOnClickListener {
            viewModel.share()
        }
}

fun formatCount(count: Int): String {
    return when {
        count < 1_000 -> count.toString()
        count < 10_000 -> {
            val thousands = count / 1_000
            val hundreds = (count % 1_000) / 100
            if (hundreds == 0) "${thousands}K" else "${thousands}.${hundreds}K"
        }
        count < 1_000_000 -> {
            "${count / 1_000}K"
        }
        else -> {
            val millions = count / 1_000_000
            val hundredsOfThousands = (count % 1_000_000) / 100_000
            if (hundredsOfThousands == 0) "${millions}M" else "${millions}.${hundredsOfThousands}M"
        }
    }
}
}