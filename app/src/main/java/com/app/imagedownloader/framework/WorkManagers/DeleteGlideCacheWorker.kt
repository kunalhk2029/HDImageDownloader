package com.app.imagedownloader.framework.WorkManagers


import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File

class DeleteGlideCacheWorker(val ctx: Context, val params: WorkerParameters) :
    CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        withContext(IO) {
            Glide.get(ctx).clearDiskCache()
        }
        return Result.success()
    }
}
