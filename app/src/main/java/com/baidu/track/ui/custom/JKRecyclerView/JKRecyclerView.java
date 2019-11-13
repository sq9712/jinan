package com.baidu.track.ui.custom.JKRecyclerView;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * 实现上拉加载更多的RecyclerView
 */

public class JKRecyclerView extends RecyclerView {

    LinearLayoutManager jkLayoutManager;
    LoadMoreListener loadMoreListener;


    private int currentPage = 1;
    boolean isLoading = false;
    int lastFirst = -1;

    public JKRecyclerView(Context context) {
        super(context);
        init();
    }

    public JKRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {


        //添加滑动监听
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int visibleCount = getChildCount();
                int totalCount = jkLayoutManager.getItemCount();
                int firstVisible = jkLayoutManager.findFirstVisibleItemPosition();

                if (!isLoading && (totalCount - visibleCount) <= firstVisible && lastFirst != firstVisible) {
                    currentPage++;
                    isLoading = true;
                    loadMoreListener.onLoadMore(currentPage);
                }
                lastFirst = firstVisible;


            }
        });
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        this.jkLayoutManager = (LinearLayoutManager) layout;
    }

    public void setLoadMoreListener(LoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public interface LoadMoreListener {
        void onLoadMore(int currentPage);
    }


    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}
