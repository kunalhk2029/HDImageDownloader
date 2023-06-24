package com.app.imagedownloader.framework.WorkManagers


import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File

class WorkerDeleteShared(ctx: Context, val params: WorkerParameters) :
    CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        withContext(IO) {
            try {
                val urii = inputData.getString("URI")
                val uri = Uri.parse(urii)
                File(uri.path).delete()
            } catch (_: Exception) { }
        }
        return Result.success()
    }
}
