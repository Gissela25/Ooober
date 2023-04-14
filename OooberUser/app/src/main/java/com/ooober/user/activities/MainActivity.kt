package com.ooober.user.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.ooober.user.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding.btnRegiser.setOnClickListener { goToRegister() }
        binding.btnSingIn.setOnClickListener { login() }
    }

    private fun login() {
        val email = binding.tvEmail.text.toString()
        val password = binding.tvclave.text.toString()

        if(isValidForm(email, password)){
            Toast.makeText(this, "Formulario Valido", Toast.LENGTH_SHORT).show()
        }
    }
    private fun isValidForm(email: String, password: String): Boolean{
        if (email.isEmpty()) {
            Toast.makeText(this, "Ingresa tu correo electronico", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Ingresa tu clave", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun goToRegister() {
        val i = Intent(this, RegisterActivity::class.java)
        startActivity(i)
    }
}