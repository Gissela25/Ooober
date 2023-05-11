package com.ooober.user.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.google.android.gms.common.logging.Logger
import com.ooober.user.R
import com.ooober.user.databinding.ActivityRegisterBinding
import com.ooober.user.models.Client
import com.ooober.user.providers.AuthProvider
import com.ooober.user.providers.ClientProvider

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authProvider = AuthProvider()
    private val clientProvider = ClientProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding.GosignIn.setOnClickListener { goToLogin() }
        binding.btnSignUp.setOnClickListener { register() }
    }

    private fun register() {
        val name = binding.tvname.text.toString()
        val lastname = binding.tvlastname.text.toString()
        val email = binding.tvRemail.text.toString()
        val phone = binding.tvphone.text.toString()
        val password = binding.tvpassword.text.toString()
        val Cpassword = binding.tvCpassword.text.toString()

        if(isValidForm(name,lastname,email,phone,password,Cpassword)){
            authProvider.register(email, password).addOnCompleteListener {
                if (it.isSuccessful){
                    val client = Client(
                        id = authProvider.getId(),
                        name = name,
                        lastname = lastname,
                        phone = phone,
                        email = email
                    )
                    clientProvider.create(client).addOnCompleteListener {
                        if(it.isSuccessful){
                            Toast.makeText(this@RegisterActivity, R.string.txt_registered, Toast.LENGTH_SHORT).show()
                            goToMap()
                        }
                        else{
                            Toast.makeText(this@RegisterActivity, R.string.txt_SomethingWasWrong, Toast.LENGTH_SHORT).show()
                            Log.d("FIREBASE", "Error: ${it.exception.toString()}")
                        }
                    }
                }
                else{
                    Toast.makeText(this@RegisterActivity, R.string.txt_SignUpF, Toast.LENGTH_SHORT).show()
                    Log.d("FIREBASE", "Error: ${it.exception.toString()}")
                }
            }
        }
    }

    private fun goToMap(){
        val i = Intent(this, MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    private fun isValidForm(
        name: String,
        lastname: String,
        email: String,
        phone: String,
        password: String,
        Cpassword: String
    ): Boolean {
        if (name.isEmpty()) {
            Toast.makeText(this, R.string.txt_EntName, Toast.LENGTH_SHORT).show()
            return false
        }
        if (lastname.isEmpty()) {
            Toast.makeText(this, R.string.txt_EntLN, Toast.LENGTH_SHORT).show()
            return false
        }
        if (email.isEmpty()) {
            Toast.makeText(this, R.string.txt_EntEmail, Toast.LENGTH_SHORT).show()
            return false
        }
        if (phone.isEmpty()) {
            Toast.makeText(this, R.string.txt_EntYourPhone, Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()) {
            Toast.makeText(this, R.string.txt_EntPwd, Toast.LENGTH_SHORT).show()
            return false
        }
        if (Cpassword.isEmpty()) {
            Toast.makeText(this, R.string.txt_EntConfPwd, Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (password != Cpassword) {
            Toast.makeText(this, R.string.txt_PwdDoNotMatch, Toast.LENGTH_SHORT).show()
        }
        if (password.length < 6) {
            Toast.makeText(
                this,
                R.string.txt_SixCharacters,
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }

    private fun goToLogin() {
        val i = Intent(this, SignInActivity::class.java)
        startActivity(i)
    }
}