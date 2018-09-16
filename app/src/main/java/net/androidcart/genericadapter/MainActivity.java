package net.androidcart.genericadapter;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import net.androidcart.genericadapter.annotations.GenericAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<TestModel> models1 = new ArrayList<>();
        models1.add(new TestModel("Model_1 foo" , "Model_1 bar"));
        models1.add(new TestModel("Model_1 foo" , "Model_1 bar"));

        ArrayList<TestModel> models2 = new ArrayList<>();
        models2.add(new TestModel("Model_2 foo" , "Model_2 bar"));
        models2.add(new TestModel("Model_2 foo" , "Model_2 bar"));
        models2.add(new TestModel("Model_2 foo" , "Model_2 bar"));
        models2.add(new TestModel("Model_2 foo" , "Model_2 bar"));

        ArrayList<TestModel> models3 = new ArrayList<>();
        models3.add(new TestModel("Model_3 foo" , "Model_3 bar"));
        models3.add(new TestModel("Model_3 foo" , "Model_3 bar"));

        GenericRecyclerAdapter adapter = new GenericRecyclerAdapter();
        adapter.addSections(Section.TestView(models1, MainActivity.this));
        adapter.addSections(Section.TestView2(models2));
        adapter.addSections(Section.TestView3(models3));

        adapter.setTestView2Provider( (ctx) -> new TestView2(MainActivity.this) );

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false));

        recyclerView.setAdapter(adapter);
    }
}
