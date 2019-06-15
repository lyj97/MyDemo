package com.lu.mydemo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import android.widget.SimpleAdapter;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarEntry;
import com.tapadoo.alerter.Alerter;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import Config.ColorManager;
import UIMS.UIMS;
import View.PopWindow.LoginGetScorePopupWindow;
import View.View.LinearLayoutForListView;
import View.View.ListViewForScrollView;

public class MainActivity extends AppCompatActivity
{

//    TextView scoreStatisticsTextViewControl;
    TextView scoreStatisticsTitleTextView;
    TextView scoreStatisticsTextView;
//    TextView centerTitle;

    Toolbar toolbar;
    ActionBar actionBar;

    TextView backTextView;

    FloatingActionButton fab;

//    private TextView navigation_back;

    HashMap<Integer, String> index_id = new HashMap<>();
    HashMap<String, String> courseTypeID_courseType;

    private int requiredScoreSum = 0;
    private double requiredGPASum = 0;
    private double requiredCreditSum = 0;

//    private boolean isShow = true;
//    private SharedPreferences sp;

    public LoginGetScorePopupWindow getScorePopupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);

        //TODO TEST
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle("");

//        sp = this.getSharedPreferences("ScoreStatistics", Context.MODE_PRIVATE);
//        isShow = sp.getBoolean("show", true);

//        scoreStatisticsTextViewControl = findViewById(R.id.activity_main_textView_ScoreStatisticsControl);
        scoreStatisticsTextView = findViewById(R.id.activity_main_textView_ScoreStatistics);
//        scoreStatisticsTitleTextView = findViewById(R.id.textView_ScoreStatisticsTitle);
        backTextView = findViewById(R.id.activity_scrolling_layout_back_text);
        fab = findViewById(R.id.activity_scrolling_fab);

//        navigation_back = findViewById(R.id.activity_main_navigation_back_text);

        changeTheme();

        showLoading("加载中...");

        loadScoreList();

        scoreStatisticsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlert("关于\"必修平均成绩(绩点)\"", "数据由本地计算得出.\n\n计算方法：\n必修课程首次成绩(绩点)的加权平均数;\n权值为科目学分.\n\n本数据仅供参考，不代表教务成绩统计结果，请知悉.");
            }
        });

//        navigation_back.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });

        //TODO TEST 成绩统计隐藏测试
//        scoreStatisticsTextViewControl.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                changeScoreStatisticsState(!isShow);
//            }
//        });
//        changeScoreStatisticsState(isShow);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO flush score
//                Toast.makeText(MainActivity.this, "FLUSH!", Toast.LENGTH_SHORT).show();
                LoginGetScorePopupWindow window = new LoginGetScorePopupWindow(MainActivity.this, findViewById(R.id.activity_scrolling_layout).getHeight(), findViewById(R.id.activity_scrolling_layout).getWidth());
                window.setFocusable(true);
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                window.showAtLocation(MainActivity.this.findViewById(R.id.activity_scrolling_layout), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
                getScorePopupWindow = window;
            }
        });

        backTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

