package ru.netology.nmedia.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityNewPostBinding

class NewPostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val startingText = intent.getStringExtra(KEY_POST_TEXT)
        if (startingText != null) {
            binding.edit.setText(startingText)
        }

        binding.ok.setOnClickListener {
            val text = binding.edit.text.toString()
            if (text.isBlank()) {
                setResult(RESULT_CANCELED)
            } else {
                setResult(RESULT_OK, Intent().apply { putExtra(KEY_POST_TEXT, text) })
            }
            finish()
        }
    }

    companion object {
        val KEY_POST_TEXT = "post_text"
    }
}

object NewPostContract : ActivityResultContract<String?, String?>() {
    override fun createIntent(context: Context, input: String?): Intent {
        return Intent(context, NewPostActivity::class.java).apply {
            putExtra(NewPostActivity.KEY_POST_TEXT, input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        return if (resultCode == Activity.RESULT_OK) {
            intent?.getStringExtra(NewPostActivity.KEY_POST_TEXT)
        } else {
            null
        }
    }
}