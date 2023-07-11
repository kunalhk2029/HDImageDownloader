package com.app.imagedownloader.business.interactors.singleImagePreview

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.app.imagedownloader.R
import com.app.imagedownloader.Utils.Constants.StorageDirectoryConstants.IMAGE_MEDIA_DIRECTORY
import com.app.imagedownloader.business.data.network.retrofit.RetrofitInstance
import com.app.imagedownloader.framework.AdsManager.GeneralAdsManager
import com.app.imagedownloader.framework.Utils.Logger
import com.app.imagedownloader.framework.presentation.ui.main.MainActivity
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import okhttp3.ResponseBody
import retrofit2.Call
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaveMediaInScopedStorage
@Inject constructor(
    private val contentResolver: ContentResolver, private val context: Context,
    private val generalAdsManager: GeneralAdsManager,
) {
    operator fun invoke(url: String, contextt: Context,colorCode: Int ,playclicked: () -> Unit) {
        download(
            url, context = contextt, playclicked,colorCode
        )
    }

    private suspend fun storeMediaByParts(it: OutputStream, ips: InputStream) {
        val bool = true
        val byte = ByteArray(1024)
        while (bool) {
            val chunk = ips.read(byte)
            if (chunk > 0) {
                it.write(byte, 0, chunk)
            }
            if (chunk <= 0) {
                break
            }
        }
        ips.close()
    }

    private var downloadJob: CompletableJob = Job()

    private fun download(
        uri: String,
        context: Context,
        playclicked: () -> Unit = {},
        colorCode: Int
    ) {

        downloadJob = Job()
        lateinit var d: MaterialDialog
        lateinit var errord: MaterialDialog
        lateinit var completedd: MaterialDialog
        CoroutineScope(Default + downloadJob).launch {
            var downloadingCall: Call<ResponseBody>? = null
            withContext(Main) {
                d = MaterialDialog(context).show {
                    cancelOnTouchOutside(false)
                    setContentView(R.layout.downloadingdialog)
                }

                d.findViewById<TextView>(R.id.dismisssd).setOnClickListener {
                    downloadingCall?.cancel()
                    downloadJob.cancel("Cancelled By User")
                    d.dismiss()
                }

                d.findViewById<NativeAdView>(R.id.native_ad_view)
                    ?.let {
                        generalAdsManager.showNativeHomeScreenAd(it).let {
                            withContext(Default) {
                                downloadingCall = RetrofitInstance.api.downloadMedia(
                                    uri
                                )
                            }
                        }
                    }
            }

            delay(1000L)
            var fname = ""

            fname = "IMG_${
                UUID.randomUUID()
            }_##${colorCode}##.jpg"


            downloadingCall?.let {
                downloadForSavingFromStreamingUrl(
                    it,
                    name = fname
                )
            }

            downloadJob.invokeOnCompletion {
                Logger.log("654654654  = " + it?.message)
                if (it?.message == "Error") {
                    CoroutineScope(Main).launch {
                        d.dismiss()
                        errord = MaterialDialog(context).show {
                            cancelOnTouchOutside(false)
                            setContentView(R.layout.sharingerror)
                        }
                        errord.findViewById<TextView>(R.id.dismisssd).setOnClickListener {
                            errord.dismiss()
                        }
                    }
                }
                if (it == null) {
                    CoroutineScope(Main).launch {
                        val showCompleteDialog = {
                            CoroutineScope(Main).launch {
                                completedd = MaterialDialog(context).show {
                                    cancelOnTouchOutside(false)
                                    setContentView(R.layout.downloadcompleteddialog)
                                }
                                completedd.findViewById<NativeAdView>(R.id.native_ad_view)
                                    ?.let { generalAdsManager.showNativeHomeScreenAd(it) }

                                completedd.findViewById<TextView>(R.id.dismisssd)
                                    .setOnClickListener {
                                        completedd.dismiss()
                                    }
                                completedd.findViewById<TextView>(R.id.playdownloaded)
                                    .setOnClickListener {
                                        playclicked.invoke()
                                        completedd.dismiss()
                                    }
                            }
                        }

                        val hideDownloading = {
                            CoroutineScope(Main).launch {
                                d.dismiss()
                            }
                        }
                        launch {
                            val list = listOf(showCompleteDialog, hideDownloading)
                            MainActivity.showFullScreenAds.send(list)
                        }
                    }
                }
            }
        }
    }

    private suspend fun downloadForSavingFromStreamingUrl(
        g: Call<ResponseBody>,
        name: String,
    ) {
        g.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: retrofit2.Response<ResponseBody>,
            ) {
                if (response.isSuccessful) {
                    val ips = response.body()?.byteStream()
                    CoroutineScope(Default + downloadJob).launch {
                        val savingstatus = ips?.let {
                            saveInSharedStorage(
                                name = name, it
                            )
                        }
                        Logger.log("6848998 ousidev = " + savingstatus)

                        savingstatus?.let {
                            if (it) {
                                downloadJob.complete()
                            } else {
                                downloadJob.cancel("Error")
                            }
                        } ?: (downloadJob.cancel("Error"))
                    }
                } else {
                    downloadJob.cancel("Error")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                downloadJob.cancel("Error")
            }
        })
    }

    private suspend fun saveInSharedStorage(
        name: String,
        ips: InputStream,
    ): Boolean {
        var uri: Uri? = null
        withContext(IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, name)
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/$IMAGE_MEDIA_DIRECTORY/"
                    )
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                }

                uri = contentResolver.insert(
                    MediaStore.Images.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL_PRIMARY
                    ),
                    contentValues
                )

                if (uri != null) {
                    contentResolver.openOutputStream(uri!!).use {
                        it?.let { it1 ->
                            storeMediaByParts(it1, ips)
                        } ?: {
                            contentResolver.delete(uri!!, null, null)
                            uri = null
                        }
                    }
                }
            } else {
                var ext = ".jpg"
                lateinit var dr: File
                dr = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                )

                if (!dr.exists()) {
                    dr.mkdir()
                }

                val fol = File(
                    dr.path,
                    "/$IMAGE_MEDIA_DIRECTORY/"
                )
                fol.mkdirs()

                val fileSavePath =
                    File(
                        fol.path,
                        name

                    )
                val fos =
                    FileOutputStream(fileSavePath)

                storeMediaByParts(fos, ips)
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(fileSavePath.toString()),
                    null
                ) { _, _ ->
                }
                ips.close()
            }
        }
        return uri != null
    }
}