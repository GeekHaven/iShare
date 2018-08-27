package org.geekhaven.ishare

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.listview_customlayout.view.*
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
        val userData = userdata[position]
        listItemView.nameListTV.text = userData.mName
        listItemView.AdressListTV.text = userData.mAddress
        val s = userData.mLocation
        listItemView.LocationListTV.text = userData.mLocation
        return listItemView
    }
}