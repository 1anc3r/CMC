package me.lancer.cmc.mvp.score;

import java.util.List;

import me.lancer.cmc.mvp.base.IBasePresenter;

/**
 * Created by HuangFangzhi on 2016/12/14.
 */

public class ScorePresenter implements IBasePresenter<IScoreView>, IScorePresenter {

    private IScoreView view;
    private ScoreModel model;

    public ScorePresenter(IScoreView view) {
        attachView(view);
        model = new ScoreModel(this);
    }

    @Override
    public void attachView(IScoreView view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    public void loadScore(String number, String name, String cookie, boolean refresh) {
        if (view != null) {
            view.showLoad();
            model.loadScore(number, name, cookie, refresh);
        }
    }

    @Override
    public void loadScoreSuccess(List<ScoreBean> list) {
        if (view != null) {
            view.showScore(list);
            view.hideLoad();
        }
    }

    @Override
    public void loadScoreFailure(String log) {
        if (log != null && log.length() > 0 && view != null) {
            view.showMsg(log);
            view.hideLoad();
        }
    }
}
