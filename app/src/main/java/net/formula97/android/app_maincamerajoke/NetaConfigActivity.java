package net.formula97.android.app_maincamerajoke;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class NetaConfigActivity extends ActionBarActivity implements
        View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener,
        AddNetaDialogFragment.OnDialogClosedCallback, LongClickMenuDialogFragment.OnDialogCloseCallback {

    private ListView lvNetaList;
    private Button btnNetaConfigAdd;
    private List<NetaMessages> messageses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neta_config);

        setTitle(R.string.neta_config);

        lvNetaList = (ListView) findViewById(R.id.lvNetaList);
        btnNetaConfigAdd = (Button) findViewById(R.id.btnNetaConfigAdd);
    }

    @Override
    protected void onResume() {
        super.onResume();

        btnNetaConfigAdd.setOnClickListener(this);

        messageses = new ArrayList<NetaMessages>();
        NetaMessagesModel model = new NetaMessagesModel(this);
        try {
            messageses = (List<NetaMessages>) model.findBySingleArg(new NetaMessages(), "userDefined", true);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (messageses.size() > 0) {
            NetaAdapter adapter = new NetaAdapter(this, R.layout.user_neta_list_item, messageses);
            lvNetaList.setAdapter(adapter);
        }
        lvNetaList.setOnItemClickListener(this);
        lvNetaList.setOnItemLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnNetaConfigAdd:
                AddNetaDialogFragment fragment = AddNetaDialogFragment.getDialog("");
                fragment.show(getSupportFragmentManager(), "AddNeta");
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String target = ((NetaMessages) parent.getAdapter().getItem(position)).getMessageBody();
        AddNetaDialogFragment fragment = AddNetaDialogFragment.getDialog(target);
        fragment.show(getSupportFragmentManager(), "AddNeta");

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        String target = ((NetaMessages) parent.getAdapter().getItem(position)).getMessageBody();
        LongClickMenuDialogFragment fragment = LongClickMenuDialogFragment.getInstance(target);
        fragment.show(getSupportFragmentManager(), "ModifyNeta");
        return true;
    }

    @Override
    public void onDialogClosed(boolean isPositive) {
        refreshList();
    }

    /**
     * ListViewをリフレッシュする。
     */
    private void refreshList() {
        NetaMessagesModel model = new NetaMessagesModel(this);
        try {
            messageses = (List<NetaMessages>) model.findBySingleArg(new NetaMessages(), "userDefined", true);
            lvNetaList.setAdapter(null);
            NetaAdapter adapter = new NetaAdapter(this, R.layout.user_neta_list_item, messageses);
            lvNetaList.setAdapter(adapter);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDialogClose(int which) {
        refreshList();
    }
}
