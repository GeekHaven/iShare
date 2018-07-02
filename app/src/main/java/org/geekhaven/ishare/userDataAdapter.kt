package org.geekhaven.ishare

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.util.ArrayList

/**
 * Created by rtk154 on 3/7/18.
 */
class userDataAdapter(context: Context, objects: ArrayList<UserInfo>) : ArrayAdapter<UserInfo>(context, 0, objects) {

    internal var userdata = ArrayList<UserInfo>()

    init {
        userdata = objects
    }

    @SuppressLint("ResourceAsColor")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val listItemView = LayoutInflater.from(context).inflate(
                R.layout.listview_customlayout, null, true)
        val nameList = listItemView.findViewById<View>(R.id.nameListTV) as TextView
        val AddressList = listItemView.findViewById<View>(R.id.AdressListTV) as TextView
        val locationList = listItemView.findViewById<View>(R.id.LocationListTV) as TextView
        val userData = userdata[position]
        nameList.text = userData.getmName()
        AddressList.text = userData.getmAddress()
        val s = userData.getmLocation()
        locationList.text = userData.getmLocation()
        return listItemView
    }
}