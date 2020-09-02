package com.zhytnik.freemarket

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class StartASaleActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener  {
    private val PERMISSIONS_REQUEST = 100

    private var btnStartAsale: Button? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null




    private var city: String? = null
    private var firstName: String? = null
    private var phoneNumber: String? = null
    private var commodityToSale: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_a_sale)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initialise()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST && grantResults.size == 2
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        }
    }


    private fun initialise() {
        btnStartAsale = findViewById<View>(R.id.btn_start_a_sale_act) as Button
        btnStartAsale!!.setOnClickListener({startASale()})


        val spinner: Spinner = findViewById(R.id.spinner_start_a_sale)
        ArrayAdapter.createFromResource(
            this,
            R.array.categories_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.onItemSelectedListener = this@StartASaleActivity
        }
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference!!.child("Users")

        val mUser = mAuth!!.currentUser
        val mUserReference = mDatabaseReference!!.child(mUser!!.uid)

        mUserReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                firstName = dataSnapshot.child("firstName").value.toString()
                phoneNumber = dataSnapshot.child("phoneNumber").value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        commodityToSale = spinner.selectedItem.toString()
    }

    private fun startASale() {
        requestCoordinatesAndStartASale()
    }

    private fun requestCoordinatesAndStartASale() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    // Got last known location. In some rare situations this can be null.
                    val geocoder = Geocoder(this)
                    val addresses: List<Address>?
                    addresses = geocoder.getFromLocation(location!!.latitude, location!!.longitude, 1)
                    city = addresses.get(0).locality
                    createASale(firstName, phoneNumber, city, commodityToSale)
                }
                .addOnFailureListener {it ->
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }

        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSIONS_REQUEST)
        }
    }

    private fun createASale(
        firstName: String?,
        phoneNumber: String?,
        city: String?,
        commodityToSale: String?
    ) {
        val db = Firebase.firestore

        val sale = hashMapOf(
            "firstName" to firstName,
            "phoneNumber" to phoneNumber,
            "city" to city,
            "commodity" to commodityToSale
        )

        db.collection("sales")
            .add(sale)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(
                    this@StartASaleActivity, R.string.sale_created_message,
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@StartASaleActivity, R.string.sale_creation_error_message,
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        commodityToSale = parent.getItemAtPosition(pos).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
    }

}