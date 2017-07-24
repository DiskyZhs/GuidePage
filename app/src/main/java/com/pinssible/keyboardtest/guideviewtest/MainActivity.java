package com.pinssible.keyboardtest.guideviewtest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pinssible.keyboardtest.guideviewtest.guide.GuideComponent;
import com.pinssible.keyboardtest.guideviewtest.guide.GuideMaskView;

public class MainActivity extends Activity {

    private TextView button;
    private GuideMaskView view;
    private RelativeLayout container, container2;
    private LinearLayout testLin;
    private GuideComponent component;
    private GuideMaskView maskView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.test_view);
        container = findViewById(R.id.container);
        //container2 = findViewById(R.id.container2);
        testLin = findViewById(R.id.test_lin);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Test", Toast.LENGTH_SHORT).show();
                component.dismiss();
            }
        });

        LayoutInflater li = LayoutInflater.from(this);
        View attachedView = li.inflate(R.layout.layout_attached, null);

        //创造MaskView
        maskView = new GuideMaskView.Builder(this, container, testLin)
                .setMaskColor(Color.RED)
                .setTargetShape(GuideMaskView.TargetShape.Oval)
                .setOvalXRadius(300)
                .setOvalYRadius(500)
                .build();
        //创造Component
        component = new GuideComponent.Builder(attachedView)
                .setComponentDirection(GuideComponent.ComponentDirection.LEFT_TOP)
                .setXOffset(150)
                .setYOffset(150)
                .build();
        component.showOnMaskView(maskView);
    }
}
