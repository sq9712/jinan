package com.baidu.track.ui.view;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.baidu.track.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yishangfei on 2017/3/30 0030.
 * 个人主页：http://yishangfei.me
 * Github:https://github.com/yishangfei
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    private int selectMax;
    public final int TYPE_CAMERA = 1;
    public final int TYPE_PICTURE = 2;
    private List<Uri> mUris=new ArrayList<>();
    private List<String> mPaths=new ArrayList<>();

    private Context mContext;
    private LayoutInflater mInflater;


    public void setData(List<Uri> uris, List<String> paths) {
        mUris = uris;
        mPaths = paths;
        notifyDataSetChanged();
    }


    //点击添加图片跳转
    private onAddPicListener mOnAddPicListener;

    public interface onAddPicListener {
        void onAddPicClick(int type, int position);
    }


    public PhotoAdapter(Context context, onAddPicListener mOnAddPicListener) {
        mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mOnAddPicListener = mOnAddPicListener;
    }

    //设置图片选择最大值
    public void setSelectMax(int selectMax) {
        this.selectMax = selectMax;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView mPhoto_image;
        ImageView mPhoto_del;

        public ViewHolder(View view) {
            super(view);
            mPhoto_image = (ImageView) view.findViewById(R.id.photo_image);
            mPhoto_del = (ImageView) view.findViewById(R.id.photo_del);
        }
    }

    @Override
    public int getItemCount() {
        if (mPaths.size() < selectMax) {
            return mPaths.size() + 1;
        } else {
            return mPaths.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isShowAddItem(position)) {
            return TYPE_CAMERA;
        } else {
            return TYPE_PICTURE;
        }
    }

    /**
     * 创建ViewHolder
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.activity_photo_item,
                viewGroup, false);
        final ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    private boolean isShowAddItem(int position) {
        int size = mPaths.size() == 0 ? 0 : mPaths.size();
        return position == size;
    }

    /**
     * 设置值
     */
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        //少于3张，显示继续添加的图标
        Log.d("...", "onBindViewHolder: " + position);
        if (getItemViewType(position) == TYPE_CAMERA) {
            viewHolder.mPhoto_image.setImageResource(R.mipmap.icon_addpic);
            viewHolder.mPhoto_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnAddPicListener.onAddPicClick(0, viewHolder.getAdapterPosition());
                }
            });
            viewHolder.mPhoto_del.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.mPhoto_del.setVisibility(View.VISIBLE);
            viewHolder.mPhoto_del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnAddPicListener.onAddPicClick(1, viewHolder.getAdapterPosition());
                }
            });

            Glide.with(mContext)
                    .load(mUris.get(position))
                    .crossFade()
                    .into(viewHolder.mPhoto_image);
        }
    }
}

