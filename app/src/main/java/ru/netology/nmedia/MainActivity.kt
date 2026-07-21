package ru.netology.nmedia

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post

class MainActivity : AppCompatActivity() {
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
        val post = Post(
            id = 1,
            author = "Нетология. Университет интернет-профессий будущего",
            published = "21 мая в 18:36",
            content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
            likes = 999,
            likedByMe = false,
            shares = 0,
            views = 9_150_000
        )
        with(binding) {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            avatar.setImageResource(R.drawable.post_avatar_drawable)
            countLikes.text = formatCount(post.likes)
            countShares.text = formatCount(post.shares)
            countViews.text = formatCount(post.views)
            like.setImageResource(if (post.likedByMe) R.drawable.ic_liked_24 else R.drawable.ic_like_24)
            like.setOnClickListener {
                if (post.likedByMe) post.likes -- else  post.likes ++
                post.likedByMe = !post.likedByMe
                like.setImageResource(if (post.likedByMe) R.drawable.ic_liked_24 else R.drawable.ic_like_24)
                countLikes.text = formatCount(post.likes)
            }
            share.setOnClickListener {
                post.shares ++
                countShares.text = formatCount(post.shares)
            }
            avatar.setOnClickListener {
                println("Сработал avatar.setOnClickListener")
            }
            root.setOnClickListener {
                println("Сработал root.setOnClickListener")
            }
        }
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