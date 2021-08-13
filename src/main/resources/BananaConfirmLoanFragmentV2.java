package com.lingyue.banana.modules.loan.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.lingyue.banana.BuildConfig;
import com.lingyue.banana.R;
import com.lingyue.banana.activities.RenbaoFeeConfirmActivity;
import com.lingyue.banana.adapters.BankCardsAdapter;
import com.lingyue.banana.authentication.activities.YqdBindBankCardActivityV3;
import com.lingyue.banana.common.dialog.BananaDialogChain;
import com.lingyue.banana.common.dialog.CouponDialogBuilder;
import com.lingyue.banana.infrastructure.BananaUmengEvent;
import com.lingyue.banana.infrastructure.YqdBaseFragment;
import com.lingyue.banana.models.UserGlobal;
import com.lingyue.banana.models.request.CouponInfoResponse;
import com.lingyue.banana.models.response.BananaCreditsStatus;
import com.lingyue.banana.modules.loan.BaseVipCardView;
import com.lingyue.banana.modules.loan.ConfirmLoanCreditAuthDialog;
import com.lingyue.banana.modules.loan.ForceOpenVipConfirmDialog;
import com.lingyue.banana.modules.loan.LoanAmountFilter;
import com.lingyue.banana.modules.loan.NewRepayPlanDialog;
import com.lingyue.banana.modules.loan.OpenVipRetainDialog;
import com.lingyue.banana.modules.loan.OpenVipRetainDialogV2;
import com.lingyue.banana.modules.loan.OpenVipSecondRetainDialog;
import com.lingyue.banana.modules.loan.RepayPlanDialog;
import com.lingyue.banana.modules.loan.VipCardView;
import com.lingyue.banana.modules.loan.VipCardViewV2;
import com.lingyue.banana.modules.loan.YqdLoanResultActivity;
import com.lingyue.banana.modules.loan.dialog.ConfirmLoanExitDialog;
import com.lingyue.banana.utilities.YqdUtils;
import com.lingyue.bananalibrary.common.imageLoader.Imager;
import com.lingyue.generalloanlib.commons.YqdLoanConstants;
import com.lingyue.generalloanlib.commons.YqdStatisticsEvent;
import com.lingyue.generalloanlib.infrastructure.YqdBuildConfig;
import com.lingyue.generalloanlib.interfaces.ICommonPicDialogData;
import com.lingyue.generalloanlib.interfaces.ICouponDialogInfo;
import com.lingyue.generalloanlib.interfaces.IIncreaseCoupon;
import com.lingyue.generalloanlib.models.AuthScene;
import com.lingyue.generalloanlib.models.BaseLoanBankCard;
import com.lingyue.generalloanlib.models.CanCreateOrderStatusEnum;
import com.lingyue.generalloanlib.models.CommonOption;
import com.lingyue.generalloanlib.models.CouponTipBar;
import com.lingyue.generalloanlib.models.DialogInfo;
import com.lingyue.generalloanlib.models.EventSelectCoupon;
import com.lingyue.generalloanlib.models.HxcgActionProviderEnum;
import com.lingyue.generalloanlib.models.HxcgUmengPointEnum;
import com.lingyue.generalloanlib.models.InsuranceItem;
import com.lingyue.generalloanlib.models.LoanCoupon;
import com.lingyue.generalloanlib.models.LoanCouponEnum;
import com.lingyue.generalloanlib.models.LoanCouponVO;
import com.lingyue.generalloanlib.models.LoanRate;
import com.lingyue.generalloanlib.models.OrderConfirmInfoItem;
import com.lingyue.generalloanlib.models.OrderConfirmInfoUrlType;
import com.lingyue.generalloanlib.models.PopupData;
import com.lingyue.generalloanlib.models.ProductConfig;
import com.lingyue.generalloanlib.models.ProviderDetail;
import com.lingyue.generalloanlib.models.ProviderInfo;
import com.lingyue.generalloanlib.models.request.CreateOrderInfo;
import com.lingyue.generalloanlib.models.response.CanCreateOrderResponse;
import com.lingyue.generalloanlib.models.response.CashLoanCreateOrderResponse;
import com.lingyue.generalloanlib.models.response.CreditDialogResponse;
import com.lingyue.generalloanlib.models.response.LoanConfirmInfoResponse;
import com.lingyue.generalloanlib.models.response.YqdBaseResponse;
import com.lingyue.generalloanlib.models.response.YqdBooleanResponse;
import com.lingyue.generalloanlib.module.arouter.UriHandler;
import com.lingyue.generalloanlib.network.YqdObserver;
import com.lingyue.generalloanlib.utils.LoanUriUtil;
import com.lingyue.generalloanlib.utils.ThirdPartEventUtils;
import com.lingyue.generalloanlib.utils.YqdHeaderUtils;
import com.lingyue.generalloanlib.widgets.MultiLineRadioGroup;
import com.lingyue.generalloanlib.widgets.dialog.BaseDialog;
import com.lingyue.generalloanlib.widgets.dialog.BottomCommonOptionSelectDialog;
import com.lingyue.generalloanlib.widgets.dialog.BottomSingleColumnSelectDialog;
import com.lingyue.generalloanlib.widgets.dialog.CommonPicDialog;
import com.lingyue.supertoolkit.customtools.CollectionUtils;
import com.lingyue.supertoolkit.customtools.Logger;
import com.lingyue.supertoolkit.formattools.SpannableUtils;
import com.lingyue.supertoolkit.formattools.TimeUtils;
import com.lingyue.supertoolkit.functiontools.KeyboardStateChangeAssistant;
import com.lingyue.supertoolkit.widgets.BaseUtils;
import com.yangqianguan.statistics.StatisticsTextWatcher;
import com.yangqianguan.statistics.autotrack.TrackDataApi;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import br.tiagohm.markdownview.MarkdownView;
import br.tiagohm.markdownview.css.InternalStyleSheet;
import br.tiagohm.markdownview.css.styles.Github;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import kotlin.Unit;
import retrofit2.Response;

import static com.lingyue.generalloanlib.commons.YqdLoanConstants.INTENT_KEY_ISVICEORDER;
import static com.lingyue.generalloanlib.models.response.LoanConfirmInfoResponse.PICKER_CONTROL;
import static com.lingyue.generalloanlib.models.response.LoanConfirmInfoResponse.RADIOBUTTON_CONTROL;
import static com.lingyue.supertoolkit.widgets.BaseUtils.isFastClick;

/**
 * 新版借款确认页
 * 改版时间 2021年05月
 * 对应Task https://code.yangqianguan.com/T50124
 */
public class BananaConfirmLoanFragmentV2 extends YqdBaseFragment {

  @BindView(R.id.rg_term_radio) MultiLineRadioGroup rgTermCheck;
  @BindView(R.id.nsv_wrapper) NestedScrollView nsvWrapper;
  @BindView(R.id.et_loan_amount) EditText etLoanAmount;
  @BindView(R.id.rl_term_pick) RelativeLayout rlTermPick;
  @BindView(R.id.tv_loan_range) TextView tvLoanRange;
  @BindView(R.id.tv_loan_period) TextView tvLoanPeriod;
  @BindView(R.id.btn_loan_confirm) Button btnConfirm;
  @BindView(R.id.tv_bank_card_number) TextView tvBankCardNumber;
  @BindView(R.id.tv_loan_use) TextView tvLoanUse;
  @BindView(R.id.rl_loan_use) RelativeLayout rlLoanUse;
  @BindView(R.id.ll_protocol) LinearLayout llProtocol;
  @BindView(R.id.cb_protocol) CheckBox cbProtocol;
  @BindView(R.id.mk_loan_protocol) MarkdownView mkLoanProtocol;
  @BindView(R.id.tv_insurance_warn_info) TextView tvInsuranceWarnInfo;
  @BindView(R.id.tv_insurance_fee) TextView tvInsuranceFee;
  @BindView(R.id.tv_insurance_auth) TextView tvInsuranceAuth;
  @BindView(R.id.tv_reject_insurance_fee) TextView tvRejectInsuranceFee;
  @BindView(R.id.tv_risk_warning) TextView tvRiskWarning;
  @BindView(R.id.tv_coupon) TextView tvCouponContent;
  @BindView(R.id.tv_funding_label) TextView tvFundingLabel;
  @BindView(R.id.iv_funding_logo) ImageView ivFundingLogo;
  @BindView(R.id.tv_funding) TextView tvFunding;
  @BindView(R.id.rl_funding) RelativeLayout rlFunding;
  @BindView(R.id.ll_insurance_card) LinearLayout llInsuranceCard;
  @BindView(R.id.ll_detail_card) LinearLayout llDetailCard;
  @BindView(R.id.ll_period_card) LinearLayout llPeriodCard;
  @BindView(R.id.rl_vip_container) RelativeLayout rlVipContainer;
  @BindView(R.id.tv_vip_content) TextView tvVipContent;
  @BindView(R.id.fl_vip_card_container) FrameLayout flVipCardContainer;
  @BindView(R.id.rl_rate_container) RelativeLayout rlRateContainer;
  @BindView(R.id.tv_rate_label) TextView tvRateLabel;
  @BindView(R.id.iv_rate_info) ImageView ivRateInfo;
  @BindView(R.id.tv_rate) AppCompatTextView tvRate;
  @BindView(R.id.tv_real_rate) AppCompatTextView tvRealRate;
  @BindView(R.id.tv_rate_discount) TextView tvRateDiscount;
  @BindView(R.id.tv_funding_tips) TextView tvFundingTips;
  @BindView(R.id.ll_funding_dynamic) LinearLayout llFundingDynamic;
  @BindView(R.id.rl_tip_bar_container) RelativeLayout rlTipBarContainer;
  @BindView(R.id.tv_tip_bar) TextView tvTipBar;
  @BindView(R.id.btn_amount) Button btnAmount;
  @BindView(R.id.tv_title) TextView tvTitle;
  @BindView(R.id.btn_navigation) ImageView ivNavigation;
  @BindView(R.id.tv_repay_label) TextView tvRepayLabel;
  @BindView(R.id.tv_repay_content) TextView tvRepayContent;
  @BindView(R.id.ll_middle_dynamic) LinearLayout llMiddleDynamic;
  @BindView(R.id.tv_coupon_tip_bar) TextView tvCouponTipBar;

