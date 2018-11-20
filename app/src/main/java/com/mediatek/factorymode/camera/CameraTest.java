package com.mediatek.factorymode.camera;

import java.io.IOException;
import java.util.List;

import com.mediatek.factorymode.AppDefine;
import com.mediatek.factorymode.FactoryModeFeatureOption;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.TestActivity;
import com.mediatek.factorymode.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class CameraTest extends TestActivity implements OnClickListener {

    public static String TAG = "CameraTest";
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera mCamera;
    private Button mBtTake;
    private Button mBtOk;
    private Button mBtFailed;
    private SharedPreferences mSp;
    private boolean safeToTakePicture;
    private Camera.CameraInfo cameraInfo;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.camera);
        mSp = getSharedPreferences("FactoryMode", Context.MODE_PRIVATE);
        setupViews();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    private void setupViews() {
        surfaceView = (SurfaceView) findViewById(R.id.camera_view);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(surfaceCallback);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mBtTake = (Button) findViewById(R.id.camera_take);
        mBtTake.setOnClickListener(this);
        mBtOk = (Button) findViewById(R.id.camera_btok);
        mBtOk.setOnClickListener(this);
        mBtFailed = (Button) findViewById(R.id.camera_btfailed);
        mBtFailed.setOnClickListener(this);
        updateButtonStatus(false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_CAMERA || keyCode == KeyEvent.KEYCODE_SEARCH) {
            takePic();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void takePic() {
        if (!safeToTakePicture) {   //拍照前一定要先预览
            mCamera.startPreview();
            safeToTakePicture = true;
            mBtTake.setText(R.string.Camera_takepic);
        }else {
            mCamera.takePicture(null, null, pictureCallback);
            safeToTakePicture = false;
            mBtTake.setText(R.string.Camera_retake);
            updateButtonStatus(true);
        }
    }

    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
        }
    };

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.e("CameraTest","surfaceCreated()");
            safeToTakePicture = true;
            mBtTake.setText(R.string.Camera_takepic);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.e("CameraTest","surfaceChanged()");
            initCamera(holder);
            if (mCamera == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Utils.SetPreferences(getApplicationContext(), mSp, R.string.camera_name, AppDefine.FT_FAILED);
                finish();
                return;
            }
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPictureFormat(PixelFormat.JPEG);
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);

            Size preSize = parameters.getPreviewSize();

            List<Size> preSizes = parameters.getSupportedPreviewSizes();
            Size optimalSize = null;
            if (preSize != null && preSize.height != 0)
                optimalSize = getOptimalPreviewSize(preSizes, (double) width / (height));
            if (optimalSize != null) {
                int camOr = cameraInfo.orientation;
                if (FactoryModeFeatureOption.getInt("ro.sf.hwrotation", 0) == 270) {
                    if (camOr == 0 || camOr == 180) {
                        parameters.setPreviewSize(optimalSize.width, optimalSize.height);
                        parameters.setPictureSize(optimalSize.width, optimalSize.height);
                    } else {
                        parameters.setPreviewSize(optimalSize.height, optimalSize.width);
                        parameters.setPictureSize(optimalSize.height, optimalSize.width);
                    }
                } else {
                    if (camOr == 0 || camOr == 180) {
                        parameters.setPreviewSize(optimalSize.height, optimalSize.width);
                        parameters.setPictureSize(optimalSize.height, optimalSize.width);
                    } else {
                        parameters.setPreviewSize(optimalSize.width, optimalSize.height);
                        parameters.setPictureSize(optimalSize.width, optimalSize.height);
                    }
                }
            }
            mCamera.setParameters(parameters);
            if(safeToTakePicture){
                mCamera.startPreview();
                safeToTakePicture = true;
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.e("CameraTest","surfaceDestroyed()");
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        }
    };

    public void initCamera(SurfaceHolder holder){
        int cameraCount = 0;
        int result = 0;
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

            cameraInfo = new Camera.CameraInfo();
            cameraCount = Camera.getNumberOfCameras();
            for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    if (FactoryModeFeatureOption.getInt("ro.sf.hwrotation", 0) == 270
                            || FactoryModeFeatureOption.getInt("ro.sf.hwrotation", 0) == 90) {
                            result = (cameraInfo.orientation + degrees + 270) % 360;
                    } else {
                        result = (cameraInfo.orientation + degrees + 180) % 360;
                    }
                    result = (360 - result) % 360;
                    try {
                        mCamera = Camera.open(camIdx);
                        mCamera.setDisplayOrientation(result);
                        mCamera.setPreviewDisplay(holder);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

       

    private void updateButtonStatus(boolean status) {
        if (null != mBtOk) {
            mBtOk.setEnabled(status);
        }

    }

    private Size getOptimalPreviewSize(List<Size> sizes, double targetRatio) {
        final double ASPECT_TOLERANCE = 0.05;
        if (sizes == null)
            return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        Display display = getWindowManager().getDefaultDisplay();
        int targetHeight = Math.min(display.getHeight(), display.getWidth());

        if (targetHeight <= 0) {
            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            targetHeight = windowManager.getDefaultDisplay().getHeight();
        }

        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mBtTake.getId()) {
            takePic();
        } else if (v.getId() == mBtOk.getId()) {
            Utils.SetPreferences(getApplicationContext(), mSp, R.string.camera_name, AppDefine.FT_SUCCESS);
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
            finish();
        } else if (v.getId() == mBtFailed.getId()) {
            Utils.SetPreferences(getApplicationContext(), mSp, R.string.camera_name, AppDefine.FT_FAILED);
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
            finish();
        }
    }
}
