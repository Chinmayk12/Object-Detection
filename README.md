### Object Detection App - Torchit Internship Assignment

**Object Detection App** is an Android application that leverages **CameraX** for camera functionality and **ML Kit** for image labeling and object detection. It allows users to capture images via the camera or select images from the gallery and detect objects in the image in real-time.

This app was built overnight as part of a personal project, and I encourage contributions to enhance its functionality and performance.

## Features

- **CameraX Integration**: Provides a reliable camera interface for taking pictures.
- **Image Labeling**: Uses **ML Kit** to detect and label objects in captured images with confidence scores.
- **Gallery Access**: Allows users to choose images from the device's gallery for object detection.
- **Real-time Detection**: Detects objects in real-time and displays the labels with confidence.
- **Permissions Handling**: Requests appropriate permissions for camera and storage access.
  
## Technologies Used

- **CameraX**: For managing camera access, providing easy-to-use APIs for photo capture and video analysis.
- **ML Kit**: Google's machine learning library for vision-based image labeling.
- **Android SDK**: Used for building the Android application, including views, activities, and services.
- **Kotlin**: Programming language used for app development.

## Requirements

- **Android 8.0 (API level 26)** or higher
- **Android Studio** to build and run the project
- **Permissions**: CAMERA, READ_EXTERNAL_STORAGE (or READ_MEDIA_IMAGES for Android 13+)

## Installation

### Step 1: Clone the repository

Clone this repository to your local machine using:

```bash
git clone https://github.com/your-username/object-detection-app.git
```

### Step 2: Open the project

Open the cloned project in **Android Studio**.

### Step 3: Add dependencies

In the `build.gradle` file (Module: app), add the following dependencies:

```gradle
dependencies {
    implementation 'androidx.camera:camera-camera2:1.2.0'
    implementation 'androidx.camera:camera-lifecycle:1.2.0'
    implementation 'androidx.camera:camera-view:1.0.0'
    implementation 'com.google.mlkit:image-labeling:17.0.0'
    implementation 'com.google.mlkit:mlkit-vision:17.0.0'
}
```

Sync the project with Gradle files.

### Step 4: Add permissions

Ensure the following permissions are added in your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

For Android 13 and above, use `READ_MEDIA_IMAGES` for accessing images from the gallery.

### Step 5: Configure File Provider

In your `AndroidManifest.xml`, configure the FileProvider:

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="com.chinmay.object_detection.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

### Step 6: Build and Run

Build and run the project on a physical Android device (CameraX may not function properly on emulators with camera support).

## Usage

1. **Start Camera**: Tap on the **"Start Camera"** button to view the live camera feed.
2. **Capture Image**: Tap the **"Capture"** button to take a picture.
3. **Gallery Access**: Tap the **"Gallery"** button to pick an image from the device's gallery.
4. **Object Detection**: After capturing or selecting an image, the app uses **ML Kit** to detect objects in the image and display labels with confidence scores.

## Screenshots

![object-1](https://github.com/user-attachments/assets/2a31ba39-73a2-4fec-9784-59770a2728a6)
![object-2](https://github.com/user-attachments/assets/bc21ecdc-1030-47eb-bb0a-ee74ea79c3c6)



# Contributing

This app was built overnight as part of a personal project, and I welcome contributions to enhance its functionality, add more features, or improve the overall user experience. To contribute:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature-name`).
3. Make your changes.
4. Commit your changes (`git commit -am 'Add new feature'`).
5. Push to the branch (`git push origin feature-name`).
6. Create a pull request.

Don't hesitate to contribute – your ideas and improvements are highly appreciated!

