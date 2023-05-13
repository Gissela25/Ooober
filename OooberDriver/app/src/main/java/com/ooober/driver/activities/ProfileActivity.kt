package com.ooober.driver.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.ooober.driver.R
import com.ooober.driver.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    //Invocar para enviar correo de verificacion al usuario
    private fun verifyUserEmail(){
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val userEmail = user?.email

        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "${resources.getString(R.string.txt_resetPassMailSent)} $userEmail", Toast.LENGTH_LONG).show()
                }
                else{
                    Toast.makeText(applicationContext, "Error",Toast.LENGTH_LONG).show()
                }
            }
    }


}