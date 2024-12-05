package com.chinmay.object_detection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ObjectScreen extends AppCompatActivity {
    private static final String TAG = "ObjectScreen";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 ?
                    Manifest.permission.READ_EXTERNAL_STORAGE :
                    Manifest.permission.READ_MEDIA_IMAGES
    };

    private ImageView imageView;
    private TextView resultTextView;
    private PreviewView viewFinder;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private boolean isCameraMode = false;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    processImageUri(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.object_activity);

        // Initialize views
        imageView = findViewById(R.id.imageView);
        resultTextView = findViewById(R.id.resultTextView);
        viewFinder = findViewById(R.id.viewFinder);
        Button galleryButton = findViewById(R.id.galleryButton);
        Button cameraButton = findViewById(R.id.cameraButton);

        cameraExecutor = Executors.newSingleThreadExecutor();

        galleryButton.setOnClickListener(v -> {
            if (isCameraMode) {
                stopCamera();
            }
            galleryLauncher.launch("image/*");
        });

        cameraButton.setOnClickListener(v -> {
            if (isCameraMode) {
                captureImage();
            } else {
                if (allPermissionsGranted()) {
                    startCamera();
                } else {
                    ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                            REQUEST_CODE_PERMISSIONS);
                }
            }
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                Camera camera = cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                );

                // Show camera preview, hide ImageView
                viewFinder.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                isCameraMode = true;

                Button cameraButton = findViewById(R.id.cameraButton);
                cameraButton.setText(R.string.capture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void stopCamera() {
        ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                ProcessCameraProvider.getInstance(this).get().unbindAll();
                viewFinder.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                isCameraMode = false;

                Button cameraButton = findViewById(R.id.cameraButton);
                cameraButton.setText(R.string.camera);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Failed to stop camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureImage() {
        if (imageCapture == null) return;

        //File photoFile = new File(getCacheDir(), "photo.jpg");
        File photoFile = new File(getCacheDir(), "photo.jpg");

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        Uri savedUri = FileProvider.getUriForFile(
                                ObjectScreen.this,
                                "com.chinmay.object_detection.fileprovider",  // Ensure this matches the manifest
                                photoFile
                        );

                        stopCamera();
                        processImageUri(savedUri);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Image capture failed", exception);
                        Toast.makeText(ObjectScreen.this,
                                "Image capture failed", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void processImageUri(Uri uri) {
        try {
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source =
                        ImageDecoder.createSource(getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }
            imageView.setImageBitmap(bitmap);
            labelImage(bitmap);
        } catch (IOException e) {
            Log.e(TAG, "Error processing image", e);
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    private void labelImage(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

        labeler.process(image)
                .addOnSuccessListener(labels -> {
                    StringBuilder result = new StringBuilder();
                    for (ImageLabel label : labels) {
                        String text = label.getText();
                        float confidence = label.getConfidence();
                        result.append(String.format("%s (%.1f%%)\n",
                                text, confidence * 100));
                    }
                    resultTextView.setText(result.toString());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Image labeling failed", e);
                    Toast.makeText(ObjectScreen.this,
                            "Image labeling failed", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
