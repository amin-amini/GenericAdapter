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
import net.androidcart.genericadapter.databinding.ViewTest3Binding;
import net.androidcart.genericadapter.databinding.ViewTestBinding;

/**
 * Created by Amin Amini on 9/5/18.
 */

@GenericAdapter
public class TestView3 extends RelativeLayout implements GenericAdapterView<TestModel> {
    public TestView3(Context context) {
        super(context);
        init();
    }

    public TestView3(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TestView3(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TestView3(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    ViewTest3Binding binding;

    void init(){

        LayoutInflater inflater = (LayoutInflater)
                getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        binding = DataBindingUtil.inflate(
                inflater, R.layout.view_test3, this, true);
    }

    @Override
    public void onBind(TestModel model, int position, Object extraObject) {
        binding.foo.setText(model.getA() + " position : " + position);
        binding.bar.setText(model.getB());
    }

}
