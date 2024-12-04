package com.example.storyhub.view.signup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.storyhub.data.result.ResultState
import com.example.storyhub.databinding.ActivitySignupBinding
import com.example.storyhub.view.MainViewModel
import com.example.storyhub.view.ViewModelFactory
import com.example.storyhub.view.login.LoginActivity

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val viewModel: MainViewModel by viewModels { ViewModelFactory.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
        startAnimations()
    }

    private fun setupUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupListeners() {
        binding.nameEditText.addTextChangedListener(createTextWatcher())
        binding.emailEditText.addTextChangedListener(createTextWatcher())
        binding.passwordEditText.addTextChangedListener(createTextWatcher())

        binding.signupButton.setOnClickListener {
            attemptSignup()
        }
    }

    private fun createTextWatcher() = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            validateInputs()
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    private fun startAnimations() {
        val animators = listOf(
            ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 0f, 1f),
            ObjectAnimator.ofFloat(binding.nameTextView, View.ALPHA, 0f, 1f),
            ObjectAnimator.ofFloat(binding.nameEditTextLayout, View.ALPHA, 0f, 1f),
            ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 0f, 1f),
            ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 0f, 1f),
            ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 0f, 1f),
            ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 0f, 1f),
            ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 0f, 1f)
        )

        AnimatorSet().apply {
            playSequentially(animators)
            startDelay = 100
            duration = 500
        }.start()
    }

    private fun validateInputs() {
        val isNameValid = binding.nameEditText.text.toString().isNotEmpty()
        val isEmailValid = binding.emailEditText.text.toString().isNotEmpty()
        val isPasswordValid = binding.passwordEditText.text.toString().length >= 8

        binding.signupButton.isEnabled = isNameValid && isEmailValid && isPasswordValid

        if (!isPasswordValid) {
            binding.passwordEditTextLayout.error = "Password must be at least 8 characters"
        } else {
            binding.passwordEditTextLayout.error = null
        }
    }

    private fun attemptSignup() {
        val name = binding.nameEditText.text.toString()
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showToast("All fields are required")
            return
        }

        showLoading(true)

        viewModel.userRegister(name, email, password).observe(this) { result ->
            showLoading(false)
            when (result) {
                is ResultState.Loading -> showLoading(true)
                is ResultState.Success -> handleSignUpSuccess(result.data ?: "Registration successful")
                is ResultState.Error -> handleSignUpError(result.exception.message ?: "An error occurred")
            }
        }
    }

    private fun handleSignUpSuccess(message: String) {
        showToast(message)
        showSuccessDialog()
    }

    private fun handleSignUpError(message: String) {
        showToast(message)
        if (message.contains("Email is already taken")) {
            showEmailTakenPopup()
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Registration Successful!")
            setMessage("Your account has been created. Please log in.")
            setPositiveButton("Continue") { _, _ -> navigateToLoginActivity() }
            create()
            show()
        }
    }

    private fun showEmailTakenPopup() {
        AlertDialog.Builder(this).apply {
            setTitle("Email already taken")
            setMessage("The email you entered is already associated with another account. Please try again with a different email.")
            setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            create()
            show()
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
