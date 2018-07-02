package org.geekhaven.ishare

/**
 * Created by rtk154 on 27/6/18.
 */

class UserInfo {
    lateinit var mName: String
    lateinit var mAddress: String
    lateinit var mLocation: String

    constructor() {}
    constructor(Name: String, Address: String, Location: String) {
        this.mName = Name
        this.mAddress = Address
        this.mLocation = Location
    }

    fun getmName(): String {
        return mName
    }

    fun setmName(mName: String) {
        this.mName = mName
    }

    fun getmAddress(): String {
        return mAddress
    }

    fun setmAddress(mAddress: String) {
        this.mAddress = mAddress
    }

    fun getmLocation(): String {
        return mLocation
    }

    fun setmLocation(mLocation: String) {
        this.mLocation = mLocation
    }
}
