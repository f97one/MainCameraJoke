package net.formula97.android.app_maincamerajoke;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by HAJIME on 14/01/20.
 */
public class CamView extends SurfaceView implements SurfaceHolder.Callback {

    private Context ctx;

    /**
     * コンストラクタ。引き渡されたContextオブジェクトを記憶する。
     * @param context Context型、引き渡されたContextオブジェクト
     */
    public CamView(Context context) {
        super(context);
        this.ctx = context;
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
}
