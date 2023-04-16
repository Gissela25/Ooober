package com.ooober.user.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ooober.user.R
import com.ooober.user.databinding.ActivitySignInBinding
import com.ooober.user.models.Client
import com.ooober.user.providers.AuthProvider
import com.ooober.user.providers.ClientProvider

class SignInActivity : AppCompatActivity() {

    private val authProvider = AuthProvider()
    private val driverProvider = ClientProvider()
    private lateinit var binding: ActivitySignInBinding
    private lateinit var client: GoogleSignInClient
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val callbackManager = CallbackManager.Factory.create()
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

        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        client = GoogleSignIn.getClient(this, options)
        binding.btnSgoogle.setOnClickListener {
            val intent = client.signInIntent
            startActivityForResult(intent, 10001)
        }

        binding.btnSfacebook.setOnClickListener {
            LoginManager.getInstance()
                .logInWithReadPermissions(this, listOf("email", "public_profile"))

            LoginManager.getInstance()
                .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult) {
                        handleFacebookAccessToken(result.accessToken)
                    }

                    override fun onCancel() {
                        Log.d("FACEBOOK", "facebook:onCancel")
                    }

                    override fun onError(error: FacebookException) {
                        Log.d("FACEBOOK", "facebook:onError", error)
                    }
                })
        }

    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("FACEBOOK", "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("FACEBOOK", "signInWithCredential:success")
                    val user = task.result?.user
                    if (task.isSuccessful) {
                        val client = Client(
                            id = authProvider.getId(),
                            name = user?.displayName,
                            email = user?.email,
                            phone = user?.phoneNumber
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
                                    "Ocurrio  un error al ingresar",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d("FIREBASE", "Error: ${it.exception.toString()}")
                            }
                        }

                    } else {
                        // En caso de errores se imprimiran los siguintes mensajes
                        Log.w("FACEBOOK", "signInWithCredential:failure", task.exception)
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }
            }
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
                        val client = Client(
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
                                    "Ocurrio  un error al ingresar",
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
