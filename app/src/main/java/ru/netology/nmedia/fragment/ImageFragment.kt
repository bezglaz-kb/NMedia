package ru.netology.nmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import ru.netology.nmedia.databinding.FragmentImageBinding
import ru.netology.nmedia.util.StringArg

class ImageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentImageBinding.inflate(inflater, container, false)

        val baseUrl = "http://10.0.2.2:9999"
        arguments?.imageUrlArg?.let { filename ->
            Glide.with(binding.image)
                .load("$baseUrl/media/$filename")
                .timeout(15_000)
                .into(binding.image)
        }

        return binding.root
    }

    companion object {
        var Bundle.imageUrlArg by StringArg
    }
}