package com.lu.mydemo.Activity;

/**
 * 文档
 * https://www.yuque.com/zhuangfei/timetableview/
 * GitHub
 * https://github.com/zfman/TimetableView
 */

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lu.mydemo.Config.OptionManager;
import com.lu.mydemo.R;
import com.tapadoo.alerter.Alerter;
import com.zhuangfei.timetable.TimetableView;
import com.zhuangfei.timetable.listener.ISchedule;
import com.zhuangfei.timetable.listener.IWeekView;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.view.WeekView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import com.lu.mydemo.Config.ColorManager;
import com.lu.mydemo.Utils.Course.MySubject;
import com.lu.mydemo.Utils.Course.SubjectRepertory;
import com.lu.mydemo.Utils.Database.MyCourseDBHelper;
import com.lu.mydemo.Utils.Thread.MyThreadController;
import com.lu.mydemo.View.PopWindow.CourseDetailPopupWindow;
import com.lu.mydemo.View.PopWindow.CourseListPopupWindow;

import org.jetbrains.annotations.NotNull;

public class WeekCourseActivity extends BaseActivity implements View.OnClickListener {

    //控件
    TimetableView mTimetableView;
    WeekView mWeekView;

    private TextView navigation_back;

    LinearLayout layout;
    TextView titleTextView;
    List<MySubject> mySubjects;

    //记录切换的周次，不一定是当前周
    int target = -1;
    AlertDialog alertDialog;

