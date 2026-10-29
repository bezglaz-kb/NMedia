package ru.netology.nmedia.fragment

import android.content.Intent
import androidx.core.net.toUri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentPostBinding
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

                like.text = formatCount(post.likes)
                //share.text = formatCount(post.shares)

                like.isChecked = post.likedByMe

//                if (!post.video.isNullOrBlank()) {
//                    videoGroup.visibility = View.VISIBLE
//                    val playVideoAction = View.OnClickListener {
//                        val intent = Intent(Intent.ACTION_VIEW, post.video.toUri())
//                        startActivity(intent)
//                    }
//                    videoWallpaper.setOnClickListener(playVideoAction)
//                    videoPlayButton.setOnClickListener(playVideoAction)
//                } else {
//                    videoGroup.visibility = View.GONE
//                }

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