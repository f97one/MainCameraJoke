package net.formula97.android.app_maincamerajoke;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.sql.SQLException;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HAJIME on 2014/08/03.
 */
public class LongClickMenuDialogFragment extends DialogFragment {
    private static final String RECEIVED_NETA = "FlagReceivedNeta";
    private String receivedNetaBody;
    private OnDialogCloseCallback callback = null;

    public static LongClickMenuDialogFragment getInstance(String netaMessage) {
        LongClickMenuDialogFragment fragment = new LongClickMenuDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(RECEIVED_NETA, netaMessage);
        fragment.setArguments(bundle);
        return fragment;
    }

    public String getReceivedNetaBody() {
        return receivedNetaBody;
    }

    public void setReceivedNetaBody(String receivedNetaBody) {
        this.receivedNetaBody = receivedNetaBody;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnDialogCloseCallback) {
            callback = (OnDialogCloseCallback) activity;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setReceivedNetaBody(getArguments().getString(RECEIVED_NETA));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final CharSequence[] menus = getActivity().getResources().getStringArray(R.array.longClickMenu);
        builder.setItems(menus, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NetaMessagesModel model = new NetaMessagesModel(getActivity());
                Map<String, Object> args = new HashMap<String, Object>();
                args.put("messageBody", getReceivedNetaBody());
                args.put("userDefined", true);
                NetaMessages messages = null;
                switch (which) {
                    case 0:
                        // 削除メニュー
                        try {
                            List<NetaMessages> results = (List<NetaMessages>) model.findByFieldValuesArgs(new NetaMessages(), args);
                            model.delete(results.get(0));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 1:
                        // 修正メニュー
                        AddNetaDialogFragment fragment = AddNetaDialogFragment.getDialog(getReceivedNetaBody());
                        fragment.show(getFragmentManager(), "AddNeta");
                        break;
                }
                if (callback != null) {
                    callback.onDialogClose(which);
                }
            }
        });

        return builder.create();
    }

    public interface OnDialogCloseCallback extends EventListener {
        public void onDialogClose(int which);
    }
}
