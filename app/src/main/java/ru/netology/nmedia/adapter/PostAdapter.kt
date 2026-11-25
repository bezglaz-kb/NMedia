package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.formatCount

interface PostListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onRemove(post: Post)
    fun onEdit(post: Post)
    fun onPlayVideo(post: Post) {}
    fun onPostClick (post: Post)
    fun onAttachmentClick(attachment: Attachment) {}
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
                published.text = post.published.toString()
                content.text = post.content
                val BASE_URL = "http://10.0.2.2:9999"
                val avatarUrl = post.authorAvatar?.let { filename ->
                    "$BASE_URL/avatars/$filename"
                }
                binding.avatarImageView.setImageResource(0)
                Glide.with(itemView.context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_loading_avatar)
                    .error(R.drawable.ic_error_avatar)
                    .circleCrop()
                    .timeout(15_000)
                    .into(binding.avatarImageView)

                val postAttachment = post.attachment
                if (postAttachment != null && postAttachment.type == AttachmentType.IMAGE) {
                    attachment.isVisible = true
                    Glide.with(itemView.context)
                        .load("$BASE_URL/media/${postAttachment.url}")
                        .timeout(15_000)
                        .into(attachment)

                    attachment.setOnClickListener {
                        listener.onAttachmentClick(postAttachment)
                    }
                } else {
                    attachment.isVisible = false
                    attachment.setImageResource(0)
                    attachment.setOnClickListener(null)
                }



                like.text = formatCount(post.likes)
                like.isChecked = post.likedByMe

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
                postCard.setOnClickListener {
                    listener.onPostClick(post)
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
