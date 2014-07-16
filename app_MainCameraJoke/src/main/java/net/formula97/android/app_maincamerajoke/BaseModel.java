package net.formula97.android.app_maincamerajoke;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by HAJIME on 2014/06/22.
 */
public class BaseModel {

    /**
     * コンテキスト
     */
    private Context mContext;
    /**
     * DatabaseHelper
     */
    private DatabaseHelper databaseHelper;

    public BaseModel(Context context) {
        mContext = context;
    }
    /**
     * DatabaseHelperのインスタンスを得る。
     *
     * @return DatabaseHelperのインスタンス
     */
    public DatabaseHelper getHelper() {
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
     * 指定されたエンティティをデータベースに登録する。<br />
     * データベースに該当レコードがある場合はUpdate、ない場合はInsertされる。
     *
     * @param entity データベースに登録したいエンティティ
     * @throws SQLException
     */
    public <T> void save(T entity) throws SQLException {
        DatabaseHelper helper = getHelper();

        Dao<T, Integer> dao = helper.getDao((Class<T>) entity.getClass());
        dao.createOrUpdate(entity);
    }

    public <T> void save(List<?> entities) throws SQLException {
        DatabaseHelper helper = getHelper();

        Dao<T, Integer> dao = helper.getDao((Class<T>) entities.get(0).getClass());

        for (Object entity : entities) {
            dao.createOrUpdate((T) entity);
        }
    }

    public <T> int delete(T entity) throws SQLException {
        DatabaseHelper helper = getHelper();

        Dao<T, Integer> dao = helper.getDao((Class<T>) entity.getClass());
        return dao.delete(entity);
    }

    public <T> List<?> findAll(T entity) throws SQLException {
        DatabaseHelper helper = getHelper();

        Dao<T, Integer> dao = helper.getDao((Class<T>) entity.getClass());

        return dao.queryForAll();
    }


}
