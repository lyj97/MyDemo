package View.View;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

/**
 * 取代ListView的LinearLayout，使之能够成功嵌套在ScrollView中
 * @author terry_龙
 */
public class LinearLayoutForListView extends LinearLayout {
    private BaseAdapter adapter;
    private OnClickListener onClickListener = null;

    public LinearLayoutForListView(Context context, AttributeSet set){
        super(context, set);
    }

    public LinearLayoutForListView(Context context, AttributeSet set, int style){
        super(context, set, style);
    }

    /**
     * 绑定布局
     */
    public void bindLinearLayout() {
        int count = adapter.getCount();
        this.removeAllViews();
        for (int i = 0; i < count; i++) {
            View v = adapter.getView(i, null, null);
            v.setOnClickListener(this.onClickListener);
            addView(v, i);
        }
        Log.v("countTAG", "" + count);
    }

    public LinearLayoutForListView(Context context) {
        super(context);
    }
}
