package com.app.imagedownloader.framework.presentation.ui.StartUp

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.app.imagedownloader.R
import com.app.imagedownloader.business.data.SharedPreferencesRepository.SharedPrefRepository
import com.app.imagedownloader.framework.Glide.GlideManager
import com.app.imagedownloader.framework.presentation.ui.main.MainActivity
import com.app.imagedownloader.framework.presentation.ui.OnBoarding.OnBoardingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StartupActivity : AppCompatActivity() {

    @Inject
    lateinit var glideManager: GlideManager

    @Inject
    lateinit var sharedPrefRepository: SharedPrefRepository
    private var startupimg: ImageView? = null
    private var opened = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)
        startupimg = findViewById(R.id.startupimg)
    }

    override fun onResume() {
        super.onResume()
        if (!opened) {
            if (sharedPrefRepository.getOnBoarding()) {
                val intent = Intent(this@StartupActivity, OnBoardingActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                CoroutineScope(Main).launch {
                    sharedPrefRepository.getTheme().run {
                        if (this == 0) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        } else if (this == 1) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        } else {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        }
                    }
                    startupimg?.let {
                        glideManager.setImageFromUrl(it, R.drawable.artboard_1,glideSuccessUnit = {
                            CoroutineScope(Main).launch {
                            delay(1200L)
                                val intent =
                                    Intent(
                                        this@StartupActivity,
                                        MainActivity::class.java
                                    )
                                startActivity(intent)
                                opened = true
                                finish()
                            }
                            kotlin.run {
                            }
                        })
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        startupimg = null
    }
}