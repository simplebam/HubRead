package com.yueyue.readhub.feature.more;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.bugtags.library.Bugtags;
import com.yueyue.readhub.R;
import com.yueyue.readhub.base.BaseFragment;
import com.yueyue.readhub.common.Constant;
import com.yueyue.readhub.common.utils.AlipayUtil;
import com.yueyue.readhub.common.utils.ToastUtils;
import com.yueyue.readhub.common.utils.VersionUtil;
import com.yueyue.readhub.feature.common.WebViewFragment;
import com.yueyue.readhub.feature.main.MainActivity;
import com.yueyue.readhub.feature.main.MainFragment;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * author : yueyue on 2018/4/4 19:59
 * desc   :
 */
public class MoreFragment extends BaseFragment {
    public static final String TAG = MoreFragment.class.getSimpleName();

    public static MoreFragment newInstance() {
        return new MoreFragment();
    }

    @BindView(R.id.scroll_view)
    NestedScrollView mScrollView;
    @BindView(R.id.txt_app_info)
    TextView mTxtAppInfo;

    @Override
    public int getFragmentLayout() {
        return R.layout.fragment_more;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String version = VersionUtil.getAppVersionName(getContext());
        if (!TextUtils.isEmpty(version)) {
            mTxtAppInfo.setText(getString(R.string.app_info_format, version));
        }
    }

    @OnClick({R.id.btn_go_readhub_page, R.id.img_portrait})
    void goReadhubPage() {
        ((MainActivity) getContext()).findFragment(MainFragment.class)
                .start(WebViewFragment.newInstance(Constant.READHUB_PAGE_URL));
    }

    @OnClick({R.id.btn_check_update, R.id.txt_app_info})
    void checkUpdate() {
        VersionUtil.checkVersion(getContext());
    }

    @OnClick(R.id.btn_submit_issue)
    void submitIssue() {
        EditText editText = new EditText(getContext());
        editText.setHint("请输入反馈信息");
        ViewGroup.LayoutParams parmas = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(parmas);
        editText.setCursorVisible(true);


        new AlertDialog.Builder(getActivity())
                .setView(editText)
                .setCancelable(true)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (Dialog, which) -> {
                    String feedBack = editText.getText().toString().trim();
                    if (TextUtils.isEmpty(feedBack)) return;
                    Bugtags.sendFeedback(feedBack);
                    ToastUtils.showShort("很感谢你的反馈");
                })
                .show();

    }

    @OnClick(R.id.btn_source_url)
    void openSourceUrl() {
        Uri uri = Uri.parse(Constant.SOURCE_URL);   //指定网址
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);           //指定Action
        intent.setData(uri);                            //设置Uri
        startActivity(intent);        //启动Activity
    }

    @OnClick(R.id.btn_donate_me)
    void donateMe() {
        if (!AlipayUtil.hasInstalledAlipayClient()) {
            ToastUtils.showShort(R.string.have_no_alipay_client);
            return;
        }
        AlipayUtil.startAlipayClient(getActivity(), Constant.ALI_PAY);
        ToastUtils.showShort(R.string.openning_alipay_client);
    }

    public void onTabClick() {
        mScrollView.smoothScrollTo(0, 0);
    }
}

