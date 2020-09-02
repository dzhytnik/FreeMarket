package com.zhytnik.freemarket

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CreateAccountActivity : AppCompatActivity() {
    private val TAG = "CreateAccountActivity"
    private var etName: EditText? = null
    private var etEmail: EditText? = null
    private var etPhone: EditText? = null
    private var etPassword: EditText? = null
    private var etConfirmPassword: EditText? = null

    private var btnCreateAccount: Button? = null
    private var mProgressBar: ProgressDialog? = null


    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        initialise()
    }

    private fun initialise() {
        etName = findViewById<View>(R.id.createActPersonName) as EditText
        etPhone = findViewById<View>(R.id.createActPhone) as EditText
        etEmail = findViewById<View>(R.id.createActEmail) as EditText
        etPassword = findViewById<View>(R.id.createActPassword) as EditText
        etConfirmPassword = findViewById<View>(R.id.createActConfirmPassword) as EditText

        btnCreateAccount = findViewById<View>(R.id.buttonCreateAccount) as Button
        btnCreateAccount!!.setOnClickListener({createAccount()})

        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference!!.child("Users")
        mAuth = FirebaseAuth.getInstance()
        mProgressBar = ProgressDialog(this)
    }

    private fun createAccount() {
        val firstName = etName?.text.toString()
        val email = etEmail?.text.toString()
        val phoneNumber = etPhone?.text.toString()
        val password = etPassword?.text.toString()
        val confirmPassword = etConfirmPassword?.text.toString()

        val context = this

        var validationPassed = true

        if (TextUtils.isEmpty(firstName)) {
            etName!!.setError(getString(R.string.validation_field_should_not_be_empty))
            validationPassed = false
        }

        if (TextUtils.isEmpty(email)) {
            etEmail!!.setError(getString(R.string.validation_field_should_not_be_empty))
            validationPassed = false
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            etPhone!!.setError(getString(R.string.validation_field_should_not_be_empty))
            validationPassed = false
        }

        if (TextUtils.isEmpty(password)) {
            etPassword!!.setError(getString(R.string.validation_field_should_not_be_empty))
            validationPassed = false
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword!!.setError(getString(R.string.validation_field_should_not_be_empty))
            validationPassed = false
        }

        if (!password.equals(confirmPassword)) {
            etPassword!!.setError(getString(R.string.validation_passwords_should_match))
            validationPassed = false
        }

        if (validationPassed) {
            mProgressBar!!.show()
            mAuth!!
                .createUserWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(this) {task ->
                    mProgressBar!!.hide()

                    if (task.isSuccessful) {

                        val userId = mAuth!!.currentUser!!.uid
                        val currentUserDb = mDatabaseReference!!.child(userId)
                        data class User(val firstName: String, val phoneNumber: String)
                        val currentUser = User(firstName!!, phoneNumber!!)
                        currentUserDb.setValue(currentUser, object: DatabaseReference.CompletionListener {
                            override fun onComplete(err: DatabaseError?, ref: DatabaseReference) {
                                if (err != null && err?.code != 0) {
                                    Toast.makeText(context, "Create user failed: " + err?.message,
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                        })

                        //Verify Email
                        verifyEmail();
                        //update user profile information

                    } else {
                        Toast.makeText(this, "Create user failed: " + task.exception?.localizedMessage,
                            Toast.LENGTH_SHORT).show()
                    }

                }
                .addOnFailureListener(this) {exc ->
                    Toast.makeText(this, exc.localizedMessage, Toast.LENGTH_SHORT).show()
                    mProgressBar!!.hide()
                }
        }

    }

    private fun verifyEmail() {
        val mUser = mAuth!!.currentUser;
        mUser!!.sendEmailVerification()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@CreateAccountActivity,
                        getString(R.string.verification_email_sent) + mUser.getEmail(),
                        Toast.LENGTH_SHORT).show()
                    showUserLoginActivity()
                } else {
                    Log.e(TAG, "sendEmailVerification", task.exception)
                    Toast.makeText(this@CreateAccountActivity,
                        "Failed to send verification email.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showUserLoginActivity() {
        val intent = Intent(this@CreateAccountActivity, UserLoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}