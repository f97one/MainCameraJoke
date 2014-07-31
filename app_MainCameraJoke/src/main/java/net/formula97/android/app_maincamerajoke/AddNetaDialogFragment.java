package net.formula97.android.app_maincamerajoke;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.sql.SQLException;
import java.util.EventListener;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddNetaDialogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddNetaDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class AddNetaDialogFragment extends DialogFragment {

    private MainActivity activity;
    private OnDialogClosedCallback callback = null;

    public interface OnDialogClosedCallback extends EventListener {
        public void onDialogClosed(boolean isPositive);
    }

    public AddNetaDialogFragment() {
        // Required empty public constructor
    }

    public void addCallback(OnDialogClosedCallback callback) {
        this.callback = callback;
    }

    public void removeCallback() {
        this.callback = null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (MainActivity) activity;

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.fragment_add_neta_dialog, null);
        builder.setView(view);
        final EditText editText = (EditText) view.findViewById(R.id.editText);
        builder.setTitle(R.string.add_neta);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 特に何もしない。

                if (callback != null) {
                    // ダイアログを閉じたことを通知する
                    callback.onDialogClosed(false);
                }
            }
        });
        builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO DBに追加する処理を書く
                NetaMessages neta = new NetaMessages(editText.getText().toString());
                NetaMessagesModel model = new NetaMessagesModel(activity);
                try {
                    model.save(neta);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (callback != null) {
                    // ダイアログを閉じたことを通知する
                    callback.onDialogClosed(true);
                }
            }
        });

        return builder.create();
    }
}
