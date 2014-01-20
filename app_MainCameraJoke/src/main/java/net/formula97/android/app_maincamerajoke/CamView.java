package net.formula97.android.app_maincamerajoke;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Build;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by HAJIME on 14/01/20.
 */
public class CamView extends SurfaceView implements SurfaceHolder.Callback {

    private Context ctx;

    private SurfaceHolder holder;
    private Camera camera;

    /**
     * コンストラクタ。引き渡されたContextオブジェクトを記憶する。
     * @param context Context型、引き渡されたContextオブジェクト
     */
    public CamView(Context context) {
        super(context);
        this.ctx = context;
        holder = getHolder();
        holder.addCallback(this);
        // SurfaceHolder#setType()がAPI Level 11(=Build.VERSION_CODES.HONEYCOMB)以上では
        // 無視されるので、条件分けをする必要はないといえばないのだが、念のため。
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
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
        camera = safeCamOpen(CameraInfo.CAMERA_FACING_BACK);
        try {
            camera.setPreviewDisplay(holder);
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
        int viewWidth = ((SurfaceView) findViewById(R.id.sv_camPreview)).getWidth();
        int viewHeight = ((SurfaceView) findViewById(R.id.sv_camPreview)).getHeight();

        camera.stopPreview();

        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(viewWidth, viewHeight);
        camera.setParameters(parameters);

        camera.startPreview();
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

    private Camera safeCamOpen(int camId) {
        Camera c = null;

        int numberOfCams = Camera.getNumberOfCameras();

        for (int i = 0; i <= numberOfCams; i++) {
            Camera.CameraInfo info = null;
            Camera.getCameraInfo(i, info);
            if (info.facing == camId) {
                c = Camera.open(i);
            }
        }
        return c;
    }

}
