package com.app.imagedownloader.framework.presentation.ui.AppUpdateInfo

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.app.imagedownloader.R
import com.app.imagedownloader.databinding.ActivityUpdateAvailableBinding

class UpdateAvailableActivity : AppCompatActivity() {
    var binding: ActivityUpdateAvailableBinding? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateAvailableBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        val criticalUpdate = intent.getBooleanExtra("criticalUpdate", false)
        binding?.let {
            if (criticalUpdate) {
                it.updatenotnow.visibility = View.GONE
                it.updateheading.text = getString(R.string.criticalupdate)
                it.message.text = getString(R.string.mandatoryupdatewarning)
            }
            it.updatenow.setOnClickListener {
                val intent = Intent().apply {
                    action = Intent.ACTION_VIEW
                    data =
                        Uri.parse("https://play.google.com/store/apps/details?id=com.app.imagedownloader")
                }
                startActivity(intent)
                onBackPressedDispatcher.onBackPressed()
                finish()
            }

            it.updatenotnow.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}