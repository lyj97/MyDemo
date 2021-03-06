package com.lu.mydemo.View.Fragment;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.lu.mydemo.Activity.NoneScoreCourseActivity;
import com.lu.mydemo.Notification.AlertCenter;
import com.lu.mydemo.R;
import com.lu.mydemo.sample.adapter.BaseAdapter;
import com.lu.mydemo.sample.adapter.MainAdapter;
import com.tapadoo.alerter.Alerter;
import com.yanzhenjie.recyclerview.OnItemMenuClickListener;
import com.yanzhenjie.recyclerview.SwipeMenu;
import com.yanzhenjie.recyclerview.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.SwipeMenuItem;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;

import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.lu.mydemo.Config.ColorManager;
import com.lu.mydemo.Utils.Exam.ExamSchedule;
import com.lu.mydemo.View.PopWindow.AddCourseExamPopupWindow;
import com.lu.mydemo.View.View.ItemMaskLayout;

public class ExamFragment extends Fragment implements ItemMaskLayout.ItemMaskClickListener {

    private LinearLayout myFragmentLayout;
    private NoneScoreCourseActivity context;

    private Button addButton;

    private AddCourseExamPopupWindow popupWindow;

    private SwipeRecyclerView swipeRecyclerView;
    private BaseAdapter myAdapter;
    private List<HashMap<String, Object>> dataList;

    private static JSONObject tempJsonObject;

    public static boolean flush = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        myFragmentLayout = (LinearLayout) inflater.inflate(R.layout.fragment_course_exam, container, false);
        return myFragmentLayout;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        flush = false;
        context = ((NoneScoreCourseActivity) getActivity()).getContext();

        addButton = myFragmentLayout.findViewById(R.id.course_exam_fragment_add_button);

        swipeRecyclerView = myFragmentLayout.findViewById(R.id.course_exam_fragment_SwipeRecyclerView);

        MyAddExamListener addExamListener = new MyAddExamListener();
        addButton.setOnClickListener(addExamListener);

        myAdapter = createAdapter();

        swipeRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        swipeRecyclerView.setSwipeMenuCreator(new SwipeMenuCreator() {
            @Override
            public void onCreateMenu(SwipeMenu leftMenu, SwipeMenu rightMenu, int position) {
                SwipeMenuItem collectItem = new SwipeMenuItem(context);
                collectItem.setImage(getResources().getDrawable(R.drawable.ic_delete_black_24dp));
                collectItem.setText("删除");
                collectItem.setHeight(ViewGroup.MarginLayoutParams.MATCH_PARENT);
                collectItem.setBackground(getResources().getDrawable(R.drawable.shape_collect_swap_menu_background));

                rightMenu.addMenuItem(collectItem);
            }
        });

