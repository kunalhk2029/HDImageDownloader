package com.app.imagedownloader.framework.presentation.ui.StartUp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
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
    var startupimg: ImageView? = null
    var opened = false
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
                    startupimg?.let {
                        glideManager.setImageFromUrl(it, R.drawable.ic_layer_1)
                    }
                    delay(1200L)
                    launch {
                        val intent =
                            Intent(
                                this@StartupActivity,
                                MainActivity::class.java
                            )
                        startActivity(intent)
                        opened = true
                        finish()
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