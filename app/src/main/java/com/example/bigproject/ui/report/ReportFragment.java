package com.example.bigproject.ui.report;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.bigproject.FileHelper;
import com.example.bigproject.R;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class ReportFragment extends Fragment {

    private ReportViewModel reportViewModel;

    //测试数据,待补充，需要从内存文件中读取使用时间
    private float sleep,
        move,
        dark,
        sum;

//    private Context mcontext;


    private float unhealthy;
    private float[] values={sleep,move,dark};

    private TextView tv_date;
    private TextView tv_health;
    private TextView tv_dark;
    private TextView tv_move;
    private TextView tv_sleep;

    private CategorySeries data_set;
    private LinearLayout ll_expense_piechart;
    private GraphicalView graphicalView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        reportViewModel =
                ViewModelProviders.of(this).get(ReportViewModel.class);
        View root = inflater.inflate(R.layout.fragment_report, container, false);
//        final TextView textView = root.findViewById(R.id.text_gallery);
//        galleryViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

        tv_date=root.findViewById(R.id.date);

        tv_dark=root.findViewById(R.id.tvDark);
        tv_health=root.findViewById(R.id.tvHealth);
        tv_move=root.findViewById(R.id.tvMove);
        tv_sleep=root.findViewById(R.id.tvSleep);

        Calendar calendar=Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));//设置时区
        String mMonth=String.valueOf(calendar.get(Calendar.MONTH)+1);
        String mDay=String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        tv_date.setText(mMonth+"月"+mDay+"日");

        String detail = "";
        FileHelper fHelper2 = new FileHelper(getContext());
        try {
            String fname ="myFile.txt";
            detail = fHelper2.read(fname);
            String stringArray[] = detail.split(" ");
            float num[] = new float[stringArray.length];
            for (int i = 0; i < stringArray.length; i++) {
                num[i] = Float.parseFloat(stringArray[i]);
            }
            values[0]=num[0];sleep=values[0];
            values[1]=num[1];move=values[1];
            values[2]=num[2];dark=values[2];
            unhealthy =move+sleep+dark;
            //Toast.makeText(getContext(), values[0]+" "+values[1]+" "+values[2]+" "+sum+" ", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        data_set=buildCategoryDataset("使用报告",(values), unhealthy);

        tv_health.setText((int) unhealthy +"分钟");
        tv_move.setText((int)move+"分钟");
        tv_sleep.setText((int)sleep+"分钟");
        tv_dark.setText((int)dark+"分钟");

        ll_expense_piechart = root.findViewById(R.id.ll_expense_piechart);
        ll_expense_piechart.removeAllViews();

        graphicalView = ChartFactory.getPieChartView(getContext()
                ,data_set, renderer);//饼状图
        graphicalView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        ll_expense_piechart.addView(graphicalView);
        return root;

    }


    private CategorySeries buildCategoryDataset(String title, float[] values, double sum)
    {
        CategorySeries series =new CategorySeries(title);
        series.add("躺卧时使用",values[0]/ unhealthy);
        series.add("运动时使用",values[1]/ unhealthy);
        series.add("黑暗环境中使用",values[2]/ unhealthy);
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