        swipeRecyclerView.setOnItemMenuClickListener(new OnItemMenuClickListener() {
            @Override
            public void onItemClick(SwipeMenuBridge menuBridge, int adapterPosition) {
                menuBridge.closeMenu();

                Log.i("NewsSavedActivity", "menuBridge.getPosition:\t" + menuBridge.getPosition());


                Alerter.create(context)
                        .setText("已删除考试【" + dataList.get(adapterPosition).get("title") + "】\n\n" +
                                "如需撤销，请点击此处.")
                        .enableSwipeToDismiss()
                        .setProgressColorRes(R.color.color_alerter_progress_bar)
                        .setDuration(6000)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ExamSchedule.add(tempJsonObject);
                                flushList();
                                AlertCenter.hideAlert(context);
                                NoneScoreCourseFragment.setFlush();
                            }
                        })
                        .setBackgroundColorInt(ColorManager.getTopAlertBackgroundColor())
                        .show();
                tempJsonObject = new JSONObject();
                tempJsonObject.put("title", dataList.get(adapterPosition).get("title"));
                tempJsonObject.put("time", ((String) dataList.get(adapterPosition).get("time")).substring(0, 10) + ((String) dataList.get(adapterPosition).get("time")).substring(15));
                tempJsonObject.put("place", dataList.get(adapterPosition).get("place"));
                tempJsonObject.put("xkkh", "");
                tempJsonObject.put("flagTop", false);
                ExamSchedule.delete(adapterPosition);
                flushList();
                NoneScoreCourseFragment.setFlush();
            }
        });
        swipeRecyclerView.setOnItemClickListener(addExamListener);

        swipeRecyclerView.setAdapter(myAdapter);

        flushList();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.i("ExamFragment", "isVisibleToUser " + isVisibleToUser);
        if (isVisibleToUser) {
            // 相当于onResume()方法
            if(flush){
                flushList();
            }
        } else {
            // 相当于onpause()方法
            flush = false;
        }
    }

    protected MainAdapter createAdapter() {
        return new CourseExamAdapter(context);
    }

    public static void setFlush(){
        flush = true;
    }

    private void flushList(){
        dataList = getExamList();
        swipeRecyclerView.setAdapter(myAdapter);
        myAdapter.notifyDataSetChanged(dataList);
    }

    private List<HashMap<String, Object>> getExamList(){

        List<JSONObject> examList = ExamSchedule.getCourseTitle_examTime(context.getApplicationContext());

        List<HashMap<String,Object>> dataList = new ArrayList<>();
        HashMap<String,Object> map;

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Locale.setDefault(Locale.CHINA);

        Calendar cal = Calendar.getInstance();
        final String[] dayOfWeekName = new String[]{"","星期一","星期二","星期三","星期四","星期五","星期六","星期日"};
        int day_of_week;

        try {
            Iterator<JSONObject> iterator = examList.iterator();

            JSONObject temp;

            while(iterator.hasNext()) {
                temp = iterator.next();
                map = new HashMap<>();

                Date exam_date = df.parse(temp.getString("time"));

                cal.setTime(exam_date);
                day_of_week = cal.get(Calendar.DAY_OF_WEEK) - 1;
                if (day_of_week <= 0)
                    day_of_week = 7;

                map.put("title", temp.getString("title"));
                map.put("time", temp.getString("time").split(" ")[0] + "（" + dayOfWeekName[day_of_week] + "） " + temp.getString("time").split(" ")[1]);
                map.put("flagTop", temp.getBoolean("flagTop"));
                try{
                    map.put("place", temp.getString("place"));
                }catch (Exception e){
//                    e.printStackTrace();
                    map.put("place", "");
                }

                int day_distance = getTimeDistance(new Date(), exam_date);

                if(day_distance == 0){
                    map.put("time_left","今天考试啦，祝顺利~");
                }
                else if(day_distance > 0){
                    map.put("time_left","距考试 " + day_distance + " 天");
                }
                else{
                    map.put("time_left","考试已结束");
                }

                dataList.add(map);
            }

            return dataList;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void delete() {
//        Toast.makeText(context, "delete", Toast.LENGTH_SHORT).show();
    }

    class CourseExamAdapter extends MainAdapter {

        private List<Map<String,Object>> mDataList;

        public CourseExamAdapter(Context context){
            super(context);
        }

        public void notifyDataSetChanged(List dataList) {
            this.mDataList = (List<Map<String,Object>>)dataList;
            super.notifyDataSetChanged(mDataList);
        }

        @Override
        public int getItemCount() {
            return mDataList == null ? 0 : mDataList.size();
        }

        @NonNull
        @Override
        public MainAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CourseExamAdapter.ViewHolder(getInflater().inflate(R.layout.list_item_news, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MainAdapter.ViewHolder holder, int position) {
            boolean flagTop = false;
            try{
                flagTop = (boolean) mDataList.get(position).get("flagTop");
            }
            catch (Exception e){
                e.printStackTrace();
            }
            holder.setData((String) mDataList.get(position).get("title"), (String) mDataList.get(position).get("time"), (String) mDataList.get(position).get("time_left"), flagTop, (String) mDataList.get(position).get("place"));
        }

        class ViewHolder extends MainAdapter.ViewHolder {

            TextView tvTitle;
            TextView tvDepartment;
            TextView tvTime;

            public ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.news_list_item_title);
                tvDepartment = itemView.findViewById(R.id.news_list_item_department);
                tvTime = itemView.findViewById(R.id.news_list_item_time);
            }

            public void setData(String title, String time, String time_left, boolean flagTop, String place) {

                tvTitle.setTextColor(ColorManager.getNews_normal_text_color());

                if(flagTop){
                    tvTitle.setTextColor(ColorManager.getNews_notice_text_color());
                    this.tvTitle.setText("[置顶] " + title);
                }
                else {
                    tvTitle.setTextColor(ColorManager.getPrimaryColor());
                    this.tvTitle.setText(title);
                }

                if(place != null && place.length() > 0){
                    this.tvDepartment.setText(time + ", \t" + place);
                }
                else {
                    this.tvDepartment.setText(time);
                }
                this.tvTime.setText(time_left);

                if(time_left.length() == 0 && time.length() == 0){
                    tvTitle.setGravity(Gravity.CENTER);
                }

            }
        }

    }

    class MyAddExamListener implements android.view.View.OnClickListener, com.yanzhenjie.recyclerview.OnItemClickListener {

        @Override
        public void onClick(View v) {
            openPopWindow(-1, false);
        }

        @Override
        public void onItemClick(View view, int adapterPosition) {
            openPopWindow(adapterPosition, true);
        }

        public void openPopWindow(int adapterPosition, boolean setExamTitle){
            if(setExamTitle) popupWindow = new AddCourseExamPopupWindow(context, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "已添加【" + popupWindow.getTitle() + "】考试", Toast.LENGTH_SHORT).show();
                    ExamSchedule.add(popupWindow.getTitle(), popupWindow.getExam_date() + " " + popupWindow.getExam_time(), popupWindow.getExam_place());
                    flushList();
                    ExamFragment.setFlush();
                }
            }, context.findViewById(R.id.course_and_exam_layout).getHeight(), context.findViewById(R.id.course_and_exam_layout).getWidth(),
                    ((String) dataList.get(adapterPosition).get("title")).split("（")[0],
                    ((String) dataList.get(adapterPosition).get("time")).split(" ")[0],
                    ((String) dataList.get(adapterPosition).get("time")).split(" ")[1],
                    (String) dataList.get(adapterPosition).get("place")
            );
            else popupWindow = new AddCourseExamPopupWindow(context, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "已添加:" + popupWindow.getTitle(), Toast.LENGTH_SHORT).show();
                    ExamSchedule.add(popupWindow.getTitle(), popupWindow.getExam_date() + " " + popupWindow.getExam_time(), popupWindow.getExam_place());
                    NoneScoreCourseFragment.setFlush();
                    flushList();
                }
            }, context.findViewById(R.id.course_and_exam_layout).getHeight(), context.findViewById(R.id.course_and_exam_layout).getWidth());
            popupWindow.setFocusable(true);
            popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            //添加pop窗口关闭事件
            popupWindow.setOnDismissListener(new poponDismissListener());
