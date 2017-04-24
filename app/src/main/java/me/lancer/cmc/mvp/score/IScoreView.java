package me.lancer.cmc.mvp.score;

import java.util.List;

import me.lancer.cmc.mvp.base.IBaseView;

/**
 * Created by HuangFangzhi on 2016/12/13.
 */

public interface IScoreView extends IBaseView {

    void showScore(List<ScoreBean> list);
}
