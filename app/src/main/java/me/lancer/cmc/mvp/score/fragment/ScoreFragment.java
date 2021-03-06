package me.lancer.cmc.mvp.score.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import me.lancer.cmc.R;
import me.lancer.cmc.mvp.base.fragment.PresenterFragment;
import me.lancer.cmc.mvp.score.IScoreView;
import me.lancer.cmc.mvp.score.ScoreBean;
import me.lancer.cmc.mvp.score.ScorePresenter;
import me.lancer.cmc.mvp.score.adapter.TermAdapter;
import me.lancer.cmc.ui.application.mApp;
import me.lancer.cmc.ui.view.cardstackview.CardStackView;

public class ScoreFragment extends PresenterFragment<ScorePresenter> implements IScoreView {

    private mApp app;

    CardStackView cardStackView;
    TermAdapter cardStackAdapter;
    ProgressDialog pdLogin;

    List<Integer> colorList = new ArrayList<>();
    List<ScoreBean> scoreList = new ArrayList<>();
    List<String> termList = new ArrayList<>();
    Map<String, List<ScoreBean>> termMap = new TreeMap<>();

    public static Integer[] palette = new Integer[]{
            R.color.primary_dark,
            R.color.blue,
            R.color.teal,
            R.color.green,
            R.color.yellow,
            R.color.orange,
            R.color.red,
            R.color.pink,
            R.color.purple,
    };

    String number, name, cookie;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    pdLogin.dismiss();
                    break;
                case 1:
                    pdLogin.show();
                    break;
                case 2:
                    Log.e(getString(R.string.log), (String) msg.obj);
                    break;
                case 3:
                    app.setScore(false);
                    cardStackAdapter = new TermAdapter(getActivity(), termList, termMap);
                    cardStackView.setAdapter(cardStackAdapter);
                    new Handler().postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    cardStackAdapter.updateData(colorList);
                                }
                            }, 200
                    );
                    break;
            }
        }
    };

    Runnable loadScore = new Runnable() {
        @Override
        public void run() {
            presenter.loadScore(number, name, cookie, app.isScore());
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_score, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        initData();
    }

    private void initView(View view) {
        cardStackView = (CardStackView) view.findViewById(R.id.csv_score);
        cardStackView.setItemExpendListener(new CardStackView.ItemExpendListener() {
            @Override
            public void onItemExpend(boolean expend) {

            }
        });
        cardStackAdapter = new TermAdapter(getActivity(), termList, termMap);
        cardStackView.setAdapter(cardStackAdapter);
        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        cardStackAdapter.updateData(colorList);
                    }
                }, 200
        );
        pdLogin = new ProgressDialog(getActivity());
        pdLogin.setMessage(getString(R.string.score_loading));
        pdLogin.setCancelable(false);
    }

    private void initData() {
        app = (mApp) getActivity().getApplication();
        number = app.getNumber();
        name = app.getName();
        cookie = app.getEduCookie();
        new Thread(loadScore).start();
    }

    @Override
    protected ScorePresenter onCreatePresenter() {
        return new ScorePresenter(ScoreFragment.this);
    }

    @Override
    public void showScore(List<ScoreBean> list) {
        scoreList = list;
        for (ScoreBean item : scoreList) {
            if (!item.getScoreYear().equals("等级考试") && !item.getScoreTerm().equals("等级考试")) {
                String termStr = item.getScoreYear() + getString(R.string.year) + item.getScoreTerm() + getString(R.string.term);
                if (termMap.get(termStr) == null) {
                    List<ScoreBean> itemList = new ArrayList<>();
                    itemList.add(item);
                    termList.add(termStr);
                    termMap.put(termStr, itemList);
                } else {
                    termMap.get(termStr).add(item);
                }
            } else {
                String termStr = "等级考试";
                if (termMap.get(termStr) == null) {
                    List<ScoreBean> itemList = new ArrayList<>();
                    itemList.add(item);
                    termList.add(termStr);
                    termMap.put(termStr, itemList);
                } else {
                    termMap.get(termStr).add(item);
                }
            }
        }
        for (int i = 0; i < termMap.size(); i++) {
            colorList.add(palette[i]);
        }
        Message msg = new Message();
        msg.what = 3;
        handler.sendMessage(msg);
    }

    @Override
    public void showMsg(String log) {
        Message msg = new Message();
        msg.what = 2;
        msg.obj = log;
        handler.sendMessage(msg);
    }

    @Override
    public void showLoad() {
        Message msg = new Message();
        msg.what = 1;
        handler.sendMessage(msg);
    }

    @Override
    public void hideLoad() {
        Message msg = new Message();
        msg.what = 0;
        handler.sendMessage(msg);
    }
}
