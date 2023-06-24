package com.app.imagedownloader.framework.presentation.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.app.imagedownloader.R
import com.app.imagedownloader.Utils.VibrateExtension
import com.app.imagedownloader.business.domain.DataState.DataState
import com.app.imagedownloader.databinding.ActivityMainBinding
import com.app.imagedownloader.framework.AdsManager.GeneralAdsManager
import com.app.imagedownloader.framework.AdsManager.GeneralAdsManager.Companion.bannerAdVisibilityHidden
import com.app.imagedownloader.framework.Glide.GlideManager
import com.app.imagedownloader.framework.InAppUpdatesManager.InAppUpdatesManager
import com.app.imagedownloader.framework.PlayStoreRatingFlow.PlayStoreRatingFlow
import com.app.imagedownloader.framework.Utils.Logger
import com.app.imagedownloader.framework.presentation.ui.UICommunicationListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), UICommunicationListener {

    @Inject
    lateinit var playStoreRatingFlow: PlayStoreRatingFlow

    @Inject
    lateinit var updatesManager: InAppUpdatesManager

    @Inject
    lateinit var vibrateExtension: VibrateExtension

    @Inject
    lateinit var glideManager: GlideManager


    @Inject
    lateinit var generalAdsManager: GeneralAdsManager

    var binding: ActivityMainBinding? = null
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var onDestinationChangedListener: NavController.OnDestinationChangedListener

    val viewModel by viewModels<MainViewModel>()

    companion object {
        val IS_SHARED = "IS_SHARED"
        var premiumLiveData: Channel<Int> = Channel()
        var adsInfoLoadingStatus: MutableSharedFlow<Boolean> = MutableSharedFlow()
        var showInterstitialAd: Channel<List<(() -> Job)>?> = Channel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        val navView: BottomNavigationView = binding!!.navView

        navController = findNavController(R.id.fragmentContainerView)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            topLevelDestinationIds = setOf(
                R.id.home,
                R.id.downloadedMedia,
                R.id.settings,
            )
        )
        navView.setupWithNavController(navController)

        setupNavigation()

        handlePremiumClick()

        handlePremiumNav()

        handleNavDrawerClick()

        handleBottomNavClick()

        handleStoragePermission()

        handleAdsOnDownloadCompletion()

        askForRating()

    }

    private fun handleStoragePermission() {
        if (!isreadallowed()) {
            requestreadperm()
        }
    }

    private fun handleAdsOnDownloadCompletion() {
        lifecycleScope.launch {
            showInterstitialAd.receiveAsFlow().collectLatest { job ->
                generalAdsManager.handleNativeFull(
                    { job?.first()?.invoke() },
                    this@MainActivity, 0, false, afterInterstitialShown = {
                        job?.last()?.invoke()
                    }
                )
            }
        }
    }

    private fun isreadallowed(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun handlePremiumNav() {
        premiumLiveData.receiveAsFlow().onEach {
            if (it == 1 || it == 2) {
                showToolbar()
                binding?.nativeInterstitialAd?.root?.visibility = View.GONE
                navController.navigate(
                    R.id.payment,
                    null,
                    NavOptions.Builder().setEnterAnim(R.anim.fromright).setExitAnim(R.anim.toleft)
                        .setPopExitAnim(R.anim.toright).setPopEnterAnim(R.anim.fromleft).build()
                )
            }
            if (it == 3) navController.navigate(
                R.id.payment,
                null,
                NavOptions.Builder().setEnterAnim(R.anim.fromright).setExitAnim(R.anim.toleft)
                    .setPopExitAnim(R.anim.toright).setPopEnterAnim(R.anim.fromleft).build()
            )
        }.launchIn(lifecycleScope)
    }

    private fun handlePremiumClick() {
        binding?.adtext?.findViewById<View>(R.id.goadsfreesmall)?.setOnClickListener {
            lifecycleScope.launch {
                premiumLiveData.send(3)
            }
        }
        binding?.nativeInterstitialAd?.pbt?.setOnClickListener {
            binding?.root?.findViewById<View>(R.id.native_interstitial_ad)?.visibility = View.GONE
            lifecycleScope.launch {
                premiumLiveData.send(1)
            }
        }
    }

    override fun showTooltip(text: String, onHide: (() -> Unit)?) {
    }


    private fun handleBottomNavClick() {
        binding?.let {
            it.navView.menu[0].setOnMenuItemClickListener {
                if (handleBottomNavBackstack(R.id.home)) return@setOnMenuItemClickListener true
                navController.navigate(
                    R.id.home, null
                )
                true
            }

            it.navView.menu[1].setOnMenuItemClickListener {
                if (handleBottomNavBackstack(R.id.downloadedMedia)) return@setOnMenuItemClickListener true
                navController.navigate(
                    R.id.downloadedMedia, null
                )
                true
            }

            it.navView.menu[2].setOnMenuItemClickListener {
                if (handleBottomNavBackstack(R.id.settings)) return@setOnMenuItemClickListener true
                navController.navigate(
                    R.id.settings, null
                )
                true
            }
        }
    }


    private fun handleNavDrawerClick() {
//        handleDrawerHeader()
//        binding?.topAppBar?.setNavigationOnClickListener {
//            if (viewModel.showBottomTooltip()) return@setNavigationOnClickListener
//            if (navController.currentDestination?.id == R.id.autodownloadtiles) navController.navigate(
//                R.id.action_autodownloadtiles_to_bottomMenu
//            )
//            else onBackPressed()
//        }
    }

    override fun setToolbarTitleText(title: String) {
        binding?.topAppBar?.let {
            it.title = title
        } ?: kotlin.run {
            lifecycleScope.launch {
                while (binding == null) {
                    delay(250L)
                }
                binding?.topAppBar?.title = title
            }
        }
    }

    override fun hideToolbar() {
        binding?.topAppBar?.let {
            it.isVisible = false
        } ?: kotlin.run {
            lifecycleScope.launch {
                while (binding == null) {
                    delay(250L)
                }
                binding?.topAppBar?.isVisible = false
            }
        }
    }

    override fun showToolbar() {
        binding?.topAppBar?.let {
            it.isVisible = true
        } ?: kotlin.run {
            lifecycleScope.launch {
                while (binding == null) {
                    delay(250L)
                }
                binding?.topAppBar?.isVisible = true
            }
        }
    }

    override fun askForRating(force: Boolean) {
        lifecycleScope.launch {
            delay(500L)
            playStoreRatingFlow.askForRating(force, this@MainActivity)
        }
    }

    override fun showDimScreen() {
        binding?.let {
            it.tooltipshadow.visibility = View.VISIBLE
        } ?: kotlin.run {
            lifecycleScope.launch {
                while (binding == null) {
                    delay(250L)
                }
                binding?.tooltipshadow?.visibility = View.VISIBLE
            }
        }
    }

    override fun hideDimScreen() {
        binding?.let {
            it.tooltipplaceholder.visibility = View.GONE
            it.tooltipshadow.visibility = View.GONE
        } ?: kotlin.run {
            lifecycleScope.launch {
                while (binding == null) {
                    delay(250L)
                }
                binding?.tooltipshadow?.visibility = View.GONE
            }
        }
    }

    override fun showBannerAdOnLoadingFinished(dataState: DataState<*>?, force: Boolean) {
        dataState?.loading?.let {
            if (!it) {
                showBannerAd()
            }
        } ?: kotlin.run {
            if (force) {
                showBannerAd()
            }
        }
    }

    override fun hideBannerAd() {
        bannerAdVisibilityHidden = true
        binding?.root?.findViewById<ConstraintLayout>(R.id.badd)?.visibility = View.GONE
        binding?.adtext?.visibility = View.GONE
    }

    private fun showBannerAd() {
        bannerAdVisibilityHidden = false
        binding?.let {
            lifecycleScope.launch {
                it.root.findViewById<ConstraintLayout>(R.id.badd).let { it1 ->
                    viewModel.generalAdsManager.showNativeBannerAd(
                        it1, it.appbar, it.adtext, supportFragmentManager, lifecycle
                    )
                }
            }
        }
    }

    private fun setCurrentScreen(screenName: String) = FirebaseAnalytics.getInstance(this).run {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName);
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    override fun showBottomNav() {
        binding?.navView?.let {
            it.visibility = View.VISIBLE
        } ?: kotlin.run {
            lifecycleScope.launch {
                while (binding == null) {
                    delay(100L)
                }
                binding?.navView?.visibility = View.VISIBLE
            }
        }
    }

    override fun hideBottomNav() {
        binding?.navView?.let {
            it.visibility = View.GONE
        } ?: kotlin.run {
            lifecycleScope.launch {
                while (binding == null) {
                    delay(100L)
                }
                binding?.navView?.visibility = View.GONE
            }
        }
    }

    override fun lockDrawer() {
        binding?.let {
            it.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        } ?: kotlin.run {
            lifecycleScope.launch() {
                while (binding == null) {
                    delay(100L)
                }
                binding?.drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }
    }

    override fun closeDrawer() {
        binding?.let {
            it.drawerLayout.close()
        } ?: kotlin.run {
            lifecycleScope.launch() {
                while (binding == null) {
                    delay(100L)
                }
                binding?.drawerLayout?.close()
            }
        }
    }

    override fun unlockDrawer() {
//        binding?.let {
//            it.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
//        } ?: kotlin.run {
//            lifecycleScope.launch() {
//                while (binding == null) {
//                    delay(100L)
//                }
//                binding?.drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
//            }
//        }
    }

    override fun askPermission(execute: () -> Unit) {
        requestreadperm()
        viewModel.permissionGranted.receiveAsFlow().onEach {
            if (it) {
                execute.invoke()
                viewModel.permissionGranted.close()
            }
        }.launchIn(lifecycleScope)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                lifecycleScope.launch {
                    try {
                        viewModel.permissionGranted.send(true)
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }


    private fun setupNavigation() {
        binding?.let {
            it.navifationView.setupWithNavController(navController)
            it.topAppBar.setupWithNavController(navController, it.drawerLayout)
            setSupportActionBar(it.topAppBar)
            it.topAppBar.setNavigationOnClickListener { _ ->
//                if ((navController.currentDestination?.id == R.id.autoDownloadManager || navController.currentDestination?.id == R.id.autodownloadtiles || navController.currentDestination?.id == R.id.favouriteusers || navController.currentDestination?.id == R.id.favouritestories) && navController.backQueue.size <= 2) {
//                    binding?.drawerLayout?.open()
//                } else {
//                    onBackPressedDispatcher.onBackPressed()
//                }
            }
        }
        onDestinationChangedListener =
            NavController.OnDestinationChangedListener { _, destination, _ ->
                setCurrentScreen(destination.displayName)
                vibrateExtension.vibrate(true)

                val disableBannerAd =
                    destination.id != R.id.downloadedMedia

                if (disableBannerAd) {
                    hideBannerAd()
                }
//                else if (destination.id != R.id.bottomMenu) {
//                    binding?.appbar?.setExpanded(true)
//                }
                if (
                    destination.id == R.id.payment ||
                    destination.id == R.id.singleImagePreview ||
                    destination.id == R.id.searchResultPhotosPreview ||
                    destination.id == R.id.downloadOptionsBottomSheet ||
                    destination.id == R.id.moreOptionsBottomSheet ||
                    navController.backQueue.find { it.destination.id==R.id.singleImagePreview }!=null||
                    destination.id == R.id.feedback
                        ) {
                    hideBottomNav()
                } else {
                    showBottomNav()
                }
            }
    }

    @SuppressLint("RestrictedApi")
    private fun handleBottomNavBackstack(id: Int): Boolean {
        if (navController.backQueue.any { it.destination.id == id } && navController.currentDestination?.id != id) {
            navController.popBackStack(id, false)
            return true
        }
        if (navController.currentDestination?.id == id) {
            return true
        }
        return false
    }

    @SuppressLint("RestrictedApi")
    private fun handlePopBackstack(id: Int): Boolean {
        val f = navController.backQueue.size
        if (f >= 3 || navController.currentDestination?.id == id) {
            Logger.log("Debug 5665656t = t = true")
            return true
        }
        return false
    }

    private fun requestreadperm() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 100
        )
    }

    override fun onPause() {
        super.onPause()
        navController.removeOnDestinationChangedListener(onDestinationChangedListener)
    }

    override fun onResume() {
        super.onResume()
        navController.addOnDestinationChangedListener(onDestinationChangedListener)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.fragmentContainerView).navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}