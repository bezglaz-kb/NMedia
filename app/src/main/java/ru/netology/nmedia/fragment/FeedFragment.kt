package ru.netology.nmedia.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.adapter.PostListener
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.fragment.NewPostFragment.Companion.contentArg
import ru.netology.nmedia.fragment.PostFragment.Companion.idArg
import ru.netology.nmedia.viewmodel.PostViewModel

class FeedFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(layoutInflater)


        val viewModel: PostViewModel by activityViewModels()

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
                    findNavController().navigate(
                        R.id.action_feedFragment_to_newPostFragment,
                        Bundle().apply {
                            contentArg = post.content
                        }
                    )
                }

//                override fun onPlayVideo(post: Post) {
//                    val videoUrl = post.video ?: return
//                    val intent = Intent(Intent.ACTION_VIEW, videoUrl.toUri())
//                    startActivity(intent)
//                }

                override fun onPostClick(post: Post) {
                    findNavController().navigate(
                        R.id.action_feedFragment_to_postFragment,
                        Bundle().apply {
                            idArg = post.id.toString() // Передаем ID поста во фрагмент деталей
                        }
                    )
                }
            }
        )

        binding.list.adapter = adapter
        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
            binding.progress.isVisible = state.loading
            binding.emptyText.isVisible = state.empty
            binding.errorGroup.isVisible = state.error
        }

        binding.retry.setOnClickListener {
            viewModel.load()
        }

        binding.add.setOnClickListener {
            viewModel.cancelEdit()
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        return binding.root
    }
}