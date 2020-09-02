package com.zhytnik.freemarket

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.lang.Exception


class ForgotPasswordActivity : AppCompatActivity() {
    private var email: String? = null
    private var etEmail: EditText? = null
    private var mProgressBar: ProgressBar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        initialize()
    }

    private fun initialize() {
        etEmail = findViewById<View>(R.id.forgotPasswordEmailAddress) as EditText
        mProgressBar = findViewById(R.id.progressBarSendRecoveryLink)
    }

    fun sendRecoveryLink(view: View) {
        val mAuth = FirebaseAuth.getInstance()
        mProgressBar!!.visibility = View.VISIBLE
        mAuth.sendPasswordResetEmail(etEmail?.text.toString()).addOnCompleteListener {task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    this@ForgotPasswordActivity, R.string.forgot_password_message,
                    Toast.LENGTH_SHORT
                ).show()
                showMainActivity()

            } else {
                try {
                    throw (task.exception!!)
                } catch (e: Exception) {
                    Toast.makeText(this@ForgotPasswordActivity, e.localizedMessage,
                    Toast.LENGTH_SHORT).show()
                }
            }
            mProgressBar!!.visibility = View.INVISIBLE
        }
    }

    private fun showMainActivity() {
        val intent = Intent(this@ForgotPasswordActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

}