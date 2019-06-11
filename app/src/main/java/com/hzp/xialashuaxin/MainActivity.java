package com.hzp.xialashuaxin;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;

import com.hzp.xialashuaxin.view.PullToRefreshListView;
import com.hzp.xialashuaxin.view.PullToRefreshListView.OnRefreshListenter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private PullToRefreshListView mListView;

    private int i=0;
    private int j=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        initView();
    }

    /**
     * 初始化控件
     *
     */
    private void initView() {
        mListView = (PullToRefreshListView) findViewById(R.id.pulltorefreshlistview);

        final List<String> list = new ArrayList<String>();
        //显示数据
        for (int i = 0; i < 10; i++) {
            list.add("德玛西亚"+i+"区");
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        mListView.setAdapter(adapter);

        mListView.setOnRefreshListener(new OnRefreshListenter() {

            @Override
            public void refresh() {
                new Handler().postDelayed( new Runnable() {

                    @Override
                    public void run() {
                        //添加新的数据操作
                        list.add(0,"艾欧尼亚"+(++i)+"区");
                        adapter.notifyDataSetChanged();
                        //刷新完成，取消刷新操作
                        mListView.finish();
                    }
                }, 3000);
            }

            @Override
            public void loadmore() {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        //添加新的数据操作
                        list.add("黑色玫瑰"+(++j)+"区");
                        adapter.notifyDataSetChanged();
                        //刷新完成，取消刷新操作
                        mListView.finish();
                    }
                }, 3000);
            }
        });
    }
}
