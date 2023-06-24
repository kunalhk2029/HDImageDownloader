package com.app.imagedownloader.framework.presentation.ui.OnBoarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.app.imagedownloader.business.data.SharedPreferencesRepository.SharedPrefRepository
import com.app.imagedownloader.databinding.ActivityOnBoardingBinding
import com.app.imagedownloader.framework.presentation.Adapters.OnBoardingStatePagerAdapter
import com.app.imagedownloader.framework.presentation.ui.OnBoarding.Features_screen.OnBoarding1
import com.app.imagedownloader.framework.presentation.ui.OnBoarding.Features_screen.OnBoarding10
import com.app.imagedownloader.framework.presentation.ui.OnBoarding.Features_screen.OnBoarding4
import com.app.imagedownloader.framework.presentation.ui.OnBoarding.Features_screen.OnBoarding9
import com.app.imagedownloader.framework.presentation.ui.main.MainActivity
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@AndroidEntryPoint
class OnBoardingActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPrefRepository: SharedPrefRepository
    lateinit var adapter: OnBoardingStatePagerAdapter
    var pageno = 0
    val viewModel by viewModels<OnBoardingViewModel>()
    var binding: ActivityOnBoardingBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        adapter =
            OnBoardingStatePagerAdapter(
                lifecycle, manager = supportFragmentManager, listOf(
                    OnBoarding1(),
                    OnBoarding10(),
                    OnBoarding9(),
                    OnBoarding4(),
                )
            )
        binding?.onboardingviewpager?.adapter = adapter
        TabLayoutMediator(binding!!.tablayout, binding!!.onboardingviewpager) { _, _ ->
        }.attach()

        val pageCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                pageno = position
            }
        }
        binding?.onboardingviewpager?.registerOnPageChangeCallback(pageCallback)
        viewModel.nextListener.receiveAsFlow().onEach {
            if (it) {
                pageno++
                binding!!.onboardingviewpager.setCurrentItem(pageno, false)
            }
        }.launchIn(lifecycleScope)

        viewModel.skipListener.receiveAsFlow().onEach {
            if (it) {
                binding?.skipshadow?.visibility = View.VISIBLE
                binding?.loadingpb?.visibility = View.VISIBLE
                sharedPrefRepository.setOnBoarding(false)
                val intent = Intent(this@OnBoardingActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.launchIn(lifecycleScope)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}