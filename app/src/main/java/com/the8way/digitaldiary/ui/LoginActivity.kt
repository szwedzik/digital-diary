package com.the8way.digitaldiary.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.the8way.digitaldiary.R
import com.the8way.digitaldiary.databinding.ActivityLoginBinding
import com.the8way.digitaldiary.utils.SecurityUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val pinViewModel: PinViewModel by viewModels()

    private var isSettingPin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val pin = binding.pinEditText.text.toString()
            if (pin.length < 4 || pin.length > 10) {
                Toast.makeText(this, R.string.pin_length_error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val storedPin = pinViewModel.getPin()
                if (storedPin == null) {
                    if (isSettingPin) {
                        val confirmPin = binding.confirmPinEditText.text.toString()
                        if (pin == confirmPin) {
                            pinViewModel.setPin(SecurityUtils.hashPin(pin))
                            Toast.makeText(this@LoginActivity, R.string.pin_set, Toast.LENGTH_SHORT).show()
                            proceedToMainActivity()
                        } else {
                            Toast.makeText(this@LoginActivity, R.string.pin_mismatch, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        binding.confirmPinEditText.visibility = View.VISIBLE
                        isSettingPin = true
                        Toast.makeText(this@LoginActivity, R.string.pin_confirm, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (SecurityUtils.verifyPin(pin, storedPin.pinHash)) {
                        proceedToMainActivity()
                    } else {
                        Toast.makeText(this@LoginActivity, R.string.pin_incorrect, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun proceedToMainActivity() {
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }
}
