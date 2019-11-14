package com.baidu.track.ui.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.baidu.track.R;
import com.baidu.track.ui.custom.JKRecyclerView.onItemClickListener;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder>{

    private Context mContext;
    private LayoutInflater inflater;
    //列表项和按钮的点击监听
    private onItemClickListener itemClickListener;
    private List<String> mPaths = new ArrayList<>();

    public ImageAdapter(Context context, List<String> paths) {
        mContext = context;
        mPaths = paths;
        inflater=LayoutInflater. from(mContext);
    }

    @Override
    public int getItemCount() {

        return mPaths.size();
    }

    //填充onCreateViewHolder方法返回的holder中的控件
    @Override
    public void onBindViewHolder(ImageAdapter.MyViewHolder holder, final int position) {

        Glide.with(mContext)
                .load(mPaths.get(position))
                .crossFade()
                .into(holder.mPhoto_image);
    }

    //重写onCreateViewHolder方法，返回一个自定义的ViewHolder
    @Override
    public ImageAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.activity_image_item,parent, false);
        ImageAdapter.MyViewHolder holder= new ImageAdapter.MyViewHolder(view);
        return holder;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView mPhoto_image;

        public MyViewHolder(View view) {
            super(view);
            mPhoto_image = (ImageView) view.findViewById(R.id.photo_image);
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


}
