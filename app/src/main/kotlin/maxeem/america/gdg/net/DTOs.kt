package maxeem.america.gdg.net

import com.squareup.moshi.Json
import maxeem.america.gdg.domain.GdgChapter
import maxeem.america.gdg.domain.LatLong

data class GdgResponse (
        @Json(name = "filters_") val filters : Map<String, Any>,
        @Json(name = "data")     val chapters: List<GdgChapterDTO>
) {
        val regions = runCatching { filters.getValue("region") as List<String> }.getOrElse { emptyList() }
}

data class GdgChapterDTO(
        @Json(name = "chapter_name") val name: String,
        @Json(name = "cityarea") val city: String,
        val country: String,
        val region: String,
        val website: String,
        val geo: LatLongDTO
)

data class LatLongDTO(
    val lat: Double,
    val lng: Double
)

fun LatLongDTO.toDomain() =
        LatLong(lat = this.lat, lng = this.lng)

fun List<GdgChapterDTO>.toDomain() = map { it.toDomain() }

fun GdgChapterDTO.toDomain() = GdgChapter(
        name = this.name,
        city = this.city,
        country = this.country,
        region = this.region,
        website = this.website,
        geo = this.geo.toDomain()
)
