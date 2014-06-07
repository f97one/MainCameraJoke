package net.formula97.android.app_maincamerajoke;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * カメラプレビューとガンマーカーを表示するActivity。
 * @author HAJIME Fukuna
 */
public class MainActivity extends ActionBarActivity implements SurfaceHolder.Callback {

    /**
     * カメラのインスタンスを保持するフィールド。
     */
    Camera mCamera;

    /**
     * カメラプレビューを保持するSurfaceViewのフィールド。
     */
    SurfaceView camPreview;
    /**
     * ガンマーカーを保持するSurfaceViewのフィールド。
     */
    SurfaceView hudView;
    /**
     * WAKE_LOCKで使う識別子。
     */
    private static final String WAKE_LOCK_TAG = "net.formula97.android.app_maincamerajoke.ACTION_SCREEN_KEEP";
    /**
     * WAKE_LOCKのインスタンスを保持するフィールド。
     */
    PowerManager.WakeLock lock = null;
    /**
     * プレビュー画像保存処理が実行中か否かを保持するフラグ。
     */
    private boolean mProgressFlag = false;
    /**
     * プレビュー表示中か否かを格納するフラグ。
     */
    private boolean previewEnable = false;
    /**
     * フォーカス調整を定期実行するときの実行間隔（単位：ミリ秒）
     */
    private static final int FOCUS_REPEAT_INTERVAL = 5 * 1000;
    /**
     * Handlerに渡すメッセージコード。
     */
    private static final int HANDLER_MESSAGE_CODE = 0x7fff8001;

    private final String savedPreviewFilename = "SavedPreviewForAnalyze.raw";

    /**
     * Activity生成時に最初に呼ばれる。
     * @param savedInstanceState 保存されたBundleオブジェクト。
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
                PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, WAKE_LOCK_TAG);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // WAKE_LOCKを取得する
        lock.acquire();

        // Handlerの初回実行を定義
        // この方法だと、フラグをオフ->オンとなった場合、Handlerを再度動かす必要がある
        sendMessageToHandler();
    }

    /**
     * Handlerにメッセージ付きの実行指示を送る。
     */
    private void sendMessageToHandler() {
        Message message = new Message();
        message.what = HANDLER_MESSAGE_CODE;
        focusHandler.sendMessageDelayed(message, FOCUS_REPEAT_INTERVAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera.stopPreview();
        previewEnable = false;
        // このままアプリを終了させるので、Handlerの再定義は行わない。

        // WAKE_LOCKを開放する
        lock.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera.release();
    }

    /**
     * 安全にカメラを開く。
     * @param camId システムに登録されているカメラの向きを表すID
     * @return 開くことができたCameraオブジェクト
     */
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

        // プレビューサイズの選定
        // まずSurfaceViewのサイズを取得
        int viewHeight = camPreview.getHeight();
        int viewWidth = camPreview.getWidth();
        int deltaHeight = 65535;
        int deltaWidth = 65535;
        int prevH = 0;
        int prevW = 0;

        // 選択可能なプレビューサイズリストをカメラから取得
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();

        if (BuildConfig.DEBUG) {
            Log.d("MainActivity#surfaceChanged", "Supported preview size as follow :");
            for (int i = 0; i < sizeList.size(); i++) {
                Log.d("MainActivity#surfaceChanged", "Type " + String.valueOf(i + 1) + " : " +
                        sizeList.get(i).width + " x " + sizeList.get(i).height);
            }
        }

        // 表示可能なプレビューサイズのうち、うちわで一番近いものを選択
        for (int i = 0; i < sizeList.size(); i++) {
            if (viewHeight - sizeList.get(i).height < 0 || viewWidth - sizeList.get(i).width < 0) {
                // SurfaceViewのサイズを超えている場合は無視
                continue;
            } else {
                if (viewHeight - sizeList.get(i).width < deltaHeight && viewWidth - sizeList.get(i).height < deltaWidth) {
                    deltaHeight = viewHeight - sizeList.get(i).height;
                    deltaWidth = viewWidth - sizeList.get(i).width;
                    prevH = sizeList.get(i).height;
                    prevW = sizeList.get(i).width;
                }
            }
        }
        parameters.setPreviewSize(prevH, prevW);

        // 画面の向きに応じて、プレビューの角度を変える
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
        int rotationDegree = getWindowManager().getDefaultDisplay().getRotation() * 90;
        int degree;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            degree = (info.orientation + rotationDegree) % 360;
            degree = (360 - degree) % 360;
        } else {
            degree = (info.orientation - rotationDegree + 360) % 360;
        }
        mCamera.setDisplayOrientation(degree);

        mCamera.startPreview();
        previewEnable = true;
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

    /**
     *
     */
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

//                    mCamera.stopPreview();  // プレビュー表示をいったん停止
//                    previewEnable = false;
//
//                    // ストレージにプレビューを上書きするので、ファイル名は決め打ち
//                    String filepath = Environment.getExternalStorageDirectory().getPath() +
//                            "/" + savedPreviewFilename;
//
//                    FileOutputStream stream = null;
//
//                    try {
//                        stream = new FileOutputStream(filepath);
//                        stream.write(data);
//                        stream.close();
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    // TODO 上書きしたプレビューの「明るさ」要素を分析する処理を書く

                    mCamera.startPreview(); // プレビュー表示を再開
                    previewEnable = true;
                    sendMessageToHandler();

                    mProgressFlag = false;  // コールバックセットを指示
                }
            };

    /**
     * オートフォーカスのコールバック。<br />
     * ここでは、「オートフォーカスでフォーカス調整を行う」ことが目的なので、イベント発生に連動して
     * なにか処理を行わせる予定はない。
     */
    private Camera.AutoFocusCallback mAutoFocusListener = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            // オートフォーカスのイベント発生に連動して、何らかの処理を行う場合
        }
    };

    /**
     * フォーカス調整を定期的に行うHandler。<br />
     * 内部から同じメッセージコードでHandlerを動かすことで、定期的な実行を実現している。
     */
    private Handler focusHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (previewEnable) {
                // オートフォーカスを実行
                mCamera.autoFocus(mAutoFocusListener);
                // 次回実行を定義
                focusHandler.sendMessageDelayed(obtainMessage(), FOCUS_REPEAT_INTERVAL);
            }
        }
    };
}