//    private void changeScoreStatisticsState(boolean show) {
//        if (show) {
//            scoreStatisticsTextViewControl.setText("⇧隐藏成绩统计");
//            scoreStatisticsTextView.setVisibility(View.VISIBLE);
//            isShow = true;
//            sp.edit().putBoolean("show", true).apply();
//        } else {
//            scoreStatisticsTextViewControl.setText("⇩展开成绩统计");
//            scoreStatisticsTextView.setVisibility(View.INVISIBLE);
//            isShow = false;
//            sp.edit().putBoolean("show", false).apply();
//        }
//    }

    private void loadScoreList(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                courseTypeID_courseType = UIMS.getCourseTypeId_courseType();
                final List<Map<String, Object>> datalist = getScoreList();
                final ListViewForScrollView lv = findViewById(R.id.activity_main_list_view);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lv.setAdapter(new colorAdapter(MainActivity.this, datalist, R.layout.list_item, new String[]{"title", "context1", "context2", "context3", "context4"}, new int[]{R.id.list_item_title, R.id.list_item_context1, R.id.list_item_context2, R.id.list_item_context3, R.id.list_item_context4}));
                        lv.addHeaderView(new ViewStub(MainActivity.this));
                        lv.addFooterView(new ViewStub(MainActivity.this));
                        lv.setSelector(new ColorDrawable(Color.TRANSPARENT));
                        lv.setOnItemClickListener(new OnItemClickListener() {
                            //list点击事件
                            @Override
                            public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4) {
                                // TODO: Implement this method
//                Toast.makeText(MainActivity.this, "TEMP", Toast.LENGTH_SHORT).show();
//                Alerter.create(MainActivity.this)
//                        .setTitle("Title")
//                        .setText("p1:\t" + p1 + "\n" +
//                                "p2:\t" + p2 + "\n" +
//                                "p3:\t" + p3 + "\n" +
//                                "p4:\t" + p4 + "\n"
//                        )
//                        .enableSwipeToDismiss()
//                        .setBackgroundColorInt(Color.rgb(100,100,100))
//                        .show();
//                Log.i("MainActivity:showPercent", index_id.get(p3 - 1) + ((String) datalist.get(p3 - 1).get("context1")).contains("是"));
                                showPercent(index_id.get(p3 - 1), ((String) datalist.get(p3 - 1).get("context1")).contains("是"));
                            }
                        });
                        Alerter.hide();
                    }
                });
            }
        }).start();
    }

    public void reloadScoreList(){
        final List<Map<String, Object>> datalist = getScoreList();
        final ListViewForScrollView lv = findViewById(R.id.activity_main_list_view);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lv.setAdapter(new colorAdapter(MainActivity.this, datalist, R.layout.list_item, new String[]{"title", "context1", "context2", "context3", "context4"}, new int[]{R.id.list_item_title, R.id.list_item_context1, R.id.list_item_context2, R.id.list_item_context3, R.id.list_item_context4}));
//                lv.addHeaderView(new ViewStub(MainActivity.this));
//                lv.addFooterView(new ViewStub(MainActivity.this));
//                lv.setSelector(new ColorDrawable(Color.TRANSPARENT));
//                lv.setOnItemClickListener(new OnItemClickListener() {
//                    //list点击事件
//                    @Override
//                    public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4) {
//                        // TODO: Implement this method
//                        showPercent(index_id.get(p3 - 1), ((String) datalist.get(p3 - 1).get("context1")).contains("是"));
//                    }
//                });
            }
        });
    }

    private List<Map<String,Object>> getScoreList(){

        List<Map<String,Object>> dataList = new ArrayList<>();
        Map<String,Object> map;

        JSONObject scoreJSON = UIMS.getScoreJSON();

        JSONArray scores = scoreJSON.getJSONArray("value");

        try {

            for (int i = 0; i < scores.size(); i++) {
                map = new HashMap<>();

                JSONObject temp = scores.getJSONObject(i);
                JSONObject teachingTerm = temp.getJSONObject("teachingTerm");
                JSONObject course = temp.getJSONObject("course");
                String courName = course.getString("courName");
                String termName = teachingTerm.getString("termName");
                String courScore = temp.getString("score");
                int scoreNum = temp.getInt("scoreNum");
                String isReselect = (temp.getString("isReselect").contains("Y")) ? "是" : "否";
                String gPoint = temp.getString("gpoint");
                String dateScore = temp.getString("dateScore");
                String type5 = temp.getString("type5");
                String adviceCredit = course.getString("adviceCredit");
                dateScore = dateScore.replaceAll("T", "  ");

                index_id.put(i, temp.getString("asId"));

                map.put("title", courName + "(" + courseTypeID_courseType.get(type5) + ")");
//                map.put("context1","成绩:" + courScore + "  \t  " +
//                        "重修:" + isReselect);
//                map.put("context2",
//                        termName);
                map.put("context1", termName + "   \t   " +
                        "重修?  " + isReselect + "    绩点： " + gPoint);
                map.put("context2",
                        courScore);
                map.put("context3",
                        "发布时间： " + dateScore);
                map.put("context4",
                        adviceCredit);
                map.put("type", type5);

                if (isReselect.equals("否") && type5.equals("4160")) {
                    requiredScoreSum += scoreNum * Double.parseDouble(adviceCredit);
                    requiredGPASum += Double.parseDouble(gPoint) * Double.parseDouble(adviceCredit);
                    requiredCreditSum += Double.parseDouble(adviceCredit);
                }

                dataList.add(map);

            }

//            dataList.add(getScoreStatistics());
            getScoreStatistics();

            return dataList;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private Map<String,Object> getScoreStatistics() {

        JSONObject scoreStatistics = UIMS.getScoreStatisticsJSON();

        double avgScoreBest = scoreStatistics.getJSONArray("value").getJSONObject(0).getDouble("avgScoreBest");
        double avgScoreFirst = scoreStatistics.getJSONArray("value").getJSONObject(0).getDouble("avgScoreFirst");
        double gpaFirst = scoreStatistics.getJSONArray("value").getJSONObject(0).getDouble("gpaFirst");
        double gpaBest = scoreStatistics.getJSONArray("value").getJSONObject(0).getDouble("gpaBest");

        Map<String, Object> map = new HashMap<>();
//        map.put("title","绩点统计");
//        map.put("context","【首次成绩】：平均成绩:" + avgScoreFirst + "  " + "平均绩点:" + gpaFirst + "  \n" +
//                "【最好成绩】：平均成绩:" + avgScoreBest + "  " + "平均绩点:" + gpaBest);

//        String title = "绩点统计";
        String title = "学分成绩";
        String value = "【首次成绩】 \t平均成绩:" + String.format("%.2f", avgScoreFirst) + "  " + "平均绩点:" + String.format("%.2f", gpaFirst) + "\n" +
                "【最好成绩】 \t平均成绩:" + String.format("%.2f", avgScoreBest) + "  " + "平均绩点:" + String.format("%.2f", gpaBest) + "\n" +
                "必修平均成绩:" + String.format("%.2f", requiredScoreSum / requiredCreditSum) + " \t 必修平均绩点:" + String.format("%.2f", requiredGPASum / requiredCreditSum);
        scoreStatisticsTextView.setText(value);
//        scoreStatisticsTitleTextView.setText(title);
        return map;

    }

    public void showPercent(final String asID, boolean isReSelect){
        final JSONObject percent = UIMS.getScorePercentJSON(asID);

        if(percent == null){
            showAlert("成绩分布走丢了(っ °Д °;)っ\n\n登录后点击“刷新信息”再试一下吧.");
            return;
        }

        JSONArray items = percent.getJSONArray("items");

        StringBuilder stringBuilder = new StringBuilder();
        JSONObject temp;
        String label;
        double perc;
        int i = 0;
        try{
            while(true) {
                temp = items.getJSONObject(i);
                label = temp.getString("label");
                perc = temp.getDouble("percent");

                if(i != 0) stringBuilder.append('\n');
                stringBuilder.append(label);
                if(label.length() == 8) stringBuilder.append(":\t");
                else if(label.length() < 8) stringBuilder.append(":\t\t\t");
                else if(label.length() > 8) stringBuilder.append(":\t\t");
                stringBuilder.append(getScorePercentString(perc));
                stringBuilder.append(String.format("%.1f", perc) + " %");
                if(UIMS.getCourseTypeId(asID).equals("4160") && ! isReSelect) stringBuilder.append("\t(约 " + String.format("%.0f",UIMS.getStudCnt() * perc * 0.01) + " 人)");
                i++;
            }

        }catch (Exception e) {
//            e.printStackTrace();
            if(UIMS.getCourseTypeId(asID).equals("4160") && ! isReSelect) stringBuilder.append("\n\n人数仅供必修课参考，其他课程无意义哦╥﹏╥...");
        }
        final String str = stringBuilder.toString();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Alerter.create(MainActivity.this)
                        .setTitle(percent.getString("courName"))
                        .setText(str)
                        .enableSwipeToDismiss()
                        .setDuration(4000)
                        .hideIcon()
//                        .enableProgress(true)
                        .setBackgroundColorInt(ColorManager.getTopAlertBackgroundColor())
                        .show();
            }
        });
    }

    private String getScorePercentString(double percent){
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0; i<(int)percent/3; i++){
            stringBuilder.append("|");
        }
        stringBuilder.append("\t\t");
        return stringBuilder.toString();
    }

    public void showResponse(final String string){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show();
//                showAlert(string);
            }
        });
    }

    public class NestedListView extends ListView  {

        public NestedListView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            //测量的大小由一个32位的数字表示，前两位表示测量模式，后30位表示大小，这里需要右移两位才能拿到测量的大小
            int heightSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, heightSpec);
        }

    }

    private ColorStateList getColorStateListTest() {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled}, // enabled
                new int[]{-android.R.attr.state_enabled}, // disabled
                new int[]{-android.R.attr.state_checked}, // unchecked
                new int[]{android.R.attr.state_pressed}  // pressed
        };
        int color = ColorManager.getTopAlertBackgroundColor();
        int[] colors = new int[]{color, color, color, ColorManager.getTopAlertBackgroundColor()};
        return new ColorStateList(states, colors);
    }

    private void changeTheme(){
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ColorManager.getPrimaryColor());

        findViewById(R.id.activity_scrolling_layout).setBackground(ColorManager.getMainBackground_full());
        fab.setBackgroundTintList(getColorStateListTest());
