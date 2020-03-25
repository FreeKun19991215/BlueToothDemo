package com.study.bluetooth.BTConnect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class MessageAdapter extends BaseAdapter {

    private List<String> mData;
    private Context mContext;

    public MessageAdapter(List<String> data, Context context) {
        mData = data;
        mContext = context;
    }

    @Override
    public int getCount() {
            return mData.size();
    }

    @Override
    public Object getItem(int position) {
            return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        //复用view，优化性能
        if(view == null){
            view = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent,false);
        }

        String message = (String) getItem(position);

        TextView line1 = view.findViewById(android.R.id.text1);

        line1.setText(message);

        return view;
    }

}
