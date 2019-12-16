package maxeem.america.gdg.ui

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.SearchView
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.transition.*
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import maxeem.america.app
import maxeem.america.ext.asColor
import maxeem.america.ext.asString
import maxeem.america.ext.tru
import maxeem.america.gdg.NavGraphDirections
import maxeem.america.gdg.R
import maxeem.america.gdg.databinding.FragmentGdgListBinding
import maxeem.america.gdg.domain.GdgChapter
import maxeem.america.gdg.misc.Conf
import maxeem.america.gdg.misc.LocationHelper
import maxeem.america.gdg.repo.ApiStatus
import maxeem.america.gdg.ui.adapter.AdapterItemsDashDecoration
import maxeem.america.gdg.ui.adapter.GdgListAdapter
import maxeem.america.gdg.ui.adapter.GdgLoadingAdapter
import maxeem.america.gdg.ui.transition.Stagger
import maxeem.america.type.Bool
import org.jetbrains.anko.info
import org.jetbrains.anko.landscape
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

private const val LOCATION_PERMISSION = "android.permission.ACCESS_COARSE_LOCATION"

@RuntimePermissions
class GdgListFragment : BaseFragment() {

    private val model: GdgListViewModel by viewModels { SavedStateViewModelFactory(app, this)}
    private val locationHelper by lazy {
        LocationHelper(requireContext()) {
            model.onLocationUpdated(it)
        }
    }

    private lateinit var searchView: SearchView
    private fun SearchView.isOpened() = !isIconified
    private fun SearchView.clearAndClose() {
        setQuery("", false)
        isIconified = true
    }

