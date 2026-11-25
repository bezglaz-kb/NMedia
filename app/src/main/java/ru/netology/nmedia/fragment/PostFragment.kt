package ru.netology.nmedia.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.fragment.ImageFragment.Companion.imageUrlArg
import ru.netology.nmedia.fragment.NewPostFragment.Companion.contentArg
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.util.formatCount
import ru.netology.nmedia.viewmodel.PostViewModel

class PostFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostBinding.inflate(layoutInflater)
        val postId = arguments?.idArg?.toLongOrNull() ?: findNavController().navigateUp()
        val baseUrl = "http://10.0.2.2:9999"

        viewModel.data.observe(viewLifecycleOwner) { state ->
            val post = state.posts.find { it.id == postId }
            if (post == null) {
                findNavController().navigateUp()
                return@observe
            }

            with(binding.postCard) {
                author.text = post.author
                published.text = post.published.toString()
                content.text = post.content
                avatarImageView.setImageResource(R.drawable.post_avatar_drawable)

                val postAttachment = post.attachment
                if (postAttachment != null && postAttachment.type == AttachmentType.IMAGE) {
                    attachment.isVisible = true
                    Glide.with(this@PostFragment)
                        .load("$baseUrl/media/${postAttachment.url}")
                        .timeout(15_000)
                        .into(attachment)

                    attachment.setOnClickListener {
                        findNavController().navigate(
                            R.id.action_postFragment_to_imageFragment,
                            Bundle().apply {
                                imageUrlArg = postAttachment.url
                            }
                        )
                    }
                } else {
                    attachment.isVisible = false
                    attachment.setImageResource(0)
                    attachment.setOnClickListener(null)
                }

                like.text = formatCount(post.likes)
                like.isChecked = post.likedByMe

                like.setOnClickListener {
                    viewModel.likeById(post.id)
                }

                share.setOnClickListener {
                    viewModel.shareById(post.id)
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, post.content)
                        type = "text/plain"
                    }
                    val shareIntent =
                        Intent.createChooser(intent, getString(R.string.chooser_share_post))
                    startActivity(shareIntent)
                }
                menu.setOnClickListener { view ->
                    PopupMenu(view.context, view).apply {
                        inflate(R.menu.post_menu)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.remove -> {
                                    viewModel.removeById(post.id)
                                    true
                                }

                                R.id.edit -> {
                                    viewModel.edit(post)
                                    findNavController().navigate(
                                        R.id.action_postFragment_to_newPostFragment,
                                        Bundle().apply {
                                            contentArg = post.content
                                        }
                                    )
                                    true
                                }

                                else -> false
                            }
                        }
                    }.show()
                }
            }
        }

        return binding.root
    }

    companion object {
        var Bundle.idArg by StringArg
    }
}