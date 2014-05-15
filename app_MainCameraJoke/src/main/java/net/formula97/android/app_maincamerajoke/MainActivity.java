package net.formula97.android.app_maincamerajoke;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 *
 */
public class MainActivity extends ActionBarActivity implements SurfaceHolder.Callback {

    Camera mCamera;
    //private CamView camView;

    SurfaceView camPreview;
    SurfaceView hudView;
    //private SurfaceHolder holder;
    private final String mWakeLockTag = "net.formula97.android.app_maincamerajoke.ACTION_SCREEN_KEEP";
    PowerManager.WakeLock lock = null;
    private boolean mProgressFlag = false;

    /**
     * Activity生成時に最初に呼ばれる。
     * @param savedInstanceState
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // オーバーレイ表示
        HudView hudViewCallback = new HudView(getApplicationContext());
        hudView = (SurfaceView)findViewById(R.id.hudDrawView);
        hudView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        hudView.getHolder().addCallback(hudViewCallback);

        // カメラプレビュー表示
        camPreview = (SurfaceView) findViewById(R.id.sv_camPreview);
        camPreview.getHolder().addCallback(this);
        // API Level 11以上では無視される。
        camPreview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // SurfaceViewにプレビューをセットする
        mCamera = safeCamOpen(Camera.CameraInfo.CAMERA_FACING_BACK);

        // WAKE_LOCKの取得準備
        PowerManager powerManager = (PowerManager)getSystemService(POWER_SERVICE);
        lock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, mWakeLockTag);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // WAKE_LOCKを取得する
        lock.acquire();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera.stopPreview();

        // WAKE_LOCKを開放する
        lock.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera.release();
    }

    private Camera safeCamOpen(int camId) {
        Camera c = null;
        Camera.CameraInfo info = new Camera.CameraInfo();
        int numberOfCams = Camera.getNumberOfCameras();

        for (int i = 0; i <= numberOfCams; i++) {
            if (info.facing == camId) {
                c = Camera.open(i);
                break;
            }
        }
        return c;
    }

    /**
     * This is called immediately after the surface is first created.
     * Implementations of this should start up whatever rendering code
     * they desire.  Note that only one thread can ever draw into
     * a {@link Surface}, so you should not draw into the Surface here
     * if your normal rendering will be in another thread.
     *
     * @param holder The SurfaceHolder whose surface is being created.
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(camPreview.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This is called immediately after any structural changes (format or
     * size) have been made to the surface.  You should at this point update
     * the imagery in the surface.  This method is always called at least
     * once, after {@link #surfaceCreated}.
     *
     * @param holder The SurfaceHolder whose surface has changed.
     * @param format The new PixelFormat of the surface.
     * @param width  The new width of the surface.
     * @param height The new height of the surface.
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // カメラプレビューを開始する
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        Camera.Size selected = sizeList.get(0);
        parameters.setPreviewSize(selected.width, selected.height);
        mCamera.setDisplayOrientation(90);  // 縦画面にする
        mCamera.startPreview();

    }

    /**
     * This is called immediately before a surface is being destroyed. After
     * returning from this call, you should no longer try to access this
     * surface.  If you have a rendering thread that directly accesses
     * the surface, you must ensure that thread is no longer touching the
     * Surface before returning from this function.
     *
     * @param holder The SurfaceHolder whose surface is being destroyed.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    /**
     * プレビュー表示をキャプチャーするためのコールバックをセットする。
     */
    public void takePreviewRawData() {
        if (!mProgressFlag) {
            mProgressFlag = true;
            mCamera.setPreviewCallback(editPreviewImage);
            //プレビューコールバックをセット
        }
    }

    private final Camera.PreviewCallback editPreviewImage =
            new Camera.PreviewCallback() {

                /**
                 * Called as preview frames are displayed.  This callback is invoked
                 * on the event thread {@link #open(int)} was called from.
                 * <p/>
                 * <p>If using the {@link android.graphics.ImageFormat#YV12} format,
                 * refer to the equations in {@link android.hardware.Camera.Parameters#setPreviewFormat}
                 * for the arrangement of the pixel data in the preview callback
                 * buffers.
                 *
                 * @param data   the contents of the preview frame in the format defined
                 *               by {@link android.graphics.ImageFormat}, which can be queried
                 *               with {@link android.hardware.Camera.Parameters#getPreviewFormat()}.
                 *               If {@link android.hardware.Camera.Parameters#setPreviewFormat(int)}
                 *               is never called, the default will be the YCbCr_420_SP
                 *               (NV21) format.
                 * @param camera the Camera service object.
                 */
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    mCamera.setPreviewCallback(null);  // プレビューコールバックを解除

                    mCamera.stopPreview();  // プレビュー表示をいったん停止

                    // TODO 内蔵ストレージにプレビューを上書きする処理を書く

                    // TODO 上書きしたプレビューの「明るさ」要素を分析する処理を書く

                    mCamera.startPreview(); // プレビュー表示を再開
                    mProgressFlag = false;  // コールバックセットを指示
                }
            };

}
