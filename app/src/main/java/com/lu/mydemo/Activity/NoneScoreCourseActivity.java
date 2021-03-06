package com.lu.mydemo.Activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.Arrays;
import java.util.List;

import com.lu.mydemo.Config.ColorManager;
import com.lu.mydemo.R;
import com.lu.mydemo.View.Fragment.ExamFragment;
import com.lu.mydemo.View.Fragment.NoneScoreCourseFragment;

public class NoneScoreCourseActivity extends BaseActivity {

    private ViewPager courseExamLayout;

    private TextView navigation_back;

    public NoneScoreCourseFragment noneScoreFragment;
    public ExamFragment examFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = this;
        setContentView(R.layout.activity_course_and_exam);

        courseExamLayout = findViewById(R.id.course_and_exam_layout_viewpager);

        navigation_back = findViewById(R.id.course_and_exam_layout_navigation_back_text);

        navigation_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        ColorManager.loadConfig(getApplicationContext(), NoneScoreCourseActivity.this);
        changeTheme();

        noneScoreFragment = new NoneScoreCourseFragment();

        examFragment = new ExamFragment();

        courseExamLayout.setAdapter(new MyPagerAdapter(getSupportFragmentManager(), this, Arrays.asList(new String[]{"考试安排", "成绩未发布课程"})));

    }

    public NoneScoreCourseActivity getContext(){
        return this;
    }

    public class MyPagerAdapter extends FragmentStatePagerAdapter {
        private Context mContext;
        private List<String> mData;

        public MyPagerAdapter(FragmentManager manager, Context context , List<String> list) {
            super(manager);
            mContext = context;
            mData = list;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0){
                return examFragment;
            }
            if(position == 1){
                return noneScoreFragment;
            }
            return null;
        }

        /**
         * 每次更新完成ViewPager的内容后，调用该接口，此处复写主要是为了让导航按钮上层的覆盖层能够动态的移动
         */
        @Override
        public void finishUpdate(ViewGroup container)
        {
            super.finishUpdate(container);//这句话要放在最前面，否则会报错
            //获取当前的视图是位于ViewGroup的第几个位置，用来更新对应的覆盖层所在的位置

        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mData.get(position);
        }
    }

    public void changeTheme(){
        super.changeTheme();
        findViewById(R.id.course_and_exam_layout).setBackground(ColorManager.getMainBackground_full());
    }

    public void showResponse(final String string){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NoneScoreCourseActivity.this, string, Toast.LENGTH_SHORT).show();
//                showAlert(string);
            }
        });
    }
}
