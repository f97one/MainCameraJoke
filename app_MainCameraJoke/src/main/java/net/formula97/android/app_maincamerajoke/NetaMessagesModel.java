package net.formula97.android.app_maincamerajoke;

import android.content.Context;

/**
 * NetaMessagesを操作するModelクラス。
 * Created by HAJIME on 2014/06/19.
 */
public class NetaMessagesModel extends BaseModel {

    /**
     * コンテキスト
     */
    private Context mContext;

    /**
     * コンストラクタ。<br />
     * コンテキストを受け取る。
     *
     * @param context コンテキスト
     */
    public NetaMessagesModel(Context context) {
        super(context);
        this.mContext = context;

    }
}
