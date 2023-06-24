package com.app.imagedownloader.framework.presentation.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnBoardingStatePagerAdapter(
    lifecycle: Lifecycle,
    manager: FragmentManager,
    val list: List<Fragment>
) : FragmentStateAdapter(manager, lifecycle) {
    override fun getItemCount(): Int {
        return list.size
    }

    override fun createFragment(position: Int): Fragment {
        return list[position]
    }
}