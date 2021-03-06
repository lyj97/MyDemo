package com.lu.mydemo.Activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lu.mydemo.Config.OptionManager;
import com.lu.mydemo.Notification.AlertCenter;

import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.lu.mydemo.CJCX.CJCX;
import com.lu.mydemo.Config.ColorManager;
import com.lu.mydemo.R;
import com.lu.mydemo.ToolFor2045_Site.InformationUploader;
import com.lu.mydemo.UIMS.UIMS;
import com.lu.mydemo.Utils.Score.ScoreConfig;
import com.lu.mydemo.Utils.Score.ScoreInf;
import com.lu.mydemo.Utils.Thread.MyThreadController;
import com.lu.mydemo.View.MyView.MyToolBar;
import com.lu.mydemo.View.PopWindow.SetEmailPopupWindow;

public class SettingActivity extends BaseActivity {
    MyToolBar toolBar;

    private Spinner spinner;
    private ArrayList<String> termList;
    private boolean hasTerm = false;

    private boolean scoreConfigChanged = false;

    private CheckBox check_box_bixiu;
    private CheckBox check_box_xuanxiu;
    private CheckBox check_box_xianxuan;
    private CheckBox check_box_PE;
    private CheckBox check_box_xiaoxuanxiu;
    private CheckBox check_box_chongxiu;
    private TextView text_bixiu;
    private TextView text_xuanxiu;
    private TextView text_xianxuan;
    private TextView text_PE;
    private TextView text_xiaoxuanxiu;
    private TextView text_chongxiu;

    private CheckBox check_box_cjcx_enable;
    private TextView text_cjcx_enable;

    private CheckBox check_box_show_not_cur_week_course;
    private TextView text_show_not_cur_week_course;

    private CheckBox check_box_test;
    private TextView text_test;

    private CheckBox check_box_transparent_navigation;
    private TextView text_transparent_navigation;

    private LinearLayout layout_to_theme;

    private TextView email_text_tv;

    private String theme = ColorManager.getThemeName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        toolBar = new MyToolBar(this);

        spinner = findViewById(R.id.activity_setting_term_spinner);

        check_box_bixiu = findViewById(R.id.activity_setting_score_bixiu_checkbox);
        check_box_xuanxiu = findViewById(R.id.activity_setting_score_xuanxiu_checkbox);
        check_box_xianxuan = findViewById(R.id.activity_setting_score_xianxuan_checkbox);
        check_box_PE = findViewById(R.id.activity_setting_score_PE_checkbox);
        check_box_xiaoxuanxiu = findViewById(R.id.activity_setting_score_xiaoxuanxiu_checkbox);
        check_box_chongxiu = findViewById(R.id.activity_setting_score_chongxiu_checkbox);

        text_bixiu = findViewById(R.id.activity_setting_score_bixiu_text);
        text_xuanxiu = findViewById(R.id.activity_setting_score_xuanxiu_text);
        text_xianxuan = findViewById(R.id.activity_setting_score_xianxuan_text);
        text_PE = findViewById(R.id.activity_setting_score_PE_text);
        text_xiaoxuanxiu = findViewById(R.id.activity_setting_score_xiaoxuanxiu_text);
        text_chongxiu = findViewById(R.id.activity_setting_score_chongxiu_text);

        check_box_cjcx_enable = findViewById(R.id.activity_setting_score_cjcx_checkbox);
        text_cjcx_enable = findViewById(R.id.activity_setting_score_cjcx_text);

        check_box_show_not_cur_week_course = findViewById(R.id.activity_setting_course_show_no_cur_week_checkbox);
        text_show_not_cur_week_course = findViewById(R.id.activity_setting_course_show_no_cur_week_text);

        check_box_test = findViewById(R.id.activity_setting_test_enable_checkbox);
        text_test = findViewById(R.id.activity_setting_test_enable_text);

