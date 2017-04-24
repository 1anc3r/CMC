package me.lancer.cmc.mvp.score;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.lancer.cmc.util.ContentGetterSetter;

/**
 * Created by HuangFangzhi on 2016/12/13.
 */

public class ScoreModel {

    IScorePresenter presenter;

    String value, number, name, cookie;

    Bundle yearBundle, termBundle;

    public ScoreModel(IScorePresenter presenter) {
        this.presenter = presenter;
    }

    public void loadScore(String number, String name, String cookie, boolean refresh) {
        this.number = number;
        this.name = name;
        this.cookie = cookie;
        ContentGetterSetter contentGetterSetter1 = new ContentGetterSetter("score_", number);
        ContentGetterSetter contentGetterSetter2 = new ContentGetterSetter("level_", number);
        String url1 = "http://222.197.143.7/xscj.aspx?xh=" + number + "&xm=" + name + "&gnmkdm=N121604";
        String url2 = "http://222.197.143.7/xsdjkscx.aspx?xh=" + number + "&xm=" + name + "&gnmkdm=N121605";
        String path = Environment.getExternalStorageDirectory().toString();
        String content1, content2;
        List<ScoreBean> list1, list2 = null;
        if (!(content1 = contentGetterSetter1.getContentFromFile(path)).contains("失败!") && !(content2 = contentGetterSetter2.getContentFromFile(path)).contains("失败!") && !refresh) {
            list1 = getScoreFromJson(content1);
            list2 = getLevelFromJson(content2);
            list1.addAll(list2);
            presenter.loadScoreSuccess(list1);
            Log.e("loadScore", "获取成绩成功!");
        } else if (!(content1 = contentGetterSetter1.getContentFromHtml(url1, cookie)).contains("失败!") && !(content2 = contentGetterSetter2.getContentFromHtml(url2, cookie)).contains("失败!") && refresh) {
            yearBundle = getYearBundleFromContent(content1);
            termBundle = getTermBundleWithYearBundle(yearBundle);
            list1 = getScoreFromBundle(yearBundle, termBundle);
            list2 = getLevelFromContent(content2);
            content1 = setScoreToJson(list1);
            content2 = setLevelToJson(list2);
            contentGetterSetter1.setContentToFile(path, content1);
            contentGetterSetter2.setContentToFile(path, content2);
            list1.addAll(list2);
            presenter.loadScoreSuccess(list1);
            Log.e("loadScore", "获取成绩成功!");
        } else {
            presenter.loadScoreFailure(content1);
            Log.e("loadScore", "获取成绩失败!");
        }
    }

    public Bundle getYearBundleFromContent(String content) {
        Bundle yearBundle = new Bundle();
        Document document = Jsoup.parse(content);
        Element element = document.getElementById("Form1");
        Elements elements = element.getElementsByAttributeValue("name", "__VIEWSTATE");
        for (int j = 0; j < elements.size(); j++) {
            if (elements.get(j).tag().toString().equals("input")) {
                value = elements.get(j).attr("value");
            }
        }
        element = document.getElementById("ddlXN");
        if (element != null) {
            int i = 0;
            for (Element element1 : element.getAllElements()) {
                if (!element1.text().equals("") && element1.tag().toString().equals("option")) {
                    yearBundle.putString("year" + i, element1.text());
                    i++;
                }
            }
        }
        return yearBundle;
    }

