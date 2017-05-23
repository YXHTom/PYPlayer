package com.neuroandroid.pyplayer.base;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.gson.Gson;
import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.utils.ColorUtils;
import com.neuroandroid.pyplayer.utils.SystemUtils;
import com.neuroandroid.pyplayer.widget.LoadingLayout;
import com.neuroandroid.pyplayer.widget.TitleBar;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by NeuroAndroid on 2017/3/8.
 */

public abstract class BaseActivity<T extends IBasePresenter> extends AppCompatActivity implements IBaseView<T> {
    /**
     * 把 LoadingLayout 放在基类统一处理，@Nullable 表明 View 可以为 null
     */
    @Nullable
    @BindView(R.id.loading_layout)
    LoadingLayout mLoadingLayout;

    @Nullable
    @BindView(R.id.title_bar)
    TitleBar mTitleBar;

    protected T mPresenter;
    private Unbinder mUnbinder;
    /**
     * 是否支持沉浸式状态栏
     */
    public boolean mImmersive;
    protected Gson mGson;
    protected Intent mIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(attachLayout());
        mGson = new Gson();
        mIntent = new Intent();
        mImmersive = SystemUtils.setTranslateStatusBar(this);
        if (useEventBus())
            EventBus.getDefault().register(this);
        mUnbinder = ButterKnife.bind(this);
        initPresenter();
        initView();
        initData();
        initListener();
    }

    /**
     * 绑定布局文件
     *
     * @return 布局文件ID
     */
    @LayoutRes
    protected abstract View attachLayout();

    protected void initPresenter() {
    }

    protected abstract void initView();

    protected void initData() {
    }

    protected void initListener() {
    }

    @Override
    public void showLoading() {
        if (mLoadingLayout != null) {
            mLoadingLayout.setStatus(LoadingLayout.STATUS_LOADING);
        }
    }

    @Override
    public void hideLoading() {
        if (mLoadingLayout != null) {
            mLoadingLayout.hide();
        }
    }

    @Override
    public void showError(LoadingLayout.OnRetryListener onRetryListener) {
        if (mLoadingLayout != null) {
            mLoadingLayout.setStatus(LoadingLayout.STATUS_NO_NET);
            mLoadingLayout.setOnRetryListener(onRetryListener);
        }
    }

    @Override
    public void setPresenter(T presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showTip(String tip) {
    }

    protected void initTitleBar(CharSequence title) {
        if (mTitleBar != null) {
            mTitleBar.setImmersive(mImmersive);
            mTitleBar.setTextColor(Color.WHITE);
            mTitleBar.setTitle(title);
        }
    }

    protected void initLeftAction(TitleBar.Action action) {
        if (mTitleBar != null) {
            mTitleBar.addLeftAction(action);
        }
    }

    protected void initRightAction(TitleBar.Action action) {
        if (mTitleBar != null) {
            mTitleBar.addRightAction(action);
        }
    }

    protected void setBackgroundColor(int color) {
        if (getTitleBar() != null) {
            getTitleBar().setBackgroundColor(color);
        }
    }

    protected TitleBar getTitleBar() {
        return mTitleBar;
    }

    /**
     * 是否使用EventBus
     */
    protected boolean useEventBus() {
        return false;
    }

    /**
     * 更改"最近用过"屏幕中活动的视觉属性，如活动的颜色、标签和图标
     */
    public void setTaskDescriptionColor(@ColorInt int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            color = ColorUtils.stripAlpha(color);
            setTaskDescription(new ActivityManager.TaskDescription((String) getTitle(), null, color));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放资源
        if (mPresenter != null) mPresenter.onDestroy();
        if (mUnbinder != Unbinder.EMPTY) mUnbinder.unbind();
        if (useEventBus()) EventBus.getDefault().unregister(this);
        this.mUnbinder = null;
        this.mPresenter = null;
    }
}
