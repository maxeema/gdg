package maxeem.america.gdg.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize data class GdgChapter(
        val name: String,
        val city: String,
        val country: String,
        val region: String,
        val website: String,
        val geo: LatLong
): Parcelable
