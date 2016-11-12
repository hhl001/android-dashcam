package com.example.tianyi.dashcam;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class CameraActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    private Boolean mIsRecording = false;
    private GPSTracker mGPSTracker;
    private Session mSession;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mCamera = getCameraInstance();
        mGPSTracker = new GPSTracker(CameraActivity.this);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        // Add a listener to the Capture button
        final Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setText("Record");
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if GPS enabled
                if (!mGPSTracker.canGetLocation()) {
                    mGPSTracker.showSettingsAlert();
                }

                if (mIsRecording) {
                    // stop recording and release camera
                    mMediaRecorder.stop();  // stop the recording
                    releaseMediaRecorder(); // release the MediaRecorder object
                    mCamera.lock();         // take camera access back from MediaRecorder

                    mGPSTracker.stop();
                    mSession.flushLocationData();

                    captureButton.setText("Record");
                    mIsRecording = false;
                } else {
                    mSession = new Session();
                    mGPSTracker.setSession(mSession);

                    // initialize video camera
                    if (prepareVideoRecorder()) {
                        // Camera is available and unlocked, MediaRecorder is prepared,
                        // now you can start recording

                        mMediaRecorder.start();
                        captureButton.setText("Stop");

                        mIsRecording = true;
                    } else {
                        releaseMediaRecorder();
                    }
                }
            }
        });
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(0); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.e("Error", "Camera not available!");
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGPSTracker.stop();
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    private boolean prepareVideoRecorder() {
        mCamera = getCameraInstance();
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(mSession.getVideoPath());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }

        return true;
    }
}
