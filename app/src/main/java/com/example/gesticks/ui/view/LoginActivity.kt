package com.example.gesticks.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gesticks.R
import com.example.gesticks.databinding.ActivityLoginBinding
import com.example.gesticks.ui.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupAnimations()
        setupListeners()
        observeViewModel()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupAnimations() {
        binding.header.alpha = 0f
        binding.header.translationY = 50f
        binding.form.translationY = 100f

        binding.header.animate().alpha(1f).translationY(0f).setDuration(800).start()
        binding.form.animate().translationY(0f).setDuration(1000).setStartDelay(300).start()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim().lowercase()
            val password = binding.etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                viewModel.login(email, password)
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun observeViewModel() {
        viewModel.loginResponse.observe(this) { response ->
            if (response != null && response.isSuccessful) {
                val loginData = response.body()
                if (loginData?.accessToken != null && loginData.user != null) {
                    val sessionManager = com.example.gesticks.data.network.SessionManager(this)
                    sessionManager.saveAuthToken(loginData.accessToken)
                    sessionManager.saveUserId(loginData.user.id)
                    sessionManager.saveUserName(loginData.user.name)
                    sessionManager.saveUserEmail(loginData.user.email)
                    navigateToMain()
                } else {
                    Toast.makeText(this, "Error: Respuesta del servidor incompleta", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnLogin.isEnabled = !isLoading
        }

        viewModel.error.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
