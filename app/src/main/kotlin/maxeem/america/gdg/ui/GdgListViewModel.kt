package maxeem.america.gdg.ui

import android.location.Location
import androidx.lifecycle.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import maxeem.america.common.ConsumableEvent
import maxeem.america.ext.asConsumable
import maxeem.america.ext.asImmutable
import maxeem.america.ext.asMutable
import maxeem.america.gdg.domain.GdgChapter
import maxeem.america.gdg.domain.GdgData
import maxeem.america.gdg.domain.LatLong
import maxeem.america.gdg.repo.ApiStatus
import maxeem.america.gdg.repo.GdgRepository
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.time.measureTime

class GdgListViewModel(state: SavedStateHandle): ViewModel(), AnkoLogger, KoinComponent {

    private companion object {
        var cachedLocation : LatLong? = null

        const val MIN_JOB_DURATION = 500L

        const val STATE_KEY_LOCATION = "location"
        const val STATE_KEY_REGION   = "region"
    }

    private val repository : GdgRepository by inject()
    private var job: Job? = null

    private
    val location   = state.getLiveData<LatLong>(STATE_KEY_LOCATION, cachedLocation).asImmutable()
    val region     = state.getLiveData<String>(STATE_KEY_REGION).asImmutable()

    val gdgList    = MutableLiveData<List<GdgChapter>>().asImmutable()
    val regionList = MutableLiveData<List<String>>().asImmutable()
    val dataChangedEvent = ConsumableEvent().asImmutable()

    val hasData = gdgList.map { !it.isNullOrEmpty() }.apply { asMutable().value = false }
    val hasRegions = regionList.map { !it.isNullOrEmpty() }.apply { asMutable().value = false }

    val applyEvent = ConsumableEvent().asImmutable()
    fun onApply() { applyEvent.asConsumable().setValue(true) }
    val aboutEvent = ConsumableEvent().asImmutable()
    fun onAbout() { aboutEvent.asConsumable().setValue(true) }

    val status = MutableLiveData<ApiStatus?>().asImmutable()

    init {
        info("init, has regions: ${hasRegions.value}, region: ${region.value}, location: ${location.value}")
        performJob()
    }

    private fun performJob() {
        info("performJob(), lastLocation: ${location.value}, currentJob: $job, viewModelScope: $viewModelScope (isActive: ${viewModelScope.isActive})")
        status.asMutable().value = ApiStatus.Loading
        job?.cancel()
        viewModelScope.launch {
            job = this as Job
            info(" - performJob, launch, this: $this, job: $job, viewModelScope: $viewModelScope")
            val r : Result<GdgData>
            measureTime {
                r = runCatching {
                    repository.getData(region.value, location.value)
                }
            }.apply {
                info(" - execution time: millis: ${toLongMilliseconds()}, $this")
                if (toLongMilliseconds() < MIN_JOB_DURATION)
                    delay(MIN_JOB_DURATION - toLongMilliseconds())
            }
            info(" - job in the middle, isCancelled: $isCancelled, is same ${job===this}, $this")
            if (job != this || isCancelled) return@launch
            r.onSuccess { data ->
                info(" - performJob, onSuccess, $this")
                dataChangedEvent.asConsumable().setValue(true)
                gdgList.asMutable().value = data.chapters
                if (data.regions != regionList.value)
                    regionList.asMutable().value = data.regions
                status.asMutable().value = ApiStatus.Success
            }.onFailure { err ->
//                if (err is CancellationException) return@launch //isCancelled above already handles that
                info(" - performJob, onFailure, $this")
                error("  - error is: $err"); err.printStackTrace()
                status.asMutable().value = ApiStatus.Error.of(err)
            }
        }.apply {
            invokeOnCompletion {
                info(" - invokeOnCompletion, job: $job")
                if (job == this) job = null
            }
        }
    }

    fun retryOnError() {
        info("retryOnError, status: ${status.value}, job: $job")
        performJob()
    }

    fun onLocationUpdated(updatedLocation: Location) {
        val newLocation = LatLong(
            updatedLocation.latitude,
            updatedLocation.longitude
        )
        info("onLocationUpdated, new & old are same: ${newLocation == location.value} \n new lat long $newLocation, full new: ${location.value}")
        if (newLocation != location.value) {
            location.asMutable().value = newLocation
            cachedLocation = newLocation
            performJob()
        }
    }

    fun onRegionChanged(changedRegion: String?) {
        info("onRegionChanged, new & old are same: ${region.value == changedRegion}, new $changedRegion, old ${region.value}")
        if (region.value != changedRegion) {
            region.asMutable().value = changedRegion
            performJob()
        }
    }

}
