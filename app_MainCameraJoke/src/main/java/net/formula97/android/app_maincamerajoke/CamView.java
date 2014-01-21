package net.formula97.android.app_maincamerajoke;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by HAJIME on 14/01/20.
 */
public class CamView extends SurfaceView implements SurfaceHolder.Callback {

    private Context ctx;

    private SurfaceHolder mHolder;
    private Camera camera;

    /**
     * コンストラクタ。引き渡されたContextオブジェクトを記憶する。
     * @param context Context型、引き渡されたContextオブジェクト
     */
    public CamView(Context context, Camera cam) {
        super(context);
        this.ctx = context;
        this.camera = cam;

        mHolder = getHolder();
        mHolder.addCallback(this);
        // SurfaceHolder#setType()がAPI Level 11(=Build.VERSION_CODES.HONEYCOMB)以上では
        // 無視されるので、条件分けをする必要はないといえばないのだが、念のため。
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
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
        // カメラを開く
        startCamPreview(holder);
    }

    private void startCamPreview(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            Log.e("CamView#surfaceCreated()", "Error setting camera preview: " + e.getMessage());
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

        // 有効なSurfaceViewがない場合は処理をやめる
        if(holder.getSurface() == null) return;

        camera.stopPreview();

        // カメラがサポートするプレビューサイズを取得する
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> previewSize = parameters.getSupportedPreviewSizes();

        // サポートするプレビューサイズのうち、一番大きいものをセットする
        int tmpWidth = 0;
        int prevWidth = width;
        int prevHeight = height;

        for (Camera.Size currSize : previewSize) {
            if (prevWidth < currSize.width || prevHeight < currSize.height) {
                continue;
            }

            if (tmpWidth < currSize.width) {
                tmpWidth = currSize.width;
                prevWidth = currSize.width;
                prevHeight = currSize.height;
            }
        }

        parameters.setPreviewSize(prevWidth, prevHeight);

        camera.setParameters(parameters);

        startCamPreview(mHolder);
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
        camera.stopPreview();
        camera.release();
    }

    /**
     * Contextオブジェクトを記憶する。
     * @param context Context型、記憶するContextオブジェクト
     */
    private void setCtx(Context context) {
        this.ctx = context;
    }

    /**
     * 記憶したContextオブジェクトを取り出す。
     * @return Context型、記憶されているContextオブジェクト
     */
    public Context getCtx() {
        return ctx;
    }

    /**
     * デバイスがカメラを持っているかどうかを判断する。
     * @param context Context型、アプリケーションコンテクスト
     * @return boolean型、カメラを有している場合はtrue、そうでない場合はfalse
     * @see http://developer.android.com/guide/topics/media/camera.html#detect-camera
     */
    public boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    public void releaseCam() {
        mHolder.removeCallback(this);
        this.camera.stopPreview();
        this.camera.release();
    }
}
