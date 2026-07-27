package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.formatCount

class PostAdapter(
    private val likeClickListener: (Post) -> Unit,
    private val shareClickListener: (Post) -> Unit,
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffItemCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostViewHolder = PostViewHolder(
        CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        likeClickListener, shareClickListener
    )

    override fun onBindViewHolder(
        holder: PostViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    class PostViewHolder(
        private val binding: CardPostBinding,
        private val likeClickListener: (Post) -> Unit,
        private val shareClickListener: (Post) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
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
                    likeClickListener(post)
                }
                share.setOnClickListener {
                    shareClickListener(post)
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
