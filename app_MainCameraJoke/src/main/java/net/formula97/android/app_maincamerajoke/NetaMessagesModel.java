package net.formula97.android.app_maincamerajoke;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

/**
 * NetaMessagesを操作するModelクラス。
 * Created by HAJIME on 2014/06/19.
 */
public class NetaMessagesModel {

    /**
     * コンテキスト
     */
    private Context mContext;
    /**
     * DatabaseHelper
     */
    private DatabaseHelper databaseHelper;

    /**
     * コンストラクタ。<br />
     * コンテキストを受け取る。
     *
     * @param context コンテキスト
     */
    public NetaMessagesModel(Context context) {
        this.mContext = context;

    }

    /**
     * DatabaseHelperのインスタンスを得る。
     *
     * @return DatabaseHelperのインスタンス
     */
    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(mContext, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    /**
     * DBインスタンスを開放する。
     */
    public void release() {
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    /**
     * ネタメッセージを１件追加する。
     *
     * @param netaMessages 保存したいNetaMessagesのインスタンス
     */
    public void save(NetaMessages netaMessages) {
        DatabaseHelper helper = getHelper();

        try {
            Dao<NetaMessages, Integer> dao = helper.getDao(NetaMessages.class);
            dao.createOrUpdate(netaMessages);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            release();
        }
    }

    /**
     * リスト化したネタメッセージをまとめて追加する。
     *
     * @param netaMessagesList ネタメッセージの集合
     */
    public void save(List<NetaMessages> netaMessagesList) {
        DatabaseHelper helper = getHelper();

        try {
            Dao<NetaMessages, Integer> dao = helper.getDao(NetaMessages.class);

            for (NetaMessages messages : netaMessagesList) {
                dao.createOrUpdate(messages);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            release();
        }
    }

    /**
     * 現在登録されているネタメッセージをすべて返す。
     *
     * @return 現在登録されているネタメッセージのすべて
     */
    public List<NetaMessages> findAll() {
        DatabaseHelper helper = getHelper();
        List<NetaMessages> results = null;
        Dao<NetaMessages, Integer> dao = null;
        try {
            dao = helper.getDao(NetaMessages.class);
            results = dao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            release();
        }
        return results;
    }
}
