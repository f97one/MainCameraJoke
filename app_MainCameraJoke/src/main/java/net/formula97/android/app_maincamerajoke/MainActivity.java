package net.formula97.android.app_maincamerajoke;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

/**
 *
 */
public class MainActivity extends ActionBarActivity {

    private Camera mCamera;
    private CamView camView;

    private SurfaceView camPreview;
    private SurfaceHolder holder;

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

    @Override
    protected void onResume() {
        super.onResume();

        // SurfaceViewにプレビューをセットする
        //mCamera = safeCamOpen(Camera.CameraInfo.CAMERA_FACING_BACK);
        mCamera = Camera.open();
        camView = new CamView(this, mCamera);
    }

    @Override
    protected void onPause() {
        super.onPause();
        camView.releaseCam();
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
