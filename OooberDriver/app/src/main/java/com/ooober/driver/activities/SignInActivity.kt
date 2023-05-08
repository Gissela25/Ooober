package com.ooober.driver.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
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
import com.ooober.driver.providers.ClientProvider
import com.ooober.driver.providers.DriverProvider

class SignInActivity : AppCompatActivity() {

    private val authProvider = AuthProvider()
    private val driverProvider = DriverProvider()
    private val clientProvider = ClientProvider()
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


        binding.btnSgoogle.setOnClickListener {
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            client = GoogleSignIn.getClient(this, options)
            client.signOut()
            val intent = client.signInIntent
            startActivityForResult(intent, GOOGLE_SIGN_IN)
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

    private fun signWithGoogle() {

        Log.d("FIRESTORE/GOOGLE", "Succesfuly again")
        val i = Intent(this, MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    private fun signInGoogleForFirstTime(account: GoogleSignInAccount) {
        val client = Driver(
            id = authProvider.getId(),
            name = account.displayName,
            email = account.email,
            image = account.photoUrl.toString()
        )
        driverProvider.create(client).addOnCompleteListener {
            if (it.isSuccessful) {

                Log.d("FIRESTORE/GOOGLE", "Succesfuly")
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        clientProvider.getClientById(authProvider.getId()).addOnSuccessListener {
                            if (it.exists()) {
                                Toast.makeText(this,"Esta cuenta es de tipo cliente",Toast.LENGTH_LONG).show()
                            }
                            else{
                                driverProvider.getDriver(authProvider.getId()).addOnCompleteListener { snapshot->
                                    val driverSnapshot = snapshot.result
                                    if(driverSnapshot != null && driverSnapshot.exists()) {
                                       signWithGoogle()
                                    }
                                    else{
                                        signInGoogleForFirstTime(account)
                                    }
                                }
                            }
                        }

                    } else {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }

                }
            } catch (e: ApiException) {
                Log.d("GOOGLE", "Google sign in failed", e)
            }
        }
    }

    companion object {
        private const val GOOGLE_SIGN_IN = 2
    }
}