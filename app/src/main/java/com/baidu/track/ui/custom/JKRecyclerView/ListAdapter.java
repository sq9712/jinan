package com.baidu.track.ui.custom.JKRecyclerView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.track.R;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.MyViewHolder>{
    private List<String> mDatas;
    private Context mContext;
    private LayoutInflater inflater;
    //列表项和按钮的点击监听
    private onItemClickListener itemClickListener;

    public ListAdapter(Context context, List<String> datas){
        this. mContext=context;
        this. mDatas=datas;
        inflater=LayoutInflater. from(mContext);
    }

    @Override
    public int getItemCount() {

        return mDatas.size();
    }

    //填充onCreateViewHolder方法返回的holder中的控件
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        holder.tv.setText( mDatas.get(position));
    }

    //重写onCreateViewHolder方法，返回一个自定义的ViewHolder
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.item_home,parent, false);
        MyViewHolder holder= new MyViewHolder(view);
        return holder;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv;

        public MyViewHolder(View view) {
            super(view);
            tv=(TextView) view.findViewById(R.id.tv_item);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(itemClickListener!=null){
                        itemClickListener.itemClick(getAdapterPosition());
                    }
                }
            });
        }

    }
    public void setItemClickListener(onItemClickListener itemClickListenerr) {
        this.itemClickListener = itemClickListenerr;

    }

    public void updateData(List<String> data) {
        mDatas = data;
        notifyDataSetChanged();
    }

}
