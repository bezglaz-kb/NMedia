package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.formatCount

interface PostListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onRemove(post: Post)
    fun onEdit(post: Post)
    fun onPlayVideo(post: Post) {}
}

class PostAdapter(
    private val listener: PostListener,
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffItemCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostViewHolder = PostViewHolder(
        CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        listener
    )

    override fun onBindViewHolder(
        holder: PostViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    class PostViewHolder(
        private val binding: CardPostBinding,
        private val listener: PostListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            with(binding) {
                author.text = post.author
                published.text = post.published
                content.text = post.content
                avatar.setImageResource(R.drawable.post_avatar_drawable)
                like.text = formatCount(post.likes)
                share.text = formatCount(post.shares)
                countViews.text = formatCount(post.views)

                if (!post.video.isNullOrBlank()) {
                    videoGroup.visibility = View.VISIBLE

                    videoWallpaper.setOnClickListener {
                        listener.onPlayVideo(post)
                    }
                    videoPlayButton.setOnClickListener {
                        listener.onPlayVideo(post)
                    }
                } else {
                    videoGroup.visibility = View.GONE
                }

                like.isChecked = post.likedByMe

                //like.setImageResource(if (post.likedByMe) R.drawable.ic_liked_24 else R.drawable.ic_like_24)

                menu.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.post_menu)

                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.remove -> {
                                    listener.onRemove(post)
                                    true
                                }

                                R.id.edit -> {
                                    listener.onEdit(post)
                                    true
                                }

                                else -> false
                            }
                        }

                        show()
                    }
                }

                like.setOnClickListener {
                    listener.onLike(post)
                }
                share.setOnClickListener {
                    listener.onShare(post)
                }
            }
        }
    }

    class PostDiffItemCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(
            oldItem: Post,
            newItem: Post
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Post,
            newItem: Post
        ): Boolean = oldItem == newItem

        override fun getChangePayload(oldItem: Post, newItem: Post): Any = Unit
    }
}