  private static final String BUNDLE_KEY_CREATE_ORDER_INFO = "createOrderInfo";
  protected List<BaseLoanBankCard> repayBankCards;
  protected BaseLoanBankCard selectedBankCard;
  /**
   * 创建订单后的缓存数据
   * <p>
   * 由于在不保留活动状态下页面数据存在变化的可能性，所以调用接口时请不要直接从页面取值
   */
  protected CreateOrderInfo createOrderInfo;
  /**
   * 分流试验样式选择
   */
  protected String termDisplayType = PICKER_CONTROL;
  /**
   * 是否需要返回默认优惠券，仅在第一次试算时默认返回优惠券
   */
  protected boolean isNeedDefaultCoupon = true;
  /**
   * 是否是副单
   */
  boolean isViceOrder = false;

  public BigDecimal customUpperLimit;

  public BigDecimal customLowestLimit;

  private BaseVipCardView vipCardView;
  private boolean isCloseTipBar = false;
  /**
   * 当前有效的最大可借额度
   */
  private BigDecimal maxValidAmount;
  private BigDecimal minValidAmount;
  private LoanConfirmInfoResponse.Body trialResult;
  private LoanConfirmInfoResponse.AmountButton amountButton;
  private final List<String> loanTermList = new ArrayList<>();
  private final LinkedHashMap<String, ProductConfig> loanTrialConfigs = new LinkedHashMap<>();
  private boolean loanProtocolSwitch;
  /**
   * 接口返回的本次数据是依赖多少本金计算得出的，用来做校验
   */
  private String responsePrincipal;
  private boolean isHideSoftInputByCoding;
  private BottomCommonOptionSelectDialog selectLoanUseDialog;
  private final List<CommonOption> loanUseList = new ArrayList<>();
  private CommonOption selectedLoanUse;
  /**
   * 前几项展示标志位,默认展示3条
   */
  private boolean isShowLoanUse;
  private String protocolReadNotification;
  private BottomSingleColumnSelectDialog selectLoanTermBottomSheetDialog;
  private ConfirmLoanCreditAuthDialog confirmLoanCreditAuthDialog;
  private InsuranceItem selectedInsuranceItem;
  private final Set<String> authedBankCard = new HashSet<>();
  private boolean isOnSupplementAuthentication;
  /**
   * 用户未添加银行卡时点击立即借款，绑卡成功后继续之前的操作，避免多一次点击影响转化率
   */
  private boolean isContinueToLoan = false;
  private IIncreaseCoupon increaseLoanAmountCoupon;
  /**
   * 当前产品使用的提额券Id，如果借款金额较小未使用到提额券时，为null
   */
  private String currentCouponId;
  private String couponListPageUrl;
  private String currentSelectedTermName;
  private int currentSelectedTerm;
  private LoanCouponVO loanCouponVO;
  private ProductConfig currentSelectedProductConfig;
  private boolean isOpenVip;
  private LoanAmountFilter loanAmountInputFilter;
  private int sendLoanTrialRequestTime;
  /**
   * 每次试算仅展示一次amountTip提示内容
   */
  private boolean isAmountTipShown;
  private Boolean lastKeyBoardChangeResult;

  private BananaDialogChain dialogChain;
  private CountDownTimer couponCountDownTimer;
  /**
   * 试算接口请求返回的时间，作为优惠券倒计时的起始时间
   */
  private long requestTime;

  @Override
  protected void init() {
    EventBus.getDefault().register(this);
  }

  @Override
  protected void handleArguments() {
    if (getArguments() != null) {
      customUpperLimit = (BigDecimal) getArguments().getSerializable(YqdLoanConstants.INTENT_KEY_CUSTOM_UPPERLIMIT);
      customLowestLimit = (BigDecimal) getArguments().getSerializable(YqdLoanConstants.INTENT_KEY_CUSTOM_LOWESTLIMIT);
      isViceOrder = getArguments().getBoolean(INTENT_KEY_ISVICEORDER, false);
    }
  }

  protected BigDecimal getUpperLimit() {
    if (customUpperLimit != null) {
      return customUpperLimit;
    } else {
      return userGlobal.upperLimit;
    }
  }

  protected BigDecimal getLowestLimit() {
    if (customLowestLimit != null) {
      return customLowestLimit;
    } else {
      return userGlobal.lowestLimit;
    }
  }

  protected InternalStyleSheet makeMarkdownStyle() {
    InternalStyleSheet styleSheet = new Github();
    styleSheet.addRule("body",
        "font-size:12px;",
        "background-color:#f0f2fa",
        "line-height:1.4",
        "padding: 5px 0 0 0",
        "color:#8d8ea6");
    styleSheet.addRule("a",
        "color:#4E37E6",
        "-webkit-tap-highlight-color:rgba(78,55,230,0.6)");
    return styleSheet;
  }

  protected boolean isYqgDomain(String url) {
    return YqdUtils.isYqgDomain(url);
  }

  protected Observable<Response<LoanConfirmInfoResponse>> makeLoanTrialObservable(String amount, String couponId, String lookingAtTerm) {
    return commonApiHelper.getRetrofitApiHelper()
        .getLoanTrialInfo(amount, etLoanAmount.getText().toString(),
            couponId, isViceOrder, isNeedDefaultCoupon, lookingAtTerm, getUpperLimit().toString());
  }

  protected void sendUploadCashLoanOrderInfoRequest() {
    if (createOrderInfo != null) {
      commonApiHelper.getRetrofitApiHelper()
          .uploadCashLoanOrderInfo(
              YqdHeaderUtils.getEnvironmentInfoV2(hostActivity, gson),
              null,
              createOrderInfo.loanAmount,
              createOrderInfo.bankAccountId,
              createOrderInfo.productId)
          .subscribe(new YqdObserver<YqdBaseResponse>(hostActivity) {
            @Override
            public void onSuccess(YqdBaseResponse result) {
              dismissLoadingDialog();
              authHelper.get().nextAuthStep(hostActivity);
              finish();
            }
          });
    }
  }

  protected void processLoanConfirmResponse(CashLoanCreateOrderResponse response) {
    YqdLoanResultActivity.startActivity(hostActivity, response.body);
    finish();
  }

  protected void onHXCGTradeCallBackEvent(String value) {
    ThirdPartEventUtils.onEvent(hostActivity, BananaUmengEvent.EVENT_ID_HXCG_TRADE_CALLBACK, value);
  }

  public boolean isCreditsStatusAccepted() {
    return BananaCreditsStatus.fromName(((UserGlobal) userGlobal.get()).creditsStatus) == BananaCreditsStatus.ACCEPTED;
  }

  protected void showSelectBankCardDialog() {
    final BankCardsAdapter<BaseLoanBankCard> adapter = new BankCardsAdapter<>(hostActivity, R.layout.layout_bank_list_item, repayBankCards);
    new AlertDialog.Builder(hostActivity, R.style.CommonAlertDialog)
        .setTitle("选择收款储蓄卡")
        .setAdapter(adapter, (dialog, which) -> {
          selectedBankCard = repayBankCards.get(which);
          bindBankCardData();
        }).show();
  }

  @Override
  protected int getLayoutID() {
    return R.layout.layout_new_loan_confirm_fragment;
  }