    override fun consumeBackPressed(): Boolean {
        return if (searchView.isOpened()) {
            searchView.clearAndClose()
            true
        } else super.consumeBackPressed()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?)
        = FragmentGdgListBinding.inflate(inflater).apply {

        lifecycleOwner = viewOwner
        viewModel = model

        toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        fun updateTitle() {
            val regionLabel = model.region.value ?: R.string.global.asString()
            toolbar.title = if (model.region.value == null) R.string.gdg_global.asString() else R.string.gdg_in.asString(regionLabel)
        }

        val gdgListAdapter = GdgListAdapter { v ->
            startActivity(Intent(Intent.ACTION_VIEW, (v.tag as GdgChapter).website.toUri()))
        }.apply {
            setFilterable(viewLifecycleOwner.lifecycleScope) {
                appbar.setExpanded(true, true)
                appbar.setLifted(false)
                recycler.scrollToPosition(0)
                empty.visibleOn(it.isEmpty())
            }
        }
        recycler.addItemDecoration(
            AdapterItemsDashDecoration(
                R.color.listItemsDecorationLine
            )
        )
        val titleText = lambda@ { show: Bool ->
            if (resources.configuration.landscape) return@lambda
            val colorFrom = if (!show) R.color.primaryColor.asColor() else android.R.color.transparent.asColor()
            val colorTo = if (!show) android.R.color.transparent.asColor() else R.color.primaryColor.asColor()
            ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo).apply {
                addUpdateListener { animator ->
                    toolbar.setTitleTextColor(animator.animatedValue as Int)
                }
                start()
            }
        }
        val searchItem = toolbar.menu.findItem(R.id.search)
        searchView = (searchItem.actionView as SearchView).apply {
            queryHint = R.string.where_search.asString(R.string.global.asString())
            isSubmitButtonEnabled = false
            setOnSearchClickListener {
                TransitionManager.beginDelayedTransition(toolbar, ChangeBounds())
                titleText(false)
            }
            setOnCloseListener {
                TransitionManager.beginDelayedTransition(toolbar, ChangeBounds())
                updateTitle()
                titleText(true)
                appbar.setExpanded(true, true)
                false
            }
            setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                var lastQueryText = ""
                override fun onQueryTextChange(newText: String?) = true.apply exit@ {
                    val newQueryText = newText?.trim() ?: ""
                    if (lastQueryText != newQueryText) {
                        gdgListAdapter.filter(newQueryText)
                        lastQueryText = newQueryText
                    }
                }
                override fun onQueryTextSubmit(query: String?) = false
            })
        }
        val title = {
            if (toolbar.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(appbar.parent as ViewGroup, AutoTransition())
                toolbar.visibleOn(true)
            } else {
                TransitionManager.beginDelayedTransition(appbar, AutoTransition())
            }
            appbar.setExpanded(true, true)
            appbar.setLifted(false)
            updateTitle()
        }
        val regions = lambda@ { animate: Bool ->
            val regionList = model.regionList.value
            if (regionList.isNullOrEmpty()) return@lambda
            info(" regionList observe, data size: ${regionList.size}, model region: ${model.region.value}")
            val regionGroup = ChipGroup(activity!!).apply {
                isSingleLine = true
                isSingleSelection = true
            }
            val chipInflater = LayoutInflater.from(activity!!)
            regionList.forEach { regionName ->
                chipInflater.inflate(R.layout.region_chip, regionGroup, false).apply { this as Chip
                    regionGroup.addView(this)
                    tag = regionName; text = regionName
                    if (model.region.value == regionName)
                        isChecked = true
                }
            }
            regionsScroll.removeAllViews()
            if (animate && bottomAppbar.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(bottomAppbar.parent as ViewGroup, Slide(Gravity.BOTTOM).apply {
                    addListener(object: TransitionListenerAdapter() {
                        override fun onTransitionEnd(transition: Transition) {
                            TransitionManager.beginDelayedTransition(regionsScroll, Fade())
                            regionsScroll.addView(regionGroup, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
                        }
                    })
                })
            } else {
                regionsScroll.addView(regionGroup, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
                bottomAppbar.performShow()
            }
            bottomAppbar.visibility = View.VISIBLE
            regionGroup.setOnCheckedChangeListener { group, id ->
                val newRegion = if (id == View.NO_ID) null else group.findViewById<Chip>(id).tag as String
                info(" chip checked: $id, new region: $newRegion, model region: ${model.region.value}")
                if (model.region.value != newRegion)
                    model.onRegionChanged(newRegion)
            }
        }
        recycler.adapter = model.gdgList.value.isNullOrEmpty() tru {
            GdgLoadingAdapter()
        } ?: gdgListAdapter
        val stagger = Stagger().apply {
            addListener(object: TransitionListenerAdapter() {
                override fun onTransitionEnd(transition: Transition) {
                    removeListener(this)
                    viewOwner?.lifecycleScope?.launchWhenCreated {
                        title()
                        viewOwner?.lifecycleScope?.launchWhenCreated {
                            delay(300)
                            regions(true)
                        }
                    }
                }
            })
        }
        model.status.observe(viewLifecycleOwner) { status ->
            if (searchView.isOpened())
                searchView.clearAndClose()
            (status == ApiStatus.Loading && model.hasData.value ?: false) tru {
                gdgListAdapter.submitList(emptyList())
            }
        }
        model.gdgList.observe(viewLifecycleOwner) { gdgList ->
            (recycler.adapter != gdgListAdapter) tru {
                model.dataChangedEvent.value?.consume()
                TransitionManager.beginDelayedTransition(recycler, stagger)
                recycler.adapter = gdgListAdapter
                gdgListAdapter.submitList(gdgList)
            } ?: {
                empty.visibleOn(false)
                val hasChanged = model.dataChangedEvent.value?.consume() ?: false
                gdgListAdapter.submitList(gdgList) {
                    title()
                    searchView.queryHint = R.string.where_search.asString(
                        model.region.value ?: R.string.global.asString()
                    )
                    searchItem.isVisible = gdgList.isNotEmpty()
                    searchView.onActionViewCollapsed()
                    hasChanged tru {
                        recycler.scrollToPosition(0)
                    }
                    if (regionsScroll.getChildAt(0).id == R.id.stub)
                        regions(false)
                }
            }()
        }
        model.applyEvent.observe(viewLifecycleOwner) {
            it.consume() ?: return@observe
            startActivity(Intent(Intent.ACTION_VIEW, Conf.GDG.APPLY_FORM_URL.toUri()))
        }
        model.aboutEvent.observe(viewLifecycleOwner) {
            it.consume() ?: return@observe
            findNavController().navigate(NavGraphDirections.actionGlobalFragmentAbout())
        }

    }.root

    override fun onStart() { super.onStart()
        if (locationHelper.client != null) {
            locationHelper.listenToUpdates()
        } else viewOwner?.lifecycleScope?.launch {
            delay(2_000)
            viewOwner?.lifecycleScope?.launchWhenStarted {
                if (!locationHelper.hasRequestedLastLocationOnStart) {
                    locationHelper.hasRequestedLastLocationOnStart = true
                    requestLastLocationWithPermissionCheck()
                }
            }
        }
    }
    override fun onStop() { super.onStop()
        locationHelper.stopUpdates()
        searchView.clearFocus()
    }

    @NeedsPermission(LOCATION_PERMISSION)
    fun requestLastLocation() {
        locationHelper.requestLastLocation()
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

}
