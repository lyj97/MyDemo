package com.lu.mydemo.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.lu.mydemo.Config.OptionManager;
import com.lu.mydemo.Notification.AlertCenter;
import com.lu.mydemo.OA.NewsClient;
import com.lu.mydemo.R;
import com.lu.mydemo.Utils.Course.SubjectRepertory;
import com.lu.mydemo.Utils.Database.MyCourseDBHelper;
import com.lu.mydemo.Utils.Time.TeachTimeTools;
import com.tapadoo.alerter.Alerter;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import com.lu.mydemo.CJCX.CJCX;
import com.lu.mydemo.Config.ColorManager;
import com.lu.mydemo.Config.Version;
import com.lu.mydemo.Sensor.SensorManagerHelper;
import com.lu.mydemo.ToolFor2045_Site.InformationUploader;
import com.lu.mydemo.ToolFor2045_Site.GetInternetInformation;
import com.lu.mydemo.UIMS.UIMS;
import com.lu.mydemo.UIMSTool.ClassSetConvert;
import com.lu.mydemo.UIMSTool.CourseJSONTransfer;
import com.lu.mydemo.Utils.Course.CourseScheduleChange;
import com.lu.mydemo.Utils.Course.MySubject;
import com.lu.mydemo.Utils.Score.ScoreConfig;
import com.lu.mydemo.Utils.Score.ScoreInf;
import com.lu.mydemo.Utils.Thread.MyThreadController;
import com.lu.mydemo.View.PopWindow.*;

import org.apache.commons.collections.CollectionUtils;

public class MainActivity extends BaseActivity {

    private LinearLayout activity_login;

    private TextView timeInformation;
    private TextView termInformation;
    private ListView courseList;
    private ListView mMessageListLv;

    private Button webPagesButton;
    private Button get_save_button;
    private Button getNoneScoreCourseButton;
    private Button getNewsButton;
    private static SharedPreferences sp;
    private static final int PASSWORD_MIWEN = 0x81;

    private TextView enterWeekCourseTextView;

    private TextView toSettingTv;

    private TextView load_internet_inf_tv;
    private TextView goToTestTv;

    private ScrollView login_main_view;
    private TextView linearLayoutView_down_text;

    private TranslateAnimation mShowAction;
    private TranslateAnimation mHiddenAction ;

    private boolean isMainShow = true;
    private static boolean isInternetInformationShowed;
    private static boolean isCourseNeedReload = false;
    private static boolean reLoadTodayCourse = true;
    private boolean listHaveHeadFoot = false;

    public static long now_week;//当前周
    public static int weeks;//共多少周
    private int day_of_week;//今天周几
    public static final int MAX_VOCATION_WEEK_NUMBER = 9;//设置学期最大教学周数，用于时间正确性判断

    public static List<Map<String, Object>> todayCourseList = null;

    private int clickCount = 0;
    private TextView UIMSTest;

    public static UIMS uims;
//    public static boolean isLoginIn = false;
    public static boolean isLocalValueLoaded = false;
    public static Context context;

    String user;
    String pass;

    public LoginPopWindow popWindow;
    public LoginGetCourseSchedulePopupWindow courseSchedulePopupWindow;

    private static boolean acceptTestFun = false;

//    private String theme = "blue";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        //找到相应的布局及控件
        setContentView(R.layout.activity_main);

        sp = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        context = this;
        timeInformation = findViewById(R.id.time_information);
        termInformation = findViewById(R.id.term_information);
        courseList = findViewById(R.id.course_list);
        mMessageListLv = findViewById(R.id.message_list);
        UIMSTest = findViewById(R.id.UIMSTest);
        activity_login = findViewById(R.id.activity_login);
        load_internet_inf_tv = findViewById(R.id.load_internet_information_tv);
        get_save_button = findViewById(R.id.get_saved_button);
        getNoneScoreCourseButton = findViewById(R.id.load_none_score_course_information_button);
        getNewsButton = findViewById(R.id.get_news_button);

        enterWeekCourseTextView = findViewById(R.id.enterWeekCourseTextView);

        toSettingTv = findViewById(R.id.login_goto_setting_text_view);

        login_main_view = findViewById(R.id.login_main_view);
        linearLayoutView_down_text = findViewById(R.id.LinearLayoutView_down_text);

        webPagesButton = findViewById(R.id.web_pages_button);
        webPagesButton.setVisibility(View.GONE);

        //测试功能
        goToTestTv = findViewById(R.id.login_test);
        goToTestTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TestFunctionActivity.class));
            }
        });

        /**
         * 改变主题颜色
         */
        loadColorConfig();
        changeTheme();

        isCourseNeedReload = sp.getBoolean("isCourseNeedReload", false);

        isMainShow = sp.getBoolean("isMainShow", isMainShow);

        mShowAction = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        mShowAction.setDuration(500);

        mHiddenAction = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f);
        mHiddenAction.setDuration(500);

        if(!(timeInformation.getText().length() > 0)) timeInformation.setText("时间(首次查询后刷新)");

        if(!isMainShow){
            hideMainView(false);
        }

//        if(!isLoginIn) {
//            activity_main.requestLayout();
//        }
//        else{
//            loginSuccess();
//        }

        // i love you.
        if(sp.getBoolean("showEgg", true)){
            UIMSTest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickCount++;
                    if(clickCount == 7 && sp.getBoolean("showEgg", true)){
                        AlertCenter.showAlert(MainActivity.this, "", "I LOVE YOU. (❤ ω ❤)");
                        sp.edit().putBoolean("showEgg", false).apply();
                    }
                }
            });
        }

//        if(isLocalInformationAvailable()) loadLocalInformation(false);

        load_internet_inf_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                 * TEST 登录逻辑
                 */
                LoginPopWindow window = new LoginPopWindow(MainActivity.this, findViewById(R.id.activity_login).getHeight(), findViewById(R.id.activity_login).getWidth());
                window.setFocusable(true);
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                window.showAtLocation(MainActivity.this.findViewById(R.id.activity_login), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
                popWindow = window;

            }
        });

        get_save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                get_save_button.setEnabled(false);
                get_save_button.setText("数据转换中，请稍候...");

                //TEST 成绩查询不验证数据直接进入
//                com.lu.mydemo.CJCX.loadCJCXJSON(getApplicationContext());
//                ScoreActivity.context = getApplicationContext();
                Intent intent = new Intent(MainActivity.this, ScoreActivity.class);
                startActivity(intent);
            }
        });

        getNoneScoreCourseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                myTestFunction();
                if(isLocalValueLoaded){
                    Intent intent = new Intent(MainActivity.this,NoneScoreCourseActivity.class);
                    startActivity(intent);
//                    overridePendingTransition(R.anim.up_in, R.anim.up_out);
                }
                else{
                    AlertCenter.showAlert(MainActivity.this, "还没有已经保存的信息哦，点击\"更新信息\"再试试吧(*^_^*).");
                }
