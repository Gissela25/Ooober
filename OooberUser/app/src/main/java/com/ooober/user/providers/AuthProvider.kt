package com.ooober.user.providers

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class AuthProvider {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun login(email: String, password: String): Task<AuthResult>{
        return auth.signInWithEmailAndPassword(email, password)
    }

    fun register(email: String, password: String): Task<AuthResult>{
        return auth.createUserWithEmailAndPassword(email, password)
    }

    fun getId(): String {
        return auth.currentUser?.uid ?: ""
    }

    fun existSession(): Boolean{
        var exist = false
        if (auth.currentUser != null){
            exist = true
        }
        return exist
    }
    fun logout(){
        auth.signOut()
    }
}