        check_box_transparent_navigation = findViewById(R.id.activity_setting_transparent_navigation_enable_checkbox);
        text_transparent_navigation = findViewById(R.id.activity_setting_transparent_navigation_enable_text);

        layout_to_theme = findViewById(R.id.activity_setting_to_theme_layout);

        email_text_tv = findViewById(R.id.activity_setting_email);

        changeTheme();

        setSpinnerItems();

        loadScoreSelect();

        loadTestSelect();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (hasTerm) {
                    String term = termList.get(position);
                    Log.i("Term", term);
                    if (term != null) {
                        if (term.equals(UIMS.getTermName())) {
                            Log.i("SetTerm", "Ignored! Term not change.");
                            MainActivity.setIsCourseNeedReload(false);
                            return;
                        }
                        JSONObject termJSON = UIMS.getTermJSON(term);
                        if (termJSON == null) {
//                            AlertCenter.showWarningAlert(SettingActivity.this, "ERROR", "TermJSON is null.");
                            List<Exception> exceptions = UIMS.getExceptions();
                            exceptions.add(new IllegalStateException("TermJSON is null."));
                            AlertCenter.showErrorAlertWithReportButton(SettingActivity.this, "抱歉,数据出错!", exceptions, UIMS.getUser());
                            return;
                        }
                        Log.i("TermJSON", termJSON.toString());
                        UIMS.setTeachingTerm(termJSON);
                        MainActivity.saveTeachingTerm();
                        Toast.makeText(SettingActivity.this, "当前学期已设为：\t" + UIMS.getTermName(), Toast.LENGTH_SHORT).show();
                    } else {
//                    Toast.makeText(CourseScheduleChangeActivity.this, "TermJSON is NULL!", Toast.LENGTH_SHORT).show();
                        Log.e("TermJSON", "TermJSON is NULL!");
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ScoreActivity.setReLoadSocreList(true);
                switch (buttonView.getId()) {
                    case R.id.activity_setting_score_bixiu_checkbox: {
                        OptionManager.setBixiu_select(getApplicationContext(), check_box_bixiu.isChecked());
                        scoreConfigChanged = true;
                        break;
                    }
                    case R.id.activity_setting_score_xuanxiu_checkbox: {
                        OptionManager.setXuanxiu_select(getApplicationContext(), check_box_xuanxiu.isChecked());
                        scoreConfigChanged = true;
                        break;
                    }
                    case R.id.activity_setting_score_xianxuan_checkbox: {
                        OptionManager.setXianxuan_select(getApplicationContext(), check_box_xianxuan.isChecked());
                        scoreConfigChanged = true;
                        break;
                    }
                    case R.id.activity_setting_score_xiaoxuanxiu_checkbox: {
                        OptionManager.setXiaoxuanxiu_select(getApplicationContext(), check_box_xiaoxuanxiu.isChecked());
                        scoreConfigChanged = true;
                        break;
                    }
                    case R.id.activity_setting_score_PE_checkbox: {
                        OptionManager.setPE_select(getApplicationContext(), check_box_PE.isChecked());
                        scoreConfigChanged = true;
                        break;
                    }
                    case R.id.activity_setting_score_chongxiu_checkbox: {
                        OptionManager.setChongxiu_select(getApplicationContext(), check_box_chongxiu.isChecked());
                        scoreConfigChanged = true;
                        break;
                    }
                    case R.id.activity_setting_score_cjcx_checkbox: {
                        CJCX.setCJCXEnable(getApplicationContext(), check_box_cjcx_enable.isChecked());
                        break;
                    }
                    case R.id.activity_setting_test_enable_checkbox: {
                        MainActivity.setAcceptTestFun(check_box_test.isChecked());
                        break;
                    }
                    case R.id.activity_setting_course_show_no_cur_week_checkbox: {
                        OptionManager.setShow_not_current_week_course(getApplicationContext(), check_box_show_not_cur_week_course.isChecked());
                        break;
                    }
                    case R.id.activity_setting_transparent_navigation_enable_checkbox: {
                        OptionManager.setTransparent_navigation_bar(getApplicationContext(), check_box_transparent_navigation.isChecked());
                        break;
                    }
                }
            }
        };

