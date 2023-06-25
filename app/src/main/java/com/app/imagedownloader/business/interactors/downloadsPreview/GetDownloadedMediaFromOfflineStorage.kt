package com.app.imagedownloader.business.interactors.downloadsPreview

import android.content.ContentResolver
import android.content.ContentUris
import android.os.Build
import android.provider.MediaStore
import com.app.imagedownloader.Utils.Constants.StorageDirectoryConstants.IMAGE_MEDIA_DIRECTORY
import com.app.imagedownloader.business.domain.core.DataState.DataState
import com.app.imagedownloader.business.domain.model.DownloadedMediaInfo
import com.app.imagedownloader.framework.presentation.ui.main.downloadsPreview.state.DownloadsPreviewViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetDownloadedMediaFromOfflineStorage @Inject constructor(
    private val contentResolver: ContentResolver
) {

    operator fun invoke():Flow<DataState<DownloadsPreviewViewState>>{
        return  flow {
            emit(DataState.loading())
            queryOfflineStorage(contentResolver)?.let {
                emit(DataState.success(DownloadsPreviewViewState(it)))
            }?: kotlin.run {
                emit(DataState.error("Error"))
            }
        }
    }

    private fun queryOfflineStorage(contentResolver: ContentResolver):List<DownloadedMediaInfo>?{
        val imageMediapath =
            if (versionAbove29())
                "Pictures/${IMAGE_MEDIA_DIRECTORY}"
            else "/storage/emulated/0/Pictures/${IMAGE_MEDIA_DIRECTORY}/"


        val list = mutableListOf<DownloadedMediaInfo>()
        val imageUri = if (versionAbove29()) MediaStore.Images.Media.getContentUri(
            MediaStore.VOLUME_EXTERNAL
        ) else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val imageProjection = arrayOf(
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media._ID,
            if (versionAbove29()) MediaStore.Images.Media.RELATIVE_PATH else MediaStore.Images.Media.DATA,
        )

        val imageSelection = if (versionAbove29())
            "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?" else
            "${MediaStore.Images.Media.DATA} LIKE ?"

        val imageSelectionArgs = arrayOf("$imageMediapath%")

        val imageQuery = contentResolver.query(
            imageUri,
            imageProjection,
            imageSelection,
            imageSelectionArgs,
            null,
            null
        )
        imageQuery.use { cursor ->
            val columnid = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val columndate = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val columnname =
                cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            while (cursor!!.moveToNext()) {
                val id = columnid?.let { cursor.getLong(it) }
                val name = columnname?.let { cursor.getString(it) }
                val date = columndate?.let { cursor.getInt(it) }
                val urii = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id!!
                )
                name?.let {
                    list.add(DownloadedMediaInfo(urii,name,date,colorCodefromname(name)))
                }
            }
        }

        return list.sortedByDescending { it.created_at }
    }

   private fun colorCodefromname(name: String): String {
        return try {
            val index = name.indexOf("##", 0, false)
            val extensionindex = name.indexOf("##", index + 2, false)
            val color = name.subSequence(index + 2, extensionindex).toString()
            "#$color"
        } catch (e: Exception) {
            "#ffffff"
        }
    }
    private fun versionAbove29(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }
}