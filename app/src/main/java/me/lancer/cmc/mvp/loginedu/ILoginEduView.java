package me.lancer.cmc.mvp.loginedu;

import me.lancer.cmc.mvp.base.IBaseView;

/**
 * Created by HuangFangzhi on 2016/12/13.
 */

public interface ILoginEduView extends IBaseView {

    void showCheckCode(String cookie);

    void login(String cookie);

    void home(String number, String cookie);
}