        check_box_bixiu.setOnCheckedChangeListener(onCheckedChangeListener);
        check_box_xuanxiu.setOnCheckedChangeListener(onCheckedChangeListener);
        check_box_xianxuan.setOnCheckedChangeListener(onCheckedChangeListener);
        check_box_xiaoxuanxiu.setOnCheckedChangeListener(onCheckedChangeListener);
        check_box_PE.setOnCheckedChangeListener(onCheckedChangeListener);
        check_box_chongxiu.setOnCheckedChangeListener(onCheckedChangeListener);
        check_box_cjcx_enable.setOnCheckedChangeListener(onCheckedChangeListener);
        check_box_test.setOnCheckedChangeListener(onCheckedChangeListener);
        check_box_show_not_cur_week_course.setOnCheckedChangeListener(onCheckedChangeListener);
        check_box_transparent_navigation.setOnCheckedChangeListener(onCheckedChangeListener);

        View.OnClickListener text_onClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.activity_setting_score_bixiu_text: {
                        check_box_bixiu.setChecked(!check_box_bixiu.isChecked());
                        break;
                    }
                    case R.id.activity_setting_score_xuanxiu_text: {
                        check_box_xuanxiu.setChecked(!check_box_xuanxiu.isChecked());
                        break;
                    }
                    case R.id.activity_setting_score_xianxuan_text: {
                        check_box_xianxuan.setChecked(!check_box_xianxuan.isChecked());
                        break;
                    }
                    case R.id.activity_setting_score_PE_text: {
                        check_box_PE.setChecked(!check_box_PE.isChecked());
                        break;
                    }
                    case R.id.activity_setting_score_xiaoxuanxiu_text: {
                        check_box_xiaoxuanxiu.setChecked(!check_box_xiaoxuanxiu.isChecked());
                        break;
                    }
                    case R.id.activity_setting_score_chongxiu_text: {
                        check_box_chongxiu.setChecked(!check_box_chongxiu.isChecked());
                        break;
                    }
                    case R.id.activity_setting_score_cjcx_text: {
                        check_box_cjcx_enable.setChecked(!check_box_cjcx_enable.isChecked());
                        break;
                    }
                    case R.id.activity_setting_test_enable_text: {
                        check_box_test.setChecked(!check_box_test.isChecked());
                        break;
                    }
                    case R.id.activity_setting_course_show_no_cur_week_text: {
                        check_box_show_not_cur_week_course.setChecked(!check_box_show_not_cur_week_course.isChecked());
                        break;
                    }
                    case R.id.activity_setting_transparent_navigation_enable_text: {
                        check_box_transparent_navigation.setChecked(!check_box_transparent_navigation.isChecked());
                        break;
                    }
                }
            }
        };

        text_bixiu.setOnClickListener(text_onClick);
        text_xuanxiu.setOnClickListener(text_onClick);
        text_xianxuan.setOnClickListener(text_onClick);
        text_PE.setOnClickListener(text_onClick);
        text_xiaoxuanxiu.setOnClickListener(text_onClick);
        text_chongxiu.setOnClickListener(text_onClick);
        text_cjcx_enable.setOnClickListener(text_onClick);
        text_test.setOnClickListener(text_onClick);
        text_show_not_cur_week_course.setOnClickListener(text_onClick);
        check_box_transparent_navigation.setOnClickListener(text_onClick);

        layout_to_theme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this, ColorConfigActivity.class));
            }
        });

        View.OnClickListener emailClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetEmailPopupWindow window = new SetEmailPopupWindow(SettingActivity.this, null, findViewById(R.id.activity_setting).getHeight(), findViewById(R.id.activity_setting).getWidth());
                window.setFocusable(true);
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                window.showAtLocation(SettingActivity.this.findViewById(R.id.activity_setting), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
            }
        };
        findViewById(R.id.activity_setting_email_layout).setOnClickListener(emailClickListener);
        email_text_tv.setOnClickListener(emailClickListener);

        toolBar.setSubTitle("设置");
        toolBar.setRightIcon(getDrawable(R.drawable.ic_info_outline_white_24dp));
        toolBar.setRightOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this, AboutActivity.class));
            }
        });

        updateData();

    }

    @Override
    protected void onStart() {
        super.onStart();
        changeTheme();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(hasFocus) updateData();
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(scoreConfigChanged){
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    ScoreInf.setScoreListLoaded(false);
                    ScoreInf.loadScoreList();
                }
            };
            MyThreadController.commit(runnable);
        }
    }

    private void updateData(){
        email_text_tv.setText(InformationUploader.USER_MAIL);
    }

    private void setSpinnerItems(){
        if(MainActivity.isLocalValueLoaded){
            termList = CourseScheduleChangeActivity.getTermArray();
            if(!(termList.size() > 0)){
                termList = new ArrayList<>();
                termList.add("暂无学期数据\n请在首页点击“更新信息”按钮");
                spinner.setAdapter(new ArrayAdapter<>(this, R.layout.list_item_spinner, R.id.select_text_item, termList));
                hasTerm = false;
            }
            else{
                spinner.setAdapter(new ArrayAdapter<>(this, R.layout.list_item_spinner, R.id.select_text_item, termList));
                spinner.setSelection(termList.indexOf(UIMS.getTermName()));
                hasTerm = true;
            }
        }
        else{
            hasTerm = false;
            termList = new ArrayList<>();
            termList.add("暂无学期数据\n请在首页点击“更新信息”按钮");
            spinner.setAdapter(new ArrayAdapter<>(this, R.layout.list_item_spinner, R.id.select_text_item, termList));
//            finish();
        }
    }

    private void loadScoreSelect() {
        check_box_bixiu.setChecked(OptionManager.isBixiu_select());
        check_box_xuanxiu.setChecked(OptionManager.isXuanxiu_select());
        check_box_xianxuan.setChecked(OptionManager.isXianxuan_select());
        check_box_xiaoxuanxiu.setChecked(OptionManager.isXiaoxuanxiu_select());
        check_box_PE.setChecked(OptionManager.isPE_select());
        check_box_chongxiu.setChecked(OptionManager.isChongxiu_select());
        check_box_cjcx_enable.setChecked(ScoreConfig.isIsCJCXEnable());
        check_box_show_not_cur_week_course.setChecked(OptionManager.isShow_not_current_week_course());
    }

    private void loadTestSelect(){
        check_box_test.setChecked(MainActivity.isAcceptTestFun());
        check_box_transparent_navigation.setChecked(OptionManager.isTransparent_navigation_bar());
    }

    private ColorStateList getColorStateListTest() {
        int[][] states = new int[][]{
//                new int[]{android.R.attr.state_enabled}, // enabled
//                new int[]{-android.R.attr.state_enabled}, // disabled
//                new int[]{android.R.attr.state_checked}, // unchecked
//                new int[]{android.R.attr.state_pressed},  // pressed
                new int[]{android.R.attr.colorAccent}
        };
        int color = ColorManager.getPrimaryColor();
        int[] colors = new int[]{color};
        return new ColorStateList(states, colors);
    }

    public void changeTheme(){
        super.changeTheme();
        findViewById(R.id.activity_setting).setBackground(ColorManager.getMainBackground_full());
        check_box_xuanxiu.setForegroundTintList(getColorStateListTest());
        check_box_xianxuan.setForegroundTintList(getColorStateListTest());
        check_box_PE.setForegroundTintList(getColorStateListTest());
        check_box_xiaoxuanxiu.setForegroundTintList(getColorStateListTest());
    }

    public void showResponse(final String string){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SettingActivity.this, string, Toast.LENGTH_SHORT).show();
//                showAlert(string);
            }
        });
    }
}
