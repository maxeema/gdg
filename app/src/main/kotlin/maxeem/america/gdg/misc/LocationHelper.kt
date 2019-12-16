package maxeem.america.gdg.misc

import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info

class LocationHelper(val ctx: Context, val onLocationChanged: (location: Location)->Unit):
    AnkoLogger {

    var callback : LocationCallback? = null
        private set
    var client: FusedLocationProviderClient? = null
        private set
    var listeningToUpdates : Boolean = false
        private set
    var hasRequestedLastLocationOnStart : Boolean  = false

    fun requestLastLocation() {
        info("call requestLastLocation")
        val activeClient = client ?: LocationServices.getFusedLocationProviderClient(ctx)
        if (client == null) {
            client = activeClient
            info(" requestLastLocation, new client: $client")
            activeClient.lastLocation.apply {
                addOnSuccessListener { location ->
                    info(" requestLastLocation, success location: $location")
                    if (location == null) {
                        startUpdates()
                    } else {
                        onGotLocation(location)
                    }
                }
                addOnFailureListener {
                    error(" requestLastLocation failure location, $it", it)
                }
            }
        }
    }

    fun startUpdates() {
        if (callback == null) {
            callback = object: LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    info(" startUpdates, onLocationResult: $locationResult")
                    val location = locationResult?.lastLocation ?: return
                    onGotLocation(location)
                }
            }
            info("call startUpdates, new callback: $callback")
        }
        listenToUpdates()
    }

    fun listenToUpdates() {
        info("call listenToUpdates\n client: $client \n callback: $callback")
        if (!listeningToUpdates && client != null && callback != null) {
            client!!.requestLocationUpdates(
                LocationRequest().setPriority(
                    LocationRequest.PRIORITY_LOW_POWER
                ),
                        callback, Looper.getMainLooper()
            )
            listeningToUpdates = true
        }
    }
    fun stopUpdates() {
        info("call stopUpdates: client $client, callback $callback")
        if (listeningToUpdates && client != null && callback != null) {
            client?.removeLocationUpdates(callback)
            listeningToUpdates = false
        }
    }

    fun onGotLocation(location: Location) {
        onLocationChanged(location)
        release()
    }
    private fun release() {
        stopUpdates()
        client = null
        callback = null
    }

}