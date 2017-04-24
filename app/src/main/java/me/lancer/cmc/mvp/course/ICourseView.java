package me.lancer.cmc.mvp.course;

import java.util.List;

import me.lancer.cmc.mvp.base.IBaseView;

/**
 * Created by HuangFangzhi on 2016/12/13.
 */

public interface ICourseView extends IBaseView {

    void showCourse(List<CourseBean> list);
}