//                popupWindow.setAnimationStyle(R.style.popwin_anim_style);
            //显示窗口
            popupWindow.showAtLocation(context.findViewById(R.id.course_and_exam_layout), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
        }
    }

    /**
     * 设置添加屏幕的背景透明度
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha)
    {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        context.getWindow().setAttributes(lp);
    }

    /**
     * 添加新笔记时弹出的popWin关闭的事件，主要是为了将背景透明度改回来
     * @author cg
     *
     */
    class poponDismissListener implements PopupWindow.OnDismissListener{

        @Override
        public void onDismiss() {
            //Log.v("List_noteTypeActivity:", "我是关闭事件");
            backgroundAlpha(1f);
        }

    }

    public static int getTimeDistance(Date beginDate , Date endDate ) {
        Calendar beginCalendar = Calendar.getInstance();
        beginCalendar.setTime(beginDate);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endDate);
        long beginTime = beginCalendar.getTime().getTime();
        long endTime = endCalendar.getTime().getTime();

        if(beginTime > endTime) return -1;//只适用于判断已过endtime

        int betweenDays = (int)((endTime - beginTime) / (1000 * 60 * 60 *24));//先算出两时间的毫秒数之差大于一天的天数

        endCalendar.add(Calendar.DAY_OF_MONTH, -betweenDays);//使endCalendar减去这些天数，将问题转换为两时间的毫秒数之差不足一天的情况
        endCalendar.add(Calendar.DAY_OF_MONTH, -1);//再使endCalendar减去1天
        if(beginCalendar.get(Calendar.DAY_OF_MONTH)==endCalendar.get(Calendar.DAY_OF_MONTH))//比较两日期的DAY_OF_MONTH是否相等
            return betweenDays + 1;	//相等说明确实跨天了
        else
            return betweenDays;	//不相等说明确实未跨天
    }

}
