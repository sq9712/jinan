package com.baidu.track.ui.custom.JKRecyclerView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.track.R;
import com.baidu.track.data.Task;

import java.util.List;

/**
 * 自定义适配器
 */

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Context mContext;
    //列表数据
    private List<Task> mDatas;
    //上一次被划开的数据项，本次点击后自动返回
    private View lastSwipeView;
    private int lastSwiptPos;

    //按钮滑动状态监听，主要是和主页面侧滑菜单解决滑动冲突
    private onSwipeMenuListener swipeMenuListener;


    //列表项和按钮的点击监听
    private onItemClickListener itemClickListener;

    public TaskAdapter(Context context, List<Task> data) {
        mContext = context;
        mDatas = data;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TaskViewHolder holder = new TaskViewHolder(LayoutInflater.from(
                mContext).inflate(R.layout.lesson_item, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        Task modle = mDatas.get(position);
        //holder.les_image.setImageBitmap(modle.getImage());
        holder.les_name.setText(modle.getTaskName());
        holder.les_des.setText(modle.getContent());
        if(modle.getState().equals("2")){
            holder.les_statu.setText("处理完成");
            holder.les_statu.setBackgroundResource(R.drawable.finished_icon);
        }else if(modle.getState().equals("1")){
            holder.les_statu.setText("配合责任");
            holder.les_statu.setBackgroundResource(R.drawable.finish_icon);
        }else if(modle.getState().equals("0")){
            holder.les_statu.setText("未处理");
            holder.les_statu.setBackgroundResource(R.drawable.unfinish_icon);
        }else{
            holder.les_statu.setText("处理中");
            holder.les_statu.setBackgroundResource(R.drawable.finishing_icon);
        }


    }

    public void updateData(List<Task> data) {
        mDatas = data;
        notifyDataSetChanged();
    }

    public void setSwipeMenuListener(onSwipeMenuListener swipeMenuListener) {
        this.swipeMenuListener = swipeMenuListener;
    }


    public void setItemClickListener(onItemClickListener itemClickListenerr) {
        this.itemClickListener = itemClickListenerr;

    }


    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        //布局控件
        //ImageView les_image;
        TextView les_name;
        TextView les_des;
        TextView les_statu;
        ViewGroup les_content;
        boolean opened = false;


       TaskViewHolder(final View view) {
            super(view);

            //获取列表项控件
            les_name = (TextView) view.findViewById(R.id.lesson_name);
            les_des = (TextView) view.findViewById(R.id.lesson_des);
            les_statu = view.findViewById(R.id.lesson_statu);
            les_content = (ViewGroup) view.findViewById(R.id.lesson_content);

            //绑定相关监听
            view.setClickable(true);
            view.setOnTouchListener(new SwipeTouchListener());

        }


        class SwipeTouchListener implements View.OnTouchListener {
            int startPos, scrollStartX, distance;
            //最小滑动距离，如果滑动的距离小于此值，则判定为点击事件

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                int scrollX = v.getScrollX();

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        if (lastSwipeView != null) {
                            if (lastSwiptPos != getAdapterPosition())
                                lastSwipeView.scrollTo(0, 0);
                        }
                        lastSwiptPos = getAdapterPosition();
                        lastSwipeView = v;

                        distance = 0;
                        scrollStartX = scrollX;
                        startPos = (int) event.getX();
                        opened = false;

                        break;
                    case MotionEvent.ACTION_UP:
                        swipeMenuListener.open();
                            itemClickListener.itemClick(getAdapterPosition());

                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return true;
            }
        }
    }
}