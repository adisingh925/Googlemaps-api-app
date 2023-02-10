package com.adreal.randomplaces.ViewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.adreal.randomplaces.SharedPreferences.SharedPreferences
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.*
import kotlin.collections.ArrayList

class ViewModel(application: Application) : AndroidViewModel(application) {

    val locationData = MutableLiveData<QuerySnapshot>()

    companion object{
        const val LOCATIONS = "locations"
        const val DOCUMENT = "doc"
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
        docRef.get().addOnCompleteListener {
            if(it.isSuccessful){
                val document = it.result
                locationData.postValue(document)
            }
        }
    }
}