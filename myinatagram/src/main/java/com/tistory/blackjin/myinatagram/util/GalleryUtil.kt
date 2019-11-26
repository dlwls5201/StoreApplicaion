package com.tistory.blackjin.myinatagram.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.tistory.blackjin.myinatagram.builder.type.MediaType
import com.tistory.blackjin.myinatagram.model.Album
import com.tistory.blackjin.myinatagram.model.Media
import io.reactivex.Single
import timber.log.Timber
import java.io.File
import java.util.*

internal object GalleryUtil {

    init {
        Timber.d("GalleryUtil init")
    }

    private const val INDEX_MEDIA_URI = MediaStore.MediaColumns.DATA
    private const val INDEX_DATE_ADDED = MediaStore.MediaColumns.DATE_ADDED

    private lateinit var albumName: String

    internal fun getMedia(context: Context, mediaType: MediaType): Single<List<Album>> {
        return Single.create { emitter ->
            try {

                val uri: Uri

                when (mediaType) {
                    MediaType.IMAGE -> {
                        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        albumName = MediaStore.Images.Media.BUCKET_DISPLAY_NAME
                    }
                    MediaType.VIDEO -> {
                        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        albumName = MediaStore.Video.Media.BUCKET_DISPLAY_NAME
                    }
                }

                val sortOrder = "$INDEX_DATE_ADDED DESC"
                val projection = arrayOf(INDEX_MEDIA_URI, albumName, INDEX_DATE_ADDED)
                val cursor = context.contentResolver.query(uri, projection, null, null, sortOrder)

                val albumList: List<Album> = cursor?.let {

                    Timber.d(Arrays.toString(it.columnNames))

                    val totalImageList =
                        generateSequence { if (cursor.moveToNext()) cursor else null }
                            .map(::getImage)
                            .filterNotNull()
                            .toList()

                    val albumList: List<Album> = totalImageList.asSequence()
                        .groupBy { media -> media.albumName }
                        .toSortedMap(Comparator { albumName1: String, albumName2: String ->
                            if (albumName2 == "Camera") {
                                1
                            } else {
                                albumName1.compareTo(albumName2, true)
                            }
                        })
                        .map(::getAlbum)
                        .toList()

                    return@let albumList
                    /*val totalAlbum = totalImageList.run {
                        val albumName = context.getString(R.string.image_picker_album_all)
                        Album(
                            albumName,
                            getOrElse(0) { Media(albumName, Uri.EMPTY, 0) }.uri,
                            this
                        )
                    }

                    Timber.d("totalAlbum : $totalAlbum")

                    mutableListOf(totalAlbum).apply {
                        addAll(albumList)
                    }*/
                } ?: emptyList()

                cursor?.close()
                emitter.onSuccess(albumList)
            } catch (exception: Exception) {
                emitter.onError(exception)
            }

        }
    }

    private fun getAlbum(entry: Map.Entry<String, List<Media>>): Album {
        Timber.e("${entry.key} -> ${entry.value}")
        return Album(entry.key, entry.value[0].uri, entry.value)
    }


    /**
     *  @mediaPath : /storage/emulated/0/DCIM/..
     *  @mediaUri : file:///storage/emulated/0/...
     */
    private fun getImage(cursor: Cursor): Media? =
        try {
            cursor.run {
                val folderName = getString(getColumnIndex(albumName))
                val mediaPath = getString(getColumnIndex(INDEX_MEDIA_URI))
                val mediaUri: Uri = Uri.fromFile(File(mediaPath))
                Timber.d("folderName : $folderName -> mediaUri : $mediaUri")
                val datedAddedSecond = getLong(getColumnIndex(INDEX_DATE_ADDED))
                Media(folderName, mediaUri, datedAddedSecond)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            null
        }
}