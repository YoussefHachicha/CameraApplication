package com.example.cameraxapplication.utils.helpers

import android.hardware.camera2.CameraCharacteristics
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider

@androidx.annotation.OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)
fun selectExternalOrBestCamera(provider: ProcessCameraProvider): CameraSelector {
    val cam2Infos = provider.availableCameraInfos.map {
        Camera2CameraInfo.from(it)
    }.sortedByDescending {
        // HARDWARE_LEVEL is Int type, with the order of:
        // LEGACY < LIMITED < FULL < LEVEL_3 < EXTERNAL
        it.getCameraCharacteristic(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
    }

    return when {
        cam2Infos.isNotEmpty() -> {
            CameraSelector.Builder()
                .addCameraFilter {
                    it.filter { camInfo ->
                        // cam2Infos[0] is either EXTERNAL or best built-in camera
                        val thisCamId = Camera2CameraInfo.from(camInfo).cameraId
                        thisCamId == cam2Infos[0].cameraId
                    }
                }.build()
        }

        else -> CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
    }
}
