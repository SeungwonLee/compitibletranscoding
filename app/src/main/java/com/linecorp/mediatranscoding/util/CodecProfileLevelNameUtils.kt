@file: JvmName("CodecProfileLevelNameUtils")

package com.linecorp.mediatranscoding.util

import android.media.MediaCodecInfo
import android.media.MediaCodecInfo.CodecProfileLevel

// http://code.taobao.org/p/IJK_DEMO/diff/4/ijkplayer-java/java/tv

fun getProfileName(profile: Int): String {
    return when (profile) {
        MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline -> "Baseline"
        MediaCodecInfo.CodecProfileLevel.AVCProfileMain -> "Main"
        MediaCodecInfo.CodecProfileLevel.AVCProfileHigh -> "High"
        MediaCodecInfo.CodecProfileLevel.AVCProfileHigh10 -> "High10"
        MediaCodecInfo.CodecProfileLevel.AVCProfileHigh422 -> "High422"
        MediaCodecInfo.CodecProfileLevel.AVCProfileHigh444 -> "High444"
        else -> "Unknown:"
    }
}

fun getLevelName(level: Int): String {
    when (level) {
        CodecProfileLevel.AVCLevel1 -> return "1"
        CodecProfileLevel.AVCLevel1b -> return "1b"
        CodecProfileLevel.AVCLevel11 -> return "11"
        CodecProfileLevel.AVCLevel12 -> return "12"
        CodecProfileLevel.AVCLevel13 -> return "13"
        CodecProfileLevel.AVCLevel2 -> return "2"
        CodecProfileLevel.AVCLevel21 -> return "21"
        CodecProfileLevel.AVCLevel22 -> return "22"
        CodecProfileLevel.AVCLevel3 -> return "3"
        CodecProfileLevel.AVCLevel31 -> return "31"
        CodecProfileLevel.AVCLevel32 -> return "32"
        CodecProfileLevel.AVCLevel4 -> return "4"
        CodecProfileLevel.AVCLevel41 -> return "41"
        CodecProfileLevel.AVCLevel42 -> return "42"
        CodecProfileLevel.AVCLevel5 -> return "5"
        CodecProfileLevel.AVCLevel51 -> return "51"
        65536 /*CodecProfileLevel.AVCLevel52: */ -> return "52"
        else -> return "0"
    }
}
