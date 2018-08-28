package org.geekhaven.ishare

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast



/**
 * Created by rtk154 on 28/6/18.
 */

object CheckPermission {

    //  CHECK FOR LOCATION PERMISSION
    fun checkPermission(activity: Activity): Boolean {
        val result = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)
        return if (result == PackageManager.PERMISSION_GRANTED) {

            true

        } else {

            false

        }
    }

    //REQUEST FOR PERMISSSION
    fun requestPermission(activity: Activity, code: Int) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)) {

            Toast.makeText(activity, "GPS permission allows us to access location data. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show()

        } else {

            ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), code)
        }
    }

}
