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

    /**
     * コンストラクタ。<br />
     * OrmLiteの動作に必要。
     */
    public NetaMessages() {

    }

    /**
     * ユーザーによるメッセージ本文とみなすコンストラクタ。
     *
     * @param messageBody 登録したいメッセージ本文
     */
    public NetaMessages(String messageBody) {
        setMessageBody(messageBody);
        setUserDefined(true);
    }

    /**
     * メッセージ登録者がユーザーか否かを明示したい場合に使用するコンストラクタ。
     *
     * @param messageBody 登録したいメッセージ本文
     * @param userDefined ユーザーによる登録とする場合はtrue、システムによる登録とする場合はfalse
     */
    public NetaMessages(String messageBody, boolean userDefined) {
        setMessageBody(messageBody);
        setUserDefined(userDefined);
    }

    public Integer get_id() {
        return _id;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public boolean isUserDefined() {
        return userDefined;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public void setUserDefined(boolean userDefined) {
        this.userDefined = userDefined;
    }
}
