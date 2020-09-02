package com.zhytnik.freemarket

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private var btnAddAComment: Button? = null
    private var tvUserName: TextView? = null
    private var tvLogout: TextView? = null

    //Firebase references
    private var mAuth: FirebaseAuth? = null
    private var mDatabase: FirebaseDatabase? = null
    private var mDatabaseReference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initialise()
    }

    private fun initialise() {
        btnAddAComment = findViewById<View>(R.id.btn_add_a_comment) as Button
        btnAddAComment!!.setOnClickListener({startAddACommentActivity()})

        tvUserName = findViewById<View>(R.id.helloNameTextView) as TextView
        tvLogout = findViewById<View>(R.id.logoutTextView) as TextView

        tvLogout!!.setOnClickListener({logoutUser()})
        tvUserName!!.setOnClickListener({editProfile()})


        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference!!.child("Users")

        showUserName()
    }

    private fun showUserName() {
        val mUser = mAuth!!.currentUser
        val mUserReference = mDatabaseReference!!.child(mUser!!.uid)
        mUserReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val fName = dataSnapshot.child("firstName").value
                tvUserName!!.text = fName.toString()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    private fun startAddACommentActivity() {
        TODO("Not yet implemented")
    }

    private fun logoutUser() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("FreeMarket")
        builder.setMessage("Вы хотите выйти?")
        builder.setPositiveButton("OK") { dialog, which ->
            mAuth!!.signOut()
            val intent = Intent(this@MainActivity, UserLoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        builder.setNegativeButton("Отмена", null)
        builder.show()

    }

    fun startASaleActivity(view: View) {
        val intent = Intent(this@MainActivity, StartASaleActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    fun findAGoodActivity(view: View) {
        val intent = Intent(this@MainActivity, FindAGoodActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    fun editProfile() {
        val intent = Intent(this@MainActivity, ProfileActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}