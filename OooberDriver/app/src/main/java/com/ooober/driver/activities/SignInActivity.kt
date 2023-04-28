package com.ooober.driver.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ooober.driver.R
import com.ooober.driver.databinding.ActivityHomeBinding
import com.ooober.driver.databinding.ActivitySignInBinding
import com.ooober.driver.models.Driver
import com.ooober.driver.providers.AuthProvider
import com.ooober.driver.providers.DriverProvider

class SignInActivity : AppCompatActivity() {

    private val authProvider = AuthProvider()
    private val driverProvider = DriverProvider()
    private lateinit var binding: ActivitySignInBinding
    private lateinit var client: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding.btnESignIn.setOnClickListener { goToLogin() }
        binding.GosignIn.setOnClickListener { goToRegister() }
        binding.btnLanguage.setOnClickListener { goToSetLanguage() }
        binding.btnSGithub.setOnClickListener { goToGithubAuth()}

        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        client = GoogleSignIn.getClient(this, options)
        binding.btnSgoogle.setOnClickListener {
            val intent = client.signInIntent
            startActivityForResult(intent, 10001)
        }

    }

    private fun goToGithubAuth() {
        val i = Intent(this, GithubAuthActivity::class.java)
        startActivity(i)
    }

    private fun goToLogin() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }

    private fun goToRegister() {
        val i = Intent(this, RegisterActivity::class.java)
        startActivity(i)
    }

    private fun goToSetLanguage() {
        val i = Intent(this, SettingsActivity::class.java)
        startActivity(i)
    }

    private fun SignGoogle() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val client = Driver(
                            id = authProvider.getId(),
                            name = account.displayName,
                            email = account.email,
                        )
                        driverProvider.create(client).addOnCompleteListener {
                            if (it.isSuccessful) {

                                Log.d("FIREBASE", "Succesfuly")
                                val i = Intent(this, MapActivity::class.java)
                                i.flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(i)
                            } else {
                                Toast.makeText(
                                    this@SignInActivity,
                                    R.string.m_errorSignIn2,
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d("FIREBASE", "Error: ${it.exception.toString()}")
                            }
                        }

                    } else {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }

                }
        }
    }


}