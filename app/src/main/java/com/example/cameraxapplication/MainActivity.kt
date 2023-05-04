package com.example.cameraxapplication

import android.app.Application
import android.content.ContentValues
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.util.Rational
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.camera.camera2.Camera2Config
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.cameraxapplication.ui.theme.CameraXApplicationTheme
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private lateinit var cameraControllerFeature: LifecycleCameraController

    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContent {
            CameraXApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
//                    VideoCaptureScreen()

                    // Request camera permissions
                    if (allPermissionsGranted()) {
                        StartCamera()
                    } else {
                        ActivityCompat.requestPermissions(
                            this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
                        )
                    }

                    cameraExecutor = Executors.newSingleThreadExecutor()
                }
            }
        }
    }

//    @Deprecated("Deprecated in Java")
//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<String>, grantResults:
//        IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == REQUEST_CODE_PERMISSIONS) {
//            if (allPermissionsGranted()) {
//                startCamera()
//            } else {
//                Toast.makeText(
//                    this,
//                    "Permissions not granted by the user.",
//                    Toast.LENGTH_SHORT
//                ).show()
//                finish()
//            }
//        }
//    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            }
        )
    }

    // Implements VideoCapture use case, including start and stop capturing.
    private fun captureVideo() {
        val videoCapture = this.videoCapture ?: return


        val curRecording = recording
        if (curRecording != null) {
            // Stop the current recording session.
            curRecording.stop()
            recording = null
            return
        }

        // create and start a new recording session
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()
        recording = videoCapture.output
            .prepareRecording(this, mediaStoreOutputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(
                        this@MainActivity,
                        android.Manifest.permission.RECORD_AUDIO
                    ) ==
                    PermissionChecker.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        //vfdsqvvds
                    }

                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded: " +
                                    "${recordEvent.outputResults.outputUri}"
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT)
                                .show()
                            Log.d(TAG, msg)
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(
                                TAG, "Video capture ends with error: " +
                                        "${recordEvent.error}"
                            )
                        }

                        //fvdvq


                    }
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @Composable
    fun StartCamera() {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current


        val cameraProviderFuture = remember {
            ProcessCameraProvider.getInstance(context)
        }


        // Preview
        val previewView = remember {
            PreviewView(context).apply {
                id = R.id.preview_view
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
                val preview = androidx.camera.core.Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
//                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//                val cameraSelector = CameraSelector.Builder()
//                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
//                    .build()
                val cameraSelector = selectExternalOrBestCamera(cameraProvider)

                val viewPort = display?.let { it1 -> ViewPort.Builder(Rational(16, 9), it1.rotation).build() }
                val useCaseGroup = viewPort?.let { it1 ->
                    UseCaseGroup.Builder()
                        .addUseCase(preview)
                        .addUseCase(imageCapture!!)
                        .addUseCase(videoCapture!!)
                        .setViewPort(it1)
                        .build()
                }
                if (useCaseGroup != null) {
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, useCaseGroup)
                }

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
            androidx.compose.material.Button(
                onClick = { takePhoto() },
                modifier = Modifier
                    .size(110.dp)
                    .padding(bottom = 50.dp)
                    .weight(1f)
            )
            {
                Text(text = stringResource(id = R.string.take_photo))

            }

            androidx.compose.material.Button(
                onClick = { captureVideo() },
                modifier = Modifier
                    .size(110.dp)
                    .padding(bottom = 50.dp)
                    .weight(1f)
            )
            {
                Text(text = stringResource(id = R.string.record_video))

            }

        }

    }

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

    // create a CameraSelector for the USB camera (or highest level internal camera)


    @androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
    fun isBackCameraLevel3Device(cameraProvider: ProcessCameraProvider): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return CameraSelector.DEFAULT_BACK_CAMERA
                .filter(cameraProvider.availableCameraInfos)
                .firstOrNull()
                ?.let { Camera2CameraInfo.from(it) }
                ?.getCameraCharacteristic(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) ==
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3
        }
        return false
    }

    @androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
    fun isBackCameraLimitedDevice(cameraProvider: ProcessCameraProvider): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return CameraSelector.DEFAULT_BACK_CAMERA
                .filter(cameraProvider.availableCameraInfos)
                .firstOrNull()
                ?.let { Camera2CameraInfo.from(it) }
                ?.getCameraCharacteristic(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) ==
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED
        }
        return false
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


