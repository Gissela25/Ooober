package com.ooober.user.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.ooober.user.R
import com.google.firebase.auth.FirebaseAuth

class ForgotPassActivity : AppCompatActivity() {
    val auth = FirebaseAuth.getInstance()
    lateinit var btnResetPass : Button
    lateinit var btnGoSign : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_pass)

        btnResetPass = findViewById<Button>(R.id.btn_ResetPass)
        btnGoSign = findViewById<TextView>(R.id.btn_GosignIn)

        btnResetPass.setOnClickListener{ resetPassword() }
        btnGoSign.setOnClickListener { goSignIn()}
    }

    private fun resetPassword(){
        val emailAddress = findViewById<EditText>(R.id.txtEdit_emailForgot).text.toString()

        auth.sendPasswordResetEmail(emailAddress)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "${resources.getString(R.string.txt_resetPassMailSent)} $emailAddress",Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(applicationContext, "Error",Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun goSignIn(){
        val i = Intent(this, SignInActivity::class.java)
        startActivity(i)
    }

}