package com.example.nativeopencvandroidtemplate;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "MainActivity";
    private static final int CAMERA_PERMISSION_REQUEST = 1;
    private static final Timer timer = new Timer();

    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");

                // Load native library after(!) OpenCV initialization
                System.loadLibrary("native-lib");
                mOpenCvCameraView.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Permissions for Android 6+
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST
        );

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mOpenCvCameraView = findViewById(R.id.main_surface);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);


        mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
//        mOpenCvCameraView.set
        mOpenCvCameraView.enableFpsMeter();
//                mOpenCvCameraView.setRotation(180);
//        mOpenCvCameraView.resi
        mOpenCvCameraView.setCvCameraViewListener(this);
        FaceDetectV2.init(getAssets(), 0, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mOpenCvCameraView.setCameraPermissionGranted();
            } else {
                String message = "Camera permission was not granted";
                Log.e(TAG, message);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(TAG, "Unexpected permission request");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame frame) {
        // get current camera frame as OpenCV Mat object
//        Mat mat = frame.gray();
        timer.tic();
        Mat mRgba = frame.rgba();
//        Imgproc.rot
        Mat rotateMat = Imgproc.getRotationMatrix2D(new Point(mRgba.rows() / 2, mRgba.cols() / 2), 90, 1);
        Imgproc.warpAffine(mRgba, mRgba, rotateMat, mRgba.size());

        Scalar green = new Scalar(0, 255, 0);
        Bitmap bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mRgba, bmp);

        timer.tic();
        float []result = FaceDetectV2.detectByBitmap(bmp);
        timer.toc("face detect v2 total cost");
        if(result != null){
            Imgproc.rectangle(mRgba, new Point(result[1], result[2]), new Point(result[3], result[4]), green);
        }

        timer.toc("process frame");

//        Utils.bitmapToMat(bmp, mRgba);
//        Mat out = new Mat();
//        Imgproc.resize(mRgba, out, new Size(), 0.5, 0.5);
        // native call to process current camera frame
//        adaptiveThresholdFromJNI(mat.getNativeObjAddr());
//        Imgproc.rectangle(mRgba, new Rect(0, 0, 100, 100), green);
//        Imgproc.putText(mRgba, ""+mRgba.width()+"x"+mRgba.height(),  new Point(0, 20) ,Imgproc.FONT_HERSHEY_SIMPLEX ,1, green);

        // return processed frame for live preview
        return mRgba;
    }

    private native void adaptiveThresholdFromJNI(long mat);
}
