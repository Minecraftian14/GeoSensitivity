package `in`.mcxiv.geosensitivity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlin.math.sqrt

data class Coordinates(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) {
    operator fun times(other: Coordinates): Double {
        val dx = (latitude - other.latitude) *3600* 28.96
        val dy = (longitude - other.longitude) *3600* 30.98
        return sqrt(dx * dx + dy * dy)
    }
}

@SuppressLint("MissingPermission")
@Composable
fun getLocation(context: Context): Coordinates {

    // BLOCK 1: Request for permissions if not granted

    val locationPermissionRequest =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            when {
                it.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {}
                it.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {}
                else -> {}
            }
        }
    LaunchedEffect(key1 = context) {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // BLOCK 2: Creating a temporary object to hold our position and also the client which will update us with it

    var currentUserLocation by remember { mutableStateOf(Coordinates()) }
    val client = remember { LocationServices.getFusedLocationProviderClient(context) }
    DisposableEffect(key1 = client) {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                client.lastLocation
                    .addOnSuccessListener { location ->
                        location?.let {
                            // Somehow, it does not work if I don't print it...
                            println("HELLOOOOO $location")
                            currentUserLocation = Coordinates(
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        }
                    }
                    .addOnFailureListener {
                        Log.e("Location_error", "${it.message}")
                    }
            }
        }

        // BLOCK 3: Creating a loopback-ed callback to accept updates

        locationCallback.let {
            val locationRequest: LocationRequest =
                LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    2_000
                )
                    .setMinUpdateIntervalMillis(1_000)
                    .setMaxUpdateDelayMillis(3_000)
                    .build()
            client.requestLocationUpdates(
                locationRequest,
                it,
                Looper.getMainLooper()
            )
        }

        // BLOCK 4: Closing everything

        onDispose {
            try {
                val removeTask = client.removeLocationUpdates(locationCallback)
                removeTask.addOnCompleteListener { task ->
                    if (task.isSuccessful)
                        Log.d("location_tag", "Location Callback removed.")
                    else Log.d("location_tag", "Failed to remove Location Callback.")
                }
            } catch (se: SecurityException) {
                Log.e("location_tag", "Failed to remove Location Callback.. $se")
            }
        }
    }

    return currentUserLocation
}