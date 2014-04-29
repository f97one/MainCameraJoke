package net.formula97.android.app_maincamerajoke;

import android.content.Context;
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

    public HudView(Context context) {
        super(context);
        this.mContext = context;
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
