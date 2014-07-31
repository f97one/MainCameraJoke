package net.formula97.android.app_maincamerajoke;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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

    /**
     * エンティティのリストをまとめてデータベースに保存する。<br />
     * データベースに該当レコードがある場合はUpdate、ない場合はInsertされる。
     *
     * @param entities データベースに登録したいエンティティの集合
     * @param <T>
     * @throws SQLException
     */
    public <T> void save(List<?> entities) throws SQLException {
        DatabaseHelper helper = getHelper();

        Dao<T, Integer> dao = helper.getDao((Class<T>) entities.get(0).getClass());

        for (Object entity : entities) {
            dao.createOrUpdate((T) entity);
        }
    }

    /**
     * 指定したエンティティを削除する。
     *
     * @param entity 削除したいエンティティ
     * @param <T>
     * @return 影響のあった（＝削除された）レコード数
     * @throws SQLException
     */
    public <T> int delete(T entity) throws SQLException {
        DatabaseHelper helper = getHelper();

        Dao<T, Integer> dao = helper.getDao((Class<T>) entity.getClass());
        return dao.delete(entity);
    }

    /**
     * エンティティのテーブルに格納されているレコードをすべて取得する。
     *
     * @param entity 取得したいエンティティ
     * @param <T>
     * @return テーブルに格納されている全レコードの集合
     * @throws SQLException
     */
    public <T> List<?> findAll(T entity) throws SQLException {
        DatabaseHelper helper = getHelper();

        Dao<T, Integer> dao = helper.getDao((Class<T>) entity.getClass());

        return dao.queryForAll();
    }

    /**
     * 単一のフィールドに指定した値を持つレコードを取得する。
     *
     * @param entity 取得したいエンティティ
     * @param fieldName 検査するフィールド名称
     * @param value 検査対象の値
     * @param <T>
     * @return 条件に合致するレコードの集合
     * @throws SQLException
     */
    public <T> List<?> findBySingleArg(T entity, String fieldName, Object value) throws SQLException {
        DatabaseHelper helper = getHelper();
        Dao<T, Integer> dao = helper.getDao((Class<T>) entity.getClass());

        return dao.queryForEq(fieldName, value);
    }

    /**
     * フィールドと値のセットに該当するレコードを取得する。
     *
     * @param entity 取得したいエンティティ
     * @param fieldValues フィールドと値のセット
     * @param <T>
     * @return 条件に合致するレコードの集合
     * @throws SQLException
     */
    public <T> List<?> findByFieldValuesArgs(T entity, Map<String, Object> fieldValues) throws SQLException {
        DatabaseHelper helper = getHelper();
        Dao<T, Integer> dao = helper.getDao((Class<T>) entity.getClass());

        return dao.queryForFieldValuesArgs(fieldValues);
    }
}