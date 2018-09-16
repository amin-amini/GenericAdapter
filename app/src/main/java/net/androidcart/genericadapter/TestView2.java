package net.androidcart.genericadapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import net.androidcart.genericadapter.annotations.GenericAdapter;
import net.androidcart.genericadapter.annotations.GenericAdapterView;
import net.androidcart.genericadapter.databinding.ViewTest2Binding;
import net.androidcart.genericadapter.databinding.ViewTestBinding;

/**
 * Created by Amin Amini on 9/5/18.
 */

@GenericAdapter
public class TestView2 extends RelativeLayout implements GenericAdapterView<TestModel> {

    MainActivity activity;
    public TestView2(MainActivity activity){
        super(activity);
        this.activity = activity;
        init();
    }

    public TestView2(Context context) {
        super(context);
        init();
    }

    public TestView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TestView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TestView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    ViewTest2Binding binding;

    void init(){

        LayoutInflater inflater = (LayoutInflater)
                getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        binding = DataBindingUtil.inflate(
                inflater, R.layout.view_test2, this, true);
    }

    @Override
    public void onBind(TestModel model, int position, Object extraObject) {
        binding.foo.setText(model.getA() + " position : " + position);
        binding.bar.setText(model.getB() + " activityClass : " + activity.getClass());
    }
}
