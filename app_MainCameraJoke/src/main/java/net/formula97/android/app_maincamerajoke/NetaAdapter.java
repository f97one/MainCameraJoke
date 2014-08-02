package net.formula97.android.app_maincamerajoke;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by HAJIME on 2014/08/02.
 */
public class NetaAdapter extends ArrayAdapter<NetaMessages> {
    private Context mContext;
    private int resId;
    List<NetaMessages> netaLists;
    private LayoutInflater inflater;

    public NetaAdapter(Context context, int resource, List<NetaMessages> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.resId = resource;
        this.netaLists = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (convertView != null) {
            v = convertView;
        } else {
            v = getInflater(mContext).inflate(resId, null);
        }
        NetaMessages messages = netaLists.get(position);

        ((TextView) v.findViewById(R.id.netaBody)).setText(messages.getMessageBody());
        return super.getView(position, convertView, parent);
    }

    /**
     * ContextからLayoutInflaterを得る。
     *
     * @param context 取得に必要なContext
     * @return LayoutInflaterのインスタンス
     */
    private LayoutInflater getInflater(Context context) {
        if (inflater == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        return inflater;
    }
}
