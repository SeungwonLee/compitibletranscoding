package com.linecorp.mediatranscoding

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.ApplicationMediaCapabilities
import android.media.MediaExtractor
import android.media.MediaFeature
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.FileDescriptor

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (isNeedRequestPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_PERMISSION
            )
            return
        }

        startSystemPicker()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (grantResult in grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions are not allowed", Toast.LENGTH_LONG).show()
                return
            }
        }

        startSystemPicker()
    }

    private fun startSystemPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "video/*"
        }
        startActivityForResult(intent, REQUEST_CODE_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK || data == null /*|| Build.VERSION.SDK_INT < Build.VERSION_CODES.S*/) {
            return
        }

        // Create media uri
        val pathSegments =
            data.data?.lastPathSegment?.split(":".toRegex())?.toTypedArray() ?: return

        val isImageType = pathSegments[0] == "image"
        val id = pathSegments[1]

        val context = this
        val mediaUri = getContentUri(id, isImageType)

        logVideoInfo(context, mediaUri)
        logBitrate(context, mediaUri)

        val currentTimeMs = System.currentTimeMillis()

        // Create `ApplicationMediaCapabilities` to request transcoding.
        val mediaCapabilities =
            ApplicationMediaCapabilities.Builder()
                .addUnsupportedVideoMimeType(MediaFormat.MIMETYPE_VIDEO_HEVC)
                .addUnsupportedHdrType(MediaFeature.HdrType.HDR10)
                .addUnsupportedHdrType(MediaFeature.HdrType.HDR10_PLUS)
                .build()

        // Create `Bundle` to save `ApplicationMediaCapabilities`.
        val providerOptions = Bundle().apply {
            putParcelable(MediaStore.EXTRA_MEDIA_CAPABILITIES, mediaCapabilities)
        }

        val type = contentResolver.getType(mediaUri) ?: return
        Log.d(TAG, "type=$type")

        // Access a media with `Bundle` to request transcoding.
        contentResolver.openTypedAssetFileDescriptor(mediaUri, type, providerOptions)
            .use { assetFd ->
                // Content will be transcoded based on values defined in the
                // ApplicationMediaCapabilities provided.
                // Content will be locked but return `fileDescriptor` very soon.
                val fileDescriptor = assetFd?.fileDescriptor ?: return

                // It will take time to access a media because content will be locked to transcoding.
                logVideoInfo(fileDescriptor)
                logBitrate(fileDescriptor)

                Log.d(TAG, "Transcoding is taken = ${System.currentTimeMillis() - currentTimeMs}")
            }
    }

    private fun logBitrate(context: Context, mediaUri: Uri) {
        val retriever = kotlin.runCatching {
            MediaMetadataRetriever().apply {
                setDataSource(context, mediaUri)
            }
        }.getOrNull()

        retriever ?: return

        Log.d(
            TAG,
            "bitrate=${retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)}"
        )
        retriever.release()
    }

    private fun logBitrate(fd: FileDescriptor) {
        val retriever = MediaMetadataRetriever().apply {
            setDataSource(fd)
        }
        Log.d(
            TAG,
            "bitrate=${retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)}"
        )
        retriever.release()
    }

    private fun logVideoInfo(context: Context, mediaUri: Uri) {
        val mediaExtractor = kotlin.runCatching {
            MediaExtractor().apply {
                setDataSource(context, mediaUri, null)
            }
        }.getOrNull()

        mediaExtractor ?: return

        logVideoInfo(mediaExtractor)

        mediaExtractor.release()
    }

    private fun logVideoInfo(fd: FileDescriptor) {
        val mediaExtractor = MediaExtractor().apply {
            setDataSource(fd)
        }
        logVideoInfo(mediaExtractor)
        mediaExtractor.release()
    }

    private fun logVideoInfo(mediaExtractor: MediaExtractor) {
        repeat(mediaExtractor.trackCount) {
            val trackFormat = mediaExtractor.getTrackFormat(it)
            val mimeType = trackFormat.getString(MediaFormat.KEY_MIME)

            Log.d(TAG, "mimeType=$mimeType")
        }
    }

    private fun getContentUri(id: String, isImage: Boolean): Uri {
        val contentUri =
            if (isImage) MediaStore.Images.Media.EXTERNAL_CONTENT_URI else MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        return ContentUris.withAppendedId(contentUri, id.toLong())
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isNeedRequestPermissions(): Boolean =
        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_PICKER = 1
        private const val REQUEST_CODE_PERMISSION = 2
    }
}