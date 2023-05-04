package com.example.cameraxapplication.extensions

import android.app.Application
import android.util.Log
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import coil.ImageLoader
import coil.ImageLoaderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

class CameraExtensionsApplication : Application(), CameraXConfig.Provider, ImageLoaderFactory {
    override fun getCameraXConfig(): CameraXConfig =
        CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
            .setCameraExecutor(Dispatchers.IO.asExecutor())
            .setMinimumLoggingLevel(Log.ERROR)
            .build()

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .crossfade(true)
            .build()
}