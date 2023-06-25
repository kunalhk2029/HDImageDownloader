package com.app.imagedownloader.framework.presentation.ui

import com.app.imagedownloader.business.domain.core.DataState.DataState


interface UICommunicationListener {

    fun showBannerAdOnLoadingFinished(dataState: DataState<*>?, force: Boolean = false)

    fun askForRating(force: Boolean = false)

    fun setToolbarTitleText(title: String)

    fun hideToolbar()

    fun showToolbar()

    fun showTooltip(text: String, onHide: (() -> Unit)?)

    fun showDimScreen()

    fun hideDimScreen()

    fun lockDrawer()

    fun unlockDrawer()

    fun closeDrawer()

    fun showBottomNav()

    fun hideBottomNav()

    fun hideBannerAd()

    fun askPermission(execute: () -> Unit)

}