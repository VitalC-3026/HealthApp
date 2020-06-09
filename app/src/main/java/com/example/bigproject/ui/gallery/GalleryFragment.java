package com.example.bigproject.ui.gallery;

import android.graphics.Color;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.bigproject.R;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class GalleryFragment extends Fragment {

    private GalleryViewModel galleryViewModel;

    //测试数据,待补充，需要从内存文件中读取使用时间
    private int sleep=60,
        move=30,
        dark=60,
        sum=300;
    private int health=move+sleep+dark;
    private double[] values={sleep,move,dark};

    private TextView tv_date;
    private TextView tv_sum;
    private TextView tv_health;
    private TextView tv_dark;
    private TextView tv_move;
    private TextView tv_sleep;

    private CategorySeries data_set=buildCategoryDataset("使用报告",values,health);
    private LinearLayout ll_expense_piechart;
    private GraphicalView graphicalView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
//        final TextView textView = root.findViewById(R.id.text_gallery);
//        galleryViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

        tv_date=root.findViewById(R.id.date);

        tv_sum=root.findViewById(R.id.tvSum);
        tv_dark=root.findViewById(R.id.tvDark);
        tv_health=root.findViewById(R.id.tvHealth);
        tv_move=root.findViewById(R.id.tvMove);
        tv_sleep=root.findViewById(R.id.tvSleep);

        Calendar calendar=Calendar.getInstance();
        String mMonth=String.valueOf(calendar.get(Calendar.MONTH)+1);
        String mDay=String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)-1);
        tv_date.setText(mMonth+"月"+mDay+"日");

        tv_sum.setText(sum+"分钟");
        tv_health.setText(health+"分钟");
        tv_move.setText(move+"分钟");
        tv_sleep.setText(sleep+"分钟");
        tv_dark.setText(dark+"分钟");

        ll_expense_piechart = root.findViewById(R.id.ll_expense_piechart);
        ll_expense_piechart.removeAllViews();

        graphicalView = ChartFactory.getPieChartView(getContext()
                ,data_set, renderer);//饼状图
        graphicalView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        ll_expense_piechart.addView(graphicalView);
        return root;


    }


    private   CategorySeries buildCategoryDataset(String title,double[]values,double sum)
    {
        CategorySeries series =new CategorySeries(title);
        series.add("躺卧时使用",values[0]/sum);
        series.add("运动时使用",values[1]/sum);
        series.add("黑暗环境中使用",values[2]/sum);
        return series;
    }
    private DefaultRenderer getPieRenderer(){
        DefaultRenderer renderer = new DefaultRenderer();
        renderer.setZoomButtonsVisible(true);//设置显示放大放小缩小按钮
        renderer.setZoomEnabled(true);//设置允许放大放小
        //设置各个类别分别对应的颜色
        SimpleSeriesRenderer yellowRenderer = new SimpleSeriesRenderer();
        yellowRenderer.setColor(Color.YELLOW);
        SimpleSeriesRenderer blueRenderer = new SimpleSeriesRenderer();
        blueRenderer.setColor(Color.GRAY);
        SimpleSeriesRenderer redRenderer = new SimpleSeriesRenderer();
        redRenderer.setColor(Color.RED);
        renderer.addSeriesRenderer(yellowRenderer);
        renderer.addSeriesRenderer(blueRenderer);
        renderer.addSeriesRenderer(redRenderer);

        renderer.setLabelsTextSize(30);//设置坐标字号
        renderer.setLegendTextSize(50);//设置图例字号
        renderer.setApplyBackgroundColor(true);//设置是否应用背景色
        renderer.setBackgroundColor(Color.BLACK);

        return renderer;
    }

    private int[] colors = {Color.GRAY, Color.GREEN, Color.RED};
    private DefaultRenderer renderer = buildCategoryRenderer(colors);

    private DefaultRenderer buildCategoryRenderer(int[] colors){
        DefaultRenderer renderer = new DefaultRenderer();
        renderer.setLegendTextSize(35);//设置左下角标注文字的大小
        renderer.setLabelsTextSize(25);//饼图上标记文字的字体大小
        renderer.setLabelsColor(Color.BLACK);//饼图上标记文字的颜色
        renderer.setPanEnabled(false);
        renderer.setDisplayValues(true);//显示数据


        for(int color : colors){
            SimpleSeriesRenderer r = new SimpleSeriesRenderer();
            r.setColor(color);
            //设置百分比
            r.setChartValuesFormat(NumberFormat.getPercentInstance());
            renderer.addSeriesRenderer(r);
        }
        return renderer;
    }

}


