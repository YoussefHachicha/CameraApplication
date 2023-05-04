package com.example.cameraxapplication.ui

//import android.Manifest
//import android.annotation.SuppressLint
//import android.content.ContentValues
//import android.content.Context
//import android.icu.text.SimpleDateFormat
//import android.net.Uri
//import android.os.Build
//import android.provider.MediaStore
//import android.util.Log
//import android.widget.Toast
//import androidx.annotation.RequiresApi
//import androidx.camera.core.*
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.video.*
//import androidx.camera.view.LifecycleCameraController
//import androidx.camera.view.PreviewView
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.material.Icon
//import androidx.compose.material.IconButton
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalLifecycleOwner
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.core.content.ContextCompat
//import androidx.core.util.Consumer
//import androidx.lifecycle.LifecycleOwner
//import androidx.lifecycle.lifecycleScope
//import com.example.cameraxapplication.MainActivity
//import com.example.cameraxapplication.R
//import com.google.accompanist.permissions.ExperimentalPermissionsApi
//import com.google.accompanist.permissions.PermissionsRequired
//import com.google.accompanist.permissions.rememberMultiplePermissionsState
//import kotlinx.coroutines.launch
//import java.io.File
//import java.net.URLEncoder
//import java.nio.charset.StandardCharsets
//import java.util.*
//import java.util.concurrent.Executor
//import java.util.concurrent.Executors
//import kotlin.coroutines.resume
//import kotlin.coroutines.suspendCoroutine
//
//
//@RequiresApi(Build.VERSION_CODES.P)
//@OptIn(ExperimentalPermissionsApi::class)
//@Composable
//fun VideoCaptureScreen(
////    navController: NavController
//) {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//
//    val permissionState = rememberMultiplePermissionsState(
//        permissions = listOf(
//            Manifest.permission.CAMERA,
//            Manifest.permission.RECORD_AUDIO
//        )
//    )
//
//    var recording: Recording? = remember { null }
//    val previewView: PreviewView = remember { PreviewView(context) }
//    val videoCapture: MutableState<VideoCapture<Recorder>?> = remember { mutableStateOf(null) }
//    val recordingStarted: MutableState<Boolean> = remember { mutableStateOf(false) }
//
//    val audioEnabled: MutableState<Boolean> = remember { mutableStateOf(false) }
//    val cameraSelector: MutableState<CameraSelector> = remember {
//        mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA)
//    }
//
//    LaunchedEffect(Unit) {
//        permissionState.launchMultiplePermissionRequest()
//    }
//
//    LaunchedEffect(Unit) {
//        permissionState.launchMultiplePermissionRequest()
//    }
//
//    LaunchedEffect(previewView) {
//        videoCapture.value = context.createVideoCaptureUseCase(
//            lifecycleOwner = lifecycleOwner,
//            cameraSelector = cameraSelector.value,
//            previewView = previewView
//        )
//    }
//    PermissionsRequired(
//        multiplePermissionsState = permissionState,
//        permissionsNotGrantedContent = { /* ... */ },
//        permissionsNotAvailableContent = { /* ... */ }
//    ) {
//        Box(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            AndroidView(
//                factory = { previewView },
//                modifier = Modifier.fillMaxSize()
//            )
//            IconButton(
//                onClick = {
//                    if (!recordingStarted.value) {
//                        videoCapture.value?.let { videoCapture ->
//                            recordingStarted.value = true
//                            val mediaDir = context.externalCacheDirs.firstOrNull()?.let {
//                                File(it, context.getString(R.string.app_name)).apply { mkdirs() }
//                            }
//
//                            recording = startRecordingVideo(
//                                context = context,
//                                filenameFormat = "yyyy-MM-dd-HH-mm-ss-SSS",
//                                videoCapture = videoCapture,
//                                outputDirectory = if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir,
//                                executor = context.mainExecutor,
//                                audioEnabled = audioEnabled.value
//                            ) { event ->
//                                if (event is VideoRecordEvent.Finalize) {
//                                    val uri = event.outputResults.outputUri
//                                    if (uri != Uri.EMPTY) {
//                                        val uriEncoded = URLEncoder.encode(
//                                            uri.toString(),
//                                            StandardCharsets.UTF_8.toString()
//                                        )
////                                        navController.navigate("${"/Users/takiacademy/Desktop"}/$uriEncoded")
//                                    }
//                                }
//                            }
//                        }
//                    } else {
//                        recordingStarted.value = false
//                        recording?.stop()
//                    }
//                },
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .padding(bottom = 32.dp)
//            ) {
//                Icon(
//                    painter = painterResource(if (recordingStarted.value) R.drawable.stop else R.drawable.record),
//                    contentDescription = "",
//                    modifier = Modifier.size(64.dp)
//                )
//            }
//            if (!recordingStarted.value) {
//                IconButton(
//                    onClick = {
//                        audioEnabled.value = !audioEnabled.value
//                    },
//                    modifier = Modifier
//                        .align(Alignment.BottomStart)
//                        .padding(bottom = 32.dp)
//                ) {
//                    Icon(
//                        painter = painterResource(if (audioEnabled.value) R.drawable.mic_on else R.drawable.mic_off),
//                        contentDescription = "",
//                        modifier = Modifier.size(64.dp)
//                    )
//                }
//            }
//            if (!recordingStarted.value) {
//                IconButton(
//                    onClick = {
//                        cameraSelector.value =
//                            if (cameraSelector.value == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA
//                            else CameraSelector.DEFAULT_BACK_CAMERA
//                        lifecycleOwner.lifecycleScope.launch {
//                            videoCapture.value = context.createVideoCaptureUseCase(
//                                lifecycleOwner = lifecycleOwner,
//                                cameraSelector = cameraSelector.value,
//                                previewView = previewView
//                            )
//                        }
//                    },
//                    modifier = Modifier
//                        .align(Alignment.BottomEnd)
//                        .padding(bottom = 32.dp)
//                ) {
//                    Icon(
//                        painter = painterResource(R.drawable.flip),
//                        contentDescription = "",
//                        modifier = Modifier.size(64.dp)
//                    )
//                }
//            }
//        }
//    }
//}
//
//
//@RequiresApi(Build.VERSION_CODES.P)
//suspend fun Context.createVideoCaptureUseCase(
//    lifecycleOwner: LifecycleOwner,
//    cameraSelector: CameraSelector,
//    previewView: PreviewView
//): VideoCapture<Recorder> {
//    val preview = Preview.Builder()
//        .build()
//        .apply { setSurfaceProvider(previewView.surfaceProvider) }
//
//    val qualitySelector = QualitySelector.from(
//        Quality.FHD,
//        FallbackStrategy.lowerQualityOrHigherThan(Quality.FHD)
//    )
//    val recorder = Recorder.Builder()
//        .setExecutor(mainExecutor)
//        .setQualitySelector(qualitySelector)
//        .build()
//    val videoCapture = VideoCapture.withOutput(recorder)
//
//    val cameraProvider = getCameraProvider()
//    cameraProvider.unbindAll()
//    cameraProvider.bindToLifecycle(
//        lifecycleOwner,
//        cameraSelector,
//        preview,
//        videoCapture
//    )
//
//    return videoCapture
//}
//
//@RequiresApi(Build.VERSION_CODES.P)
//fun createPhotoCaptureUseCase(
//    context: Context,
//    lifecycleOwner: LifecycleOwner,
//
//    previewView: PreviewView
//): ImageCapture {
//
//
//    val cameraControllerFeature: LifecycleCameraController = LifecycleCameraController(context)
//    cameraControllerFeature.bindToLifecycle(lifecycleOwner)
//    cameraControllerFeature.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//    previewView.controller = cameraControllerFeature
//
//
//
//
//
//    return ImageCapture.Builder()
//        .build()
//}
//
//
////@SuppressLint("RememberReturnType")
////@Composable
////fun Camera() {
////    val lifecycleOwner = LocalLifecycleOwner.current
////    val context = LocalContext.current
////
////    val cameraProviderFeature = remember {
////        ProcessCameraProvider.getInstance(context)
////    }
//////    lateinit var cameraControllerFeature: LifecycleCameraController
////
////    val previewView = remember {
////        PreviewView(context).apply {
////            id = R.id.preview_view
////        }
////    }
////
////    val cameraExecutor = remember {
////        Executors.newSingleThreadExecutor()
////    }
////
////    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize()) {
////        cameraProviderFeature.addListener({
////            //provider
////            val cameraProvider = cameraProviderFeature.get()
////            val preview = androidx.camera.core.Preview.Builder()
////                .build()
////                .also {
////                    it.setSurfaceProvider(previewView.surfaceProvider)
////                }
////
////            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
////
//////            val recorder = Recorder.Builder()
//////                .build()
////
//////            var videoCapture: VideoCapture<Recorder> = VideoCapture.withOutput(recorder)
////
////            val faceAnalysis = ImageAnalysis.Builder()
////                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
////                .build()
////                .also {
////                    it.setAnalyzer(cameraExecutor, FaceAnalyzer())
////                }
//////            val zoom = ImageAnalysis.Builder()
//////                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//////                .build()
//////                .also {
//////                    it.effect()
//////                }
////
////            try {
////                cameraProvider.unbindAll()
////                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, faceAnalysis)
////
////            } catch (e: Exception) {
////                Log.e("e", "CameraX ${e.localizedMessage}")
////            }
////        }, ContextCompat.getMainExecutor(context))
////    }
////}
//
//
////private class FaceAnalyzer() : ImageAnalysis.Analyzer {
////    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
////    override fun analyze(imageProxy: ImageProxy) {
////        val image = imageProxy.image
////        image?.close()
////    }
////
////}
//
//@RequiresApi(Build.VERSION_CODES.P)
//suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
//    ProcessCameraProvider.getInstance(this).also { future ->
//        future.addListener(
//            {
//                continuation.resume(future.get())
//            },
//            mainExecutor
//        )
//    }
//}
//
//@SuppressLint("MissingPermission")
//fun startRecordingVideo(
//    context: Context,
//    filenameFormat: String,
//    videoCapture: VideoCapture<Recorder>,
//    outputDirectory: File,
//    executor: Executor,
//    audioEnabled: Boolean,
//    consumer: Consumer<VideoRecordEvent>
//): Recording {
//    val videoFile = File(
//        outputDirectory,
//        SimpleDateFormat(filenameFormat, Locale.US).format(System.currentTimeMillis()) + ".mp4"
//    )
//
//    val outputOptions = FileOutputOptions.Builder(videoFile).build()
//
//    return videoCapture.output
//        .prepareRecording(context, outputOptions)
//        .apply { if (audioEnabled) withAudioEnabled() }
//        .start(executor, consumer)
//}
//
//@RequiresApi(Build.VERSION_CODES.P)
//@SuppressLint("MissingPermission")
//suspend fun takePhoto(
//    context: Context,
//    filenameFormat: String,
//    imageCapture: ImageCapture,
//    outputDirectory: File,
//    executor: Executor,
//) {
////    val imageFile = File(
////        outputDirectory,
////        SimpleDateFormat(filenameFormat, Locale.US).format(System.currentTimeMillis()) + ".jpeg"
////    )
//
////    val outputOptions = FileOutputOptions.Builder(imageFile).build()
//
//    val name = java.text.SimpleDateFormat(MainActivity.FILENAME_FORMAT, Locale.US)
//        .format(System.currentTimeMillis())
//
//    val contentValues = ContentValues().apply {
//        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
//        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
//            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
//        }
//    }
//    val outputOptions = ImageCapture.OutputFileOptions
//        .Builder(context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
//        .build()
//
//
//    val cameraProvider = context.getCameraProvider()
//
//    imageCapture.takePicture(
//        executor,
//        object : ImageCapture.OnImageCapturedCallback() {
//            override fun onError(exc: ImageCaptureException) {
//                val msg = Log.e("e", "Photo capture failed: ${exc.message}", exc)
//                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
//            }
//            override fun onCaptureSuccess(image: ImageProxy) {
//                val msg = "Photo capture succeeded: ${output.savedUri}"
//                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
//                Log.d("e", msg)
//            }
//        }
//
//    )
//
//
////    takePicture(
////        executor,
////        object : ImageCapture.OnImageCapturedCallback() {
////            override fun onError(exc: ImageCaptureException) {
////                val msg = Log.e("e", "Photo capture failed: ${exc.message}", exc)
////                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
////            }
////            override fun onCaptureSuccess(image: ImageProxy) {
////                val msg = "Photo capture succeeded: ${output.savedUri}"
////                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
////                Log.d("e", msg)
////            }
////        }
////    )
//}
//
//private fun ImageCapture.takePicture(executor: Executor, onImageSavedCallback: ImageCapture.OnImageSavedCallback) {
//    TODO("Not yet implemented")
//}
//
//private fun takePhoto(context: Context) {
//
//    // Create time stamped name and MediaStore entry.
//    val name = java.text.SimpleDateFormat(MainActivity.FILENAME_FORMAT, Locale.US)
//        .format(System.currentTimeMillis())
//
//    val contentValues = ContentValues().apply {
//        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
//        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
//            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
//        }
//    }
//
//    // Create output options object which contains file + metadata
//    val outputOptions = ImageCapture.OutputFileOptions
//        .Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
//        .build()
//
//// Set up image capture listener, which is triggered after photo has been taken
//    cameraControllerFeature.takePicture(
//        outputOptions,
//        ContextCompat.getMainExecutor(context),
//        object : ImageCapture.OnImageSavedCallback {
//            override fun onError(exc: ImageCaptureException) {
//                val msg = Log.e(MainActivity.TAG, "Photo capture failed: ${exc.message}", exc)
//                Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
//            }
//
//            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
//                val msg = "Photo capture succeeded: ${output.savedUri}"
//                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//                Log.d(MainActivity.TAG, msg)
//            }
//        }
//    )
//}
