package com.yueyue.readhub.base;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yueyue.readhub.base.mvp.IPresenter;
import com.yueyue.readhub.base.mvp.IView;

import butterknife.ButterKnife;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * author : yueyue on 2018/4/3 17:26
 * desc   :
 */
public abstract class BaseFragment<P extends IPresenter>
        extends SupportFragment
        implements IView<P> {

    private P mPresenter;
    private boolean mIsPresenterNeeded = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getFragmentLayout(), container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        detachPresenter();
    }

    @Override
    public P getPresenter() {
        if (!mIsPresenterNeeded) {
            throw new RuntimeException(
                    "This fragment hasn't attached any presenters,if the presenter is needed,please invoke attachPresenter() first.");
        }
        return mPresenter;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void attachPresenter(@NonNull P presenter) {
        mIsPresenterNeeded = true;
        mPresenter = presenter;
        presenter.attachView(this);
    }

    @Override
    public void detachPresenter() {
        if (mPresenter == null) return;
        mPresenter.detachView();
        mPresenter = null;
    }

    @LayoutRes
    public abstract int getFragmentLayout();
}
