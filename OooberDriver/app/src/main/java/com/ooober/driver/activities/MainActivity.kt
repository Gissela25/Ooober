package com.ooober.driver.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.ooober.driver.R
import com.ooober.driver.databinding.ActivityMainBinding
import com.ooober.driver.providers.AuthProvider
import com.ooober.driver.providers.ClientProvider

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authProvider = AuthProvider()
    private val clientProvider = ClientProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        binding.goSignUp.setOnClickListener { goToRegister() }
        binding.btnSingIn.setOnClickListener { login() }
    }

    private fun login() {
        val email = binding.tvEmail.text.toString()
        val password = binding.tvclave.text.toString()

        if(isValidForm(email, password)){
            authProvider.login(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    clientProvider.getClientById(authProvider.getId()).addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            Toast.makeText(this,"Esta cuenta es de tipo cliente",Toast.LENGTH_LONG).show()
                        }
                        else{
                            goToMap()
                        }
                    }
                }
                else{
                    Toast.makeText(this@MainActivity,  R.string.m_errorSignIn, Toast.LENGTH_SHORT).show()
                    Log.d("FIREBASE", "ERRO: ${it.exception.toString()}")
                }
            }
        }
    }

    private fun goToMap(){
        val i = Intent(this, MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    private fun isValidForm(email: String, password: String): Boolean{
        if (email.isEmpty()) {
            Toast.makeText(this, R.string.m_Iemail, Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()) {
            Toast.makeText(this, R.string.m_Ipassword, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun goToRegister() {
        val i = Intent(this, RegisterActivity::class.java)
        startActivity(i)
    }

    override fun onStart() {
        super.onStart()
        if(authProvider.existsSession()){
            goToMap()
        }
    }
}