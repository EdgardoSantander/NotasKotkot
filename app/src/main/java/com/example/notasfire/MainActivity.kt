package com.example.notasfire

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.security.SecureRandom

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var oneTapClient: SignInClient
    private lateinit var signUpRequest: BeginSignInRequest


    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var botonGoogle: Button
    private lateinit var statusGoogle: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        email = findViewById(R.id.emailEditText)
        password = findViewById(R.id.passwordEditText)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)
        statusTextView = findViewById(R.id.statusTextView)
        botonGoogle = findViewById(R.id.botonGoogle)
        statusGoogle = findViewById(R.id.statusGoogle)

        oneTapClient = Identity.getSignInClient(this);
        signUpRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        auth = Firebase.auth



        registerButton.setOnClickListener {
            val emailInput = email.text.toString()
            val passwordInput = password.text.toString()

            auth.createUserWithEmailAndPassword(emailInput, passwordInput)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        updateUI(user)
                        val intent = Intent(this, NotesActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        statusTextView.text = "Registration failed: ${task.exception?.message}"
                        updateUI(null)
                    }
                }
        }

        loginButton.setOnClickListener {
            val emailInput = email.text.toString()
            val passwordInput = password.text.toString()

            auth.signInWithEmailAndPassword(emailInput, passwordInput)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        updateUI(user)
                        val intent = Intent(this, NotesActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        statusTextView.text = "Login failed: ${task.exception?.message}"
                        updateUI(null)
                    }
                }

        }

        botonGoogle.setOnClickListener {
            oneTapClient.beginSignIn(signUpRequest)
                .addOnSuccessListener { result ->
                    try {
                        startIntentSenderForResult(
                            result.pendingIntent.intentSender,
                            REQ_ONE_TAP,
                            null, 0, 0, 0, null
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Google Sign-In failed", e)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "No accounts found for Google Sign-In", e)
                }
        }
    }

    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    private var showOneTapUI = true


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with Firebase.
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken,null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this){ task ->
                                    if (task.isSuccessful){
                                        val user = auth.currentUser
                                        updateUI(user)
                                        val intent = Intent(this, NotesActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                            Log.d(TAG, "Got ID token.")
                        }

                        else -> {
                            // Shouldn't happen.
                            Log.d(TAG, "No ID token!")
                        }
                    }
                } catch (e: ApiException) {
                    // ...
                }
            }
        }


    }


    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            updateUI(currentUser)
            // Si el usuario está autenticado, redirigir a NotesActivity
            val intent = Intent(this, NotesActivity::class.java)
            startActivity(intent)
            finish() // Finalizar la actividad de inicio de sesión
        } else {
            // Si no está autenticado, mostrar la pantalla de inicio de sesión
            updateUI(null)
        }

    }


    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            statusTextView.text = "Welcome, ${user.email}"
            loginButton.isEnabled = false
            registerButton.isEnabled = false
            botonGoogle.isEnabled = false
        } else {
            statusTextView.text = "Please sign in."
            loginButton.isEnabled = true
            registerButton.isEnabled = true
            botonGoogle.isEnabled = true
        }
    }








}