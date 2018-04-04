package com.yueyue.readhub.base;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.yueyue.readhub.R;
import com.yueyue.readhub.base.mvp.INetworkView;

import java.util.List;

import butterknife.BindView;
import me.yokeyword.fragmentation.SupportActivity;
import mehdi.sakout.dynamicbox.DynamicBox;

/**
 * author : yueyue on 2018/4/4 18:53
 * desc   :
 */
public abstract class BaseListFragment<D> extends BaseFragment<BaseListPresenter> implements
        INetworkView<BaseListPresenter, List<D>> {
    private static final int VIEW_TYPE_LAST_ITEM = 1;

    private SupportActivity mActivity;
    public DynamicBox mBox;
    boolean hasMore = true;

    @BindView(R.id.fab)
    FloatingActionButton mFAB;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.frame_list_container)
    FrameLayout mFrameContainer;

    private BaseAdapter<D> mAdapter = new BaseAdapter<D>() {
        @SuppressWarnings("unchecked")
        @Override
        public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_LAST_ITEM) {
                return new LoadingViewHolder(parent, hasMore());
            }
            return provideViewHolder(parent, viewType);
        }

        @Override
        public int getItemCount() {
            //有数据时，显示 loadingViewHolder
            return super.getItemCount() == 0 ? 0 : super.getItemCount() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1) {
                return VIEW_TYPE_LAST_ITEM;
            }
            return super.getItemViewType(position);
        }

    };
    private LinearLayoutManager mManager;

    @Override
    public int getFragmentLayout() {
        return R.layout.fragment_base_list;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (SupportActivity) context;
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        attachPresenter(createPresenter());
        initContent();
        if (mAdapter.getItemCount() == 0) requestData();
        setRetainInstance(true);
    }


    private void initContent() {
        mBox = new DynamicBox(mActivity, mFrameContainer);
        mBox.setClickListener(v -> {
            //加载失败时的按钮点击事件
            mAdapter.clear();
            requestData();
        });

        mRecyclerView.setAdapter(mAdapter);
        mManager = new LinearLayoutManager(mActivity);
        mRecyclerView.setLayoutManager(mManager);
        mFAB.setOnClickListener(v -> {
            //滑动到首页
            mManager.smoothScrollToPosition(mRecyclerView, null, 0);
        });

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mAdapter.clear();
            requestData();
        });

        mSwipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.color_607D8B), Color.BLACK, Color.BLUE);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mFAB.setVisibility(
                        (mManager.findFirstVisibleItemPosition() > 0) ? View.VISIBLE : View.GONE);
                if (!mSwipeRefreshLayout.isRefreshing() && hasMore() &&
                        mManager.findLastVisibleItemPosition() == mAdapter.getItemCount() - 1) {
                    requestMore();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(List datas) {
        if (mSwipeRefreshLayout.isRefreshing()) mSwipeRefreshLayout.setRefreshing(false);
        mBox.hideAll();
        if (datas != null && datas.size() != 0) mAdapter.addItems(datas);
        if (mAdapter.getItemCount() != 0) {
            RecyclerView.ViewHolder viewHolder =
                    mRecyclerView.findViewHolderForAdapterPosition(mAdapter.getItemCount() - 1);
            if (viewHolder instanceof LoadingViewHolder) {
                ((LoadingViewHolder) viewHolder).bindTo(hasMore());
            } else {
                mAdapter.notifyItemChanged(mAdapter.getItemCount() - 1);
            }
        }
    }

    @Override
    public void onError(Throwable e) {
        mBox.setOtherExceptionMessage(e.getMessage());
        mBox.showExceptionLayout();
    }

    private void requestData() {
        mBox.showLoadingLayout();
        hasMore = true;
        getPresenter().start();
    }

    private void requestMore() {
        mSwipeRefreshLayout.setRefreshing(true);//同时防止多次 requestMore
        getPresenter().startRequestMore();
    }

    public boolean hasMore() {
        return hasMore;
    }

    public void onTabClick() {
        if (mManager.findFirstCompletelyVisibleItemPosition() != 0) {
            mRecyclerView.smoothScrollToPosition(0);
            return;
        }
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
            mAdapter.clear();
            requestData();
        }
    }

    public abstract BaseViewHolder<D> provideViewHolder(ViewGroup parent, int viewType);

    public abstract BaseListPresenter<D> createPresenter();
}

