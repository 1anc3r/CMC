package me.lancer.cmc.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import me.lancer.cmc.R;
import me.lancer.cmc.mvp.base.fragment.BaseFragment;
import me.lancer.cmc.ui.activity.MainActivity;

/**
 * Created by HuangFangzhi on 2016/12/16.
 */

public class GuoFragment extends BaseFragment {

    LinearLayout llGuo;
    Toolbar tGuo;
    WebView wvGuo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_guo, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
    }

    private void initView(View view) {
        llGuo = (LinearLayout) view.findViewById(R.id.ll_guo);
        tGuo = (Toolbar) view.findViewById(R.id.t_guo);
        tGuo.setTitle("查快递，寄快递");
        ((MainActivity) getActivity()).initDrawer(tGuo);
        wvGuo = (WebView) view.findViewById(R.id.wv_guo);
        wvGuo.getSettings().setSupportZoom(true);
        wvGuo.getSettings().setBuiltInZoomControls(true);
        wvGuo.getSettings().setUseWideViewPort(true);
        wvGuo.getSettings().setJavaScriptEnabled(true);
        wvGuo.getSettings().setLoadWithOverviewMode(true);
        wvGuo.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        wvGuo.getSettings().setLoadWithOverviewMode(true);
        wvGuo.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

        });
        wvGuo.loadUrl("http://wap.guoguo-app.com/");
    }

    @Override
    public void onDestroy() {
        destroy();
        super.onDestroy();
    }

    public void destroy() {
        if (wvGuo != null) {
            ViewParent parent = wvGuo.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(wvGuo);
            }
            wvGuo.stopLoading();
            wvGuo.getSettings().setJavaScriptEnabled(false);
            wvGuo.clearHistory();
            wvGuo.clearView();
            wvGuo.removeAllViews();
            try {
                wvGuo.destroy();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }
}