//                overridePendingTransition(R.anim.up_in, R.anim.up_out);
            }
        });

        getNewsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NewsActivity.class);
                startActivity(intent);
            }
        });

        enterWeekCourseTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isLocalValueLoaded){
                    Intent intent = new Intent(MainActivity.this,WeekCourseActivity.class);

                    Bundle bundle = new Bundle();
                    bundle.putLong("now_week", now_week);
                    bundle.putLong("weeks", weeks);

                    intent.putExtra("bundle", bundle);
                    startActivity(intent);
//                    overridePendingTransition(R.anim.up_in, R.anim.up_out);
                }
                else{
                    AlertCenter.showAlert(MainActivity.this, "还没有已经保存的信息哦，点击\"更新信息\"再试试吧(*^_^*).");
                }
            }
        });

        toSettingTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SettingActivity.class);
                startActivity(intent);
            }
        });

        linearLayoutView_down_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isMainShow){
                    hideMainView();
                }
                else {
                    showMainView();
                }
            }
        });

        View.OnClickListener goToCourseScheduleChangeActivityListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLocalValueLoaded) {
                    Intent intent = new Intent(MainActivity.this, CourseScheduleChangeActivity.class);
                    startActivity(intent);
                }
                else {
                    AlertCenter.showAlert(MainActivity.this, "还没有已经保存的信息哦，点击\"更新信息\"再试试吧(*^_^*).");
                }
            }
        };

        timeInformation.setOnClickListener(goToCourseScheduleChangeActivityListener);
        termInformation.setOnClickListener(goToCourseScheduleChangeActivityListener);

        isInternetInformationShowed = sp.getBoolean("isInternetInformationShowed", false);
//        Log.i("GetInternetInformation", "isInternetInformationShowed:\t" + isInternetInformationShowed);
        if(!isInternetInformationShowed) getInternetInformation();

        acceptTestFun = sp.getBoolean("acceptTestFun", false);
        if(!acceptTestFun) {
            hideTestFun();
        }

        //初始化错误上报，获取本地保存的Email地址
        InformationUploader.initUserInformation(getApplicationContext());

        //摇一摇
