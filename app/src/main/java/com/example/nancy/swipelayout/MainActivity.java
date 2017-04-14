package com.example.nancy.swipelayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.nancy.swipelayout.view.SwipeLayout;

import java.util.ArrayList;
import java.util.List;



/**改动了一点点*/
public class MainActivity extends AppCompatActivity {

    private ListView listView;

    private List<String> mDatas = new ArrayList<>();
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initData();

    }

    private void initData() {
        for (int i = 0; i < 25; i++) {
            mDatas.add("内容" + i);
        }

        adapter.notifyDataSetChanged();
    }

    private void initView() {
        setContentView(R.layout.activity_main);


        listView = (ListView) findViewById(R.id.lv);
        adapter = new MyAdapter();
        listView.setAdapter(adapter);

    }

    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mDatas.size();//在属性的位置初始化肯定不会为null
        }

        @Override
        public String getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(MainActivity.this, R.layout.item, null);

                holder = new ViewHolder();
                holder.tv_content = (TextView) convertView.findViewById(R.id.item_tv_content);
                holder.tv_delete = (TextView) convertView.findViewById(R.id.item_tv_delete);


                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ((SwipeLayout)convertView).close();//todo 删除的情况下默认会复用前一个，所以假如没有这句话
            //todo 删除了之后，下一个还是会处于已经侧滑出删除的状态！

            holder.tv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("delete:"+v);
                    mDatas.remove(position);
                    notifyDataSetChanged();

                }
            });

            holder.tv_content.setText(getItem(position));


            return convertView;
        }

        private class ViewHolder {
            TextView tv_content;
            TextView tv_delete;
        }
    }
}
