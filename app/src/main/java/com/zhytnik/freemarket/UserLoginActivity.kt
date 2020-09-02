package com.zhytnik.freemarket

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth

class UserLoginActivity : AppCompatActivity() {
    private val TAG = "LoginActivity"
    private var btnLogin: Button? = null
    private var btnForgotPassword: TextView? = null
    private var btnCreateAccount: Button? = null
    private var mProgressBar: ProgressBar? = null

    //Firebase references
    private var mAuth: FirebaseAuth? = null

    private var email: String? = null
    private var password: String? = null

    private var etEmail: EditText? = null
    private var etPassword: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_login)
        initialise()
    }

    private fun initialise() {
        btnLogin = findViewById<View>(R.id.btn_login) as Button
        etEmail = findViewById<View>(R.id.editTextTextEmailAddress) as EditText
        etPassword = findViewById<View>(R.id.editTextTextPassword) as EditText
        mProgressBar = findViewById(R.id.progressBarLogin)
        btnForgotPassword = findViewById(R.id.forgotPasswordBtn)
        btnCreateAccount = findViewById(R.id.createAccountBtn)

        btnLogin!!.setOnClickListener { loginUser() }
        btnForgotPassword!!.setOnClickListener({showForgotPasswordActivity()})
        btnCreateAccount!!.setOnClickListener({createAccount()})

        mAuth = FirebaseAuth.getInstance()
    }

    private fun loginUser() {
        email = etEmail?.text.toString()
        password = etPassword?.text.toString()
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            mProgressBar!!.visibility = View.VISIBLE
            Log.d(TAG, "Logging in user.")

            mAuth!!.signInWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(this) { task ->
                    mProgressBar!!.visibility = View.GONE
                    if (task.isSuccessful) {
                        // Sign in success, update UI with signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        showMainActivity()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.e(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            this@UserLoginActivity, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        } else {
            Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show()
        }

    }

    private fun showMainActivity() {
        val intent = Intent(this@UserLoginActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun showForgotPasswordActivity() {
        val intent = Intent(this@UserLoginActivity, ForgotPasswordActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun createAccount() {
        val intent = Intent(this@UserLoginActivity, CreateAccountActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}