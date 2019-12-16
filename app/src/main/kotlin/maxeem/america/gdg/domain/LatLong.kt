package maxeem.america.gdg.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LatLong(
        val lat: Double,
        val lng: Double
) : Parcelable