package net.formula97.android.app_maincamerajoke;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * ネタ表示するデータを格納するエンティティクラス。
 * Created by HAJIME on 2014/06/19.
 */
@DatabaseTable(tableName  = "NetaMessages")
public class NetaMessages {

    /**
     * 管理用主キー
     */
    @DatabaseField(generatedId = true, unique = true)
    private Integer _id;
    /**
     * ネタ表示のメッセージ本文
     */
    @DatabaseField
    private String messageBody;
    /**
     * ユーザにより追加されたか否かを表す。アップグレード管理用。<br />
     * trueならユーザー追加、falseならシステム組み込み。
     */
    @DatabaseField(defaultValue = "true")
    private boolean userDefined;

    public Integer get_id() {
        return _id;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public boolean isUserDefined() {
        return userDefined;
    }

    public void set_id(Integer _id) {
        this._id = _id;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public void setUserDefined(boolean userDefined) {
        this.userDefined = userDefined;
    }
}