//        SensorManagerHelper.OnShakeListener onShakeListener = new SensorManagerHelper.OnShakeListener() {
//            @Override
//            public void onShake() {
//                if(Version.isApkInDebug(MainActivity.this)){
//                    AlertCenter.showAlert(MainActivity.this, "DEBUG MODE!");
//                }
//                else {
//                    AlertCenter.showWarningAlert(MainActivity.this, "NOT IN DEBUG MODE!");
//                }
//            }
//        };
//        registerShakeListener(onShakeListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Alerter.hide();

        ColorManager.loadConfig(getApplicationContext(), this);
        changeTheme();

        MyThreadController.commit(new Runnable() {
            @Override
            public void run() {
                if (!isLocalValueLoaded) {
                    loadLocalInformation(false);
                }
                ScoreConfig.loadScoreConfig(getApplicationContext());
                try {
                    CJCX.loadCJCXJSON(getApplicationContext());
                    CJCX.loadCJCXTermJSON(getApplicationContext());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("MainActivity", "com.lu.mydemo.CJCX Error:" + e.getMessage());
                }
                ScoreActivity.context = getApplicationContext();
                ScoreInf.loadScoreList();
            }
        });

        if(isLocalValueLoaded && isCourseNeedReload){
            showWarningAlertWithCancel_OKButton("需要刷新课程信息", "当前学期已经改变，请刷新本地课程信息。");
        }

        if(reLoadTodayCourse) loadCourseInformation();

        if(!acceptTestFun) hideTestFun();

        NewsClient.initUploadCheck();

    }

    @Override
    protected void onStop() {
        super.onStop();
        reLoadTodayCourse = true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(hasFocus) {
//            setButtonText(get_save_button, 500, "成绩查询", true);
//            initTeachTimeToolInf();
            get_save_button.setText("成绩查询");
            get_save_button.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            saveData();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void setButtonText(final Button button, final long sleepTime, final String text, final boolean enable){
        MyThreadController.commit(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(sleepTime);
                }catch (Exception e){
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button.setText(text);
                        button.setEnabled(enable);
                    }
                });
            }
        });
    }

    protected void hideTestFun(){
        goToTestTv.setVisibility(View.GONE);
    }

    public void hideMainView(){
        hideMainView(true);
    }

    public void hideMainView(boolean showAnimation){
        if(!showAnimation){
            login_main_view.setVisibility(View.INVISIBLE);
            linearLayoutView_down_text.setText("⇧显示下方区域");
            isMainShow = false;
            setMainViewGone();
        }
        else {
            login_main_view.startAnimation(mHiddenAction);
            login_main_view.setVisibility(View.INVISIBLE);
            linearLayoutView_down_text.setText("⇧显示下方区域");
            isMainShow = false;
            MyThreadController.commit(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                        setMainViewGone();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        sp.edit().putBoolean("isMainShow", isMainShow).apply();
    }

    public void setMainViewGone(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!isMainShow) login_main_view.setVisibility(View.GONE);
            }
        });
    }

    public void showMainView(){
        login_main_view.startAnimation(mShowAction);
        login_main_view.setVisibility(View.VISIBLE);
        linearLayoutView_down_text.setText("⇩隐藏下方区域");
        isMainShow = true;
        sp.edit().putBoolean("isMainShow", isMainShow).apply();
    }

    public void setWebLink(final String link){
        if(TextUtils.isEmpty(link)){
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webPagesButton.setVisibility(View.VISIBLE);
                webPagesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("link", link);
                        intent.putExtra("bundle", bundle);
                        startActivity(intent);
                    }
                });
            }
        });
    }

    public void loginSuccess() {
        sp.edit().putString("CurrentUserInfoJSON", UIMS.getCurrentUserInfoJSON().toString()).apply();
        sp.edit().putString("TermJSON", UIMS.getTermJSON().toString()).apply();
        sp.edit().putString("TeachingTermJSON", UIMS.getTeachingTermJSON().toString()).apply();

//        final JSONObject informationJSON = UIMS.getInformationJSON();
        final JSONObject teachingTermJSON = UIMS.getTeachingTermJSON();

//        isLoginIn = true;

        try {
            JSONObject value;
            try{
                value = teachingTermJSON.getJSONArray("value").getJSONObject(0);
            }catch (JSONException e){
                e.printStackTrace();
                value = teachingTermJSON;
            }
            weeks = value.getInt("weeks");
            loadTime();
            getCourseSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reLogin(){
        reLogin(false);
    }

    public void reLogin(final boolean showNotice){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Alerter.hide();
                if(showNotice) AlertCenter.showAlert(MainActivity.this, "如需登录其他账号，请删除本应用数据，以避免出现数据错误.\n\n" +
                        "删除方法：设置->应用->UIMSTest->存储->清除数据." );
//                isLoginIn = false;

                getNoneScoreCourseButton.requestLayout();
                activity_login.requestLayout();

            }
        });
    }

    public void loadSuccess(boolean reloadCourse) {
        sp.edit().putString("ScoreJSON", UIMS.getScoreJSON().toString()).apply();
        sp.edit().putString("CourseJSON", UIMS.getCourseJSON().toString()).apply();
        sp.edit().putString("StudentJSON", UIMS.getStudentJSON().toString()).apply();
        sp.edit().putString("CourseTypeJSON", UIMS.getCourseTypeJSON().toString()).apply();
        sp.edit().putString("CourseSelectTypeJSON", UIMS.getCourseSelectTypeJSON().toString()).apply();
        sp.edit().putString("ScoreStatisticsJSON", UIMS.getScoreStatisticsJSON().toString()).apply();
        sp.edit().putString("CurrentUserInfoJSON", UIMS.getCurrentUserInfoJSON().toString()).apply();
        sp.edit().putString("TermJSON", UIMS.getTermJSON().toString()).apply();
        sp.edit().putString("TeachingTermJSON", UIMS.getTeachingTermJSON().toString()).apply();

        AlertCenter.showAlert(MainActivity.this, "信息刷新成功", "以后大部分功能就不需要校园网啦!\n" +
                "祝使用愉快呀！");

        loadLocalInformationSuccess(null);
        getCourseSuccess(reloadCourse);
    }

    public static void saveData(){
        if(sp != null) {
            sp.edit().putString("ScoreJSON", UIMS.getScoreJSON().toString()).apply();
            sp.edit().putString("CourseJSON", UIMS.getCourseJSON().toString()).apply();
            sp.edit().putString("StudentJSON", UIMS.getStudentJSON().toString()).apply();
            sp.edit().putString("CourseTypeJSON", UIMS.getCourseTypeJSON().toString()).apply();
            sp.edit().putString("CourseSelectTypeJSON", UIMS.getCourseSelectTypeJSON().toString()).apply();
            sp.edit().putString("ScoreStatisticsJSON", UIMS.getScoreStatisticsJSON().toString()).apply();
            sp.edit().putString("CurrentUserInfoJSON", UIMS.getCurrentUserInfoJSON().toString()).apply();
            sp.edit().putString("TermJSON", UIMS.getTermJSON().toString()).apply();
            sp.edit().putString("TeachingTermJSON", UIMS.getTeachingTermJSON().toString()).apply();
        }
    }

    public static void saveVPNData(){
        if(sp != null) {
            sp.edit().putString("ScoreJSON", UIMS.getScoreJSON().toString()).apply();
            sp.edit().putString("StudentJSON", UIMS.getStudentJSON().toString()).apply();
            sp.edit().putString("CourseTypeJSON", UIMS.getCourseTypeJSON().toString()).apply();
            sp.edit().putString("CourseSelectTypeJSON", UIMS.getCourseSelectTypeJSON().toString()).apply();
            sp.edit().putString("ScoreStatisticsJSON", UIMS.getScoreStatisticsJSON().toString()).apply();
            sp.edit().putString("CurrentUserInfoJSON", UIMS.getCurrentUserInfoJSON().toString()).apply();
            sp.edit().putString("TeachingTermJSON", UIMS.getTeachingTermJSON().toString()).apply();
        }
    }

    public void loadCourseInformation() {
        loadTime();
        CourseJSONTransfer.transferCourseList(getApplicationContext(), UIMS.getCourseJSON(), true);
        getCourseSuccess(false);
    }

    private void loadLocalInformation(final boolean show) {

        if (!isLocalValueLoaded) {
            if(isLocalInformationAvailable()) {
                AlertCenter.showLoading(MainActivity.this, "正在加载本地数据...");

                UIMS.setCurrentUserInfoJSON(JSONObject.fromObject(sp.getString("CurrentUserInfoJSON", "")));
                UIMS.setScoreJSON(JSONObject.fromObject(sp.getString("ScoreJSON", "")));
                UIMS.setStudentJSON(JSONObject.fromObject(sp.getString("StudentJSON", "")));
                UIMS.setCourseTypeJSON(JSONObject.fromObject(sp.getString("CourseTypeJSON", "")));
                UIMS.setCourseSelectTypeJSON(JSONObject.fromObject(sp.getString("CourseSelectTypeJSON", "")));
                UIMS.setScoreStatisticsJSON(JSONObject.fromObject(sp.getString("ScoreStatisticsJSON", "")));
                UIMS.setTermJSON(JSONObject.fromObject(sp.getString("TermJSON", "")));
                UIMS.setTeachingTerm(JSONObject.fromObject(sp.getString("TeachingTermJSON", "")));
                UIMS.setCourseJSON(JSONObject.fromObject(sp.getString("CourseJSON", "")));

                loadCourseInformation();
                CJCX.loadCJCXJSON(getApplicationContext());
                CJCX.loadCJCXTermJSON(getApplicationContext());
                ScoreActivity.context = getApplicationContext();

                ScoreInf.loadScoreList();
            }
            else if(isLocalScoreInfAvailable()) {
                AlertCenter.showLoading(MainActivity.this, "正在加载本地数据...");

                UIMS.setCurrentUserInfoJSON(JSONObject.fromObject(sp.getString("CurrentUserInfoJSON", "")));
                UIMS.setScoreJSON(JSONObject.fromObject(sp.getString("ScoreJSON", "")));
                UIMS.setStudentJSON(JSONObject.fromObject(sp.getString("StudentJSON", "")));
                UIMS.setCourseTypeJSON(JSONObject.fromObject(sp.getString("CourseTypeJSON", "")));
                UIMS.setCourseSelectTypeJSON(JSONObject.fromObject(sp.getString("CourseSelectTypeJSON", "")));
                UIMS.setScoreStatisticsJSON(JSONObject.fromObject(sp.getString("ScoreStatisticsJSON", "")));
                UIMS.setTeachingTerm(JSONObject.fromObject(sp.getString("TeachingTermJSON", "")));

                CJCX.loadCJCXJSON(getApplicationContext());
                CJCX.loadCJCXTermJSON(getApplicationContext());
                ScoreActivity.context = getApplicationContext();

                ScoreInf.loadScoreList();
            }
        }

        final JSONObject teachingTermJSON = UIMS.getTeachingTermJSON();

        try {
            JSONObject value;
            try {
                value = teachingTermJSON.getJSONArray("value").getJSONObject(0);
            } catch (JSONException e) {
                e.printStackTrace();
                value = teachingTermJSON;
            }
            weeks = value.getInt("weeks");
            loadTime();
            getCourseSuccess(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (show) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
//                                long startTime = System.currentTimeMillis();
                        Bundle bundle = new Bundle();
//                                bundle.putLong("startTime", startTime);
                        Intent intent = new Intent(MainActivity.this, ScoreActivity.class);
//                                intent.putExtra("bundle", bundle);
                        startActivity(intent);
//                            overridePendingTransition(R.anim.up_in, R.anim.up_out);
                    } catch (Exception e) {
//                    AlertCenter.showWarningAlert(MainActivity.this, e.getMessage());
                        AlertCenter.showErrorAlertWithReportButton(MainActivity.this, "抱歉,出现错误!", e, user);
                        e.printStackTrace();
                    }
                }
            });
        }

        if(UIMS.getTermJSON() != null && UIMS.getTermId_termName() != null && UIMS.getTermId_termName().size() > 0) {
            loadLocalInformationSuccess(null);
        }
        else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Alerter.hide();
                }
            });
        }

    }

    private void loadLocalInformationSuccess(final String get_save_button_text){
        isLocalValueLoaded = true;
        if(isCourseNeedReload){
            showWarningAlertWithCancel_OKButton("需要刷新课程信息", "当前学期已经改变，请刷新本地课程信息。");
        }
        if(get_save_button_text != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Alerter.hide();
                    get_save_button.setText(get_save_button_text);
                }
            });
        }
        else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Alerter.hide();
                }
            });
        }
    }

    public void getCourseSuccess(boolean reloadCourse){
        try {
            courseList.setOnItemClickListener(null);
            if(reloadCourse) {
                final MyCourseDBHelper dbHelper = new MyCourseDBHelper(getApplicationContext(), "Course_DB", null, 1);
                final SQLiteDatabase db = dbHelper.getWritableDatabase();
                dbHelper.saveAll(db, SubjectRepertory.loadDefaultSubjects(getApplicationContext()), true);
                db.close();
//                todayCourseList = getCourseList();
            }
            todayCourseList = getCourseListFromDb();
            if(todayCourseList == null) {
                todayCourseList = getCourseList();
                Log.w("MainActivity", "Load course from DB ERROR!");
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("Login", "CourseListSize:\t" + todayCourseList.size());
                    courseList.setAdapter(new NoCourseBetterAdapter(context, todayCourseList, R.layout.list_item_today_course, new String[]{"index", "title", "context1"}, new int[]{R.id.get_none_score_course_title_index, R.id.get_none_score_course_title, R.id.get_none_score_course_context1}));
                    if(!listHaveHeadFoot) {
                        courseList.addHeaderView(new ViewStub(context));
                        courseList.addFooterView(new ViewStub(context));
                        listHaveHeadFoot = true;
                    }
                }
            });
        } catch (Exception e){
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    courseList.setAdapter(new NoCourseBetterAdapter(context, getCourseListNotice("暂无课程信息\n请点击\"更新信息\"登录并刷新本地数据."), R.layout.list_item_today_course, new String[]{"index", "title", "context1"}, new int[]{R.id.get_none_score_course_title_index, R.id.get_none_score_course_title, R.id.get_none_score_course_context1}));
                    courseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            AlertCenter.showAlert(MainActivity.this, "如在校内，请连接校园网后点击\"更新信息\"按钮登录教务系统，获取所需信息.\n\n" +
                                    "如不在校内，本应用暂时无法提供完整功能，仅可在校外使用\"校内通知\"、\"成绩查询\"部分功能，请谅解.\n" +
                                    "（在校内登录成功后，应用将自动保存数据，此时即可离线查看.）");
                        }
                    });
                    if(!listHaveHeadFoot) {
                        courseList.addHeaderView(new ViewStub(context));
                        courseList.addFooterView(new ViewStub(context));
                        listHaveHeadFoot = true;
                    }
                    Log.i("GetCourse", "设置提示信息");
                }
            });
        }
    }

    private void loadTime(){
        try {
            JSONObject teachingTermJSON = UIMS.getTeachingTermJSON();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            JSONObject value;
            try{
                value = teachingTermJSON.getJSONArray("value").getJSONObject(0);
            }catch (JSONException e){
                e.printStackTrace();
                value = teachingTermJSON;
            }
            final String now_time = df.format(new Date());
            final String termName = value.getString("termName");
            long startDate = df.parse(value.getString("startDate").split("T")[0]).getTime();
//            long vacationDate = df.parse(value.getString("vacationDate").split("T")[0]).getTime();
            long now = df.parse(now_time).getTime();

            Locale.setDefault(Locale.CHINA);
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            final String[] dayOfWeekName = new String[]{"","星期一","星期二","星期三","星期四","星期五","星期六","星期日"};
            day_of_week = cal.get(Calendar.DAY_OF_WEEK) - 1;
            if (day_of_week <= 0)
                day_of_week = 7;

//            Log.i("loadTime", "day_of_week:\t" + day_of_week);

            now_week = (now - startDate) / (1000 * 3600 * 24 * 7) + 1;
//            weeks = (int) (vacationDate - startDate / (1000 * 3600 * 24 * 7));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    timeInformation.setTextColor(Color.BLACK);
                    termInformation.setTextColor(Color.BLACK);
                    timeInformation.setText(now_time + " " + dayOfWeekName[day_of_week]);
                    if(now_week < 0 || now_week > MAX_VOCATION_WEEK_NUMBER + weeks){
                        termInformation.setText(termName + "\n 当前学期可能有误");
                        termInformation.setTextColor(Color.RED);
                    }
                    else {
                        if (now_week <= weeks)
                            termInformation.setText(termName + "\n 第 " + now_week + " 周(共 " + weeks + " 周)");
                        else termInformation.setText(termName + "\n本学期已结束,假期快乐!");
                    }
                }
            });

        } catch (Exception e){
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    timeInformation.setText("");
                }
            });
        }
    }

    private void loadInformation() {
        //开启线程发起网络请求
        MyThreadController.commit(new Runnable() {
            @Override
            public void run() {
                try {

                    AlertCenter.showLoading(MainActivity.this, "查询中...");
//                    showLoading("查询成绩统计...");
                    if (uims.getScoreStatistics()) {
//                        showResponse("查询成绩统计成功！");
//                        showLoading("查询成绩...");
                        if (uims.getRecentScore()) {
//                            showResponse("查询成绩成功！");
//                            showAlert("", "查询成绩成功！");
                            Log.i("Login", "getRecentScoreSucceed!");
//                            loadSuccess();
                        }
                        else{
                            showResponse("Login failed!");
                            reLogin();
                            return;
                        }
//                        showLoading("查询课程中...");
                        if(uims.getCourseSchedule()){
//                            showResponse("查询课程成功！");
//                            showAlert("", "查询课程成功！");
                            Log.i("Login", "getCourseScheduleSucceed!");
                            loadSuccess(true);
                        }
                        else{
                            showResponse("Login failed!");
                            reLogin();
                            return;
                        }
                    }
                    else{
                        showResponse("Login failed!");
                        reLogin();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    showResponse("Login failed!");
                    reLogin();
                }finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            load_internet_inf_tv.setText("更新信息");
                        }
                    });
                }
            }
        });
    }

    private boolean isLocalInformationAvailable(){
        return (
                sp.contains("CurrentUserInfoJSON") && sp.contains("ScoreJSON") &&
                sp.contains("CourseJSON") && sp.contains("ScoreStatisticsJSON") &&
                sp.contains("StudentJSON") && sp.contains("CourseTypeJSON") &&
                sp.contains("CourseSelectTypeJSON") && sp.contains("TermJSON")  &&
                sp.contains("TeachingTermJSON")
        );
    }

    private boolean isLocalScoreInfAvailable(){
        return (
                sp.contains("CurrentUserInfoJSON") && sp.contains("ScoreJSON") &&
                sp.contains("ScoreStatisticsJSON") && sp.contains("StudentJSON") &&
                sp.contains("CourseTypeJSON") && sp.contains("CourseSelectTypeJSON") &&
                sp.contains("TeachingTermJSON")
        );
    }

    private List<Map<String, Object>> getCourseListNotice(String str){
        List<Map<String,Object>> dataList = new ArrayList<>();
        Map<String,Object> map = new HashMap<>();
        map.put("index", "");
        map.put("title", str);
        map.put("context1", "");
        dataList.add(map);
        return dataList;
    }

    //TEST 从数据库中加载课程信息
    private List<Map<String, Object>> getCourseListFromDb() throws Exception{
        Log.i("GetCourse", "教学周:\t" + now_week);
        Log.i("GetCourse", "星期:\t" + day_of_week);

        if(day_of_week == 0 || now_week == 0) throw new IllegalAccessException("教学周或星期为0（未初始化）！");

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        final String now_time = df.format(new Date());

        if(CourseScheduleChange.containsDate(this, now_time)){
            return getCourseList(CourseScheduleChange.getDate(this, now_time));
        }
        else {
            if (!reLoadTodayCourse && todayCourseList != null) {
                Log.w("MainActivity", "NOT RELOAD COURSE!");
                return todayCourseList;
            }
            reLoadTodayCourse = false;

            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> map;

            List<MySubject> courseList = null;
            if(CourseJSONTransfer.transferCourseList(getApplicationContext())){
                courseList = CourseJSONTransfer.courseList;
                for(MySubject subject : courseList){
                    if(day_of_week != subject.getDay() || !subject.getWeekList().contains((int) now_week)) continue;
                    map = new HashMap<>();
                    map.put("index", subject.getStart() + " - " + (subject.getStart() + subject.getStep() - 1) + "节");
                    map.put("title", subject.getName());
                    map.put("context1", subject.getRoom());
                    dataList.add(map);
                }

                Log.i("GetCourse", "今日课程数量:\t" + dataList.size());

                if (dataList.size() == 0) {
                    map = new HashMap<>();
                    map.put("index", "");
                    map.put("title", "今天没课呀~");
                    map.put("context1", "");
                    dataList.add(map);
                } else {
                    Collections.sort(dataList, new Comparator<Map<String, Object>>() {
                        @Override
                        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                            return ((String) o1.get("index")).compareTo((String) o2.get("index"));
                        }
                    });
                }

                return dataList;
            }
            else {
                Log.e("MainActivity", "Load Course ERROR!");
//                AlertCenter.showErrorAlert(MainActivity.this, "课程加载错误！");
//                AlertCenter.showErrorAlertWithReportButton(MainActivity.this, "课程加载错误！", CourseJSONTransfer.getExceptionList(), UIMS.getUser());
                map = new HashMap<>();
                map.put("index", "");
                map.put("title", "暂无课程信息\n请点击\"更新信息\"登录并刷新本地数据.");
                map.put("context1", "");
                dataList.add(map);
                return dataList;
            }
        }
    }

    private List<Map<String, Object>> getCourseList() throws Exception{

        Log.i("GetCourse", "教学周:\t" + now_week);
        Log.i("GetCourse", "星期:\t" + day_of_week);

        if(day_of_week == 0 || now_week == 0) throw new IllegalAccessException("教学周或星期为0（未初始化）！");

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        final String now_time = df.format(new Date());

        if(CourseScheduleChange.containsDate(this, now_time)){
            return getCourseList(CourseScheduleChange.getDate(this, now_time));
        }
        else {
            if(!reLoadTodayCourse && todayCourseList != null) {
                Log.w("MainActivity", "NOT RELOAD COURSE!");
                return todayCourseList;
            }
            reLoadTodayCourse = false;

            ClassSetConvert classSetConvert = new ClassSetConvert();
            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> map;

            JSONObject allCourseJSON = UIMS.getCourseJSON();

            JSONArray json_courses = allCourseJSON.getJSONArray("value");

            Log.i("GetCourse", "课程数量:\t" + json_courses.size());

            JSONObject teachClassMaster;

            JSONArray lessonSchedules;
            JSONArray lessonTeachers;
            JSONObject teacher;
            String teacherName;
            JSONObject lessonSegment;
            String courName;

            JSONObject timeBlock;
            int classSet;
            int dayOfWeek;
            int beginWeek;
            int endWeek;
            int[] start_end;
            String weekOddEven = "";
            JSONObject classroom;
            String classroomName;

            try {

                for (int i = 0; i < json_courses.size(); i++) {

                    teachClassMaster = json_courses.getJSONObject(i).getJSONObject("teachClassMaster");

                    lessonSegment = teachClassMaster.getJSONObject("lessonSegment");
                    lessonSchedules = teachClassMaster.getJSONArray("lessonSchedules");
                    lessonTeachers = teachClassMaster.getJSONArray("lessonTeachers");

                    courName = lessonSegment.getString("fullName");

                    teacherName = lessonTeachers.getJSONObject(0).getJSONObject("teacher").getString("name");

                    for (int j = 0; j < lessonSchedules.size(); j++) {

                        map = new HashMap<>();

                        timeBlock = lessonSchedules.getJSONObject(j).getJSONObject("timeBlock");
                        classroom = lessonSchedules.getJSONObject(j).getJSONObject("classroom");
                        classroomName = classroom.getString("fullName");

                        classSet = timeBlock.getInt("classSet");
                        dayOfWeek = timeBlock.getInt("dayOfWeek");
                        beginWeek = timeBlock.getInt("beginWeek");
                        endWeek = timeBlock.getInt("endWeek");

                        if (!(beginWeek <= now_week && now_week <= endWeek)) continue;
                        if (dayOfWeek != day_of_week) continue;

                        try {
                            weekOddEven = timeBlock.getString("weekOddEven");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        switch (weekOddEven.toUpperCase()) {
                            case "": {
                                break;
                            }
                            case "E": {
                                //双周
                                if (now_week % 2 != 0) continue;
                                break;
                            }
                            case "O": {
                                //单周
                                if (now_week % 2 == 0) continue;
                                break;
                            }
                        }

                        start_end = classSetConvert.mathStartEnd(classSet);

                        map.put("index", start_end[0] + " - " + start_end[1] + "节");
                        map.put("title", courName);
                        map.put("context1", classroomName);
//                    map.put("type", selectType);

                        dataList.add(map);

                        weekOddEven = "";

                    }
                }

                Log.i("GetCourse", "今日课程数量:\t" + dataList.size());

                if (dataList.size() == 0) {
                    map = new HashMap<>();

                    map.put("index", "");
                    map.put("title", "今天没课呀~");
                    map.put("context1", "");
//                map.put("type", "");

                    dataList.add(map);
                } else {
                    Collections.sort(dataList, new Comparator<Map<String, Object>>() {
                        @Override
                        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                            return ((String) o1.get("index")).compareTo((String) o2.get("index"));
                        }
                    });
                }

                return dataList;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private List<Map<String, Object>> getCourseList(final String date) throws Exception{

        final String[] dayOfWeekName = new String[]{"","星期一","星期二","星期三","星期四","星期五","星期六","星期日"};
        if(date.equals("0000-00-00")) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            final String now_time = df.format(new Date());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    timeInformation.setTextColor(getResources().getColor(R.color.app_red));
                    timeInformation.setText(now_time.substring(2) + " " + dayOfWeekName[day_of_week] + " (第 " + now_week + " 周)\n" +
                            "今天放假啦");
                }
            });

            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            map.put("index", "");
            map.put("title", "今天放假啦o(*￣▽￣*)o");
            map.put("context1", "");
            dataList.add(map);
            return dataList;
        }

        //递归调用【！会引起逻辑错误】
