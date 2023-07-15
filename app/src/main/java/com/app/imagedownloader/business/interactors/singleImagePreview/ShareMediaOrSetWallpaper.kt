package com.app.imagedownloader.business.interactors.singleImagePreview

import android.app.WallpaperManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.afollestad.materialdialogs.MaterialDialog
import com.app.imagedownloader.R
import com.app.imagedownloader.Utils.Constants.StorageDirectoryConstants.MEDIA_FILE_SHARING_DIRECTORY
import com.app.imagedownloader.business.data.network.retrofit.RetrofitInstance
import com.app.imagedownloader.framework.Utils.Logger
import com.app.imagedownloader.framework.WorkManagers.WorkerDeleteShared
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import okhttp3.ResponseBody
import retrofit2.Call
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ShareMediaOrSetWallpaper
@Inject constructor(
    private val contentResolver: ContentResolver,
    private val context: Context,
) {

    private val workManager = WorkManager.getInstance(context)
    private var sharingJob: CompletableJob = Job()
    private var fileForSharing: Uri? = null

    operator fun invoke(uri: String, setWallpaper: Boolean, contextt: Context) {
        shareAndSetWallpaper(uri, setWallpaper, contextt)
    }

    private fun shareAndSetWallpaper(uri: String, setWallpaper: Boolean, contextt: Context) {
        sharingJob.cancel()
        sharingJob = Job()
        lateinit var d: MaterialDialog
        lateinit var errord: MaterialDialog
        CoroutineScope(Dispatchers.Default + sharingJob).launch {
            var sharingCall: Call<ResponseBody>? = null
            withContext(Dispatchers.Main) {
                d = MaterialDialog(contextt).show {
                    cancelOnTouchOutside(false)
                    setContentView(R.layout.sharing)
                }
                if (setWallpaper)
                    d.findViewById<TextView>(R.id.sharingd).text =
                        context.getString(R.string.settingwallpaper)

                d.findViewById<TextView>(R.id.dismisssd).setOnClickListener {
                    sharingJob.cancel("Cancelled By User")
                    sharingCall?.cancel()
                    d.dismiss()
                }
            }
            delay(1000L)

            var fname = ""

            fname = "IMG_${
                UUID.randomUUID()
            }.jpg"
            if (uri.contains("file:///data/user/0/com.app.imagedownloader/")) {
                contentResolver.openInputStream(Uri.parse(uri)).let {
                    shareFromStreamingUrl(null, fname, ipss = it)
                }
            } else {
                sharingCall = RetrofitInstance.api.downloadMedia(uri)
                sharingCall.let {
                    shareFromStreamingUrl(it, fname, ipss = null)
                }
            }

            sharingJob.invokeOnCompletion {
                if (it?.message == "Error") {
                    CoroutineScope(Dispatchers.Main).launch {
                        d.dismiss()
                        errord = MaterialDialog(contextt).show {
                            cancelOnTouchOutside(false)
                            setContentView(R.layout.sharingerror)
                        }
                        errord.findViewById<TextView>(R.id.dismisssd).setOnClickListener {
                            errord.dismiss()
                        }
                    }
                }
                if (it == null) {
                    CoroutineScope(Dispatchers.Main).launch {
                        d.dismiss()
                        if (setWallpaper) {
                            val manager =
                                context.getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager
                            try {
                                manager.getCropAndSetWallpaperIntent(
                                    getFileProviderUriFromFileUri(
                                        File(fileForSharing?.path)
                                    )
                                ).let {
                                    contextt.startActivity(it)
                                }
                            } catch (e: Exception) {
                                val bitmap = BitmapFactory.decodeStream(
                                    contentResolver.openInputStream(getFileProviderUriFromFileUri(
                                        File(fileForSharing?.path)
                                    ))
                                )
                                manager.setBitmap(bitmap)
                            }
                            fileForSharing!!.path?.let { it1 -> enqueueWorker(it1) }
                        } else {
                            Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "image/jpeg"
                                if (fileForSharing != null) {
                                    putExtra(
                                        Intent.EXTRA_STREAM,
                                        getFileProviderUriFromFileUri(
                                            File(fileForSharing?.path)
                                        )
                                    )
                                    val chooser = Intent.createChooser(this, "Share Story")
                                    chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    context.startActivity(chooser)
                                    fileForSharing!!.path?.let { it1 -> enqueueWorker(it1) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun shareFromStreamingUrl(
        g: Call<ResponseBody>?,
        name: String,
        ipss: InputStream? = null,
    ) {
        g?.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: retrofit2.Response<ResponseBody>,
            ) {
                if (response.isSuccessful) {
                    val ips = response.body()?.byteStream()
                    saveInStorageForSharing(name, ips)
                } else {
                    sharingJob.cancel("Error")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                sharingJob.cancel("Error")
            }
        }) ?: kotlin.run {
            saveInStorageForSharing(name, ipss)
        }
    }

    private fun enqueueWorker(uri: String) {
        val req = OneTimeWorkRequestBuilder<WorkerDeleteShared>()
            .setInitialDelay(10, TimeUnit.MINUTES)
            .setInputData(workDataOf("URI" to uri))
            .build()
        Logger.log("8598598595 Worker enqued.........")
        workManager.enqueue(req)
    }

    private fun getFileProviderUriFromFileUri(tempfile: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "com.app.imagedownloader.fileprovider",
            tempfile
        )
    }

    private fun saveInStorageForSharing(name: String, ips: InputStream?) {
        val tempfile = File(
            makeInternalFileDir(),
            name
        )

        contentResolver.openOutputStream(
            Uri.fromFile(
                tempfile
            )
        )?.let { os ->
            if (ips != null) {
                CoroutineScope(IO).launch {
                    storeMediaByParts(os, ips)
                    fileForSharing = Uri.fromFile(tempfile)
                    sharingJob.complete()
                }
            } else {
                sharingJob.cancel("Error")
            }
        }
    }

    private fun makeInternalFileDir(): File {
        val f = context.getExternalFilesDir(MEDIA_FILE_SHARING_DIRECTORY)
        if (f?.exists() == false) {
            f.mkdir()
        }
        return f!!
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
}