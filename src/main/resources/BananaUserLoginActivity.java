package com.lingyue.banana.activities;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.lingyue.banana.R;
import com.lingyue.banana.infrastructure.YqdBaseActivity;
import com.lingyue.generalloanlib.commons.YqdLoanConstants;
import com.lingyue.generalloanlib.debug.DebugUtils;
import com.lingyue.generalloanlib.infrastructure.PageRoutes;
import com.lingyue.generalloanlib.models.EventLoginOrRegisterEnd;
import com.lingyue.generalloanlib.models.LoginState;
import com.lingyue.generalloanlib.models.response.UserResponse;
import com.lingyue.generalloanlib.module.user.YqdChangeLoginMobileNumberActivity;
import com.lingyue.generalloanlib.network.YqdObserver;
import com.lingyue.generalloanlib.utils.YqdHeaderUtils;
import com.lingyue.generalloanlib.widgets.editTextBridge.separatorClearableEditText.MobileEditText;
import com.lingyue.supertoolkit.resourcetools.DeviceUtils;
import com.lingyue.supertoolkit.resourcetools.SharedPreferenceUtils;
import com.lingyue.supertoolkit.widgets.BaseUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import javax.swing.text.View;

/**
 * 密码登录页
 */
@Route(path = PageRoutes.User.PASSWORD_LOGIN_ACTIVITY)
public class BananaUserLoginActivity extends YqdBaseActivity {

  @BindView(R.id.et_phone_number) MobileEditText etPhoneNumber;
  @BindView(R.id.et_password) EditText etPassword;
  @BindView(R.id.ll_change_mobile_number) LinearLayout llChangeMobileNumber;

  public static void startLoginActivity(Context context) {
    Intent intent = new Intent(context, BananaUserLoginActivity.class);
    context.startActivity(intent);
  }

  @Override
  protected int getLayoutID() {
    return R.layout.layout_banana_user_login;
  }

  @Override
  protected void initView() {
    String phoneNumber = SharedPreferenceUtils.getString(this,
        YqdLoanConstants.LOCAL_STORAGE_KEY_USER_MOBILE, "");

    if (!TextUtils.isEmpty(phoneNumber)) {
      etPhoneNumber.setText(phoneNumber);
      etPassword.requestFocus();
    } else {
      etPhoneNumber.requestFocus();
    }

    if (userGlobal.updateContactMobileAvailable) {
      llChangeMobileNumber.setVisibility(View.VISIBLE);
    } else {
      llChangeMobileNumber.setVisibility(View.GONE);
    }
  }

  @Override
  protected void initData() {

  }

  private void sendRequestUserLogin() {
    showLoadingDialog();
    userGlobal.mobileNumber = etPhoneNumber.getTrimmedText();

    apiHelper.getRetrofitApiHelper()
        .login(YqdHeaderUtils.getEnvironmentInfoV2(this, gson),
            userGlobal.mobileNumber,
            etPassword.getText().toString(),
            DeviceUtils.getDeviceId(this)
        ).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new YqdObserver<UserResponse>(this) {
          @Override
          public void onSuccess(UserResponse response) {
            dismissLoadingDialog();
            processUserLoginResponse(response);
          }

          @Override
          protected void onError(Throwable throwable, UserResponse result) {
            super.onError(throwable, result);
            dismissLoadingDialog();
          }
        });
  }

  private void processUserLoginResponse(UserResponse response) {
    userGlobal.updateUserInfo(response);
    SharedPreferenceUtils.saveString(this,
        YqdLoanConstants.LOCAL_STORAGE_KEY_USER_MOBILE, etPhoneNumber.getTrimmedText());
    //测试环境缓存登录过的手机号用于一键登录
    DebugUtils.saveMobileNumber(this, etPhoneNumber.getTrimmedText());
    EventBus.getDefault().post(new EventLoginOrRegisterEnd(LoginState.LOGIN_SUCCESS));
    finish();
  }

  @OnClick(R.id.tv_forget_password)
  public void doUserForgetPassword() {
    if (BaseUtils.isFastClick()) {
      return;
    }
    BananaForgetPasswordStepOneActivity.startForgetActivity(this, etPhoneNumber.getTrimmedText());
  }

  @OnClick(R.id.tv_registration)
  public void doUserRegister() {
    if (BaseUtils.isFastClick()) {
      return;
    }
    ARouter.getInstance()
        .build(PageRoutes.User.LOGIN_ACTIVITY)
        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP)
        .navigation(this);
  }

  @OnClick(R.id.btn_login)
  public void userLogin() {
    if (BaseUtils.isFastClick()) {
      return;
    }

    if (etPhoneNumber.getTrimmedText().length() != 11) {
      BaseUtils.showErrorToast(this, "请输入11位手机号");
      return;
    }

    if (etPassword.getText().length() < 6 || etPassword.getText().length() > 32) {
      BaseUtils.showErrorToast(this, "请输入6~32位密码");
      return;
    }

    sendRequestUserLogin();
  }

  @SuppressWarnings("unused")
  @OnClick(R.id.tv_change_mobile_number)
  public void onChangeMobileNumberClicked() {
    YqdChangeLoginMobileNumberActivity.startActivity(this);
  }

  @Override
  protected boolean isNeedToUseEventBus() {
    return true;
  }

  @SuppressWarnings("unused")
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onEventRegisterSuccess(EventLoginOrRegisterEnd event) {
    finish();
  }


  class Listener implements View.OnClickListener {
    @Override
    public void onClick(View view) {

    }
  }
}