    int now_week;
    int weeks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_course);

        Bundle bundle =  getIntent().getBundleExtra("bundle");
        target = (int) bundle.getLong("now_week");
        now_week = target;
        weeks = (int) bundle.getLong("weeks");

        titleTextView = findViewById(R.id.week_course_title);
        layout = findViewById(R.id.weekCourseLayout);
        layout.setOnClickListener(this);

        navigation_back = findViewById(R.id.activity_week_course_navigation_back_text);

        navigation_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        changeTheme();

        initTimetableView();

        requestData();
    }

    @Override
    protected void onStop() {
        mySubjects = null;
        super.onStop();
    }

    @Override
    protected void onRestart() {
        requestData();
        super.onRestart();
    }

    private void requestData() {
        final MyCourseDBHelper dbHelper = new MyCourseDBHelper(getApplicationContext(), "Course_DB", null, 1);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        mySubjects = dbHelper.getAllCourse(db);
        if(mySubjects == null || mySubjects.size() == 0) {//尝试重新加载
            if(mySubjects != null) Log.e("WeekCourseActivity", "Err when get course from db!");
            mySubjects = SubjectRepertory.loadDefaultSubjects(getApplicationContext());
            //DB TEST
            MyThreadController.commit(new Runnable() {
                @Override
                public void run() {
                    dbHelper.saveAll(db, mySubjects, true);
                    Log.i("WeekCourseActivity", "Course:" + dbHelper.getAllCourse(db));
                    db.close();
                }
            });
        }
        else db.close();
//        showLoading("加载中，请稍候...");
        handler.sendEmptyMessage(0x123);
    }

    @SuppressLint("HandlerLeak")
    Handler handler= new Handler(){
        @Override
        public void handleMessage(@NotNull Message msg) {
            super.handleMessage(msg);
            if(alertDialog!=null) alertDialog.hide();
            if(mySubjects == null){
                Alerter.hide();
                return;
            }
            mWeekView.source(mySubjects).showView();
            mTimetableView.source(mySubjects).showView();
            Alerter.hide();
        }
    };
    /**
     * 初始化课程控件
     */
    private void initTimetableView() {
        //获取控件
        mWeekView = findViewById(R.id.id_weekview);
        mTimetableView = findViewById(R.id.id_timetableView);

        //设置周次选择属性
        mWeekView.curWeek((int) MainActivity.now_week)
                .itemCount(weeks)
                .callback(new IWeekView.OnWeekItemClickedListener() {
                    @Override
                    public void onWeekClicked(int week) {
                        int cur = mTimetableView.curWeek();
                        //更新切换后的日期，从当前周cur->切换的周week
                        mTimetableView.onDateBuildListener()
                                .onUpdateDate(cur, week);
                        mTimetableView.changeWeekOnly(week);
                    }
                })
                .callback(new IWeekView.OnWeekLeftClickedListener() {
                    @Override
                    public void onWeekLeftClicked() {
                        onWeekLeftLayoutClicked();
                    }
                })
                .isShow(false)//设置隐藏，默认显示
                .showView();

        mTimetableView.curWeek((int) MainActivity.now_week)
                //最大课程数
                .maxSlideItem(11)
                // 取消旗标布局
                .isShowFlaglayout(false)
                .isShowNotCurWeek(OptionManager.isShow_not_current_week_course())
                //透明度
                //日期栏0.1f、侧边栏0.1f，周次选择栏0.6f
                //透明度范围为0->1，0为全透明，1为不透明
                .alpha(0.2f, 0.1f, 0.6f)
//                .curTerm("大三下学期")
                .callback(new ISchedule.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, List<Schedule> scheduleList) {
                        display(scheduleList);
                    }
                })
                .callback(new ISchedule.OnWeekChangedListener() {
                    @Override
                    public void onWeekChanged(int curWeek) {
                        titleTextView.setText("第" + curWeek + "周");
                    }
                })
                .showView();
    }

    /**
     * 更新一下，防止因程序在后台时间过长（超过一天）而导致的日期或高亮不准确问题。
     */
    @Override
    protected void onStart() {
        super.onStart();
        mTimetableView.onDateBuildListener()
                .onHighLight();
    }

    /**
     * 周次选择布局的左侧被点击时回调
     * 对话框修改当前周次
     */
    protected void onWeekLeftLayoutClicked() {
        final String[] items = new String[weeks];
        int itemCount = mWeekView.itemCount();
        for (int i = 0; i < itemCount; i++) {
            items[i] = "第" + (i + 1) + "周";
        }
        target = -1;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置当前周");
        builder.setSingleChoiceItems(items, mTimetableView.curWeek() - 1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        target = i;
                    }
                });
        builder.setPositiveButton("设置为当前周", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (target != -1) {
                    mWeekView.curWeek(target + 1).updateView();
                    mTimetableView.changeWeekForce(target + 1);
                    Toast.makeText(WeekCourseActivity.this, "当前周数由学期设置自动计算，您的设置不会被保存！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    static String selectedCourseName = null;
    /**
     * 显示内容
     *
     * @param beans
     */
    protected void display(List<Schedule> beans) {
        final LinkedHashSet<String> courseNameSet = new LinkedHashSet<>();
        String str = "";
        for (Schedule bean : beans) {
            str += bean.getName() + ","+bean.getWeekList().toString()+","+bean.getStart()+","+bean.getStep()+"\n";
            courseNameSet.add(bean.getName());
        }

        final AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(selectedCourseName == null || selectedCourseName.length() < 1) return;
                MyThreadController.commit(new Runnable() {
                    @Override
                    public void run() {

                        final List<MySubject> temp_list = new ArrayList<>();
                        for(MySubject subject : mySubjects){
                            if(subject.getName().equals(selectedCourseName)) temp_list.add(subject);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //TEST course_detail.
                                CourseDetailPopupWindow informationPopWindow = new CourseDetailPopupWindow(WeekCourseActivity.this, temp_list, findViewById(R.id.weekCourseLayout).getHeight(), findViewById(R.id.weekCourseLayout).getWidth());
                                informationPopWindow.showAtLocation(WeekCourseActivity.this.findViewById(R.id.weekCourseLayout), Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
                            }
                        });
                    }
                });
            }
        };

        MyThreadController.commit(new Runnable() {
            @Override
            public void run() {
                final List<MySubject> temp_list = new ArrayList<>();
                for(MySubject subject : mySubjects){
                    if(courseNameSet.contains(subject.getName())) temp_list.add(subject);
                }
                if(courseNameSet.size() > 1) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //TEST 课程格子列表
                            CourseListPopupWindow courseDetailPopupWindow = new CourseListPopupWindow(WeekCourseActivity.this, temp_list, listener, findViewById(R.id.weekCourseLayout).getHeight(), findViewById(R.id.weekCourseLayout).getWidth());
                            courseDetailPopupWindow.showAtLocation(WeekCourseActivity.this.findViewById(R.id.weekCourseLayout), Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
                        }
                    });
                }
                else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //TEST 课程详情
                            CourseDetailPopupWindow informationPopWindow = new CourseDetailPopupWindow(WeekCourseActivity.this, temp_list, findViewById(R.id.weekCourseLayout).getHeight(), findViewById(R.id.weekCourseLayout).getWidth());
                            informationPopWindow.showAtLocation(WeekCourseActivity.this.findViewById(R.id.weekCourseLayout), Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.weekCourseLayout:
                //如果周次选择已经显示了，那么将它隐藏，更新课程、日期
                //否则，显示
                if (mWeekView.isShowing()) hideWeekView();
                else showWeekView();
                break;
        }
    }

    /**
     * 隐藏周次选择，此时需要将课表的日期恢复到本周并将课表切换到当前周
     */
    public void hideWeekView(){
        target = now_week;
        mWeekView.isShow(false);
        titleTextView.setTextColor(getResources().getColor(R.color.app_white));
        int cur = mTimetableView.curWeek();
        mTimetableView.onDateBuildListener()
                .onUpdateDate(cur, cur);
        mTimetableView.changeWeekOnly(cur);
    }

    public void showWeekView(){
        mWeekView.isShow(true);
        titleTextView.setTextColor(getResources().getColor(R.color.color_dark_text));
    }

    public void changeTheme(){
        super.changeTheme();
        findViewById(R.id.weekCourseLayout).setBackground(ColorManager.getMainBackground_full());
    }

    public static String getSelectedCourseName() {
        return selectedCourseName;
    }

    public static void setSelectedCourseName(String selectedCourseName) {
        WeekCourseActivity.selectedCourseName = selectedCourseName;
    }

}