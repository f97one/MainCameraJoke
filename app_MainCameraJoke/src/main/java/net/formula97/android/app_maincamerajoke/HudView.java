package net.formula97.android.app_maincamerajoke;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * HUDちっくな線画を描画するSurfaceView。
 *
 * Created by f97one on 14/04/29.
 */
public class HudView extends SurfaceView implements SurfaceHolder.Callback {

    public Context getMContext() {
        return mContext;
    }

    private Context mContext;
    private SurfaceHolder mHolder;

    /**
     *
     * @param context
     */
    public HudView(Context context) {
        super(context);
        this.mContext = context;
        mHolder = getHolder();
//        // 透明にする
//        mHolder.setFormat(PixelFormat.TRANSLUCENT);
        mHolder.addCallback(this);
    }

    /**
     * 継承されたsurfaceCreated。
     * @param holder
     * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder)
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        Canvas canvas = holder.lockCanvas();

        // 画面の中心と、画面幅に応じた大きさの円のサイズを算出
        float cx = canvas.getWidth() / 2;
        float cy = canvas.getHeight() / 2;
        float radius = canvas.getWidth() / 6;

        // 塗りつぶしなしで中心に円を書く
        Paint drawCircle = new Paint();
        drawCircle.setStyle(Paint.Style.STROKE);
        drawCircle.setStrokeWidth(3.0f);
        drawCircle.setColor(Color.GREEN);
        canvas.drawCircle(cx, cy, radius, drawCircle);

        holder.unlockCanvasAndPost(canvas);
    }

    /**
     * 継承されたsurfaceChanged。
     * @param holder
     * @param format
     * @param width
     * @param height
     * @see android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder, int, int, int)
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * 継承されたsurfaceDestroyed。
     * @param holder
     * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.SurfaceHolder)
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