//        if(CourseScheduleChange.containsDate(this, date)){
//            return getCourseList(CourseScheduleChange.getDate(this, date));
//        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        long time = df.parse(date).getTime();

        Locale.setDefault(Locale.CHINA);
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        JSONObject teachingTermJSON = UIMS.getTeachingTermJSON();
        JSONObject value;
        try {
            value = teachingTermJSON.getJSONArray("value").getJSONObject(0);
        }catch (Exception e){
            e.printStackTrace();
            Log.w("MainActivity", "Using teaching term information from com.lu.mydemo.CJCX.");
            value = teachingTermJSON;
        }
        long startTime = df.parse(value.getString("startDate").split("T")[0]).getTime();
        final long temp_week = (time - startTime) / (1000 * 3600 * 24 * 7) + 1;

        cal = Calendar.getInstance();
        cal.setTime(df.parse(date));
        int temp_day_of_week = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (temp_day_of_week <= 0)
            temp_day_of_week = 7;
        final int temp_day_of_week_1 = temp_day_of_week;

        final String now_time = df.format(new Date());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timeInformation.setTextColor(getResources().getColor(R.color.app_red));
                timeInformation.setText(now_time.substring(2) + " " + dayOfWeekName[day_of_week] + " (第 " + now_week + " 周)\n" +
                        "课程临时调整：\n" +
                        date.substring(2) + " " + dayOfWeekName[temp_day_of_week_1] + " (第 " + temp_week + " 周)");
            }
        });

        return getCourseList(temp_day_of_week_1, (int) temp_week);

    }

    private List<Map<String, Object>> getCourseList(int day_of_week, int now_week) throws Exception{

        Log.i("GetCourse", "教学周:\t" + now_week);
        Log.i("GetCourse", "星期:\t" + day_of_week);

        if(day_of_week == 0 || now_week == 0) throw new IllegalAccessException("教学周或星期为0！");

        if(!reLoadTodayCourse && todayCourseList != null) {
            Log.w("MainActivity", "NOT RELOAD COURSE!");
            return todayCourseList;
        }
        reLoadTodayCourse = false;

        ClassSetConvert classSetConvert = new ClassSetConvert();
        List<Map<String,Object>> dataList = new ArrayList<>();
        Map<String,Object> map;

        JSONObject allCourseJSON = UIMS.getCourseJSON();

        JSONArray json_courses = allCourseJSON.getJSONArray("value");

        Log.i("GetCourse", "课程数量:\t" + json_courses.size());

        JSONObject teachClassMaster;

        JSONArray lessonSchedules;
        JSONArray lessonTeachers;
        JSONObject teacher;
        String teacherName;
        JSONObject lessonSegment;
        String courName;

        JSONObject timeBlock;
        int classSet;
        int dayOfWeek;
        int beginWeek;
        int endWeek;
        int[] start_end;
        String weekOddEven = "";
        JSONObject classroom;
        String classroomName;

        try {

            for(int i=0; i<json_courses.size(); i++){

                teachClassMaster = json_courses.getJSONObject(i).getJSONObject("teachClassMaster");

                lessonSegment = teachClassMaster.getJSONObject("lessonSegment");
                lessonSchedules = teachClassMaster.getJSONArray("lessonSchedules");
                lessonTeachers = teachClassMaster.getJSONArray("lessonTeachers");

                courName = lessonSegment.getString("fullName");

                teacherName = lessonTeachers.getJSONObject(0).getJSONObject("teacher").getString("name");

                for (int j = 0; j < lessonSchedules.size(); j++) {

                    map = new HashMap<>();

                    timeBlock = lessonSchedules.getJSONObject(j).getJSONObject("timeBlock");
                    classroom = lessonSchedules.getJSONObject(j).getJSONObject("classroom");
                    classroomName = classroom.getString("fullName");

                    classSet = timeBlock.getInt("classSet");
                    dayOfWeek = timeBlock.getInt("dayOfWeek");
                    beginWeek = timeBlock.getInt("beginWeek");
                    endWeek = timeBlock.getInt("endWeek");

                    if(!(beginWeek <= now_week && now_week <= endWeek)) continue;
                    if(dayOfWeek != day_of_week) continue;

                    try {
                        weekOddEven = timeBlock.getString("weekOddEven");
                    } catch (Exception e) {
//                    e.printStackTrace();
                    }

                    switch (weekOddEven.toUpperCase()){
                        case "":{
                            break;
                        }
                        case "E":{
                            //双周
                            if(now_week % 2 != 0) continue;
                            break;
                        }
                        case "O":{
                            //单周
                            if(now_week % 2 == 0) continue;
                            break;
                        }
                    }

                    start_end = classSetConvert.mathStartEnd(classSet);

                    map.put("index", start_end[0] + " - " + start_end[1] + "节");
                    map.put("title", courName);
                    map.put("context1", classroomName);
//                    map.put("type", selectType);

                    dataList.add(map);

                    weekOddEven = "";

                }
            }

            Log.i("GetCourse", "今日课程数量:\t" + dataList.size());

            if(dataList.size() == 0){
                map = new HashMap<>();

                map.put("index", "");
                map.put("title", "今天没课呀o(*￣▽￣*)o");
                map.put("context1", "");
//                map.put("type", "");

                dataList.add(map);
            }
            else{
                Collections.sort(dataList, new Comparator<Map<String, Object>>() {
                    @Override
                    public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                        return ((String)o1.get("index")).compareTo((String)o2.get("index"));
                    }
                });
            }

            return dataList;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void getInternetInformation(){
        final GetInternetInformation getInf = new GetInternetInformation();
        try {
            MyThreadController.commit(new Runnable() {
                @Override
                public void run() {
                    final JSONObject responseJSON = getInf.getRemoteConfig();
                    if(responseJSON != null){
                        try{
                            final JSONObject configJSON = responseJSON.getJSONObject("data");
                            String webLink = configJSON.getString("webLink");
                            setWebLink(webLink);
                            JSONArray messageArray = configJSON.getJSONArray("message");
                            List<Map<String, Object>> messageList = new ArrayList<>();
                            for(final Object message : messageArray){
                                final Map<String, Object> messageItem = new HashMap<>();
                                JSONObject messageItemJSON = (JSONObject) message;
                                messageItemJSON.forEach(new BiConsumer() {
                                    @Override
                                    public void accept(Object o, Object o2) {
                                        messageItem.put((String) o, o2);
                                    }
                                });
                                messageList.add(messageItem);
                            }
                            int titleLine = 2;
                            int detailLine = 4;
                            int linkLine = 6;
                            try{
                                titleLine = Integer.parseInt(configJSON.get("titleLine").toString());
                                detailLine = Integer.parseInt(configJSON.get("detailLine").toString());
                                linkLine = Integer.parseInt(configJSON.get("linkLine").toString());
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                            initMessageList(messageList, titleLine, detailLine, linkLine);
                        }
                        catch (Exception e){
                            Log.e("MainActivity", "ResponseJSON:\t" + responseJSON);
                            e.printStackTrace();
                        }
                    }
                    final JSONObject object = getInf.getVersionInformation();
                    if (object != null) {
                        try {
                            int internetVersion = object.getInt("VersionCode");
                            Log.i("Version", "" + internetVersion);
                            if (!Version.isIsBeta() && internetVersion <= Version.getVersionCode())
                                return;//不是测试版 不提示同版本更新
                            else if (internetVersion < Version.getVersionCode())
                                return;//测试版 提示同版本正式版更新
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    InternetInformationPopupWindow informationPopWindow = new InternetInformationPopupWindow(MainActivity.this, object, findViewById(R.id.activity_login).getHeight(), findViewById(R.id.activity_login).getWidth());
                                    informationPopWindow.showAtLocation(MainActivity.this.findViewById(R.id.activity_login), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isInternetInformationShowed = true;
        }
    }

    private void initMessageList(final List<Map<String, Object>> list, final int titleMaxLine, final int detailMaxLine, final int linkMaxLine){
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMessageListLv.setVisibility(View.VISIBLE);
                mMessageListLv.setAdapter(new MessageListAdapter(context, list, R.layout.list_item_message,
                        new String[]{"title", "detail", "linkText"},
                        new int[]{R.id.list_item_message_detail_title, R.id.list_item_message_detail_context, R.id.list_item_message_link},
                        titleMaxLine, detailMaxLine, linkMaxLine));
            }
        });
    }

    public void changeTheme(){
        super.changeTheme();
        activity_login.setBackground(ColorManager.getMainBackground_full());
        getNewsButton.setBackground(ColorManager.getLocalInformationButtonBackground());
        get_save_button.setBackground(ColorManager.getLocalInformationButtonBackground());
        getNoneScoreCourseButton.setBackground(ColorManager.getLocalInformationButtonBackground());
        webPagesButton.setBackground(ColorManager.getLocalInformationButtonBackground());
    }

    public static void loadColorConfig(){
        ColorManager.loadColorConfig(context.getApplicationContext());
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                ColorManager.loacColorCofig(context.getApplicationContext());
//            }
//        }).start();
    }

    private void changeStatusBarTextColor(boolean isBlack) {
        if (isBlack) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//设置状态栏黑色字体
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);//恢复状态栏白色字体
        }
    }

    public static void setReLoadTodayCourse(boolean reLoadTodayCourse) {
        MainActivity.reLoadTodayCourse = reLoadTodayCourse;
    }

    public static void setIsCourseNeedReload(boolean isCourseNeedReload) {
        MainActivity.isCourseNeedReload = isCourseNeedReload;
        sp.edit().putBoolean("isCourseNeedReload", isCourseNeedReload).apply();
    }

    public static void saveTeachingTerm(){
        sp.edit().putString("TeachingTermJSON", UIMS.getTeachingTermJSON().toString()).apply();
        MainActivity.setIsCourseNeedReload(true);
    }

    public static void saveCourseJSON(){
        sp.edit().putString("CourseJSON", UIMS.getCourseJSON().toString()).apply();
    }

    public static void saveScoreJSON(){
        sp.edit().putString("ScoreJSON", UIMS.getScoreJSON().toString()).apply();
        sp.edit().putString("ScoreStatisticsJSON", UIMS.getScoreStatisticsJSON().toString()).apply();
    }

    public static boolean isAcceptTestFun() {
        return acceptTestFun;
    }

    public static void setAcceptTestFun(boolean acceptTestFun) {
        MainActivity.acceptTestFun = acceptTestFun;
        sp.edit().putBoolean("acceptTestFun", MainActivity.acceptTestFun).apply();
    }

    public void shortCutTest(){
        try{
            if(Build.VERSION.SDK_INT > 26) {
                ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

                ShortcutInfo shortcut = new ShortcutInfo.Builder(MainActivity.this, "id1")
                        .setShortLabel("Test fun.")
                        .setLongLabel("Test function activity.")
//                                        .setIcon(Icon.createWithResource(, R.drawable.icon_website))
//                                        .setIntent(intent)
                        .setIntents(
                                // this dynamic shortcut set up a back stack using Intents, when pressing back, will go to MainActivity
                                // the last Intent is what the shortcut really opened
                                new Intent[]{
                                        new Intent(Intent.ACTION_MAIN, Uri.EMPTY, MainActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
                                        new Intent(WebPagesActivity.ACCESSIBILITY_SERVICE)
                                        // intent's action must be set
                                }
                        )
//                        .setActivity(new ComponentName(getPackageName(), WebPagesActivity.class.getName()))
                        .build();

                shortcutManager.removeAllDynamicShortcuts();
                shortcutManager.setDynamicShortcuts(Arrays.asList(shortcut));
            }
            else {
                AlertCenter.showErrorAlert(MainActivity.this, "Current API:\t" + Build.VERSION.SDK_INT + "\n" +
                        "Needed API:\t26 or higher.");
            }
        } catch (Exception e){
            e.printStackTrace();
            AlertCenter.showErrorAlert(MainActivity.this, e.getMessage());
        }
    }

    private void myTestFunction(){
        MyThreadController.commit(new Runnable() {
            @Override
            public void run() {
                try{
                    user = "user";
                    pass = "pass";

                    uims = new UIMS(user, pass);
                    AlertCenter.showLoading(MainActivity.this, "正在连接到UIMS教务系统...");
                    if (uims.connectToUIMS()) {
                        AlertCenter.showLoading(MainActivity.this, "正在登录...");
                        if (uims.login()) {
                            AlertCenter.showLoading(MainActivity.this, "正在加载用户信息...");
                            if (uims.getCurrentUserInfo()) {
                                AlertCenter.showAlert(MainActivity.this, "", "欢迎您, " + uims.getNickName() + " ." + "\n" +
                                        "您是UIMS系统第 " + uims.getLoginCounter() + " 位访问者.");
//                                if(uims.getUserInformation()) loginSuccess();
//                                else{
//                                    showResponse("Login failed!");
//                                    return;
//                                }
                                loginSuccess();
                                if(!uims.getTermArray()){
                                    showResponse("Login failed!");
                                    return;
                                }
                                if (uims.getScoreStatistics()) {
//                                    showResponse("查询成绩统计成功！");
                                    AlertCenter.showLoading(MainActivity.this, "查询成绩中，请稍侯...");
                                    if (uims.getRecentScore()) {
//                                        showResponse("查询成绩成功！");
                                        Log.i("Login", "getRecentScoreSucceed!");
                                    }
                                    else{
                                        showResponse("Login failed!");
                                        reLogin();
                                        return;
                                    }
                                    if(uims.getTermArray()){
//                                        showResponse("查询学期列表成功！");
//                                        loadSuccess();
                                        Log.i("Login", "getTermArraySucceed!");
                                    }
                                    else{
                                        showResponse("Login failed!");
                                        reLogin();
                                        return;
                                    }
                                    if(uims.getCourseHistory("135")){
                                        showResponse("查询历史选课成功！(term: 135)");
                                        Log.i("Login", "getCourseHistorySucceed!");
                                    }
                                    else{
                                        showResponse("Login failed!");
                                        reLogin();
                                        return;
                                    }
                                    if(uims.getCourseHistory("134")){
                                        showResponse("查询历史选课成功！(term: 134)");
                                        Log.i("Login", "getCourseHistorySucceed!");
                                    }
                                    else{
                                        showResponse("Login failed!");
                                        reLogin();
                                        return;
                                    }
                                    if(uims.getCourseHistory("133")){
                                        showResponse("查询历史选课成功！(term: 133)");
                                        Log.i("Login", "getCourseHistorySucceed!");
                                    }
                                    else{
                                        showResponse("Login failed!");
                                        reLogin();
                                        return;
                                    }
                                }
                                else{
                                    showResponse("Login failed!");
                                    reLogin();
                                }
                            }
                            else{
//                                showResponse("Login failed!");
                                AlertCenter.showWarningAlert(MainActivity.this, "", "登录失败，请检查用户名和密码是否正确.\n\n" +
                                        "教务账号：\t您的教学号\n" +
                                        "教务密码：\t默认密码为身份证号后六位");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
//                                        button_login.setText("重新登录");
//                                        button_login.setEnabled(true);
//                                        button_login.setBackground(getResources().getDrawable(R.drawable.button_internet_background));
                                        return;
                                    }
                                });
                            }
                        }
                        else{
//                            showResponse("Login failed!");
                            AlertCenter.showWarningAlert(MainActivity.this, "", "登录失败，请检查是否连接校园网！");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                    button_login.setText("重新登录");
//                                    button_login.setEnabled(true);
//                                    button_login.setBackground(getResources().getDrawable(R.drawable.button_internet_background));
                                    return;
                                }
                            });
                        }
                    }
                    else{
                        showResponse("Login failed!");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void dismissPopWindow(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                popWindow.dismiss();
            }
        });
    }

    public void dismissCourseSchedulePopWindow(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                courseSchedulePopupWindow.dismiss();
            }
        });
    }

    public void showResponse(final String string) {
        AlertCenter.hideAlert(this);
        if (string.toLowerCase().contains("failed")) {
//            AlertCenter.showWarningAlert(this, "", "获取数据失败，请稍后重试.");
            AlertCenter.showErrorAlertWithReportButton(MainActivity.this, "抱歉,数据出错!", UIMS.getExceptions(), UIMS.getUser());
//                    button_login.setText("重新登录");
//                    button_login.setEnabled(true);
//                    button_login.setBackground(getResources().getDrawable(R.drawable.button_internet_background));
            return;
        }
        Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show();
//        showAlert(string);
    }

    public void showWarningAlertWithCancel_OKButton(final String title, final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Alerter.create(MainActivity.this)
                        .setTitle(title)
                        .setText(message)
                        .addButton("取消", R.style.AlertButton, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Alerter.hide();
                            }
                        })
                        .addButton("更新", R.style.AlertButton, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LoginGetCourseSchedulePopupWindow window = new LoginGetCourseSchedulePopupWindow(MainActivity.this, UIMS.getTermName(), findViewById(R.id.activity_login).getHeight(), findViewById(R.id.activity_login).getWidth());
                                window.setFocusable(true);
                                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                                window.showAtLocation(MainActivity.this.findViewById(R.id.activity_login), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
                                courseSchedulePopupWindow = window;
                                Alerter.hide();
                            }
                        })
                        .setBackgroundColorInt(getResources().getColor(R.color.color_alerter_warning_background))
                        .enableSwipeToDismiss()
                        .setDuration(Integer.MAX_VALUE)
                        .show();
            }
        });
    }

    //TeachTimeTools
    public void initTeachTimeToolInf(){
        TeachTimeTools.now_week = now_week;
        TeachTimeTools.weeks = weeks;
        TeachTimeTools.day_of_week = day_of_week;
    }

    //摇一摇
    public void registerShakeListener(SensorManagerHelper.OnShakeListener onShakeListener){
        SensorManagerHelper sensorManagerHelper = new SensorManagerHelper(this);
        sensorManagerHelper.setOnShakeListener(onShakeListener);
    }

    class NoCourseBetterAdapter extends SimpleAdapter {
        List<? extends Map<String, ?>> mdata;

        public NoCourseBetterAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            this.mdata = data;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LinearLayout.inflate(getBaseContext(), R.layout.list_item_today_course, null);
            }
            TextView textView = convertView.findViewById(R.id.get_none_score_course_title_index);
            String ss=(String)mdata.get(position).get("index");

            if(ss == null || !(ss.length() > 0)) {
                textView.getLayoutParams().width = 0;
                textView.setWidth(0);
            }

            return super.getView(position, convertView, parent);
        }
    }

    class MessageListAdapter extends SimpleAdapter {
        List<? extends Map<String, ?>> mData;

        int mTitleMaxLine;
        int mDetailMaxLine;
        int mLinkMaxLine;

        public MessageListAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to,
                                  int titleMaxLine, int detailMaxLine, int linkMaxLine) {
            super(context, data, resource, from, to);
            this.mData = data;

            mTitleMaxLine = titleMaxLine;
            mDetailMaxLine = detailMaxLine;
            mLinkMaxLine = linkMaxLine;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LinearLayout.inflate(getBaseContext(), R.layout.list_item_message, null);
            }

            TextView titleTv = convertView.findViewById(R.id.list_item_message_detail_title);
            TextView detailTv = convertView.findViewById(R.id.list_item_message_detail_context);
            TextView linkTv = convertView.findViewById(R.id.list_item_message_link);

            titleTv.setMaxLines(mTitleMaxLine);
            detailTv.setMaxLines(mDetailMaxLine);
            linkTv.setMaxLines(mLinkMaxLine);

            final String titleStr = (String) mData.get(position).get("title");
            final String detailStr = (String) mData.get(position).get("detail");
            final String linkTextStr = (String) mData.get(position).get("linkText");
            final String linkStr = (String) mData.get(position).get("link");
            boolean openInApp = false;
            try{
                openInApp = (Boolean) Objects.requireNonNull(mData.get(position).get("openInApp"));
            }
            catch (Exception e){
                e.printStackTrace();
            }

            if(TextUtils.isEmpty(titleStr)){
                titleTv.setHeight(0);
            }
            else {
                titleTv.setText(titleStr);
            }
            if(TextUtils.isEmpty(detailStr)){
                detailTv.setHeight(0);
            }
            else {
                detailTv.setText(detailStr);
            }
            if(TextUtils.isEmpty(linkTextStr) || TextUtils.isEmpty(linkStr)){
                linkTv.setWidth(0);
            }
            else {
                if(openInApp){
                    linkTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("link", linkStr);
                            intent.putExtra("bundle", bundle);
                            startActivity(intent);
                        }
                    });
                }
                else {
                    linkTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                Uri uri = Uri.parse(linkStr);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                            catch (Exception e){
                                e.printStackTrace();
                                AlertCenter.showErrorAlertWithReportButton(MainActivity.this, e.getMessage(), e, UIMS.getUser());
                            }
                        }
                    });
                }
            }
            return super.getView(position, convertView, parent);
        }
    }

}
