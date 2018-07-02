package org.geekhaven.ishare.activities;

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.karumi.dexter.BuildConfig
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import org.geekhaven.ishare.R
import org.geekhaven.ishare.UserInfo
import org.geekhaven.ishare.activities.LoginActivity
import org.geekhaven.ishare.userDataAdapter
import java.text.DateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    internal lateinit var mFirebaseAuth: FirebaseAuth
    internal lateinit var databaseReference: DatabaseReference
    private var user: FirebaseUser? = null
    internal var userDataList = ArrayList<UserInfo>()
    internal lateinit var mListView: ListView


    // location last updated time
    private var mLastUpdateTime: String? = null

    internal var location: String? = null
    internal var timeUpdate: String? = null
    // bunch of location related apis
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mSettingsClient: SettingsClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mLocationSettingsRequest: LocationSettingsRequest? = null
    private var mLocationCallback: LocationCallback? = null
    private var mCurrentLocation: Location? = null
    // boolean flag to toggle the ui
    private var mRequestingLocationUpdates: Boolean? = null

    internal lateinit var nameEntered: String
    internal lateinit var addressEntered: String
    internal lateinit var locationRecieved: String
    internal var flag = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val tv = findViewById<View>(R.id.HelloTv) as TextView
        mFirebaseAuth = FirebaseAuth.getInstance()
        //if user is not logged in then take it to login page;
        if (mFirebaseAuth.currentUser == null) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }
        mFirebaseAuth = FirebaseAuth.getInstance()
        user = FirebaseAuth.getInstance().currentUser
        //it sets email id of logged in user on top of the screen
        tv.text = "Hello User-ID :- " + user!!.email!!
        user = FirebaseAuth.getInstance().currentUser
        //creates a heading in database with name user in which data will be stored
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        val name = findViewById<View>(R.id.Name) as EditText
        val address = findViewById<View>(R.id.Address) as EditText
        val submit = findViewById<View>(R.id.SubmitButton) as Button
        val logoutButton = findViewById<View>(R.id.LogoutButton) as Button

        // initialize the necessary libraries
        init()

        // restore the values from saved instance state
        restoreValuesFromBundle(savedInstanceState)
        //submit button when clicked adds info to firebase database and shows in a listview
        submit.setOnClickListener(View.OnClickListener {
            //getLocation
            flag = 1
            nameEntered = name.text.toString().trim { it <= ' ' }
            addressEntered = address.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(nameEntered) || TextUtils.isEmpty(addressEntered)) {
                Toast.makeText(this@MainActivity, "Fill the Details Before Submitting", Toast.LENGTH_LONG).show()
                return@OnClickListener
            }
            //starts fetching location and updating in database and the UI every 10 seconds of interval
            startLocationFetch()

            //adds information to database
            addInfo(name, address)

            flag = 0
        })



        logoutButton.setOnClickListener {
            //stop getting location
            stopLocation()
            mFirebaseAuth.signOut()
            finish()
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        }
    }

    private fun addInfo(name: EditText, address: EditText) {
        nameEntered = name.text.toString().trim { it <= ' ' }
        addressEntered = address.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(nameEntered) || TextUtils.isEmpty(addressEntered)) {
            Toast.makeText(this@MainActivity, "Fill the Details Before Submitting", Toast.LENGTH_LONG).show()
            return
        }
        val information = UserInfo(nameEntered, addressEntered, location!!)
        information.setmLocation(locationRecieved)
        user = FirebaseAuth.getInstance().currentUser
        databaseReference.child(user!!.uid).setValue(information)
        Toast.makeText(this@MainActivity, "Information Saved ", Toast.LENGTH_SHORT).show()
    }


    private fun showInfo() {
        val progressBar = findViewById<View>(R.id.progressProfile) as ProgressBar
        progressBar.visibility = View.VISIBLE
        userDataList.clear()
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersdRef = rootRef.child("Users")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {

                    val name = ds.getValue<UserInfo>(UserInfo::class.java!!)
                    Log.d("TAG", name!!.getmAddress())
                    userDataList.add(name)
                }
                progressBar.visibility = View.GONE

                val userDataSet = userDataAdapter(this@MainActivity, userDataList)

                mListView = findViewById<View>(R.id.ListView) as ListView
                mListView.adapter = userDataSet

            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        usersdRef.addListenerForSingleValueEvent(eventListener)
    }

    private fun updateDatabaseLocation() {
        val information = UserInfo(nameEntered, addressEntered, locationRecieved)
        information.setmLocation(locationRecieved)
        user = FirebaseAuth.getInstance().currentUser
        databaseReference.child(user!!.uid).setValue(information)

        showInfo()
        Toast.makeText(this@MainActivity, "Updated... ", Toast.LENGTH_SHORT).show()
    }


    private fun init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSettingsClient = LocationServices.getSettingsClient(this)

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                // location is received
                mCurrentLocation = locationResult!!.lastLocation
                mLastUpdateTime = DateFormat.getTimeInstance().format(Date())

                updateLocationUI()
            }
        }

        mRequestingLocationUpdates = false

        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest!!.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        mLocationSettingsRequest = builder.build()
    }

    /**
     * Restoring values from saved instance state
     */

    private fun restoreValuesFromBundle(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("is_requesting_updates")) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean("is_requesting_updates")
            }

            if (savedInstanceState.containsKey("last_known_location")) {
                mCurrentLocation = savedInstanceState.getParcelable("last_known_location")
            }

            if (savedInstanceState.containsKey("last_updated_on")) {
                mLastUpdateTime = savedInstanceState.getString("last_updated_on")
            }
        }

        updateLocationUI()
    }

    /**
     * Update the UI displaying the location data
     * and toggling the buttons
     */
    private fun updateLocationUI() {
        if (mCurrentLocation != null) {
            location = "Lat: " + mCurrentLocation!!.latitude + ", " +
                    "Lng: " + mCurrentLocation!!.longitude
            locationRecieved = location as String
            if (flag == 0)
                updateDatabaseLocation()

            //  location last updated time
            timeUpdate = "Last updated on: " + mLastUpdateTime!!

        }

    }

    fun startLocationFetch() {
        // Requesting ACCESS_FINE_LOCATION using Dexter library
        Dexter.withActivity(this)
                .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        mRequestingLocationUpdates = true
                        startLocationUpdates()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        if (response.isPermanentlyDenied) {
                            // open device settings when the permission is
                            // denied permanently
                            openSettings()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }
                }).check()
    }


    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        mSettingsClient!!
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this) {
                    Log.i(TAG, "All location settings are satisfied.")

                    Toast.makeText(applicationContext, "Started location updates!", Toast.LENGTH_SHORT).show()


                    mFusedLocationClient!!.requestLocationUpdates(mLocationRequest,
                            mLocationCallback!!, Looper.myLooper())

                    updateLocationUI()
                }
                .addOnFailureListener(this) { e ->
                    val statusCode = (e as ApiException).statusCode
                    when (statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " + "location settings ")
                            try {
                                // Show the dialog by calling startResolutionForResult(), and check the
                                // result in onActivityResult().
                                val rae = e as ResolvableApiException
                                rae.startResolutionForResult(this@MainActivity, REQUEST_CHECK_SETTINGS)
                            } catch (sie: IntentSender.SendIntentException) {
                                Log.i(TAG, "PendingIntent unable to execute request.")
                            }

                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            val errorMessage = "Location settings are inadequate, and cannot be " + "fixed here. Fix in Settings."
                            Log.e(TAG, errorMessage)

                            Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }

                    updateLocationUI()
                }
    }

    private fun openSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null)
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        //        showInfo();
    }

    public override fun onResume() {
        super.onResume()

        // Resuming location updates depending on button state and
        // allowed permissions
        if (mRequestingLocationUpdates!! && checkPermissions()) {
            startLocationUpdates()
        }

        updateLocationUI()
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }


    override fun onPause() {
        super.onPause()

        if (mRequestingLocationUpdates!!) {
            // pausing location updates
            stopLocationUpdates()
        }
    }

    override fun onStop() {
        super.onStop()
        stopLocation()
    }

    fun stopLocation() {
        mRequestingLocationUpdates = false
        stopLocationUpdates()
    }

    fun stopLocationUpdates() {
        // Removing location updates
        mFusedLocationClient!!
                .removeLocationUpdates(mLocationCallback!!)
                .addOnCompleteListener(this) { Toast.makeText(applicationContext, "Location updates stopped!", Toast.LENGTH_SHORT).show() }
    }

    companion object {

        private val TAG = MainActivity::class.java!!.getSimpleName()

        // location updates interval - 10sec
        private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000

        // fastest updates interval - 5 sec
        // location updates will be received if another app is requesting the locations
        // than your app can handle
        private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = 5000

        private val REQUEST_CHECK_SETTINGS = 100
    }


//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.main_menu, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    // handle button activities
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        val id = item.itemId
//
//        if (id == R.id.logoutbutton) {
//            //stop getting location
//            stopLocation()
//            mFirebaseAuth.signOut()
//            finish()
//            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
//        }
//        return super.onOptionsItemSelected(item)
//    }

}