  @Override
  protected void initView() {
    if (!isLoanRangeValid()) {
      BaseUtils.showErrorToast(hostActivity, "暂无可借额度");
      finish();
      return;
    }
    initToolbar();
    initIncreaseLoanAmountCoupon();
    isShowLoanUse = userGlobal.showLoanUse;

    initLoanAmountEditText();
    refreshLoanAmountRange(getLowestLimit(), getUpperLimitWithIncreaseCoupon());

    KeyboardStateChangeAssistant keyboardStateChangeAssistant = new KeyboardStateChangeAssistant(hostActivity);
    keyboardStateChangeAssistant.setKeyboardChangeListener(isShow -> {
      //如果连续两次返回的结果一致，那么认为用户当前使用了悬浮输入框，此时该工具无法返回正确的键盘状态，所以禁用
      //监听键盘状态进行试算的逻辑，此时用户只能通过点击按钮或者键盘右下角的完成来触发试算
      if (lastKeyBoardChangeResult != null && lastKeyBoardChangeResult == isShow) {
        return;
      }
      lastKeyBoardChangeResult = isShow;
      if (!isShow && !isHideSoftInputByCoding) {
        handleInputResult(etLoanAmount.getText().toString());
      } else {
        isHideSoftInputByCoding = false;
      }
    });

    initDefaultLoanAmount();
    initMarkdownContainer();

    rgTermCheck.setOnCheckChangedListener((group, position, str) -> fillLoanTrialResult(str));

    rgTermCheck.setSpannableString(str -> {
      try {
        int terms = loanTrialConfigs.get(str).terms;
        SpannableString sStr = new SpannableString(terms + "期");
        sStr.setSpan(new AbsoluteSizeSpan(22, true), 0, String.valueOf(terms).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sStr.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, String.valueOf(terms).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sStr;
      } catch (Exception e) {
        return str;
      }
    });
    requireActivity().getOnBackPressedDispatcher()
        .addCallback(this, new OnBackPressedCallback(true) {
          @Override
          public void handleOnBackPressed() {
            onBackPressed();
          }
        });
  }

  private void initIncreaseLoanAmountCoupon() {
    increaseLoanAmountCoupon = userGlobal.increaseCoupon;
    currentCouponId = increaseLoanAmountCoupon == null ? null : increaseLoanAmountCoupon.getId();
  }

  private void initToolbar() {
    ivNavigation.setOnClickListener(v -> onBackPressed());
    tvTitle.setText("确认借款");
  }

  private void onBackPressed() {
    if (trialResult == null) {
      finish();
    } else if (!CollectionUtils.isNullOrEmpty(trialResult.newPopupList)) {
      showExitCouponDialog(trialResult.newPopupList);
    } else if (!CollectionUtils.isNullOrEmpty(trialResult.popupList)) {
      showExitDialog(trialResult.popupList);
    } else {
      finish();
    }
  }

  private void showExitCouponDialog(List<DialogInfo> dialogInfoList) {
    dialogChain = new BananaDialogChain(this, false);
    for (int i = 0; i < dialogInfoList.size(); i++) {
      boolean isLast = i == dialogInfoList.size() - 1;
      BaseDialog dialog = createDialog(dialogInfoList.get(i), isLast);
      if (dialog != null) {
        dialogChain.add(dialog);
      }
    }
    dialogChain.show();
  }

  private BaseDialog createDialog(DialogInfo dialogInfo, boolean isLast) {
    BaseDialog exitDialog = null;
    if (dialogInfo instanceof ICommonPicDialogData) {
      exitDialog = new CommonPicDialog.Builder(hostActivity)
          .setData((ICommonPicDialogData) dialogInfo)
          .setOnNegativeClickListener((dialogInterface, view, data) -> {
            if (isLast) {
              finish();
            }
          })
          .setOnPositiveClickListener((dialogInterface, view, data) -> {
            dialogChain.cancel();
          })
          .setId("dialog_confirm_loan_exit_common")
          .create();
    } else if (dialogInfo instanceof ICouponDialogInfo) {
      exitDialog = new CouponDialogBuilder(hostActivity)
          .setData(((ICouponDialogInfo) dialogInfo))
          .setOnNegativeListener((dialog, which) -> {
            onDialogBack(dialog, isLast);
            return false;
          }).setOnPositiveListener((dialog, which) -> {
            dialogChain.cancel();
            return true;
          })
          .build();
    }
    return exitDialog;
  }

  private void showExitDialog(List<PopupData> popups) {
    dialogChain = new BananaDialogChain(this, false);
    for (int i = 0; i < popups.size(); i++) {
      PopupData popupData = popups.get(i);
      ConfirmLoanExitDialog confirmLoanExitDialog = new ConfirmLoanExitDialog(hostActivity, popupData);
      boolean isLast = i == popups.size() - 1;
      confirmLoanExitDialog.setOnBackListener(dialog -> onDialogBack(dialog, isLast));
      confirmLoanExitDialog.setOnConfirmListener(this::onDialogConfirm);
      dialogChain.add(confirmLoanExitDialog);
    }
    dialogChain.show();
  }

  private Unit onDialogBack(Dialog dialog, boolean isLast) {
    dialog.dismiss();
    if (isLast) {
      finish();
    }
    return Unit.INSTANCE;
  }

  private Unit onDialogConfirm(Dialog dialog, PopupData popupData) {
    if (TextUtils.isEmpty(popupData.bottomContinueActionUrl)) {
      dialogChain.cancel();
      dialog.dismiss();
      handleInputResult(getUpperLimit().toString());
    } else {
      showLoadingDialog();
      Uri uri = Uri.parse(popupData.bottomContinueActionUrl);
      HashMap<String, String> params = new HashMap<>();
      for (String key : uri.getQueryParameterNames()) {
        params.put(key, uri.getQueryParameter(key));
      }
      apiHelper.getRetrofitApiHelper()
          .requestOfferCoupon(uri.getPath(), params)
          .subscribe(new YqdObserver<CouponInfoResponse>(hostActivity) {
            @Override
            public void onSuccess(CouponInfoResponse result) {
              dismissLoadingDialog();
              dialogChain.cancel();
              dialog.dismiss();
              requestLoanTrialInfoWithCoupon(result);
            }

            @Override
            protected void onError(Throwable throwable, CouponInfoResponse result) {
              dialogChain.cancel();
              dialog.dismiss();
              super.onError(throwable, result);
            }
          });
    }
    return Unit.INSTANCE;
  }

  private void requestLoanTrialInfoWithCoupon(CouponInfoResponse couponInfo) {
    isNeedDefaultCoupon = true;
    if (couponInfo.body != null) {
      currentCouponId = couponInfo.body.couponId;
    } else {
      currentCouponId = null;
    }
    handleInputResult(getUpperLimit().toString());
  }

  private void refreshLoanAmountRange(BigDecimal min, BigDecimal max) {
    minValidAmount = min;
    maxValidAmount = max;
    tvLoanRange.setText(String.format("可借范围%s~%s元", min.toString(), max.toString()));
    loanAmountInputFilter.setMaxLength(max.toString().length());
  }

  @Override
  public void initData() {
  }

  @Override
  protected void restoreParams(@NonNull Bundle savedInstanceState) {
    createOrderInfo = savedInstanceState.getParcelable(BUNDLE_KEY_CREATE_ORDER_INFO);
  }

  @Override
  protected void saveParams(@NonNull Bundle outState) {
    outState.putParcelable(BUNDLE_KEY_CREATE_ORDER_INFO, createOrderInfo);
  }

  private boolean isLoanRangeValid() {
    return getUpperLimit() != null && getLowestLimit() != null;
  }

  private void initDefaultLoanAmount() {
    if (getUpperLimit() != null) {
      try {
        refreshLoanAmountEditTextValue(maxValidAmount);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * @return 最大可借金额，如果使用提额券时会加上提额券对应的额度
   */
  private BigDecimal getUpperLimitWithIncreaseCoupon() {
    BigDecimal couponAmount = increaseLoanAmountCoupon == null ? BigDecimal.ZERO : increaseLoanAmountCoupon.getIncreaseAmount();
    return getUpperLimit().add(couponAmount);
  }

  private void onOrderConfirmItemClicked(OrderConfirmInfoItem infoItem) {
    if (isFastClick()) {
      return;
    }
    if (OrderConfirmInfoItem.ITEM_TYPE_COMPREHENSIVE.equals(infoItem.dialogType)) {
      //综合年化成本Item
      new RepayPlanDialog(hostActivity, currentSelectedProductConfig.expandList)
          .show();
    } else if (OrderConfirmInfoItem.ITEM_TYPE_NEW_COMPREHENSIVE.equals(infoItem.dialogType)) {
      //新综合年化成本Item
      new NewRepayPlanDialog(hostActivity, currentSelectedProductConfig.expandInfo)
          .show();
    } else {
      OrderConfirmInfoUrlType urlType = OrderConfirmInfoUrlType.fromName(infoItem.urlType);
      if (urlType == OrderConfirmInfoUrlType.WEB) {
        if (!TextUtils.isEmpty(infoItem.url)) {
          jumpToWebPage(appGlobal.serverConfig.getWebServer() +
              infoItem.url +
              "?principal=" + responsePrincipal +
              "&productId=" + getCurrentProductId());
        }
      }
    }
  }

  private void initLoanAmountEditText() {
    loanAmountInputFilter = new LoanAmountFilter(getUpperLimitWithIncreaseCoupon().toBigInteger().toString().length());
    etLoanAmount.setFilters(new InputFilter[]{loanAmountInputFilter});

    etLoanAmount.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override
      public void afterTextChanged(Editable s) {
        isLoanAmountValid(s == null ? "" : s.toString());
      }
    });

    etLoanAmount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (actionId) {
          case EditorInfo.IME_ACTION_DONE:
            handleInputResult(etLoanAmount.getText().toString());
            hideSoftInput();
            return true;
        }
        return false;
      }
    });
    etLoanAmount.addTextChangedListener(new StatisticsTextWatcher(etLoanAmount));
  }

  private void initMarkdownContainer() {
    mkLoanProtocol.addStyleSheet(makeMarkdownStyle());

    mkLoanProtocol.setWebViewClient(new WebViewClient() {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, final String url) {
        if (!TextUtils.isEmpty(url)) {
          if (isYqgDomain(url)) {
            jumpToWebPage(url +
                "?principal=" + etLoanAmount.getText().toString() +
                "&productId=" + getCurrentProductId());
          } else {
            jumpToWebPage(url);
          }
          return true;
        }
        return super.shouldOverrideUrlLoading(view, url);
      }
    });
    //屏蔽markdown的长按事件
    mkLoanProtocol.setOnLongClickListener(v -> true);
  }

  protected void processRepayBankCardListResponse(ArrayList<BaseLoanBankCard> bankAccounts) {
    if (repayBankCards == null) {
      repayBankCards = new ArrayList<>();
    }
    if (bankAccounts == null) {
      bankAccounts = new ArrayList<>();
    }
    repayBankCards.clear();
    repayBankCards.addAll(bankAccounts);

    if (repayBankCards.size() > 0) {
      selectedBankCard = repayBankCards.get(0);
      bindBankCardData();
      if (isContinueToLoan) {
        isContinueToLoan = false;
        onLoanConfirmClicked();
      }
    } else {
      tvBankCardNumber.setHint("请添加银行卡");
    }
  }

  protected void bindBankCardData() {
    tvBankCardNumber.setText(String.format("%s | 尾号%s", selectedBankCard.bankName, selectedBankCard.maskedAccountNumber));
    refreshInsuranceAuthState();
  }

  private void processLoanUse(List<CommonOption> data) {
    if (isShowLoanUse && !CollectionUtils.isEmpty(data)) {
      rlLoanUse.setVisibility(View.VISIBLE);
      loanUseList.clear();
      loanUseList.addAll(data);
      selectLoanUseDialog = null;
      refreshLoanUseUI(loanUseList.get(0));
    } else {
      rlLoanUse.setVisibility(View.GONE);
      loanUseList.clear();
      selectLoanUseDialog = null;
    }
  }

  private void refreshLoanUseUI(CommonOption selectedOption) {
    selectedLoanUse = selectedOption;
    tvLoanUse.setText(selectedLoanUse.label);
  }

  private void handleInputResult(String loanAmount) {
    handleInputResult(loanAmount, null);
  }

  private void handleInputResult(String loanAmount, String lookingAtTerm) {
    if (isLoanAmountValid(loanAmount)) {
      showLoadingDialog();
      sendLoanTrialInfoRequest(adjustLoanAmount2ValidScope(loanAmount), lookingAtTerm);
    } else {
      handleEmptyOrInvalidAmountUI();
    }
  }

  private boolean isLoanAmountValid(String amount) {
    if (TextUtils.isEmpty(amount) || new BigDecimal(amount).equals(BigDecimal.ZERO)) {
      btnConfirm.setBackgroundResource(R.drawable.btn_shape_rectangle_cfcfe6);
      handleEmptyOrInvalidAmountUI();
      return false;
    }
    btnConfirm.setBackgroundResource(R.drawable.btn_default_material_neu);
    return true;
  }

  private String adjustLoanAmount2ValidScope(String loanAmount) {
    BigDecimal amount = new BigDecimal(loanAmount);
    if (amount.compareTo(maxValidAmount) > 0) {
      return maxValidAmount.toString();
    } else if (amount.compareTo(minValidAmount) < 0) {
      return minValidAmount.toString();
    }
    return loanAmount;
  }

  private void sendLoanTrialInfoRequest(String amount, String lookingAtTerm) {
    sendLoanTrialRequestTime++;
    isAmountTipShown = false;
    makeLoanTrialObservable(amount, currentCouponId, lookingAtTerm)
        .subscribe(new YqdObserver<LoanConfirmInfoResponse>(hostActivity) {

          @Override
          public void onSuccess(LoanConfirmInfoResponse result) {
            dismissLoadingDialog();
            if (result.body.configs.isEmpty()) {
              BaseUtils.showNormalToast(hostActivity, "数据异常，请稍后再试");
              handleEmptyOrInvalidAmountUI();
            } else {
              saveLoanTrialData2MemoryCache(result);
              showDefaultLoanTrialResult(makeDefaultSelectedLoanTermIndex(result.body));
            }
          }

          @Override
          protected void onError(Throwable throwable, LoanConfirmInfoResponse result) {
            super.onError(throwable, result);
            dismissLoadingDialog();
            handleEmptyOrInvalidAmountUI();
          }

        });

  }

  private void saveLoanTrialData2MemoryCache(LoanConfirmInfoResponse result) {
    clearLoanTrialData();
    trialResult = result.body;
    requestTime = SystemClock.elapsedRealtime();
    amountButton = result.body.amountButton;
    protocolReadNotification = result.body.protocolReadNotification;
    loanTrialConfigs.putAll(result.body.configs);
    loanTermList.addAll(loanTrialConfigs.keySet());
    loanProtocolSwitch = result.body.protocolSwitch;
    if (result.body.termDisplayType != null) {
      termDisplayType = result.body.termDisplayType;
    }
  }

  private void setTermRadioData(List<String> datas, int defaultSelectedTermIndex) {
    if (datas.size() > 2) {
      rgTermCheck.setEqualParts(3);
    }
    rgTermCheck.removeAllViews();
    rgTermCheck.addAll(datas);
    rgTermCheck.setItemChecked(defaultSelectedTermIndex);
  }

  private int makeDefaultSelectedLoanTermIndex(LoanConfirmInfoResponse.Body body) {
    if (loanTermList == null) {
      return 0;
    }
    for (int i = 0; i < loanTermList.size(); i++) {
      String key = loanTermList.get(i);
      ProductConfig productConfig = body.configs.get(key);
      if (productConfig != null && productConfig.terms == body.defaultSelectedTerm) {
        return i;
      }
    }
    return 0;
  }

  private void showDefaultLoanTrialResult(int defaultSelectedTermIndex) {
    processTermSelectorStyle(defaultSelectedTermIndex);
    processTipBar();
    fillLoanTrialResult(loanTermList.get(defaultSelectedTermIndex));
  }

  private void processTipBar() {
    if (trialResult.tipBar == null || isCloseTipBar) {
      rlTipBarContainer.setVisibility(View.GONE);
    } else {
      rlTipBarContainer.setVisibility(View.VISIBLE);
      if (!TextUtils.isEmpty(trialResult.tipBar.tips)) {
        tvTipBar.setText(trialResult.tipBar.tips);
      }
    }
  }

  private void processTermSelectorStyle(int defaultSelectedTermIndex) {
    if (PICKER_CONTROL.equals(termDisplayType)) {
      tvLoanPeriod.setText(loanTermList.get(defaultSelectedTermIndex));
      rgTermCheck.setVisibility(View.GONE);
      rlTermPick.setVisibility(View.VISIBLE);
      tvLoanPeriod.setVisibility(View.VISIBLE);
    } else if (RADIOBUTTON_CONTROL.equals(termDisplayType)) {
      tvLoanPeriod.setVisibility(View.GONE);
      rgTermCheck.setVisibility(View.VISIBLE);
      rlTermPick.setVisibility(View.GONE);
      setTermRadioData(loanTermList, defaultSelectedTermIndex);
    }
  }

  private void fillLoanTrialResult(String termName) {
    currentSelectedTermName = termName;
    isNeedDefaultCoupon = false;

    ProductConfig productConfig = loanTrialConfigs.get(termName);

    if (productConfig == null) {
      handleEmptyOrInvalidAmountUI();
      return;
    }

    trackLoanTrialData(productConfig);

    currentSelectedProductConfig = productConfig;

    currentCouponId = productConfig.couponId;
    responsePrincipal = productConfig.principal.toString();
    loanCouponVO = productConfig.couponInfo;
    currentSelectedTerm = productConfig.terms;
    //方法调用顺序不可轻易改变！！！
    processRepayBankCardListResponse(productConfig.bankAccounts);
    processIncreaseCouponUnableToast(productConfig);
    processVip(productConfig);
    processCoupon(productConfig.couponInfo);
    checkIncreaseCouponUsable();
    processLoanAmountChangeToast(productConfig.amountTip, productConfig.principal);
    refreshLoanAmountEditTextValue(productConfig.principal);
    processRepayPlan(productConfig.normalList);
    processFunding(productConfig);
    setLoanTrialViewVisible();
    refreshRiskWarningView(productConfig);
    refreshProtocolView(productConfig.loanMessage);
    processInsurance(productConfig);
    processLoanUse(productConfig.loanUseList);
    processLoanRate(productConfig.annualizedCost);
    processProviderTips(productConfig.providerInfo);
    processProviderDetails(productConfig.providerInfo);
    processMiddleLabel(productConfig.middleList);
    processAmountButton();
  }

  private void processMiddleLabel(List<ProviderDetail> middleList) {
    if (CollectionUtils.isEmpty(middleList)) {
      llMiddleDynamic.setVisibility(View.GONE);
      llMiddleDynamic.removeAllViews();
    } else {
      llMiddleDynamic.removeAllViews();
      llMiddleDynamic.setVisibility(View.VISIBLE);
      for (ProviderDetail providerDetail : middleList) {
        addDynamicLabel(llMiddleDynamic, providerDetail);
      }
    }
  }

  private void processAmountButton() {
    if (amountButton == null) {
      btnAmount.setVisibility(View.GONE);
    } else {
      btnAmount.setVisibility(View.VISIBLE);
      if (!TextUtils.isEmpty(amountButton.buttonText)) {
        btnAmount.setText(amountButton.buttonText);
      }
      btnAmount.setOnClickListener(v -> {
        jumpToWebPage(amountButton.redirectUrl);
        finish();
      });
    }
  }

  private void processProviderTips(ProviderInfo providerInfo) {
    if (providerInfo == null || TextUtils.isEmpty(providerInfo.providerTip)) {
      tvFundingTips.setVisibility(View.GONE);
    } else {
      tvFundingTips.setVisibility(View.VISIBLE);
      tvFundingTips.setText(providerInfo.providerTip);
    }
  }

  private void processProviderDetails(ProviderInfo providerInfo) {
    if (providerInfo == null || CollectionUtils.isEmpty(providerInfo.providerDetails)) {
      llFundingDynamic.setVisibility(View.GONE);
      llFundingDynamic.removeAllViews();
    } else {
      llFundingDynamic.removeAllViews();
      llFundingDynamic.setVisibility(View.VISIBLE);
      for (ProviderDetail providerDetail : providerInfo.providerDetails) {
        addDynamicLabel(llFundingDynamic, providerDetail);
      }
    }
  }

  private void addDynamicLabel(ViewGroup viewGroup, ProviderDetail providerDetail) {
    View view = getLayoutInflater().inflate(R.layout.item_dynamic_loan_detail, llFundingDynamic, false);
    ((TextView) view.findViewById(R.id.tv_label)).setText(providerDetail.title);

    ImageView ivIcon = view.findViewById(R.id.iv_icon);
    if (!TextUtils.isEmpty(providerDetail.imageUrl)) {
      ivIcon.setVisibility(View.VISIBLE);
      Imager.get().loadIntoView(hostActivity, providerDetail.imageUrl, ivIcon);
    }

    TextView tvContent = view.findViewById(R.id.tv_content);
    tvContent.setText(providerDetail.content);
    if (!TextUtils.isEmpty(providerDetail.contentColor)) {
      try {
        int color = Color.parseColor(providerDetail.contentColor);
        tvContent.setTextColor(color);
      } catch (Exception e) {
        Logger.getLogger().e("Parse Color Failed the colorString is " + providerDetail.contentColor);
      }
    }
    viewGroup.addView(view);
  }

  private void processIncreaseCouponUnableToast(ProductConfig productConfig) {
    if (isPrincipalChanged(productConfig) && !TextUtils.isEmpty(productConfig.unableUseDesc)) {
      BaseUtils.showErrorToast(hostActivity, productConfig.unableUseDesc);
    }
  }

  private boolean isPrincipalChanged(ProductConfig productConfig) {
    return productConfig.principal != null &&
        productConfig.principal.compareTo(new BigDecimal(etLoanAmount.getText().toString())) != 0;
  }

  private void processLoanRate(LoanRate loanRate) {
    if (loanRate == null) {
      rlRateContainer.setVisibility(View.GONE);
    } else {
      rlRateContainer.setVisibility(View.VISIBLE);
      tvRateLabel.setText(loanRate.label);

      if (!TextUtils.isEmpty(loanRate.originalInterestRate) && !TextUtils.isEmpty(loanRate.actualInterestRate)) {
        SpannableString spannableString = new SpannableString(loanRate.originalInterestRate + " " + loanRate.actualInterestRate);
        spannableString.setSpan(new StrikethroughSpan(), 0, loanRate.originalInterestRate.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(ActivityCompat.getColor(hostActivity, R.color.c_8d8ea6)),
            0, loanRate.originalInterestRate.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvRate.setText(spannableString);
        tvRate.setVisibility(View.VISIBLE);
        tvRealRate.setVisibility(View.GONE);
      } else if (TextUtils.isEmpty(loanRate.originalInterestRate) && !TextUtils.isEmpty(loanRate.actualInterestRate)) {
        tvRealRate.setText(loanRate.actualInterestRate);
        tvRate.setVisibility(View.GONE);
        tvRealRate.setVisibility(View.VISIBLE);
      } else {
        tvRate.setVisibility(View.GONE);
        tvRealRate.setVisibility(View.GONE);
      }

      if (TextUtils.isEmpty(loanRate.discountDescription)) {
        tvRateDiscount.setVisibility(View.GONE);
      } else {
        tvRateDiscount.setVisibility(View.VISIBLE);
        tvRateDiscount.setText(loanRate.discountDescription);
      }

      if (TextUtils.isEmpty(loanRate.tip)) {
        ivRateInfo.setVisibility(View.GONE);
      } else {
        ivRateInfo.setVisibility(View.VISIBLE);
        ivRateInfo.setOnClickListener(v -> {
          showRateTip(loanRate.tip);
        });
      }
    }
  }

  private void showRateTip(String tip) {
    AlertDialog dialog = new AlertDialog.Builder(hostActivity, R.style.CommonAlertDialog)
        .setMessage(tip)
        .setPositiveButton("确定", (dialog1, which) -> {
          //埋点占位
        })
        .show();
    TrackDataApi.getInstance().setViewID(dialog, "dialog_loan_confirm_rate_tip");
  }

  private void trackLoanTrialData(ProductConfig productConfig) {
    if (sendLoanTrialRequestTime == 1) {
      if (YqdBuildConfig.SDK_TYPE == YqdBuildConfig.SdkType.ZEBRA) {
        ThirdPartEventUtils.onFintopiaEvent(hostActivity, YqdStatisticsEvent.ZEBRA_LOAN_CONFIRM_PAGE_SHOW,
            productConfig, userGlobal.eventUserStatus);
      }
    }
  }

  private void processVip(ProductConfig productConfig) {
    if (!trialResult.showMembershipCard || productConfig.memberCardResponse == null) {
      rlVipContainer.setVisibility(View.GONE);
      flVipCardContainer.setVisibility(View.GONE);
      return;
    }

    if (vipCardView == null || isCurrentStyleNotMatch()) {
      if (trialResult.showNewMembershipCard) {
        vipCardView = new VipCardViewV2(hostActivity, "vip_card_v2");
      } else {
        vipCardView = new VipCardView(hostActivity);
      }
      vipCardView.createRootView(flVipCardContainer);
      vipCardView.setOnOpenWebPageClickListener(this::jumpToWebPage);
      flVipCardContainer.removeAllViews();
      flVipCardContainer.addView(vipCardView.getRootView());
    }

    vipCardView.setOnCheckChangedListener((buttonView, isChecked) -> {
      isOpenVip = isChecked;
      if (isChecked) {
        tvVipContent.setText(String.format("省%s元", productConfig.memberCardResponse.memberPrincipal));
        rlVipContainer.setVisibility(View.VISIBLE);
        flVipCardContainer.setVisibility(View.GONE);
      }
    });
    vipCardView.refreshView(productConfig.memberCardResponse);
    rlVipContainer.setVisibility(View.GONE);
    flVipCardContainer.setVisibility(View.VISIBLE);
  }

  private boolean isCurrentStyleNotMatch() {
    return (trialResult.showNewMembershipCard && vipCardView instanceof VipCardView) ||
        (!trialResult.showNewMembershipCard && vipCardView instanceof VipCardViewV2);
  }

  private boolean isOpenVipChecked() {
    if (vipCardView == null) {
      return false;
    }
    return vipCardView.isChecked();
  }

  private void processFunding(ProductConfig productConfig) {
    if (TextUtils.isEmpty(productConfig.fundingProviderCompanyName)) {
      rlFunding.setVisibility(View.GONE);
    } else {
      tvFunding.setText(productConfig.fundingProviderCompanyName);
      if (TextUtils.isEmpty(productConfig.providerInstitution)) {
        tvFundingLabel.setText("资金方");
      } else {
        tvFundingLabel.setText(productConfig.providerInstitution);
      }

      if (TextUtils.isEmpty(productConfig.providerLogoUrl)) {
        ivFundingLogo.setImageDrawable(null);
      } else {
        Imager.get().loadIntoView(this, productConfig.providerLogoUrl, ivFundingLogo);
      }

      rlFunding.setVisibility(View.VISIBLE);
    }
  }

  private void processCoupon(LoanCouponVO loanCoupon) {
    currentCouponId = null;
    increaseLoanAmountCoupon = null;
    processCouponTipBar(loanCoupon == null ? null : loanCoupon.defaultCoupon);
    if (loanCoupon != null) {
      couponListPageUrl = loanCoupon.allAvailableCouponUrl;
    }
    if (loanCoupon == null || loanCoupon.couponCount <= 0) {
      tvCouponContent.setText("暂无可用");
      tvCouponContent.setTextColor(ContextCompat.getColor(hostActivity, R.color.c_8d8ea6));
      return;
    }
    if (loanCoupon.defaultCoupon == null) {
      tvCouponContent.setText(String.format("%d张优惠券", loanCoupon.couponCount));
      tvCouponContent.setTextColor(ContextCompat.getColor(hostActivity, R.color.c_242533));
    } else {
      if (loanCoupon.defaultCoupon.getType() == LoanCouponEnum.INCREASE_CREDIT_COUPON_NEW) {
        setIncreaseLoanAmountCoupon((IIncreaseCoupon) loanCoupon.defaultCoupon.template);
        increaseLoanAmountCoupon.setId(loanCoupon.defaultCoupon.id);
      }
      currentCouponId = loanCoupon.defaultCoupon.id;
      tvCouponContent.setText(loanCoupon.defaultCoupon.couponShortTitle);
      tvCouponContent.setTextColor(ContextCompat.getColor(hostActivity, R.color.c_fa5757));
    }
  }

  private void processCouponTipBar(LoanCoupon loanCoupon) {
    stopCouponCountDown();
    CouponTipBar couponTipBar;
    long millisInFuture;
    if (loanCoupon == null || (couponTipBar = loanCoupon.couponTipBar) == null ||
        (millisInFuture = couponTipBar.remainTime - (SystemClock.elapsedRealtime() - requestTime)) < 0) {
      tvCouponTipBar.setVisibility(View.GONE);
      return;
    }
    tvCouponTipBar.setVisibility(View.VISIBLE);
    //时间大于1天只显示XX天后
    long days = TimeUnit.MILLISECONDS.toDays(millisInFuture);
    if (days > 0) {
      String timeFormat = days + "天";
      formatCouponTipBarText(couponTipBar.tips, timeFormat, couponTipBar.tipsHighLights);
      return;
    }
    //小于1天时开启倒计时
    couponCountDownTimer = new CountDownTimer(millisInFuture, DateUtils.SECOND_IN_MILLIS) {
      @Override
      public void onTick(long millisUntilFinished) {
        String timeFormat = TimeUtils.millisecondsToDaysAndHoursAndMinutesAndSeconds(millisUntilFinished);
        formatCouponTipBarText(couponTipBar.tips, timeFormat, couponTipBar.tipsHighLights);
      }

      @Override
      public void onFinish() {
        handleInputResult(etLoanAmount.getText().toString());
      }
    };
    couponCountDownTimer.start();
  }

  private void formatCouponTipBarText(String content, String format, List<String> highLights) {
    String tips = content.replace("${time}", format);
    tvCouponTipBar.setText(
        SpannableUtils.setTextForeground(
            tips,
            highLights,
            ContextCompat.getColor(hostActivity, R.color.c_fa5757)
        ));
  }

  private void stopCouponCountDown() {
    if (couponCountDownTimer != null) {
      couponCountDownTimer.cancel();
      couponCountDownTimer = null;
    }
  }

  private void checkIncreaseCouponUsable() {
    if (increaseLoanAmountCoupon == null) {
      //未使用提额券时恢复默认借款范围
      refreshLoanAmountRange(getLowestLimit(), getUpperLimit());
      return;
    }
    if (increaseLoanAmountCoupon.getTerms() == null || increaseLoanAmountCoupon.getTerms().contains(currentSelectedTerm)) {
      //当前所选期数可以使用提额券
      refreshLoanAmountRange(getLowestLimit(), getUpperLimitWithIncreaseCoupon());
    } else {
      //用户当前所选期数不可用提额券，恢复默认借款范围
      refreshLoanAmountRange(getLowestLimit(), getUpperLimit());
    }
  }

  private void processLoanAmountChangeToast(String amountTip, BigDecimal principal) {
    if (!isAmountTipShown && !TextUtils.isEmpty(amountTip)) {
      BaseUtils.showNormalToast(hostActivity, amountTip);
      isAmountTipShown = true;
      return;
    }
    BigDecimal inputAmount = new BigDecimal(etLoanAmount.getText().toString());
    if (principal == null || principal.equals(inputAmount)) {
      return;
    }
    if (inputAmount.compareTo(minValidAmount) < 0) {
      BaseUtils.showNormalToast(hostActivity, String.format("当前最小可借金额为%s元，已为您自动调整", minValidAmount.toString()));
    } else if (inputAmount.compareTo(maxValidAmount) > 0) {
      BaseUtils.showNormalToast(hostActivity, String.format("当前最大可借金额为%s元，已为您自动调整", maxValidAmount.toString()));
    }
  }

  private void refreshLoanAmountEditTextValue(BigDecimal principal) {
    if (principal == null) {
      return;
    }
    //金额有可能会变化，所以以后端返回的试算结果对应的金额为准
    etLoanAmount.setText(String.valueOf(principal));
    etLoanAmount.setSelection(etLoanAmount.length());
  }

  private void refreshRiskWarningView(ProductConfig productConfig) {
    if (TextUtils.isEmpty(productConfig.overdueCreditTip)) {
      tvRiskWarning.setVisibility(View.GONE);
    } else {
      tvRiskWarning.setText(productConfig.overdueCreditTip);
      tvRiskWarning.setVisibility(View.VISIBLE);
    }
  }

  private void refreshProtocolView(String loanMessage) {
    if (!TextUtils.isEmpty(loanMessage)) {
      mkLoanProtocol.loadMarkdown(loanMessage);
      cbProtocol.setChecked(loanProtocolSwitch);
      llProtocol.setVisibility(View.VISIBLE);
    } else {
      cbProtocol.setChecked(true);
      llProtocol.setVisibility(View.GONE);
    }
  }

  private void processInsurance(ProductConfig productConfig) {
    if (productConfig.insuranceItem != null) {
      selectedInsuranceItem = productConfig.insuranceItem;
      llInsuranceCard.setVisibility(View.VISIBLE);
      llInsuranceCard.setTag(Boolean.TRUE);

      tvInsuranceWarnInfo.setText(productConfig.insuranceItem.insuranceWarnInfo);
      tvInsuranceFee.setText(String.format("保费 ¥%s", productConfig.insuranceItem.insuranceFee));
      tvRejectInsuranceFee.setText(productConfig.insuranceItem.noInsuranceWarningInfo);
      tvRejectInsuranceFee.setOnClickListener(v -> showRejectInsuranceFeeDialog(productConfig.insuranceItem.returnBackWarningInfo));

      refreshInsuranceAuthState();

    } else {
      llInsuranceCard.setVisibility(View.GONE);
      llInsuranceCard.setTag(Boolean.FALSE);
      selectedInsuranceItem = null;
    }
  }

  private void showRejectInsuranceFeeDialog(String message) {
    AlertDialog alertDialog = new AlertDialog.Builder(hostActivity, R.style.CommonAlertDialog)
        .setMessage(message)
        .setPositiveButton("确认", (dialog, which) -> {
          showLoadingDialog();
          sendRejectInsuranceFeeRequest();
        })
        .setNegativeButton("取消", (dialog, which) -> {
          //编译时全埋点插件会在该方法插入埋点代码，不可删除
        })
        .show();
    TrackDataApi.getInstance().setViewID(alertDialog, "dialog_reject_insurance_fee");
  }

  private void setLoanTrialViewVisible() {
    llPeriodCard.setVisibility(View.VISIBLE);
    llDetailCard.setVisibility(View.VISIBLE);
    llProtocol.setVisibility(View.VISIBLE);
  }

  private void handleEmptyOrInvalidAmountUI() {
    clearLoanTrialData();
    llPeriodCard.setVisibility(View.GONE);
    rgTermCheck.setVisibility(View.GONE);
    llDetailCard.setVisibility(View.GONE);
    rlLoanUse.setVisibility(View.GONE);
    llInsuranceCard.setVisibility(View.GONE);
    tvRiskWarning.setVisibility(View.GONE);
    llProtocol.setVisibility(View.GONE);
    rlFunding.setVisibility(View.GONE);
    rlVipContainer.setVisibility(View.GONE);
    flVipCardContainer.setVisibility(View.GONE);
    tvFundingTips.setVisibility(View.GONE);
    tvCouponTipBar.setVisibility(View.GONE);
    rlTipBarContainer.setVisibility(View.GONE);
    btnAmount.setVisibility(View.GONE);
  }

  private void clearLoanTrialData() {
    trialResult = null;
    createOrderInfo = null;
    responsePrincipal = "";
    loanTrialConfigs.clear();
    loanTermList.clear();
    isContinueToLoan = false;
    currentSelectedProductConfig = null;
    isOpenVip = false;
    requestTime = 0L;
    stopCouponCountDown();
  }

  private void processRepayPlan(List<OrderConfirmInfoItem> confirmInfoItems) {
    if (!CollectionUtils.isEmpty(confirmInfoItems)) {
      OrderConfirmInfoItem orderConfirmInfoItem = confirmInfoItems.get(0);
      tvRepayLabel.setText(orderConfirmInfoItem.label);
      tvRepayContent.setText(orderConfirmInfoItem.content);
      tvRepayContent.setOnClickListener(v -> {
        onOrderConfirmItemClicked(orderConfirmInfoItem);
      });
    }
  }

  @OnClick({R.id.iv_tip_bar_close})
  public void onTipBarCloseClicked() {
    rlTipBarContainer.setVisibility(View.GONE);
    isCloseTipBar = true;
  }

  @OnClick(R.id.tv_vip_content)
  public void onVipClicked() {
    flVipCardContainer.setVisibility(View.VISIBLE);
    rlVipContainer.setVisibility(View.GONE);
    vipCardView.startAnim();
  }

  @OnClick(R.id.tv_insurance_auth)
  public void doRenbaoAuth() {

    if (selectedBankCard == null) {
      BaseUtils.showErrorToast(hostActivity, "请选择银行卡");
      return;
    }

    if (authedBankCard.contains(selectedBankCard.bankAccountId)) {
      return;
    }

    if (getCurrentProductId() == null) {
      BaseUtils.showErrorToast(hostActivity, "请选择产品");
      return;
    }

    if (selectedInsuranceItem == null) {
      return;
    }
    Intent intent = new Intent(hostActivity, RenbaoFeeConfirmActivity.class);
    intent.putExtra(YqdLoanConstants.INTENT_KEY_INSURANCE_ITEM, selectedInsuranceItem);
    intent.putExtra(YqdLoanConstants.INTENT_KEY_BANK_CARD, selectedBankCard);
    intent.putExtra(YqdLoanConstants.INTENT_KEY_PRINCIPAL, etLoanAmount.getText().toString());
    intent.putExtra(YqdLoanConstants.INTENT_KEY_PRODUCT_ID, getCurrentProductId());
    startActivityForResult(intent, YqdLoanConstants.RequestCode.AUTH_INSURANCE_FEE);
  }

  @OnClick(R.id.tv_loan_use)
  public void doSelectLoanUse() {
    if (isFastClick()) {
      return;
    }

    if (selectLoanUseDialog == null) {
      initSelectLoanUseDialog();
    }
    selectLoanUseDialog.show();
  }

  private void initSelectLoanUseDialog() {
    selectLoanUseDialog = new BottomCommonOptionSelectDialog(hostActivity, loanUseList);
    selectLoanUseDialog.setTitle("借款用途选择");
    selectLoanUseDialog.setElementId(R.id.ll_loan_use);
    selectLoanUseDialog.setBackgroundResource(R.drawable.shape_r20_r20_f0f2fa);
    selectLoanUseDialog.setOnItemSelectListener(this::refreshLoanUseUI);
  }

  @SuppressWarnings("unused")
  @OnClick(R.id.btn_loan_confirm)
  public void onLoanConfirmClicked() {
    if (isFastClick()) {
      return;
    }

    if (!isLoanAmountValid(etLoanAmount.getText().toString())) {
      return;
    }
    if (isLoanTrialFailed() || isPrincipalChanged(currentSelectedProductConfig)) {
      handleInputResult(etLoanAmount.getText().toString());
      hideSoftInput();
      return;
    }
    if (llInsuranceCard.getTag() == Boolean.TRUE) {
      if (tvInsuranceAuth.getTag() != Boolean.TRUE) {
        BaseUtils.showErrorToast(hostActivity, "请完成投保");
        return;
      }
    }
    if (isShowLoanUse && !loanUseList.isEmpty() && selectedLoanUse == null) {
      BaseUtils.showErrorToast(hostActivity, "请选择借款用途");
      return;
    }
    if (!cbProtocol.isChecked()) {
      showConfirmProtocolDialog();
      return;
    }
    if (repayBankCards != null && repayBankCards.isEmpty()) {
      isContinueToLoan = true;
      jumpToBindBinkCardPage();
      return;
    }

    isOpenVip = isOpenVipChecked();
    if (!trialResult.showMembershipCard || currentSelectedProductConfig.memberCardResponse == null || isOpenVip) {
      showCreditAuthDialog();
    } else {
      processOpenVipConfirmDialog();
    }

    if (YqdBuildConfig.SDK_TYPE == YqdBuildConfig.SdkType.ZEBRA) {
      ThirdPartEventUtils.onFintopiaEvent(hostActivity, YqdStatisticsEvent.ZEBRA_LOAN_CONFIRM_PAGE_CLICK_LOAN_BTN,
          currentSelectedProductConfig, userGlobal.eventUserStatus);
    }
  }

  private void jumpToBindBinkCardPage() {
    Intent intent = new Intent(hostActivity, YqdBindBankCardActivityV3.class);
    startActivityForResult(intent, YqdLoanConstants.RequestCode.ADD_BANK_CARD);
  }

  private void processOpenVipConfirmDialog() {
    if (trialResult.mustChooseMembershipCard) {
      showForceOpenVipDialog();
    } else {
      showOpenVipRetainDialog();
    }
  }

  private void showOpenVipRetainDialog() {
    if (trialResult.showNewMembershipCard) {
      new OpenVipRetainDialogV2.Builder(hostActivity)
          .setData(currentSelectedProductConfig.memberCardResponse)
          .setOnOpenWebPageClickListener(this::jumpToWebPage)
          .setOnPositiveClickListener((dialog, which) -> {
            isOpenVip = true;
            showCreditAuthDialog();
            return true;
          })
          .setOnNegativeClickListener((dialog, which) -> {
            if (trialResult.isShowSecondPopUp) {
              showOpenVipSecondRetainDialog();
            } else {
              showCreditAuthDialog();
            }
            return true;
          }).show();
    } else {
      new OpenVipRetainDialog.Builder(hostActivity)
          .setData(currentSelectedProductConfig.memberCardResponse)
          .setOnOpenWebPageClickListener(this::jumpToWebPage)
          .setOnPositiveClickListener((dialog, which) -> {
            isOpenVip = true;
            showCreditAuthDialog();
            return true;
          })
          .setOnNegativeClickListener((dialog, which) -> {
            showCreditAuthDialog();
            return true;
          }).show();
    }
  }

  /**
   * 显示二次挽留弹窗，https://code.yangqianguan.com/T41557
   */
  private void showOpenVipSecondRetainDialog() {
    new OpenVipSecondRetainDialog.Builder(hostActivity)
        .setData(currentSelectedProductConfig.memberCardResponse)
        .setOnOpenWebPageClickListener(this::jumpToWebPage)
        .setOnPositiveClickListener((dialog, which) -> {
          isOpenVip = true;
          showCreditAuthDialog();
          return true;
        })
        .setOnNegativeClickListener((dialog, which) -> {
          showCreditAuthDialog();
          return true;
        }).show();
  }

  private void showForceOpenVipDialog() {
    new ForceOpenVipConfirmDialog.Builder(hostActivity)
        .setTitle(trialResult.notChooseMemberTips)
        .setData(currentSelectedProductConfig.memberCardResponse)
        .setShowNewMembershipCard(trialResult.showNewMembershipCard)
        .setCancelable(false)
        .setOnOpenWebPageClickListener(this::jumpToWebPage)
        .setOnClickListener((dialog, view, isChecked) -> {
          if (isChecked) {
            isOpenVip = true;
            dialog.dismiss();
            showCreditAuthDialog();
          } else {
            if (TextUtils.isEmpty(trialResult.notChooseMemberToast)) {
              trialResult.notChooseMemberToast = "当前借款人数较多，正在排队中";
            }
            BaseUtils.showCustomSnackBarWithView(view, trialResult.notChooseMemberToast);
          }
        }).show();
  }

  private void showCreditAuthDialog() {
    showLoadingDialog();
    sendGetCreditDialogInfoRequest();
  }

  private boolean isLoanTrialFailed() {
    return TextUtils.isEmpty(responsePrincipal) || loanTrialConfigs == null || loanTrialConfigs.isEmpty();
  }

  private void continueLoan() {
    showLoadingDialog();
    createOrderInfo = makeCreateOrderInfo();
    sendCanCreateOrderRequest();
  }

  /**
   * 用户从Web页返回后统一使用createOrderInfo缓存的数据进行接下来的接口请求
   * 1.保证数据一致
   * 2.避免用户开启不保留活动后数据取不到引发异常
   */
  private CreateOrderInfo makeCreateOrderInfo() {
    CreateOrderInfo createOrderInfo = new CreateOrderInfo();
    createOrderInfo.loanAmount = etLoanAmount.getText().toString();
    createOrderInfo.bankAccountId = selectedBankCard.bankAccountId;
    createOrderInfo.productId = getCurrentProductId();
    createOrderInfo.loanUseValue = getSelectedLoanUseValue();
    createOrderInfo.couponId = currentCouponId;
    return createOrderInfo;
  }

  private void showConfirmProtocolDialog() {
    if (TextUtils.isEmpty(protocolReadNotification)) {
      scrollToEnd();
    } else {
      new AlertDialog.Builder(hostActivity, R.style.CommonAlertDialog)
          .setMessage(protocolReadNotification)
          .setPositiveButton("确定", (dialog, which) -> {
            scrollToEnd();
          })
          .show();
    }
  }

  private void showCreditAuthDialog(CreditDialogResponse result) {
    if (confirmLoanCreditAuthDialog != null && confirmLoanCreditAuthDialog.isShowing()) {
      return;
    }

    confirmLoanCreditAuthDialog = new ConfirmLoanCreditAuthDialog
        .Builder(hostActivity, 0)
        .setCanceledOnTouchOutside(false)
        .setTitle(result.body.title)
        .setButtonText(result.body.buttonText)
        .setMkdContent(result.body.content)
        .setOnMkdUrlOnClickListener(url -> {
          if (!TextUtils.isEmpty(url)) {
            jumpToWebPage(url);
          }
        })
        .setOnNegativeClickListener((dialog, which) -> {
          continueLoan();
          trackClickCreditAuthDialogConfirm();
        })
        .build();
    TrackDataApi.getInstance().setViewID(confirmLoanCreditAuthDialog, "dialog_credit_auth");
    confirmLoanCreditAuthDialog.show();
  }

  private void trackClickCreditAuthDialogConfirm() {
    if (YqdBuildConfig.SDK_TYPE == YqdBuildConfig.SdkType.ZEBRA) {
      ThirdPartEventUtils.onFintopiaEvent(hostActivity, YqdStatisticsEvent.ZEBRA_LOAN_CONFIRM_PAGE_CLICK_CREDIT_AUTH_DIALOG_CONFIRM,
          null, userGlobal.eventUserStatus);
    }
  }

  private void scrollToEnd() {
    nsvWrapper.fullScroll(ScrollView.FOCUS_DOWN);
  }

  private void sendCreateOrderRequest() {
    commonApiHelper.getRetrofitApiHelper()
        .createLoanOrder(YqdHeaderUtils.getEnvironmentInfoV2(hostActivity, gson),
            createOrderInfo.loanAmount,
            createOrderInfo.bankAccountId,
            createOrderInfo.productId,
            createOrderInfo.loanUseValue,
            createOrderInfo.couponId,
            isOpenVip)
        .subscribe(new YqdObserver<CashLoanCreateOrderResponse>(hostActivity) {
          @Override
          public void onSuccess(CashLoanCreateOrderResponse result) {
            dismissLoadingDialog();
            processLoanConfirmResponse(result);
          }
        });
  }

  @Nullable
  private String getSelectedLoanUseValue() {
    return selectedLoanUse == null ? null : selectedLoanUse.value;
  }

  private String getCurrentProductId() {
    if (currentSelectedProductConfig == null) {
      if (BuildConfig.DEBUG) {
        throw new NullPointerException("currentSelectedProductConfig is null");
      } else {
        return null;
      }
    }
    return currentSelectedProductConfig.productId;
  }

  private void sendCheckPreWithdrawStatusRequest() {
    commonApiHelper.getRetrofitApiHelper()
        .checkPreWithdrawStatus(
            createOrderInfo.loanAmount,
            createOrderInfo.bankAccountId,
            createOrderInfo.productId,
            createOrderInfo.couponId,
            createOrderInfo.loanUseValue)
        .subscribe(new YqdObserver<YqdBooleanResponse>(hostActivity) {
          @Override
          public void onSuccess(YqdBooleanResponse result) {
            dismissLoadingDialog();
            if (result.body) {
              onCreateOrderSuccess(HxcgUmengPointEnum.HxcgTradeWebReturnPoint.HX_LOAN_BACK_PRESS_OK);
            } else {
              onHXCGTradeCallBackEvent(HxcgUmengPointEnum.HxcgTradeWebReturnPoint.HX_LOAN_BACK_PRESS_CANCEL.name());
            }
          }
        });
  }

  private void sendCanCreateOrderRequest() {
    commonApiHelper.getRetrofitApiHelper()
        .sendCanCreateOrderRequest(createOrderInfo.loanAmount,
            createOrderInfo.bankAccountId,
            createOrderInfo.productId,
            createOrderInfo.loanUseValue,
            createOrderInfo.couponId,
            isOpenVip)
        .subscribe(new YqdObserver<CanCreateOrderResponse>(hostActivity) {
          @Override
          public void onSuccess(CanCreateOrderResponse result) {
            processCanCreateOrderResponse(result.body);
          }
        });
  }

  private void sendGetCreditDialogInfoRequest() {
    String productId = getCurrentProductId();

    if (productId == null) {
      return;
    }

    commonApiHelper.getRetrofitApiHelper()
        .getCreditDialogInfo(productId, isOpenVip)
        .subscribe(new YqdObserver<CreditDialogResponse>(hostActivity) {
          @Override
          public void onSuccess(CreditDialogResponse result) {
            dismissLoadingDialog();
            processGetCreditDialogInfoResponse(result);
          }

          @Override
          public void onComplete() {
            dismissLoadingDialog();
          }
        });
  }

  /**
   * 通知后台拒绝保费
   */
  private void sendRejectInsuranceFeeRequest() {
    commonApiHelper.getRetrofitApiHelper()
        .noInsured()
        .subscribe(new YqdObserver<YqdBaseResponse>(hostActivity) {
          @Override
          public void onSuccess(YqdBaseResponse result) {
            //拒绝人保保费，返回首页
            finish();
          }

          @Override
          public void onComplete() {
            dismissLoadingDialog();
          }
        });
  }

  private void processGetCreditDialogInfoResponse(CreditDialogResponse result) {
    if (result.body == null) {
      continueLoan();
      return;
    }

    showCreditAuthDialog(result);
  }

  private void processCanCreateOrderResponse(final CanCreateOrderResponse.Body body) {
    switch (CanCreateOrderStatusEnum.valueOf(body.status)) {
      case NEED_SUPPLEMENT_DATA:
        openWebPageToCreateOrder(body);
        break;
      case SUCCESS:
        doConfirmLoan();
        break;
      case ROUTE_ERROR:
        showRouteErrorDialog(body.toastMsg);
        handleInputResult(etLoanAmount.getText().toString());
        break;
      case SUPPLEMENT_STEPS_SUPPLIED:
        userGlobal.authScene = AuthScene.CONFIRM_TO_SUPPLY;
        isOnSupplementAuthentication = true;
        authHelper.get().updateSupplementAuthSteps(body.supplementSteps);
        authHelper.get().startSupplementAuth(hostActivity);
        break;
    }
  }

  private void showRouteErrorDialog(String msg) {
    new AlertDialog.Builder(hostActivity, R.style.CommonAlertDialog)
        .setTitle("温馨提示")
        .setMessage(msg)
        .setNegativeButton("知道了", null)
        .show();
  }

  private void openWebPageToCreateOrder(CanCreateOrderResponse.Body body) {
    new Handler().postDelayed(() -> {
      dismissLoadingDialog();

      onHXCGTradeCallBackEvent(HxcgUmengPointEnum.HxcgTradeWebReturnPoint.HX_LOAN_START.name());
      jumpToHxcgWebPageForResultWithProvider(body.supplementDataUrl,
          YqdLoanConstants.RequestCode.HUAXIA_CALLBACK_INTENT, false,
          HxcgActionProviderEnum.LOAN.name());
    }, 1000 * getShowTime(body));
  }

  protected void jumpToHxcgWebPageForResultWithProvider(String actionUrl, int requestCode,
                                                        boolean isWebPageCanGoBack, String actionProvider) {

    UriHandler.handle(hostActivity, actionUrl, requestCode, postcard -> {
      postcard.withBoolean(YqdLoanConstants.INTENT_KEY_WEB_PAGE_CAN_GO_BACK, isWebPageCanGoBack);
      if (!TextUtils.isEmpty(actionProvider)) {
        postcard.withString(YqdLoanConstants.INTENT_KEY_ACTION_PROVIDER, actionProvider);
      }
      return postcard;
    });
  }

  /**
   * 随机生成延时时间，用来模拟债权匹配
   *
   * @return second
   */
  private int getShowTime(CanCreateOrderResponse.Body body) {
    if (body.showTime) {
      return new Random().nextInt(6) + 5;
    } else {
      return 0;
    }
  }

  public void doConfirmLoan() {
    if (!isCreditsStatusAccepted()) {
      showLoadingDialog();
      sendUploadCashLoanOrderInfoRequest();
    } else {
      showLoadingDialog();
      sendCreateOrderRequest();
    }
  }

  @SuppressWarnings("unused")
  @OnClick(R.id.tv_loan_period)
  public void selectLoanTerm() {
    if (isFastClick()) {
      return;
    }
    hideSoftInput();
    showBottomSheetDialog();
  }

  private void showBottomSheetDialog() {
    if (selectLoanTermBottomSheetDialog != null) {
      selectLoanTermBottomSheetDialog.dismiss();
    }
    selectLoanTermBottomSheetDialog = new BottomSingleColumnSelectDialog(hostActivity, loanTermList);
    selectLoanTermBottomSheetDialog.setTitle("借款期限");
    selectLoanTermBottomSheetDialog.setElementId(R.id.tv_loan_period);
    selectLoanTermBottomSheetDialog.setBackgroundResource(R.drawable.shape_r20_r20_f0f2fa);
    selectLoanTermBottomSheetDialog.setOnItemSelectListener((index, termName) -> {
      tvLoanPeriod.setText(termName);
      fillLoanTrialResult(termName);
    });
    selectLoanTermBottomSheetDialog.show();
  }

  @SuppressWarnings("unused")
  @OnClick(R.id.tv_bank_card_number)
  public void changeBankCard() {
    if (repayBankCards == null || repayBankCards.isEmpty()) {
      jumpToBindBinkCardPage();
      return;
    }
    hideSoftInput();
    showSelectBankCardDialog();
  }

  @SuppressWarnings("unused")
  @OnClick(R.id.tv_coupon)
  public void onCouponButtonClicked() {
    try {
      HashMap<String, String> hashMap = new HashMap<>();
      if (currentCouponId == null) {
        hashMap.put("selectedCouponId", "");
      } else {
        hashMap.put("selectedCouponId", currentCouponId);
      }
      String url = LoanUriUtil.replaceOrAppendParams(Uri.parse(couponListPageUrl), hashMap).toString();
      jumpToWebPage(url);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unused")
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onCouponSelected(EventSelectCoupon event) {
    LoanCoupon currentSelectedCoupon = loanCouponVO.defaultCoupon;
    LoanCoupon newLoanCoupon = event.loanCoupon;
    currentCouponId = newLoanCoupon == null ? null : newLoanCoupon.id;

    if (currentSelectedCoupon == null && newLoanCoupon == null) {
      //ignore
    } else if (currentSelectedCoupon == null) {
      loanCouponVO.defaultCoupon = newLoanCoupon;
      if (newLoanCoupon.getType() == LoanCouponEnum.INCREASE_CREDIT_COUPON_NEW) {
        setIncreaseLoanAmountCoupon((IIncreaseCoupon) newLoanCoupon.template);
      }
      handleInputResult(etLoanAmount.getText().toString(), String.valueOf(currentSelectedTerm));
    } else if (newLoanCoupon == null) {
      loanCouponVO.defaultCoupon = null;
      if (currentSelectedCoupon.getType() == LoanCouponEnum.INCREASE_CREDIT_COUPON_NEW) {
        setIncreaseLoanAmountCoupon(null);
      }
      handleInputResult(etLoanAmount.getText().toString(), String.valueOf(currentSelectedTerm));
    } else {
      if (!currentSelectedCoupon.equals(newLoanCoupon)) {
        loanCouponVO.defaultCoupon = newLoanCoupon;
        if (newLoanCoupon.getType() == LoanCouponEnum.INCREASE_CREDIT_COUPON_NEW) {
          handleInputResult(etLoanAmount.getText().toString(), String.valueOf(currentSelectedTerm));
        } else if (currentSelectedCoupon.getType() == LoanCouponEnum.INCREASE_CREDIT_COUPON_NEW) {
          handleInputResult(etLoanAmount.getText().toString(), String.valueOf(currentSelectedTerm));
        } else {
          setIncreaseLoanAmountCoupon(null);
          handleInputResult(etLoanAmount.getText().toString(), String.valueOf(currentSelectedTerm));
        }
      }
    }
  }

  private void setIncreaseLoanAmountCoupon(IIncreaseCoupon increaseLoanAmountCoupon) {
    //新增调用该方法时，需要注意currentCouponId是否更新
    this.increaseLoanAmountCoupon = increaseLoanAmountCoupon;
    checkIncreaseCouponUsable();
  }

  @SuppressWarnings("unused")
  @OnClick(R.id.ll_cb_wrapper)
  public void clickCheckBox() {
    cbProtocol.toggle();
  }

  @Override
  public void onResume() {
    super.onResume();
    //校验补充鉴权步骤是否已经完成，如果完成则直接创建订单
    if (isOnSupplementAuthentication) {
      checkHasSupplementStepsFinished();
      //无论成功失败，都应该重置状态
      isOnSupplementAuthentication = false;
    }
  }

  private void checkHasSupplementStepsFinished() {
    if (createOrderInfo != null) {
      commonApiHelper.getRetrofitApiHelper()
          .supplementStepsFinished(createOrderInfo.loanAmount,
              createOrderInfo.bankAccountId,
              createOrderInfo.productId,
              createOrderInfo.loanUseValue,
              createOrderInfo.couponId)
          .subscribe(new YqdObserver<YqdBooleanResponse>(hostActivity) {
            @Override
            public void onSuccess(YqdBooleanResponse result) {
              //用户走完了填写补充信息步骤，直接创建订单
              doConfirmLoan();
            }
          });
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case YqdLoanConstants.RequestCode.ADD_BANK_CARD:
        if (resultCode == YqdLoanConstants.ResultCode.SUCCESS) {
          handleInputResult(etLoanAmount.getText().toString());
          hideSoftInput();
        }
        break;
      case YqdLoanConstants.RequestCode.HUAXIA_CALLBACK_INTENT:
        //由于不知道用户在Web页是否操作成功，所以每次从Web返回后都调用接口通知后端校验用户状态
        if (createOrderInfo == null) {
          //接下来的每次接口调用都需要使用用户之前提交的订单信息，所以createOrderInfo == null
          //意味着用户状态异常，不应该让用户再进行任何操作
          BaseUtils.showNormalToast(hostActivity.getApplicationContext(), "数据异常，请稍后再试");
          finish();
          return;
        }
        if (resultCode == YqdLoanConstants.ResultCode.SUCCESS) {
          onCreateOrderSuccess(HxcgUmengPointEnum.HxcgTradeWebReturnPoint.HX_LOAN_OK);
        } else {
          onWebPageBackPressed();
        }
        break;
      case YqdLoanConstants.RequestCode.AUTH_INSURANCE_FEE:
        if (resultCode == YqdLoanConstants.ResultCode.SUCCESS) {
          //缓存当次借款的已投保银行卡
          authedBankCard.add(selectedBankCard.bankAccountId);
          refreshInsuranceAuthState();
        }
        break;
    }
  }

  private void onCreateOrderSuccess(HxcgUmengPointEnum.HxcgTradeWebReturnPoint hxLoanOk) {
    onHXCGTradeCallBackEvent(hxLoanOk.name());

    doConfirmLoan();
  }

  private void onWebPageBackPressed() {
    if (isFastClick()) {
      return;
    }
    showLoadingDialog();
    sendCheckPreWithdrawStatusRequest();
  }

  private void refreshInsuranceAuthState() {
    if (selectedBankCard != null && authedBankCard.contains(selectedBankCard.bankAccountId)) {
      tvInsuranceAuth.setText("已投保");
      tvInsuranceAuth.setTag(Boolean.TRUE);
    } else {
      tvInsuranceAuth.setText("去投保");
      tvInsuranceAuth.setTag(Boolean.FALSE);
    }
  }

  public void hideSoftInput() {
    isHideSoftInputByCoding = true;
    hostActivity.hideSoftInput();
  }

  void finish() {
    if (hostActivity != null) {
      hostActivity.finish();
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    userGlobal.increaseCoupon = null;
    EventBus.getDefault().unregister(this);
    stopCouponCountDown();
  }
}
