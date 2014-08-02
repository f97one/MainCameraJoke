package net.formula97.android.app_maincamerajoke;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class NetaConfigActivity extends ActionBarActivity implements
        View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    ListView lvNetaList;
    Button btnNetaConfigAdd;

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

        List<NetaMessages> messageses = new ArrayList<NetaMessages>();
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
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }
}
