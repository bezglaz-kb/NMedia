package ru.netology.nmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel
import kotlin.getValue

class NewPostFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(layoutInflater)

        binding.edit.setText(arguments?.contentArg)

        AndroidUtils.showKeyboard(binding.edit)

        viewModel.postCreated.observe(viewLifecycleOwner){
            AndroidUtils.hideKeyboard(requireView())
            findNavController().navigateUp()
        }

        binding.ok.setOnClickListener {
            if (!binding.edit.text.isNullOrBlank()) {
                val content = binding.edit.text.toString()
                viewModel.changeContent(content)
                viewModel.save(content)
            }
        }

            return binding.root
        }
    companion object {
        var Bundle.contentArg by StringArg
    }

    }

