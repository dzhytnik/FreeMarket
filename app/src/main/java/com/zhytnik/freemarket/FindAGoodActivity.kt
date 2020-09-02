package com.zhytnik.freemarket

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FindAGoodActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private val TAG = "FindAGoodActivity"
    private var startASearchButton: Button? = null
    private var tableLayout: TableLayout? = null
    private var toggleSearchMyCounty: Switch? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PERMISSIONS_REQUEST = 100

    private var commodityToSale: String? = null
    private var city: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_a_good)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initialize()
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


    private fun initialize() {
        startASearchButton = findViewById<View>(R.id.btn_find_a_good) as Button
        startASearchButton!!.setOnClickListener({startASearch()})
        val spinner: Spinner = findViewById(R.id.spinner_find_a_good)
        ArrayAdapter.createFromResource(
            this,
            R.array.categories_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.onItemSelectedListener = this@FindAGoodActivity
            spinner.prompt = "Выберите категорию товара"
        }
        tableLayout = findViewById<TableLayout>(R.id.tableSales)
        tableLayout!!.visibility = TableLayout.INVISIBLE
        toggleSearchMyCounty = findViewById((R.id.switch_search_my_county))
        toggleSearchMyCounty?.setOnCheckedChangeListener({_, isChecked -> toggleChanged(isChecked)})
    }

    private fun toggleChanged(checked: Boolean) {
        if (checked) {
            if (city == null) {
                requestCoordinates()
            }
        }
    }

    private fun startASearch() {
        val db = Firebase.firestore
        tableLayout!!.removeAllViews()
        val salesRef = db.collection("sales")
        var query: Query

        if (toggleSearchMyCounty!!.isChecked) {
            query = salesRef.whereEqualTo("city", city)
                .whereEqualTo("commodity", commodityToSale)
        } else {
            query = salesRef.whereEqualTo("commodity", commodityToSale)
        }
        query.get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    val innerLayout = LinearLayout(this)
                    val firstName = document.data.get("firstName")
                    val city = document.data.get("city")
                    val phoneNumber = document.data.get("phoneNumber")
                    val tableRow = TableRow(this)
                    val firstNameText = TextView(this)
                    val cityText = TextView(this)
                    val phoneNumberText = TextView(this)
                    firstNameText.setPadding(25,0,0,0)
                    firstNameText.setText(firstName.toString())
                    cityText.setPadding(25,0,0,0)
                    cityText.setText(city.toString())
                    phoneNumberText.setPadding(25,0,0,0)
                    phoneNumberText.setText(phoneNumber.toString())
                    innerLayout.addView(firstNameText)
                    innerLayout.addView(cityText)
                    innerLayout.addView(phoneNumberText)
                    tableRow.addView(innerLayout)
                    tableLayout!!.addView(tableRow)
                }
                tableLayout!!.visibility = TableLayout.VISIBLE
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        commodityToSale = parent!!.getItemAtPosition(pos).toString()
    }

    private fun requestCoordinates() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    // Got last known location. In some rare situations this can be null.
                    val geocoder = Geocoder(this)
                    val addresses: List<Address>?
                    addresses = geocoder.getFromLocation(location!!.latitude, location!!.longitude, 1)
                    city = addresses.get(0).locality
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
}