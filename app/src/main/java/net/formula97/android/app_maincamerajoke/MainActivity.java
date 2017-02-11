package net.formula97.android.app_maincamerajoke;

import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * カメラプレビューとガンマーカーを表示するActivity。
 *
 * @author HAJIME Fukuna
 */
public class MainActivity extends ActionBarActivity implements SurfaceHolder.Callback, Camera.PreviewCallback, AddNetaDialogFragment.OnDialogClosedCallback {

    /**
     * WAKE_LOCKで使う識別子。
     */
    private static final String WAKE_LOCK_TAG = "net.formula97.android.app_maincamerajoke.ACTION_SCREEN_KEEP";
    /**
     * フォーカス調整を定期実行するときの実行間隔（単位：ミリ秒）
     */
    private static final int FOCUS_REPEAT_INTERVAL = 5 * 1000;
    /**
     * Handlerに渡すメッセージコード。
     */
    private static final int HANDLER_MESSAGE_CODE = 0x7fff8001;
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
     * WAKE_LOCKのインスタンスを保持するフィールド。
     */
    PowerManager.WakeLock lock = null;
    Handler handler;
    /**
     * プレビュー表示中か否かを格納するフラグ。
     */
    private boolean previewEnable = false;
    private int previewAreaHeight;
    private int previewAreaWidth;
    private byte[] mFrameBuffer;
    private int offset = 32;
    private boolean netaPostOk = false;
    private boolean enableAnalyze = true;

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
//                takePreviewRawData();
                // 次回実行を定義
                focusHandler.sendMessageDelayed(obtainMessage(), FOCUS_REPEAT_INTERVAL);
            }
        }
    };
    private boolean alreadyPost = false;
    private Runnable run = new Runnable() {
        @Override
        public void run() {
            if (netaPostOk) {
                String[] neta = getResources().getStringArray(R.array.neta_message);
                Random r = new Random();
                int index = r.nextInt(neta.length);

                Toast.makeText(MainActivity.this, neta[index], Toast.LENGTH_LONG).show();
                alreadyPost = false;
            }
        }
    };

    /**
     * Activity生成時に最初に呼ばれる。
     *
     * @param savedInstanceState 保存されたBundleオブジェクト。
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();

        // WAKE_LOCKの取得準備
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        lock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, WAKE_LOCK_TAG);

        // Dialogを閉じた時のリスナーをセットする

    }

    @Override
    protected void onResume() {
        super.onResume();

        // DBにネタデータがあるかを検索
        NetaMessagesModel model = new NetaMessagesModel(this);
//        NetaMessages message = new NetaMessages();
        List<NetaMessages> messages = new ArrayList<NetaMessages>();
        try {
            messages = (List<NetaMessages>) model.findAll(new NetaMessages());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (messages.size() == 0) {
            List<NetaMessages> sysAdd = new ArrayList<NetaMessages>();
            String[] neta = getResources().getStringArray(R.array.neta_message);

            for (String netaStr : neta) {
                NetaMessages netaMessages = new NetaMessages(netaStr, false);

                try {
                    model.save(netaMessages);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }

        // WAKE_LOCKを取得する
        lock.acquire();

        // オーバーレイ表示
        HudView hudViewCallback = new HudView(getApplicationContext());
        hudView = (SurfaceView) findViewById(R.id.hudDrawView);
        hudView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        hudView.getHolder().addCallback(hudViewCallback);

        // カメラプレビュー表示
        camPreview = (SurfaceView) findViewById(R.id.sv_camPreview);
        camPreview.getHolder().addCallback(this);
        // API Level 11以上では無視される。
        camPreview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // SurfaceViewにプレビューをセットする
        mCamera = safeCamOpen(Camera.CameraInfo.CAMERA_FACING_BACK);

        // Handlerの初回実行を定義
        // この方法だと、フラグをオフ->オンとなった場合、Handlerを再度動かす必要がある
        sendMessageToHandler();

        // AdMobのロード
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

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
        releaseCam();
        // このままアプリを終了させるので、Handlerの再定義は行わない。

        // WAKE_LOCKを開放する
        lock.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCam();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        enableAnalyze = false;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // ネタ文言追加Dialogの表示
                AddNetaDialogFragment fragment = AddNetaDialogFragment.getDialog("");
                fragment.show(getSupportFragmentManager(), "AddNeta");
                break;
            case R.id.message_setting:
                // ネタ文言編集用Activityの表示
                Intent i = new Intent(this, NetaConfigActivity.class);
                startActivity(i);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void releaseCam() {
        if (mCamera != null) {
            mCamera.stopPreview();
            previewEnable = false;
            handler.removeCallbacks(run);
            alreadyPost = false;
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 安全にカメラを開く。
     *
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
            mCamera.addCallbackBuffer(mFrameBuffer);
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

        // 選択可能なプレビューサイズリストをカメラから取得
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();

        if (BuildConfig.DEBUG) {
            Log.d("MainActivity#surfaceChanged", "Supported preview size as follow :");
            for (int i = 0; i < sizeList.size(); i++) {
                Log.d("MainActivity#surfaceChanged", "Type index " + String.valueOf(i) + " : " +
                        sizeList.get(i).width + " x " + sizeList.get(i).height);
            }
        }

        // カメラがサポートするプレビューフォーマット形式を調査する
        if (BuildConfig.DEBUG) {
            List<Integer> formatList = parameters.getSupportedPreviewFormats();

            String supportedFormatName = "";
            for (Integer elem : formatList) {
                switch (elem) {
                    case ImageFormat.JPEG:
                        supportedFormatName = "JPEG";
                        break;
                    case ImageFormat.NV16:
                        supportedFormatName = "NV16 (YCbCr Video)";
                        break;
                    case ImageFormat.NV21:
                        supportedFormatName = "NV21 (YCrCb Still Picture)";
                        break;
                    case ImageFormat.RGB_565:
                        supportedFormatName = "RGB_565 (RGB Picture)";
                        break;
                    case ImageFormat.YUV_420_888:
                        supportedFormatName = "YUV_420_888 (Multi-plane Android YUV format YCbCr)";
                        break;
                    case ImageFormat.YUY2:
                        supportedFormatName = "YUY2 (YCbCr YUYV Picture)";
                        break;
                    case ImageFormat.YV12:
                        supportedFormatName = "YV12 (Android YUV format)";
                        break;
                    case ImageFormat.UNKNOWN:
                        supportedFormatName = "UNKNOWN";
                        break;
                }
                Log.d("MainActivity#surfaceChanged", "Supported preview format : " + supportedFormatName);
            }
        }
        // プレビューフォーマットをNV21に固定
        parameters.setPreviewFormat(ImageFormat.NV21);

        // 表示可能なプレビューサイズのうち、うちわで一番近いものを選択
        for (Camera.Size aSizeList : sizeList) {
            if (viewHeight - aSizeList.height < 0 || viewWidth - aSizeList.width < 0) {
                // SurfaceViewのサイズを超えている場合は無視
                continue;
            } else {
                if (viewHeight - aSizeList.width < deltaHeight && viewWidth - aSizeList.height < deltaWidth) {
                    deltaWidth = viewWidth - aSizeList.width;
                    deltaHeight = viewHeight - aSizeList.height;
                    previewAreaWidth = aSizeList.width;
                    previewAreaHeight = aSizeList.height;
                }
            }
        }
        if (BuildConfig.DEBUG) {
            Log.d("MainActivity#surfaceChanged", "Preview width : " + String.valueOf(previewAreaWidth));
            Log.d("MainActivity#surfaceChanged", "Preview height : " + String.valueOf(previewAreaHeight));
        }
        parameters.setPreviewSize(previewAreaWidth, previewAreaHeight);

        // フレームバッファの計算
        int frameBufferSize = previewAreaWidth * previewAreaHeight * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8;
        if (BuildConfig.DEBUG) {
            Log.d("MainActivity#surfaceChanged", "Frame buffer size = " + String.valueOf(frameBufferSize));
        }
        mFrameBuffer = new byte[frameBufferSize];

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

        mCamera.setParameters(parameters);

        mCamera.setPreviewCallbackWithBuffer(this);
        mCamera.startPreview();
        mCamera.addCallbackBuffer(mFrameBuffer);
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
//        mCamera.setPreviewCallbackWithBuffer(null);
    }

    /**
     * プレビューデータを解析し、ネタ表示可能か否かを返す。
     *
     * @param data カメラプレビューのbyte列
     * @return ネタ表示可能な場合はtrue、そうでない場合はfalseを返す
     */
    private boolean analyzePreviewImage(byte[] data) {
        // サンプリングポイントは、プレビューサイズに応じて中心付近を適宜算出する

        // 中心点
        int center = (previewAreaHeight / 2) * previewAreaWidth - (previewAreaWidth / 2);
        // 左
        int lw = center - offset;
        // 右
        int rw = center + offset;
        // 上
        int above = (previewAreaHeight / 2 - offset) * previewAreaWidth - (previewAreaWidth / 2);
        // 下
        int below = (previewAreaHeight / 2 + offset) * previewAreaWidth - (previewAreaWidth / 2);

        // プレビューバッファが解析点の最後尾を割り込んでいる場合は、表示不可能を返す
        if (data.length <= below) {
            return false;
        }

        int[] samplingPoints = {
                above,
                lw,
                center,
                rw,
                below
        };
        int sample = 0;
        for (int samplingPoint : samplingPoints) {
            sample += data[samplingPoint];
        }
        float avg = sample / samplingPoints.length;

        boolean ret = false;
        if (avg < 10 && avg > -10) {
            ret = true;
        }

        return ret;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (enableAnalyze) {
            // 念のためPreviewCallbackを解除
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.stopPreview();  // プレビュー表示をいったん停止
            previewEnable = false;

            netaPostOk = analyzePreviewImage(data);
            if (netaPostOk) {
                if (BuildConfig.DEBUG) {
                    Log.i("MainActivity#onPreviewFrame", "NETA post OK");
                }
                if (!alreadyPost) {
                    alreadyPost = true;

                    // ネタを表示する
                    if (BuildConfig.DEBUG) {
                        Log.i("MainActivity#onPreviewFrame", "posted with handler.");
                    }
                    handler.postDelayed(run, 10 * 1000);
                }
            } else {
                if (BuildConfig.DEBUG) {
                    Log.i("MainActivity#onPreviewFrame", "NETA post NG");
                }
                handler.removeCallbacks(run);
                alreadyPost = false;
            }

            mCamera.startPreview(); // プレビュー表示を再開
            mCamera.setPreviewCallbackWithBuffer(this);
            mCamera.addCallbackBuffer(mFrameBuffer);
            previewEnable = true;
        }

    }

    @Override
    public void onDialogClosed(boolean isPositive) {
        enableAnalyze = true;
        mCamera.addCallbackBuffer(mFrameBuffer);
    }
}
