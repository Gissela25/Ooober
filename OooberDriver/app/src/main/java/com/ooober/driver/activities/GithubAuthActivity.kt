package com.ooober.driver.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.ooober.driver.databinding.ActivityGithubAuthBinding
import com.ooober.driver.models.Driver
import com.ooober.driver.providers.AuthProvider
import com.ooober.driver.providers.DriverProvider

class GithubAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGithubAuthBinding
    private val authProvider = AuthProvider()
    private val driverProvider = DriverProvider()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGithubAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding.btnLoginGithub.setOnClickListener {
            verifyGithubAuth()
        }
    }

    private fun verifyGithubAuth() {
        val email = binding.tvEmail.text.toString()

        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            val provider = OAuthProvider.newBuilder("github.com")
            provider.addCustomParameter("login", email)

            // Request read access to a user's email addresses.
            // This must be preconfigured in the app's API permissions.
            provider.scopes = listOf("user:email")

            val pendingResultTask = auth.pendingAuthResult
            if (pendingResultTask != null) {
                pendingResultTask
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener {
                        Log.d("Github 1",it.toString())
                        Toast.makeText(this@GithubAuthActivity, ""+it.toString(), Toast.LENGTH_SHORT).show()

                    }
            } else {
                signInWithGithub(provider.build(),email)
            }

        } else {
            Toast.makeText(this@GithubAuthActivity, "Email Invalido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToMapActivity() {
        val i = Intent(this, MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    private fun signInWithGithub(provider: OAuthProvider,email: String)
    {
        auth.startActivityForSignInWithProvider( /* activity = */this, provider)
            .addOnSuccessListener {
                val client = Driver(
                    id = authProvider.getId(),
                    email = email
                )
                driverProvider.create(client).addOnCompleteListener {
                    if (it.isSuccessful) {
                        goToMapActivity()
                    }
                    }
            }
            .addOnFailureListener {
                Log.d("Github 2",it.toString())
                Toast.makeText(this@GithubAuthActivity, "Este correo está relacionada con otro metodo de inicio sesión", Toast.LENGTH_LONG).show()

            }
    }
}