//    @Composable
//    private fun StartCamera() {
//        val context = LocalContext.current
//        val lifecycleOwner = LocalLifecycleOwner.current
//
//        val previewView = remember {
//            PreviewView(context).apply {
//                id = R.id.preview_view
//            }
//        }
//
//        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize()) {
//            cameraControllerFeature = LifecycleCameraController(context)
//            cameraControllerFeature.bindToLifecycle(lifecycleOwner)
//            cameraControllerFeature.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//            previewView.controller = cameraControllerFeature
//            ContextCompat.getMainExecutor(context)
//        }
//        Column(verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally) {
//            androidx.compose.material.Button(
//                onClick = {
//
//                    takePhoto(context = context)
//                },
//                modifier = Modifier
//                    .size(110.dp)
//                    .padding(bottom = 50.dp)
//            )
//            {
//                Text(text = stringResource(id = R.string.take_normal_photo))
//
//            }
//        }
//
//
//    }
//
//    private val activityResultLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
//        { permissions ->
//
//            var permissionGranted = true
//            permissions.entries.forEach {
//                if (it.key in REQUIRED_PERMISSIONS && !it.value)
//                    permissionGranted = false
//            }
//            if (!permissionGranted) {
//                Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
//            } else {
//                setContent {
//                    CameraXApplicationTheme {
//                        // A surface container using the 'background' color from the theme
//                        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
//                            StartCamera()
//                        }
//                    }
//                }
//
//            }
//
//        }


    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

//    private fun takePhoto(context: Context) {
//
//        // Create time stamped name and MediaStore entry.
//        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
//            .format(System.currentTimeMillis())
//
//        val contentValues = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
//            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
//                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
//            }
//        }
//
//        // Create output options object which contains file + metadata
//        val outputOptions = ImageCapture.OutputFileOptions
//            .Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
//            .build()
//
//// Set up image capture listener, which is triggered after photo has been taken
//        cameraControllerFeature.takePicture(
//            outputOptions,
//            ContextCompat.getMainExecutor(context),
//            object : ImageCapture.OnImageSavedCallback {
//                override fun onError(exc: ImageCaptureException) {
//                    val msg = Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
//                    Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
//                }
//
//                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
//                    val msg = "Photo capture succeeded: ${output.savedUri}"
//                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//                    Log.d(TAG, msg)
//                }
//            }
//        )
//    }
//    @SuppressLint("RememberReturnType")
//    @Composable
//    fun Video() {
//        val lifecycleOwner = LocalLifecycleOwner.current
//        val context = LocalContext.current
//
//        val cameraProviderFeature = remember {
//            ProcessCameraProvider.getInstance(context)
//        }
//
//        val previewView = remember {
//            PreviewView(context).apply {
//                id = R.id.preview_view
//            }
//        }
//
//        val cameraExecutor = remember {
//            Executors.newSingleThreadExecutor()
//        }
//
//        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize()) {
//            cameraProviderFeature.addListener({
//                //provider
//                val cameraProvider = cameraProviderFeature.get()
//                val preview = androidx.camera.core.Preview.Builder()
//                    .build()
//                    .also {
//                        it.setSurfaceProvider(previewView.surfaceProvider)
//                    }
//
//                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
//
//                val recorder = Recorder.Builder()
//                    .build()
//
//                var videoCapture: VideoCapture<Recorder> = VideoCapture.withOutput(recorder)
//
////            val faceAnalysis = ImageAnalysis.Builder()
////                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
////                .build()
////                .also {
////                    it.setAnalyzer(cameraExecutor, FaceAnalyzer())
////                }
//                try {
//                    cameraProvider.unbindAll()
//                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, videoCapture, preview)
//
//                } catch (e: Exception) {
//                    Log.e("e", "CameraX ${e.localizedMessage}")
//                }
//
//            }, ContextCompat.getMainExecutor(context))
//        }
//    }
//
//     @SuppressLint("MissingPermission")
//     @Composable
//     fun startRecording(context: Context) {
//         val lifecycleOwner = LocalLifecycleOwner.current
////         val context = LocalContext.current
//        // create MediaStoreOutputOptions for our recorder: resulting our recording!
//        val name = "CameraX-recording-" +
//                SimpleDateFormat(FILENAME_FORMAT, Locale.US)
//                    .format(System.currentTimeMillis()) + ".mp4"
//        val contentValues = ContentValues().apply {
//            put(MediaStore.Video.Media.DISPLAY_NAME, name)
//        }
//        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
//            context.contentResolver,
//            MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
//            .setContentValues(contentValues)
//            .build()
//
//        // configure Recorder and Start recording to the mediaStoreOutput.
//        currentRecording = videoCapture.output
//            .prepareRecording(context, mediaStoreOutput)
//            .apply { if (audioEnabled) withAudioEnabled() }
//            .start(context, lifecycleOwner)
//
//        Log.i(TAG, "Recording started")
//    }
}


@Composable
fun Button() {
    androidx.compose.material.Button(
        onClick = {/* TODO: Handle click */ },
        modifier = Modifier
            .size(110.dp)
            .padding(bottom = 50.dp)
    )
    {
        Text(text = stringResource(id = R.string.take_photo))

    }
}


private class FaceAnalyzer() : ImageAnalysis.Analyzer {
    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val image = imageProxy.image
        image?.close()
    }

}

class MainApplication : Application(), CameraXConfig.Provider {
    override fun getCameraXConfig(): CameraXConfig {
        return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
            .setMinimumLoggingLevel(Log.INFO)
            .setCameraExecutor {}
            .setSchedulerHandler(Handler())
            .setAvailableCamerasLimiter(CameraSelector.DEFAULT_BACK_CAMERA)
            .build()
    }
}