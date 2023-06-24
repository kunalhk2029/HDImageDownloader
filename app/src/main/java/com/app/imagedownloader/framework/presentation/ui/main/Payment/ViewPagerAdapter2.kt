package com.app.instastorytale.framework.presentation.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.app.imagedownloader.framework.AdsManager.BannerAdPlaceholder

class ViewPagerAdapter2(
    manager: FragmentManager,
    val lifecycle: Lifecycle,
    val list: HashMap<Int, BannerAdPlaceholder>
) : FragmentStateAdapter(manager, lifecycle) {
    override fun getItemCount(): Int {
        return list.size
    }

    override fun createFragment(position: Int): Fragment {
        return try {
            list[position]!!
        } catch (e: Exception) {
            Fragment()
        }
    }
}