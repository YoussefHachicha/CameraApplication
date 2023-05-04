package com.example.cameraxapplication.home.ui

import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import android.util.Rational
import android.view.Display
import androidx.annotation.RequiresApi
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.cameraxapplication.usecases.ImageCaptureUseCase
import com.example.cameraxapplication.usecases.VideoCaptureUseCase
import com.example.cameraxapplication.utils.helpers.selectExternalOrBestCamera

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun StartCamera() {

    var imageCapture: ImageCapture? = null

    var videoCapture: VideoCapture<Recorder>? = null

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current


    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }


    // Preview
    val previewView = remember {
        PreviewView(context).apply {
            id = com.example.cameraxapplication.R.id.preview_view
        }
    }

    imageCapture = ImageCapture.Builder()
        .build()

    val recorder = Recorder.Builder()
        .setQualitySelector(
            QualitySelector.from(
                Quality.HIGHEST,
                FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
            )
        )
        .build()
    videoCapture = VideoCapture.withOutput(recorder)


    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize()) {
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
//                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//                val cameraSelector = CameraSelector.Builder()
//                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
//                    .build()
            val cameraSelector = selectExternalOrBestCamera(cameraProvider)


            cameraProvider.bindToLifecycle(lifecycleOwner,cameraSelector,preview,imageCapture,videoCapture)


//            val viewPort = display?.let { it1 -> ViewPort.Builder(Rational(16, 9), it1.rotation).build() } // display needs some work
//            val useCaseGroup = viewPort?.let { it1 ->
//                UseCaseGroup.Builder()
//                    .addUseCase(preview)
//                    .addUseCase(imageCapture!!)
//                    .addUseCase(videoCapture!!)
//                    .setViewPort(it1)
//                    .build()
//            }
//            if (useCaseGroup != null) {
//                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, useCaseGroup)
//            }

//                        val isBackCameraLvl3 = isBackCameraLevel3Device(cameraProvider)
//
//                        val isBackCameraLimited = isBackCameraLimitedDevice(cameraProvider)


//                        if (isBackCameraLimited) {
//                            try {
//                                cameraProvider.unbindAll()
//                                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, useCaseGroup)
//                            }catch (e : Exception){
//                                Log.e(TAG, "startCamera: ${e.localizedMessage}")
//                            }
//                        }else {
//                            try {
//                                cameraProvider.unbindAll()
//                                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
//                            }catch (e : Exception){
//                                Log.e(TAG, "startCamera: ${e.localizedMessage}")
//                            }
//                        }


        }, ContextCompat.getMainExecutor(context))
    }

    Row(horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
        Button(
            onClick = { ImageCaptureUseCase().takePhoto() },
            modifier = Modifier
                .size(110.dp)
                .padding(bottom = 50.dp)
                .weight(1f)
        )
        {
            Text(text = stringResource(id = com.example.cameraxapplication.R.string.take_photo))

        }

        Button(
            onClick = { VideoCaptureUseCase().captureVideo()},
            modifier = Modifier
                .size(110.dp)
                .padding(bottom = 50.dp)
                .weight(1f)
        )
        {
            Text(text = stringResource(id = com.example.cameraxapplication.R.string.record_video))

        }

    }

}





