package com.stduy.apt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.stduy.lib_annotations.BindView;
import com.stduy.lib_annotations.OnClickListener;
import com.stduy.lib_core.ButterKniffer;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.hello_world)
    TextView helloWorld;

    @BindView(R.id.button)
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKniffer.bindView(this);
        helloWorld.setText("绑定成功");
        helloWorld.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKniffer.unbind(this);
    }


    @OnClickListener(ids = {R.id.button, R.id.button2})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                Toast.makeText(this, "事件被点击了", Toast.LENGTH_LONG).show();
                break;

            case R.id.button2:
                Toast.makeText(this, "数组绑定点击事件", Toast.LENGTH_LONG).show();
                break;
        }
    }
}