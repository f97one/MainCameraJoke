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
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddNetaDialogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddNetaDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddNetaDialogFragment extends DialogFragment {

    private static final String NETAMSG = "FlagNetaMsg";
    String receivedNetaString;
    //    private MainActivity activity;
    private OnDialogClosedCallback callback = null;

    public AddNetaDialogFragment() {
        // Required empty public constructor
    }

    public static AddNetaDialogFragment getDialog(String netaMsg) {
        AddNetaDialogFragment fragment = new AddNetaDialogFragment();

        Bundle b = new Bundle();
        b.putString(NETAMSG, netaMsg);
        fragment.setArguments(b);

        return fragment;
    }

    public String getReceivedNetaString() {
        return receivedNetaString;
    }

    public void setReceivedNetaString(String receivedNetaString) {
        this.receivedNetaString = receivedNetaString;
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

        if (activity instanceof OnDialogClosedCallback) {
            callback = (OnDialogClosedCallback) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        setReceivedNetaString(getArguments().getString(NETAMSG));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.fragment_add_neta_dialog, null);
        builder.setView(view);
        final EditText editText = (EditText) view.findViewById(R.id.editText);
        String title = "";
        String caption = "";
        if (StringUtils.isNullOrEmpty(getReceivedNetaString().trim())) {
            title = getString(R.string.add_neta);
            caption = getString(R.string.add);
        } else {
            title = getString(R.string.modify_neta);
            caption = getString(R.string.update);
            editText.setText(getReceivedNetaString());
        }
        builder.setTitle(title);
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
        builder.setPositiveButton(caption, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // DBに追加する処理
                NetaMessagesModel model = new NetaMessagesModel(getActivity());

                if (!StringUtils.isNullOrEmpty(getReceivedNetaString().trim())
                        && editText.getText().toString().trim().length() == 0) {
                    // レコードを削除してよいかの確認ダイアログを出す
                    try {
                        List<NetaMessages> results = (List<NetaMessages>) model.findBySingleArg(new NetaMessages(), "messageBody", getReceivedNetaString());
                        model.delete(results.get(0));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        NetaMessages neta;
                        if (StringUtils.isNullOrEmpty(getReceivedNetaString().trim())) {
                            neta = new NetaMessages(editText.getText().toString());
                        } else {
                            List<NetaMessages> results = (List<NetaMessages>) model.findBySingleArg(new NetaMessages(), "messageBody", getReceivedNetaString());
                            neta = results.get(0);
                            neta.setMessageBody(editText.getText().toString());
                        }
                        model.save(neta);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (callback != null) {
                    // ダイアログを閉じたことを通知する
                    callback.onDialogClosed(true);
                }
            }
        });

        return builder.create();
    }

    public interface OnDialogClosedCallback extends EventListener {
        public void onDialogClosed(boolean isPositive);
    }
}