    public Bundle getTermBundleWithYearBundle(Bundle yearBundle) {
        Bundle termBundle = new Bundle();
        String url = "http://222.197.143.7/xscj.aspx?xh=" + number + "&xm=" + name + "&gnmkdm=N121604";
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setFollowRedirects(false);
        for (int i = 0; i < yearBundle.size(); i++) {
            for (int j = 2; j > 0; j--) {
                Object object;
                String year = "";
                String term = "";
                if ((object = yearBundle.get("year" + i)) != null) {
                    year = object.toString();
                    term = String.valueOf(j);
                }
                FormEncodingBuilder builder = new FormEncodingBuilder();
                builder.add("__VIEWSTATE", value)
                        .add("ddlXN", year)
                        .add("ddlXQ", term)
                        .add("txtQSCJ", "0")
                        .add("txtZZCJ", "100")
                        .add("Button1", "%E6%8C%89%E5%AD%A6%E6%9C%9F%E6%9F%A5%E8%AF%A2");
                Request request = new Request.Builder().url(url).addHeader("Cookie", cookie).addHeader("Referer", url).post(builder.build()).build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.code() == 200) {
                        BufferedReader reader = new BufferedReader(response.body().charStream());
                        StringBuilder content = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            content.append(line);
                        }
                        reader.close();
                        if ((object = yearBundle.get("year" + i)) != null) {
                            termBundle.putString(object.toString() + " " + j, content.toString());
                        }
                    } else {
                        Log.e("termBundle", "获取成绩失败!状态码:" + response.code());
                        return null;
                    }
                } catch (IOException e) {
                    Log.e("termBundle", "获取成绩失败!捕获异常:" + e.toString());
                    return null;
                }
            }
        }
        return termBundle;
    }

    public List<ScoreBean> getScoreFromBundle(Bundle yearBundle, Bundle termBundle) {
        List<ScoreBean> scoreList = new ArrayList<>();
        for (int i = 0; i < yearBundle.size(); i++) {
            Object object = yearBundle.get("year" + i);
            for (int j = 2; j > 0; j--) {
                if (object != null) {
                    Document document = Jsoup.parse(termBundle.get(object.toString() + " " + j).toString());
                    Element element = document.getElementById("DataGrid1");
                    if (element == null) {
                        element = document.getElementById("Datagrid1");
                    }
                    Elements elements = element.getElementsByTag("tr");
                    for (int k = 1; k < elements.size(); k++) {
                        ScoreBean sbItem = new ScoreBean();
                        String year = yearBundle.get("year" + i).toString();
                        String term = String.valueOf(j);
                        String name = elements.get(k).getAllElements().get(2).text();
                        String property = elements.get(k).getAllElements().get(3).text();
                        String value = elements.get(k).getAllElements().get(5).text();
                        String resit = elements.get(k).getAllElements().get(7).text();
                        String retake = elements.get(k).getAllElements().get(8).text();
                        String credit = elements.get(k).getAllElements().get(9).text();
                        sbItem.setScoreYear(year);
                        sbItem.setScoreTerm(term);
                        sbItem.setScoreName(name);
                        sbItem.setScoreProperty(property);
                        sbItem.setScoreCredit(credit);
                        sbItem.setScoreValue(value);
                        sbItem.setScoreResit(resit);
                        sbItem.setScoreRetake(retake);
                        scoreList.add(sbItem);
                    }
                }
            }
        }
        return scoreList;
    }

    public List<ScoreBean> getLevelFromContent(String content) {
        List<ScoreBean> scoreList = new ArrayList<>();
        Document document = Jsoup.parse(content);
        Element element = document.getElementById("DataGrid1");
        if (element == null) {
            element = document.getElementById("Datagrid1");
        }
        Elements elements = element.getElementsByTag("tr");
        for (int k = 1; k < elements.size(); k++) {
            ScoreBean sbItem = new ScoreBean();
            String year = "等级考试";
            String term = "等级考试";
            String name = elements.get(k).getAllElements().get(3).text()+" ("+elements.get(k).getAllElements().get(1).text()+"学年"+elements.get(k).getAllElements().get(2).text()+"学期)";
            String property = "等级考试";
            String credit = "0";
            String value = elements.get(k).getAllElements().get(6).text();
            Log.e("value", name+ " " +value+" "+(value.equals(" +")));
            if (value == null || value.equals(" ")){
                value = String.valueOf(Integer.parseInt(elements.get(k).getAllElements().get(11).text())+Integer.parseInt(elements.get(k).getAllElements().get(12).text()));
            }
            sbItem.setScoreYear(year);
            sbItem.setScoreTerm(term);
            sbItem.setScoreProperty(property);
            sbItem.setScoreCredit(credit);
            sbItem.setScoreName(name);
            sbItem.setScoreValue(value);
            scoreList.add(sbItem);
        }
        return scoreList;
    }

    public List<ScoreBean> getScoreFromJson(String json) {
        try {
            JSONObject jbScore = new JSONObject(json);
            JSONArray jaScore = (JSONArray) jbScore.get("score");
            List<ScoreBean> list = new ArrayList<>();
            for (int i = 0; i < jaScore.length(); i++) {
                ScoreBean sbItem = new ScoreBean();
                JSONObject jbItem = (JSONObject) jaScore.get(i);
                sbItem.setScoreYear((String) jbItem.get("year"));
                sbItem.setScoreTerm((String) jbItem.get("term"));
                sbItem.setScoreName((String) jbItem.get("name"));
                sbItem.setScoreProperty((String) jbItem.get("property"));
                sbItem.setScoreCredit((String) jbItem.get("credit"));
                sbItem.setScoreValue((String) jbItem.get("value"));
                sbItem.setScoreResit((String) jbItem.get("resit"));
                sbItem.setScoreRetake((String) jbItem.get("retake"));
                list.add(sbItem);
            }
            return list;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String setScoreToJson(List<ScoreBean> list) {
        try {
            JSONObject jbScore = new JSONObject();
            JSONArray jaScore = new JSONArray();
            for (ScoreBean sbItem : list) {
                JSONObject jbItem = new JSONObject();
                jbItem.put("year", sbItem.getScoreYear());
                jbItem.put("term", sbItem.getScoreTerm());
                jbItem.put("name", sbItem.getScoreName());
                jbItem.put("property", sbItem.getScoreProperty());
                jbItem.put("credit", sbItem.getScoreCredit());
                jbItem.put("value", sbItem.getScoreValue());
                jbItem.put("resit", sbItem.getScoreResit());
                jbItem.put("retake", sbItem.getScoreRetake());
                jaScore.put(jbItem);
            }
            jbScore.put("score", jaScore);
            return jbScore.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "获取成绩失败!捕获异常:" + e.toString();
        }
    }

    public List<ScoreBean> getLevelFromJson(String json) {
        try {
            JSONObject jbScore = new JSONObject(json);
            JSONArray jaScore = (JSONArray) jbScore.get("score");
            List<ScoreBean> list = new ArrayList<>();
            for (int i = 0; i < jaScore.length(); i++) {
                ScoreBean sbItem = new ScoreBean();
                JSONObject jbItem = (JSONObject) jaScore.get(i);
                sbItem.setScoreYear((String) jbItem.get("year"));
                sbItem.setScoreTerm((String) jbItem.get("term"));
                sbItem.setScoreName((String) jbItem.get("name"));
                sbItem.setScoreProperty((String) jbItem.get("property"));
                sbItem.setScoreCredit((String) jbItem.get("credit"));
                sbItem.setScoreValue((String) jbItem.get("value"));
                list.add(sbItem);
            }
            return list;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String setLevelToJson(List<ScoreBean> list) {
        try {
            JSONObject jbScore = new JSONObject();
            JSONArray jaScore = new JSONArray();
            for (ScoreBean sbItem : list) {
                JSONObject jbItem = new JSONObject();
                jbItem.put("year", sbItem.getScoreYear());
                jbItem.put("term", sbItem.getScoreTerm());
                jbItem.put("name", sbItem.getScoreName());
                jbItem.put("property", sbItem.getScoreProperty());
                jbItem.put("credit", sbItem.getScoreCredit());
                jbItem.put("value", sbItem.getScoreValue());
                jaScore.put(jbItem);
            }
            jbScore.put("score", jaScore);
            return jbScore.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "获取成绩失败!捕获异常:" + e.toString();
        }
    }
}
