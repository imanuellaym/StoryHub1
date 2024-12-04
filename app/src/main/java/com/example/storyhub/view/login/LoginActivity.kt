package com.example.storyhub.view.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.storyhub.data.model.UserModel
import com.example.storyhub.data.result.ResultState
import com.example.storyhub.databinding.ActivityLoginBinding
import com.example.storyhub.view.MainViewModel
import com.example.storyhub.view.ViewModelFactory
import com.example.storyhub.view.main.MainActivity

class LoginActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels { ViewModelFactory.getInstance(this) }
    private lateinit var binding: ActivityLoginBinding
    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        startAnimations()
        setupListeners()
    }

    private fun setupUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Gunakan WindowInsetsController untuk Android R ke atas
            window.insetsController?.hide(WindowInsets.Type.statusBars())
            window.insetsController?.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            // Untuk versi Android lebih lama
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun startAnimations() {
        val titleAnim = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 0f, 1f).apply {
            duration = 500
        }
        val subtitleAnim = ObjectAnimator.ofFloat(binding.subtitleTextView, View.ALPHA, 0f, 1f).apply {
            duration = 500
        }
        val emailAnim = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 0f, 1f).apply {
            duration = 500
        }
        val passwordAnim = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 0f, 1f).apply {
            duration = 500
        }
        val buttonAnim = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 0f, 1f).apply {
            duration = 500
        }

        AnimatorSet().apply {
            playSequentially(titleAnim, subtitleAnim, emailAnim, passwordAnim, buttonAnim)
            start()
        }
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            attemptLogin()
        }
    }

    private fun attemptLogin() {
        val email = binding.emailEditText.text?.toString().orEmpty()
        val password = binding.passwordEditText.text?.toString().orEmpty()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        mainViewModel.userLogin(email, password).observe(this) { response ->
            binding.progressBar.visibility = View.GONE
            when (response) {
                is ResultState.Success -> {
                    val user = UserModel(
                        email = email,
                        password = password,
                        token = response.data ?: "",
                        isLogin = true
                    )
                    mainViewModel.storeUserSession(user)
                    navigateToMainActivity()
                }
                is ResultState.Error -> {
                    showErrorDialog(response.exception.message ?: "Login failed")
                }
                else -> {}
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this).apply {
            setTitle("Login Failed")
            setMessage(message)
            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }
}
