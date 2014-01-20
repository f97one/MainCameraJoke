package net.formula97.android.app_maincamerajoke;

import android.hardware.Camera;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import java.io.IOException;

/**
 *
 */
public class MainActivity extends ActionBarActivity implements SurfaceHolder.Callback {

    private Camera camera;

    private SurfaceView camPreview;

    /**
     * Activity生成時に最初に呼ばれる。
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        camPreview = (SurfaceView) findViewById(R.id.sv_camPreview);
    }

    /**
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     *
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //カメラを開く
        try {
            camera = safeCamOpen(Camera.CameraInfo.CAMERA_FACING_BACK);
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param holder
     * @param format
     * @param width
     * @param height
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        camera.stopPreview();
        camera.startPreview();
    }

    /**
     *
     * @param holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        camPreview.getHolder().addCallback(this);

        // SurfaceHolder#setType()がAPI Level 11(=Build.VERSION_CODES.HONEYCOMB)以上では
        // 無視されるので、条件分けをする必要はないといえばないのだが、念のため。
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            camPreview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}
