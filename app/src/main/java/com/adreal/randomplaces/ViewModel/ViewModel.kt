package com.adreal.randomplaces.ViewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.adreal.randomplaces.SharedPreferences.SharedPreferences
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.*

class ViewModel(application: Application) : AndroidViewModel(application) {

    val locationData = MutableLiveData<QuerySnapshot>()

    companion object{
        const val LOCATIONS = "locations"
        const val DOCUMENT = "doc"
        const val SNAPSHOT = "snapshot"
    }

    private var db : FirebaseFirestore = FirebaseFirestore.getInstance()

    private fun getDeviceId() : String{
        if(SharedPreferences.read("randomId","") == ""){
            val randomId = UUID.randomUUID().toString()
            SharedPreferences.write("randomId",randomId)

            return randomId
        }

        return ""
    }

    fun updateLocation(location : com.google.android.gms.maps.model.LatLng){
        val locationData : MutableMap<String, Any> = HashMap()
        locationData["latitude"] = location.latitude
        locationData["longitude"] = location.longitude
        db.collection(LOCATIONS).document(System.currentTimeMillis().toString()).set(locationData)
    }

    fun getAllData(){
        val docRef = db.collection(LOCATIONS)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(SNAPSHOT, "Listen failed.", e)
                return@addSnapshotListener
            }

            val source = if (snapshot != null && snapshot.metadata.hasPendingWrites())
                "Local"
            else
                "Server"

            if (snapshot != null && !snapshot.isEmpty) {
                Log.d(SNAPSHOT, "$source data: $snapshot")
                locationData.postValue(snapshot)
            } else {
                Log.d(SNAPSHOT, "$source data: null")
            }
        }
    }
}