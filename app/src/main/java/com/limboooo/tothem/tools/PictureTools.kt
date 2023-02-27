package com.limboooo.tothem.tools

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.*
import android.net.Uri
import com.limboooo.tothem.entity.VideoSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt


/**
 * 视频缩略图默认压缩尺寸
 */
const val THUMBNAIL_DEFAULT_COMPRESS_VALUE = 512f

object PictureTools {

//    fun getVideoInfo(): VideoInfo {
//
//    }

//    fun getVideoSize(context: Context, uri: Uri) {
//        val retriever = MediaMetadataRetriever()
//        retriever.setDataSource(context, uri)
//        val width = retriever.extractMetadata(METADATA_KEY_VIDEO_WIDTH) //宽
//        val height = retriever.extractMetadata(METADATA_KEY_VIDEO_HEIGHT) //高
//        val rotation = retriever.extractMetadata(METADATA_KEY_VIDEO_ROTATION) //视频的方向角度
//        val duration =
//            retriever.extractMetadata(METADATA_KEY_DURATION)?.toLong()
//                ?.times(1000) //视频的长度
//    }

    /**
     * 获取视频缩略图：从路径中拿取第一帧
     */
    suspend fun getVideoThumbnail(context: Context, uri: Uri): VideoSize =
        withContext(Dispatchers.IO) {
//            val bitmap: Bitmap?
            var width: String?  //宽
            var height: String?  //高
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                width = retriever.extractMetadata(METADATA_KEY_VIDEO_WIDTH)
                height = retriever.extractMetadata(METADATA_KEY_VIDEO_HEIGHT)
                // OPTION_CLOSEST_SYNC：在给定的时间，检索最近一个同步与数据源相关联的的帧（关键帧
//                bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
//                    retriever.getScaledFrameAtTime(
//                        -1, OPTION_CLOSEST_SYNC,
//                        THUMBNAIL_DEFAULT_COMPRESS_VALUE.toInt(),
//                        THUMBNAIL_DEFAULT_COMPRESS_VALUE.toInt()
//                    )
//                } else {
//                    retriever.frameAtTime?.let { compressVideoThumbnail(it) }
//                }
            } finally {
                try {
                    retriever.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            VideoSize(width!!.toInt(), height!!.toInt())
        }


    /**
     * 压缩视频缩略图
     * @param bitmap 视频缩略图
     */
    private fun compressVideoThumbnail(bitmap: Bitmap): Bitmap? {
        val width: Int = bitmap.width
        val height: Int = bitmap.height
        val max: Int = width.coerceAtLeast(height)
        if (max > THUMBNAIL_DEFAULT_COMPRESS_VALUE) {
            val scale: Float = THUMBNAIL_DEFAULT_COMPRESS_VALUE / max
            val w = (scale * width).roundToInt()
            val h = (scale * height).roundToInt()
            return compressVideoThumbnail(bitmap, w, h)
        }
        return bitmap
    }

    /**
     * 压缩视频缩略图：宽高压缩
     * 注：如果用户期望的长度和宽度和原图长度宽度相差太多的话，图片会很不清晰。
     * @param bitmap 视频缩略图
     */
    private fun compressVideoThumbnail(bitmap: Bitmap, width: Int, height: Int): Bitmap? {
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

}