//        fab.setColorFilter(ColorManager.getPrimaryColor());
//        fab.setRippleColor(ColorManager.getPrimaryColor());
//        fab.setBackgroundColor(ColorManager.getPrimaryColor());
    }

    public void dismissGetScorePopWindow(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getScorePopupWindow.dismiss();
            }
        });
    }

    public void showLoading(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Alerter.create(MainActivity.this)
                        .setText(message)
                        .enableProgress(true)
                        .setDismissable(false)
                        .setProgressColorRes(R.color.color_alerter_progress_bar)
                        .setDuration(Integer.MAX_VALUE)
                        .setBackgroundColorInt(ColorManager.getTopAlertBackgroundColor())
                        .show();
            }
        });
    }

    public void showAlert(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Alerter.create(MainActivity.this)
                        .setTitle("提示")
                        .setText(message)
                        .enableSwipeToDismiss()
                        .setBackgroundColorInt(ColorManager.getTopAlertBackgroundColor())
                        .show();
            }
        });
    }

    public void showAlert(final String title, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Alerter.create(MainActivity.this)
                        .setTitle(title)
                        .setText(message)
                        .enableSwipeToDismiss()
                        .setBackgroundColorInt(ColorManager.getTopAlertBackgroundColor())
                        .show();
            }
        });
    }

    public void showWarningAlert(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Alerter.create(MainActivity.this)
                        .setTitle("提示")
                        .setText(message)
                        .enableSwipeToDismiss()
                        .setBackgroundColorInt(getResources().getColor(R.color.color_alerter_warning_background))
                        .show();
            }
        });
    }

    public void showWarningAlert(final String title, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Alerter.create(MainActivity.this)
                        .setTitle(title)
                        .setText(message)
                        .enableSwipeToDismiss()
                        .setBackgroundColorInt(getResources().getColor(R.color.color_alerter_warning_background))
                        .show();
            }
        });
    }

    class colorAdapter extends SimpleAdapter {
        List<? extends Map<String, ?>> mdata;

        public colorAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from,
                               int[] to) {
            super(context, data, resource, from, to);
            this.mdata = data;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LinearLayout.inflate(getBaseContext(), R.layout.list_item, null);
            }//这个TextView是R.layout.list_item里面的，修改这个字体的颜色
            TextView textView = (TextView) convertView.findViewById(R.id.list_item_title);
            //获取每次进来时 mData里面存的值  若果相同则变颜色
            //根据Key值取出装入的数据，然后进行比较
            String ss=(String)mdata.get(position).get("type");
            if(ss.equals("4160")){
                textView.setTextColor(getResources().getColor(R.color.course_bixiu));
            }else if(ss.equals("4161")){
                textView.setTextColor(getResources().getColor(R.color.course_xuanxiu));
            }else if(ss.equals("4162")){
                textView.setTextColor(getResources().getColor(R.color.course_xianxuan));
            }else if(ss.equals("4163")){
                textView.setTextColor(getResources().getColor(R.color.course_xiaoxuanxiu));
            }else if(ss.equals("4164")){
                textView.setTextColor(getResources().getColor(R.color.course_tiyu));
            }
            //Log.i("TAG", Integer.toString(position));
            //Log.i("TAG", (String) mData.get(position).get("text"));
            return super.getView(position, convertView, parent);
        }
    }


}