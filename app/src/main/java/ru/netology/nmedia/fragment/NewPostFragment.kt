package ru.netology.nmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel
import kotlin.getValue

class NewPostFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels()

    val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            if (result.resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(
                    requireContext(),
                    R.string.image_picker_error, Toast.LENGTH_LONG
                ).show()
            } else if (uri != null) {
                viewModel.changePhoto(uri, uri.toFile())
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(layoutInflater)

        binding.edit.setText(arguments?.contentArg)

        viewModel.photo.observe(viewLifecycleOwner) { photo ->
            if (photo != null) {
                binding.previewContainer.isVisible = true
                binding.preview.setImageURI(photo.uri)
            } else {
                binding.previewContainer.isVisible = false
            }
        }

        binding.removePhoto.setOnClickListener {
            viewModel.removePhoto()
        }

        AndroidUtils.showKeyboard(binding.edit)

        requireActivity().addMenuProvider(
            provider = object : MenuProvider {
                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater
                ) {
                    menuInflater.inflate(R.menu.new_post_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.save -> {
                            viewModel.changeContent(binding.edit.text.toString())
                            viewModel.save()
                            AndroidUtils.hideKeyboard(requireView())
                            true
                        }

                        else -> false
                    }
            },
            viewLifecycleOwner,
        )

//        requireActivity().addMenuProvider(
//            object : MenuProvider {
//                override fun onCreateMenu(
//                    menu: Menu,
//                    menuInflater: MenuInflater
//                ) {
//                    menuInflater.inflate(R.menu.new_post_menu, menu)
//                }
//
//                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
//                    android.util.Log.d("DEBUG_SAVE", "1. Клик по меню, id = ${menuItem.itemId}")
//                    return when (menuItem.itemId) {
//                        R.id.save -> {
//                            android.util.Log.d("DEBUG_SAVE", "2. Вызываем save(), фото = ${viewModel.photo.value}")
//                            viewModel.changeContent(binding.edit.text.toString())
//                            viewModel.save()
//                            AndroidUtils.hideKeyboard(requireView())
//                            true
//                        }
//                        else -> false
//                    }
//                }
//            },
//            viewLifecycleOwner,
//            androidx.lifecycle.Lifecycle.State.RESUMED
//        )

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .maxResultSize(MAX_SIZE_PX, MAX_SIZE_PX)
                .createIntent(imagePickerLauncher::launch)
        }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .maxResultSize(MAX_SIZE_PX, MAX_SIZE_PX)
                .createIntent(imagePickerLauncher::launch)
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            AndroidUtils.hideKeyboard(requireView())
            findNavController().navigateUp()
        }
        return binding.root
    }

    companion object {
        var Bundle.contentArg by StringArg
        private const val MAX_SIZE_PX = 2048
    }

}

