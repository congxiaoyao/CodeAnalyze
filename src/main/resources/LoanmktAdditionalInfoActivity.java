package com.lingyue.yqd.loanmarket.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.lingyue.generalloanlib.commons.UmengEvent;
import com.lingyue.generalloanlib.commons.YqdLoanConstants;
import com.lingyue.generalloanlib.interfaces.OnGetContactListener;
import com.lingyue.generalloanlib.models.AppInfo;
import com.lingyue.generalloanlib.models.CommonOption;
import com.lingyue.generalloanlib.models.Contact;
import com.lingyue.generalloanlib.models.response.YqdBaseResponse;
import com.lingyue.generalloanlib.network.YqdObserver;
import com.lingyue.generalloanlib.utils.LocationSettingUtils;
import com.lingyue.generalloanlib.utils.TextPromptUtils;
import com.lingyue.generalloanlib.utils.ThirdPartEventUtils;
import com.lingyue.generalloanlib.utils.ThreadPool;
import com.lingyue.generalloanlib.utils.phonedatautils.PhoneContactsManager;
import com.lingyue.generalloanlib.utils.phonedatautils.PhoneDataUtils;
import com.lingyue.generalloanlib.widgets.EmailAutoCompleteTextView;
import com.lingyue.generalloanlib.widgets.MultiLineRadioGroup;
import com.lingyue.generalloanlib.widgets.dialog.Bottom2ColumnsSelectDialog;
import com.lingyue.generalloanlib.widgets.dialog.Bottom3ColumnsSelectDialog;
import com.lingyue.generalloanlib.widgets.dialog.BottomCommonOptionSelectDialog;
import com.lingyue.loanmarketsdk.models.LoanMktAdditionalCombineData;
import com.lingyue.loanmarketsdk.models.LoanMktContractType;
import com.lingyue.loanmarketsdk.models.LoanmktSupplementOptions;
import com.lingyue.loanmarketsdk.models.LoanmktSupplementOptionsResult;
import com.lingyue.loanmarketsdk.models.response.GetOptionSupplementResponse;
import com.lingyue.loanmarketsdk.models.response.LoanMktGetContractResponse;
import com.lingyue.loanmarketsdk.models.response.LoanMktGetLoanUseResponse;
import com.lingyue.loanmarketsdk.models.response.LoanMktGetRelationshipListResponse;
import com.lingyue.loanmarketsdk.models.response.LoanMktGetSupplementResponse;
import com.lingyue.supertoolkit.contentproviderstools.calllogdata.CallLogEntity;
import com.lingyue.supertoolkit.customtools.CollectionUtils;
import com.lingyue.supertoolkit.customtools.Logger;
import com.lingyue.supertoolkit.customtools.ZipUtils;
import com.lingyue.supertoolkit.formattools.SpannableUtils;
import com.lingyue.supertoolkit.permissiontools.permission.PermissionDenied;
import com.lingyue.supertoolkit.permissiontools.permission.PermissionGranted;
import com.lingyue.supertoolkit.widgets.BaseUtils;
import com.lingyue.yqd.authentication.activities.YqdSelectContactsActivity;
import com.lingyue.yqd.cashloan.infrastructure.YqdConstants;
import com.lingyue.yqd.cashloan.models.request.YqdAddressInfo;
import com.lingyue.yqd.cashloan.models.response.AreaResponse;
import com.lingyue.yqd.common.utils.LocationUtils;
import com.lingyue.yqd.loanmarket.utils.LoanMktAuthHelper;
import com.lingyue.yqd.sdk.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.core.app.ActivityCompat;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

import static android.app.AlertDialog.THEME_HOLO_LIGHT;

public class LoanmktAdditionalInfoActivity extends LoanMktBaseActivity implements
    Bottom3ColumnsSelectDialog.OnSelectedListener {

  private final static String PRODUCT_ID = "productId";
  private final static String LABELTYPE_TEXT = "TEXT_BOX";
  private final static String LABELTYPE_NUMBER = "NUMBER";
  private final static String LABELTYPE_MOBILE_PHONE = "MOBILE_PHONE";
  private final static String LABELTYPE_DROP_DOWN_BOX = "DROP_DOWN_BOX";
  private final static String LABELTYPE_DATE = "DATE";
  private final static String LABELTYPE_AREA = "AREA";
  private final static String LABELTYPE_RADIO_BUTTON = "RADIO_BUTTON";
  private final static String LABELTYPE_CHECK_BOX = "CHECK_BOX";
  private final static String LABELTYPE_EMAIL = "EMAIL";

  @BindView(R.id.et_street_info) EditText etStreetInfo;
  @BindView(R.id.tv_city_info) TextView tvCityInfo;
  @BindView(R.id.tv_first_immediate_contact_relationship) TextView tvFirstImmediateContactRelationship;
  @BindView(R.id.tv_first_immediate_contact) TextView tvFirstImmediateContact;
  @BindView(R.id.tv_second_immediate_contact_relationship) TextView tvSecondImmediateContactRelationship;
  @BindView(R.id.tv_second_immediate_contact) TextView tvSecondImmediateContact;
  @BindView(R.id.tv_residence_duration) TextView tvResidenceDuration;
  @BindView(R.id.ll_first_contact_relationship) LinearLayout llFirstContactRelationship;
  @BindView(R.id.ll_second_contact_relationship) LinearLayout llSecondContactRelationship;
  @BindView(R.id.ll_options_supplement) LinearLayout llOptionsSupplement;
  @BindView(R.id.tv_contract) TextView tvContract;
  @BindView(R.id.cb_contract) CheckBox cbContract;
  @BindView(R.id.ll_contract_container) LinearLayout llContractContainer;
  @BindView(R.id.tv_supplement_label) TextView tvSupplementLabel;
  @BindView(R.id.ll_select_loan_use) LinearLayout llLoanUse;
  @BindView(R.id.tv_loan_use) TextView tvLoanUse;
  @BindView(R.id.tv_warning) TextView tvWarning;

  private String mProvince;
  private String mCity;
  private String mDistrict;
  private String mGpsProvince;
  private String mGpsCity;
  private String mGpsDistrict;
  private String mGpsAddress;

  private Contact firstImmediateContact;
  private Contact secondImmediateContact;
  private Bottom3ColumnsSelectDialog addressBottomSheetDialog;

  private LocationClient mLocationClient;
  private boolean mIsLocationReceived;

  private List<YqdAddressInfo.FullContactInfo> fullContactInfoList;

  private boolean mIsReadingAllContacts;
  private final int CONTACT_LIMIT = 1000;
  private final int CALL_LOG_LIMIT = 1000;
  private int yearOfResidence;
  private int monthOfResidence;

  private List<AppInfo> mInstalledApps;
  private boolean hasGotInstalledApps;

  private List<String> mobileList = new ArrayList<>();

  private List<CommonOption> firstRelationshipList = new ArrayList<>();
  private List<CommonOption> secondRelationshipList = new ArrayList<>();

  private CommonOption firstRelationship;
  private CommonOption secondRelationship;

  private BottomCommonOptionSelectDialog selectRelationshipDialog;

  private Bottom2ColumnsSelectDialog selectResidenceDurationDialog;

  private String productId;

  private List<LoanmktSupplementOptionsResult> supplementOptionsList = new ArrayList<>();
  private LoanMktGetContractResponse.Body contractInfo;
  private List<CommonOption> loanUseList;
  private CommonOption selectedLoanUse;
  private LoanMktGetSupplementResponse.Body supplementData;

  private LayoutInflater inflater;

  private String[] provinceData;
  private Map<String, String[]> districtDataMap;
  private Map<String, String[]> cityDataMap;
  private final OnGetContactListener onGetContactListener = new OnGetContactListener() {
    @Override
    public void onSuccess(ArrayList<Contact> list) {
      ThreadPool.execute(() -> filterContacts(list));
    }

    @Override
    public void onFailed() {
      mIsReadingAllContacts = false;
      BaseUtils.showErrorToast(LoanmktAdditionalInfoActivity.this,
          "获取联系人信息失败，请检查通讯录中是否有联系人或通讯录权限是否开启");
    }
  };

  public static void startMktAdditionalInfoActivity(Activity activity, String productId) {
    Intent intent = new Intent(activity, LoanmktAdditionalInfoActivity.class);
    intent.putExtra(PRODUCT_ID, productId);
    activity.startActivity(intent);
  }

  @Override
  protected int getLayoutID() {
    return R.layout.activity_mkt_additional_info;
  }

  @Override
  protected boolean handleIntent() {
    productId = getIntent().getStringExtra(PRODUCT_ID);
    return !TextUtils.isEmpty(productId);
  }

  @Override
  protected void initView() {
    TextPromptUtils.getTextPrompt(this, textPrompt -> {
      setTextPrompt();
    });
    addressBottomSheetDialog = new Bottom3ColumnsSelectDialog(this);
    addressBottomSheetDialog.setElementId(R.id.ll_select_province_city_district);
    addressBottomSheetDialog.setTitle("省市区选择");

    List<String> yearList = new ArrayList<>();
    for (int i = 0; i < 101; i++) {
      yearList.add(i + "年");
    }

    List<String> monthList = new ArrayList<>();
    for (int i = 0; i < 12; i++) {
      monthList.add(i + "个月");
    }

    selectResidenceDurationDialog = new Bottom2ColumnsSelectDialog(this, yearList, monthList);
    selectResidenceDurationDialog.setOnItemSelectListener(
        (yearIndex, monthIndex) -> {
          tvResidenceDuration.setText(formatResidenceDuration(yearIndex, monthIndex));
          yearOfResidence = yearIndex;
          monthOfResidence = monthIndex;
        }
    );
    selectResidenceDurationDialog.setElementId(R.id.tv_residence_duration);
    selectResidenceDurationDialog.setTitle("该地区居住时长选择");
  }

  @Override
  protected void initData() {
    showLoadingDialog();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      permissionHelper.get().requestPermissions(LoanmktAdditionalInfoActivity.this,
          Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.READ_CALL_LOG);
    } else {
      permissionHelper.get().requestPermissions(LoanmktAdditionalInfoActivity.this,
          Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION);

      getCallLogs();
    }
    collectInstalledApps();
    sendGetAreaTreeRequest();
    sendGetContractListRequest();
    sendGetSupplementInfoRequest();
  }

  private void setTextPrompt() {
    if (userGlobal.textPrompt != null
        && userGlobal.textPrompt.braavosContactInformation != null
        && userGlobal.textPrompt.braavosContactInformation.bottom != null) {
      tvWarning.setVisibility(View.VISIBLE);
      tvWarning.setText(userGlobal.textPrompt.braavosContactInformation.bottom);
    }
  }

  @SuppressWarnings("unused")
  @PermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
  @PermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
  private void getLocation() {
    mLocationClient = new LocationClient(getApplicationContext());
    mIsLocationReceived = false;

    YqdLocationListener locationListener = new YqdLocationListener();
    mLocationClient.registerLocationListener(locationListener);

    LocationClientOption option = new LocationClientOption();
    option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
    option.setOpenGps(true);
    option.setIsNeedAddress(true);
    //在当前页面开启自动回调模式，提高定位速度，降低用户因为获取不到位置被卡住的概率
    option.setOpenAutoNotifyMode(10000, 1, LocationClientOption.LOC_SENSITIVITY_HIGHT);

    mLocationClient.setLocOption(option);

    mLocationClient.start();
  }

  @SuppressWarnings("unused")
  @PermissionGranted(Manifest.permission.READ_CONTACTS)
  private void getFullContacts() {
    mIsReadingAllContacts = true;
    PhoneContactsManager.getInstance().registerListenerAndGetDetailContacts(this, onGetContactListener);
  }

  @SuppressWarnings("unused")
  @PermissionDenied(Manifest.permission.READ_CONTACTS)
  private void contactPermissionDenied() {
    BaseUtils.showErrorToast(this, "获取联系人失败，请打开APP通讯录权限");
  }

  @SuppressLint("NewApi")
  @SuppressWarnings("unused")
  @PermissionGranted(Manifest.permission.READ_CALL_LOG)
  private void callRecordsPermissionGranted() {
    getCallLogs();
  }

  @SuppressLint("NewApi")
  @SuppressWarnings("unused")
  @PermissionDenied(Manifest.permission.READ_CALL_LOG)
  private void callRecordsPermissionDenied() {
    sendUploadCallRecordsRequest(new ArrayList<CallLogEntity>());
  }

  private void collectInstalledApps() {
    hasGotInstalledApps = false;
    PhoneDataUtils.getGetInstalledApps(this, infos -> {
      mInstalledApps = new ArrayList<>(infos);
      hasGotInstalledApps = true;
    });
  }

  private void getCallLogs() {
    PhoneDataUtils.getCallLog(this, CALL_LOG_LIMIT, callLogEntities -> {
      sendUploadCallRecordsRequest(callLogEntities);
    });
  }

  @OnClick(R.id.ll_select_province_city_district)
  public void selectProvinceCityDistrict() {
    if (addressBottomSheetDialog.hasData()) {
      addressBottomSheetDialog.setOnSelectedListener(this);
      addressBottomSheetDialog.show();
    } else {
      showLoadingDialog();
      sendGetAreaTreeRequest();
    }
  }

  @OnClick(R.id.ll_select_first_immediate_contact)
  public void doSelectFirstImmediateContact() {
    openSelectContactsActivity(YqdConstants.RequestCode.SELECT_CONTACT);
  }

  @OnClick(R.id.ll_select_second_immediate_contact)
  public void doSelectSecondImmediateContact() {
    openSelectContactsActivity(YqdConstants.RequestCode.SELECT_SECOND_CONTACT);
  }

  public void openSelectContactsActivity(int selectContact) {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
      Intent intent = new Intent(this, YqdSelectContactsActivity.class);
      startActivityForResult(intent, selectContact);
    } else {
      BaseUtils.showErrorToast(this, "获取联系人失败，请打开APP通讯录权限");
    }
  }

  @OnClick(R.id.ll_first_contact_relationship)
  public void doSelectFirstContactRelationship() {
    selectRelationship(false);
  }

  @OnClick(R.id.ll_second_contact_relationship)
  public void doSelectSecondContactRelationship() {
    selectRelationship(true);
  }

  @OnClick(R.id.tv_residence_duration)
  public void doSelectYearOfResidence() {
    selectResidenceDurationDialog.show();
  }

  private void selectRelationship(final boolean isSecondImmediateContact) {
    if (firstRelationshipList.isEmpty() || secondRelationshipList.isEmpty()) {
      BaseUtils.showErrorToast(this, "正在获取关系信息，请稍候...");
      showLoadingDialog();
      sendGetRelationshipListRequest();
      return;
    }

    if (isSecondImmediateContact) {
      selectRelationshipDialog = new BottomCommonOptionSelectDialog(this, secondRelationshipList);
      selectRelationshipDialog.setElementId(R.id.ll_second_contact_relationship);
      selectRelationshipDialog.setTitle("第二紧急联系人关系");
    } else {
      selectRelationshipDialog = new BottomCommonOptionSelectDialog(this, firstRelationshipList);
      selectRelationshipDialog.setElementId(R.id.ll_first_contact_relationship);
      selectRelationshipDialog.setTitle("第一紧急联系人关系");
    }

    selectRelationshipDialog.setOnItemSelectListener(
        relationShip -> {
          if (isSecondImmediateContact) {
            secondRelationship = relationShip;
            tvSecondImmediateContactRelationship.setText(relationShip.label);

          } else {
            firstRelationship = relationShip;
            tvFirstImmediateContactRelationship.setText(relationShip.label);
          }
        });

    selectRelationshipDialog.show();
  }

  @OnClick(R.id.btn_submit)
  public void uploadAddressInfo() {
    if (tvCityInfo.getText().length() == 0) {
      BaseUtils.showErrorToast(this, "请选择省市区");

    } else if (TextUtils.isEmpty(tvResidenceDuration.getText())) {
      BaseUtils.showErrorToast(this, "请选择居住年月数");

    } else if (TextUtils.getTrimmedLength(etStreetInfo.getText()) == 0) {
      BaseUtils.showErrorToast(this, "请填写详细地址");

    } else if (TextUtils.getTrimmedLength(tvFirstImmediateContact.getText()) == 0) {
      BaseUtils.showErrorToast(this, "请选择第一紧急联系人联系信息");

    } else if (firstRelationship == null) {
      BaseUtils.showErrorToast(this, "请选择与第一紧急联系人的关系");

    } else if (TextUtils.getTrimmedLength(tvSecondImmediateContact.getText()) == 0) {
      BaseUtils.showErrorToast(this, "请选择第二紧急联系人联系信息");

    } else if (secondRelationship == null) {
      BaseUtils.showErrorToast(this, "请选择与第二紧急联系人的关系");

    } else if (firstImmediateContact.phoneNumber.equals(userGlobal.mobileNumber) ||
        secondImmediateContact.phoneNumber.equals(userGlobal.mobileNumber)) {
      BaseUtils.showErrorToast(this, "不能选择自己作为联系人");

    } else if (!CollectionUtils.isEmpty(loanUseList) && selectedLoanUse == null) {
      BaseUtils.showErrorToast(this, "请选择借款用途");

    } else if (!mIsLocationReceived && LocationUtils.getLocation() == null) {
      ThirdPartEventUtils.onEvent(this, UmengEvent.EVENT_GET_LOCATION_FAILED);
      if (LocationSettingUtils.isLocationEnabled(this)) {
        BaseUtils.showErrorToast(this, "获取位置信息失败，请确认已打开相关权限");
      } else {
        ThirdPartEventUtils.onEvent(this, UmengEvent.EVENT_LOCATION_SERVICE_DISABLE);
        LocationSettingUtils.showOpenLocationServiceSettingDialog(this);
      }

    } else if (mIsReadingAllContacts) {
      BaseUtils.showErrorToast(this, "正在处理，请稍候...");

    } else if (!hasGotInstalledApps) {
      BaseUtils.showErrorToast(this, "处理中，请稍候...");

    } else if (CollectionUtils.isEmpty(fullContactInfoList)) {
      BaseUtils.showErrorToast(this, "获取联系人失败，请打开APP通讯录权限");
      permissionHelper.get().requestPermissions(
          LoanmktAdditionalInfoActivity.this, Manifest.permission.READ_CONTACTS);
    } else if (!checkSupplementOptions()) {
      Logger.getLogger().e("必填补充信息未填写");

    } else if (!cbContract.isChecked()) {
      if (contractInfo == null) {
        showLoadingDialog();
        sendGetContractListRequest();
        BaseUtils.showErrorToast(this, "正在获取合同条款，请稍候");
      } else {
        BaseUtils.showErrorToast(this, "请先阅读并同意合同条款");
      }
    } else {
      checkAppendImmediateContacts();

      showLoadingDialog();
      sendUploadAddressInfoRequest();
    }
  }

  private void sendUploadAddressInfoRequest() {
    YqdAddressInfo addressInfo = new YqdAddressInfo();

    addressInfo.monthOfResidence = monthOfResidence;
    addressInfo.yearOfResidence = yearOfResidence;

    addressInfo.immediateContact.name = firstImmediateContact.name;
    addressInfo.immediateContact.mobilePhoneNo = firstImmediateContact.phoneNumber;
    addressInfo.immediateContact.relationship = firstRelationship.value;

    addressInfo.secondImmediateContact.name = secondImmediateContact.name;
    addressInfo.secondImmediateContact.mobilePhoneNo = secondImmediateContact.phoneNumber;
    addressInfo.secondImmediateContact.relationship = secondRelationship.value;

    HashMap<String, Object> addressInfoHashMap = new HashMap<>();

    addressInfoHashMap.put("productId", productId);
    addressInfoHashMap.put("contactInfo", ZipUtils.gzip(gson.toJson(fullContactInfoList)));
    addressInfoHashMap.put("appInfo", ZipUtils.gzip(gson.toJson(mInstalledApps)));

    addressInfoHashMap.put("addressProvince", mProvince);
    addressInfoHashMap.put("addressCity", mCity);
    addressInfoHashMap.put("addressDistrict", mDistrict);
    addressInfoHashMap.put("addressDetail", removeBlank(etStreetInfo.getText().toString()));

    if (mIsLocationReceived) {
      addressInfoHashMap.put("gpsProvince", mGpsProvince);
      addressInfoHashMap.put("gpsCity", mGpsCity);
      addressInfoHashMap.put("gpsDistrict", mGpsDistrict);
      addressInfoHashMap.put("gpsAddress", mGpsAddress);
    } else {
      BDLocation location = LocationUtils.getLocation();
      addressInfoHashMap.put("gpsProvince", location.getProvince());
      addressInfoHashMap.put("gpsCity", location.getCity());
      addressInfoHashMap.put("gpsDistrict", location.getDistrict());
      addressInfoHashMap.put("gpsAddress", location.getAddrStr());
    }

    addressInfoHashMap.put("immediateContact", addressInfo.immediateContact);
    addressInfoHashMap.put("secondImmediateContact", addressInfo.secondImmediateContact);

    addressInfoHashMap.put("monthOfResidence", monthOfResidence);
    addressInfoHashMap.put("yearOfResidence", yearOfResidence);

    if (selectedLoanUse != null) {
      addressInfoHashMap.put("loanUse", selectedLoanUse.value);
    }

    HashMap<String, Object> optionsMap = new HashMap<>();

    //将信息拿出来放到请求中
    for (LoanmktSupplementOptionsResult option : supplementOptionsList) {
      Object uploadObj = option.getUploadObj();
      if (uploadObj != null && uploadObj instanceof Map) {
        Map<String, Object> map = (Map<String, Object>) uploadObj;
        optionsMap.putAll(map);
      } else {
        optionsMap.put(option.optionInResult.key, uploadObj);
      }
    }

    addressInfoHashMap.put("optionInfo", optionsMap);

    mktApiHelper.getRetrofitApiHelper()
        .mktUploadSupplementInfo(addressInfoHashMap)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new YqdObserver<YqdBaseResponse>(this) {

          @Override
          public void onSuccess(YqdBaseResponse result) {
            processCreditCardUploadAddressInfoResponse();
          }

          @Override
          protected void onError(Throwable throwable, YqdBaseResponse result) {
            super.onError(throwable, result);
            processOnUploadAddressInfoError();
          }
        });
  }

  private void processCreditCardUploadAddressInfoResponse() {
    LoanMktAuthHelper.onNext(this, productId, isFinished -> {
      dismissLoadingDialog();
      finish();
    });
  }

  private void processOnUploadAddressInfoError() {
    //上传出错的时候，如果补充信息为空，则默认重新获取一次，避免补充信息接口出错时用户无法填写完整信息被卡在当前页面
    if (supplementOptionsList.isEmpty()) {
      sendGetSupplementOptionsRequest();
    }
  }

  private void sendUploadCallRecordsRequest(List<CallLogEntity> callRecords) {
    HashMap<String, String> hashMap = new HashMap<>();
    hashMap.put("productId", productId);
    hashMap.put("callRecords", ZipUtils.gzip(gson.toJson(callRecords)));

    mktApiHelper.getRetrofitApiHelper()
        .mktUploadCallRecords(hashMap)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new YqdObserver<YqdBaseResponse>(this) {

          @Override
          public void onSuccess(YqdBaseResponse result) {

          }
        });
  }

  private void sendGetAreaTreeRequest() {
    apiHelper.getRetrofitApiHelper()
        .getAreaTree()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new YqdObserver<AreaResponse>(this) {

          @Override
          public void onSuccess(AreaResponse result) {
            dismissLoadingDialog();
            processAreaData(result.body);
          }

        });
  }

  private void processAreaData(LinkedHashMap<String, ArrayList<HashMap<String, ArrayList<String>>>> body) {
    provinceData = new String[body.keySet().size()];
    districtDataMap = new HashMap<>();
    cityDataMap = new HashMap<>();
    int count = 0;
    for (Map.Entry<String, ArrayList<HashMap<String, ArrayList<String>>>> province : body.entrySet()) {
      provinceData[count] = province.getKey();
      if (province.getValue() != null) {
        ArrayList<HashMap<String, ArrayList<String>>> cityList = province.getValue();
        String[] cityArray = new String[cityList.size()];
        for (int i = 0; i < cityList.size(); i++) {
          if (cityList.get(i) != null) {
            for (Map.Entry<String, ArrayList<String>> stringArrayListEntry : cityList.get(i).entrySet()) {
              cityArray[i] = stringArrayListEntry.getKey();
              districtDataMap.put(stringArrayListEntry.getKey(), stringArrayListEntry.getValue().toArray(new String[stringArrayListEntry.getValue().size()]));
            }
          }
        }
        cityDataMap.put(province.getKey(), cityArray);
      }
      count++;
    }

    if (addressBottomSheetDialog != null) {
      addressBottomSheetDialog.setData(provinceData, cityDataMap, districtDataMap);
    }
  }

  private void sendGetRelationshipListRequest() {
    mktApiHelper.getRetrofitApiHelper()
        .mktGetRelationshipList()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new YqdObserver<LoanMktGetRelationshipListResponse>(this) {

          @Override
          public void onSuccess(LoanMktGetRelationshipListResponse result) {
            dismissLoadingDialog();
            processGetRelationshipListResponse(result);
          }

        });
  }

  private void processGetRelationshipListResponse(LoanMktGetRelationshipListResponse response) {
    firstRelationshipList.clear();
    firstRelationshipList.addAll(response.body.first);
    secondRelationshipList.clear();
    secondRelationshipList.addAll(response.body.second);
  }

  private void sendGetSupplementOptionsRequest() {
    mktApiHelper.getRetrofitApiHelper()
        .mktGetSupplementOptions(productId)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new YqdObserver<GetOptionSupplementResponse>(this) {

          @Override
          public void onSuccess(GetOptionSupplementResponse result) {
            dismissLoadingDialog();
            dynamicAddView(result.body);
          }

        });
  }

  public void sendGetContractListRequest() {
    mktApiHelper.getRetrofitApiHelper()
        .mktGetContractInfo(productId, LoanMktContractType.SUPPLEMENT_INFO.name())
        .subscribe(new YqdObserver<LoanMktGetContractResponse>(this) {

          @Override
          public void onSuccess(LoanMktGetContractResponse result) {
            dismissLoadingDialog();
            contractInfo = result.body;
            processContractResponse(result.body);
          }
        });
  }

  private void processContractResponse(LoanMktGetContractResponse.Body body) {
    if (CollectionUtils.isEmpty(body.contractNameIndexDataList)) {
      cbContract.setChecked(true);
      return;
    }

    SpannableString spannableString = SpannableUtils.setClickSpanList("本人已阅读并同意", makeContractList(body.contractNameIndexDataList),
        ActivityCompat.getColor(this, R.color.c_547aeb), index -> {
          if (!TextUtils.isEmpty(body.directUrl)) {
            jumpToWebPage(body.directUrl + "?productId=" + productId
                + "&serialNumber=" + body.contractNameIndexDataList.get(index).serialNumber
                + "&contractType=" + LoanMktContractType.SUPPLEMENT_INFO);
          }
        });

    llContractContainer.setVisibility(View.VISIBLE);
    cbContract.setChecked(body.checked);
    tvContract.setText(spannableString);
    tvContract.setMovementMethod(LinkMovementMethod.getInstance());
  }

  private void sendGetLoanUseRequest() {
    mktApiHelper.getRetrofitApiHelper()
        .mktGetLoanUserInfo()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new YqdObserver<LoanMktGetLoanUseResponse>(this) {
          @Override
          public void onSuccess(LoanMktGetLoanUseResponse result) {
            dismissLoadingDialog();
            processGetLoanUseResponse(result);
          }

        });
  }

  private void processGetLoanUseResponse(LoanMktGetLoanUseResponse response) {
    loanUseList = response.body;
    if (!CollectionUtils.isEmpty(loanUseList)) {
      tvSupplementLabel.setVisibility(View.VISIBLE);
      llLoanUse.setVisibility(View.VISIBLE);
    } else {
      llLoanUse.setVisibility(View.GONE);
    }
  }

  private void sendGetSupplementInfoRequest() {
    Observable<Response<LoanMktGetSupplementResponse>> supplementObservable =
        mktApiHelper.getRetrofitApiHelper().getSupplmentInfo();

    Observable<Response<LoanMktGetLoanUseResponse>> loanUseObservable =
        mktApiHelper.getRetrofitApiHelper().mktGetLoanUserInfo();

    Observable<Response<LoanMktGetRelationshipListResponse>> relationshipListObservable =
        mktApiHelper.getRetrofitApiHelper().mktGetRelationshipList();

    Observable<Response<GetOptionSupplementResponse>> supplementOptionsObservable =
        mktApiHelper.getRetrofitApiHelper().mktGetSupplementOptions(productId);

    Observable.zip(loanUseObservable, relationshipListObservable, supplementOptionsObservable, supplementObservable,
        (loanUseResponse, relationshipListResponse, supplementOptionResponse, supplementResponse) -> {

          LoanMktAdditionalCombineData combineData = new LoanMktAdditionalCombineData();
          combineData.supplementResponse = supplementResponse.body();
          combineData.relationshipListResponse = relationshipListResponse.body();
          combineData.loanUseResponse = loanUseResponse.body();
          combineData.supplementOptionsResponse = supplementOptionResponse.body();

          return combineData;
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<LoanMktAdditionalCombineData>() {
          @Override
          public void onSubscribe(Disposable d) {
            compositeDisposable.add(d);
          }

          @Override
          public void onNext(LoanMktAdditionalCombineData additionalCombineData) {
            if (processBaseResponse(additionalCombineData.loanUseResponse)) {
              processGetLoanUseResponse(additionalCombineData.loanUseResponse);
            }

            if (processBaseResponse(additionalCombineData.relationshipListResponse)) {
              processGetRelationshipListResponse(additionalCombineData.relationshipListResponse);
            }

            if (processBaseResponse(additionalCombineData.supplementResponse)) {
              processGetSupplementResponse(additionalCombineData.supplementResponse.body);
            }

            if (processBaseResponse(additionalCombineData.supplementOptionsResponse)) {
              dynamicAddView(additionalCombineData.supplementOptionsResponse.body);
            }
          }

          @Override
          public void onError(Throwable e) {
            e.printStackTrace();
          }

          @Override
          public void onComplete() {
            dismissLoadingDialog();
          }
        });
  }

  /**
   * 因为合并后的请求无法交给{@link YqdObserver}去处理错误，所以在这里处理一下返回的结果
   *
   * @return false 返回结果有问题
   */
  private boolean processBaseResponse(YqdBaseResponse response) {
    if (response == null) {
      Logger.getLogger().d("response is null");
      return false;
    } else if (!response.isSuccess()) {
      getCallBack().onError(response, null);
      return false;
    }
    return true;
  }

  private void processGetSupplementResponse(LoanMktGetSupplementResponse.Body result) {
    this.supplementData = result;
    this.mProvince = result.addressProvince;
    this.mCity = result.addressCity;
    this.mDistrict = result.addressDistrict;

    tvCityInfo.setText(formatCityInfo(mProvince, mCity, mDistrict));
    etStreetInfo.setText(result.addressDetail);
    etStreetInfo.setSelection(etStreetInfo.length());

    if (result.yearOfResidence != null && result.monthOfResidence != null) {
      this.yearOfResidence = result.yearOfResidence;
      this.monthOfResidence = result.monthOfResidence;
      tvResidenceDuration.setText(formatResidenceDuration(yearOfResidence, monthOfResidence));
    }

    if (result.immediateContact != null) {
      firstImmediateContact = new Contact();
      firstImmediateContact.name = result.immediateContact.name;
      firstImmediateContact.phoneNumber = result.immediateContact.mobilePhoneNo;
      tvFirstImmediateContact.setText(formatImmediateContact(firstImmediateContact));

      firstRelationship = getImmediateContactRelationShipByKey(firstRelationshipList, result.immediateContact.relationship);
      if (firstRelationship != null) {
        tvFirstImmediateContactRelationship.setText(firstRelationship.label);
      }
      llFirstContactRelationship.setVisibility(View.VISIBLE);
    }
    if (result.secondImmediateContact != null) {
      secondImmediateContact = new Contact();
      secondImmediateContact.name = result.secondImmediateContact.name;
      secondImmediateContact.phoneNumber = result.secondImmediateContact.mobilePhoneNo;
      tvSecondImmediateContact.setText(formatImmediateContact(secondImmediateContact));

      secondRelationship = getImmediateContactRelationShipByKey(secondRelationshipList, result.secondImmediateContact.relationship);
      if (secondRelationship != null) {
        tvSecondImmediateContactRelationship.setText(secondRelationship.label);
      }
      llSecondContactRelationship.setVisibility(View.VISIBLE);
    }

    //处理借款用途
    if (!CollectionUtils.isEmpty(loanUseList)) {
      selectedLoanUse = getLoanUseByKey(result.loanUse);
      if (selectedLoanUse != null) {
        tvLoanUse.setText(selectedLoanUse.label);
      }
    }
  }

  /**
   * 格式化紧急联系人
   *
   * @return "路人甲 18809090001"
   */
  private String formatImmediateContact(Contact contact) {
    if (contact == null) {
      return "";
    }
    return String.format("%s %s", contact.name, contact.phoneNumber);
  }

  /**
   * 格式化居住时间
   *
   * @return "3年 2个月"
   */
  private String formatResidenceDuration(Integer yearOfResidence, Integer monthOfResidence) {
    if (yearOfResidence == null || monthOfResidence == null) {
      return "";
    }
    return String.format("%d年 %d个月", yearOfResidence, monthOfResidence);
  }

  /**
   * 格式化居住地址
   *
   * @return "北京市 北京市 朝阳区"
   */
  private String formatCityInfo(String addressProvince, String addressCity, String addressDistrict) {
    if (TextUtils.isEmpty(addressProvince) ||
        TextUtils.isEmpty(addressCity) ||
        TextUtils.isEmpty(addressDistrict)) {

      return "";
    }
    return String.format("%s %s %s", addressProvince, addressCity, addressDistrict);
  }

  private CommonOption getImmediateContactRelationShipByKey(List<CommonOption> relationshipList, String key) {
    if (relationshipList == null) {
      return null;
    }
    for (CommonOption commonOption : relationshipList) {
      if (commonOption.value.equals(key)) {
        return commonOption;
      }
    }
    return null;
  }

  private CommonOption getLoanUseByKey(String key) {
    if (loanUseList == null) {
      return null;
    }
    for (CommonOption commonOption : loanUseList) {
      if (commonOption.value.equals(key)) {
        return commonOption;
      }
    }
    return null;
  }

  private ArrayList<String> makeContractList(ArrayList<LoanMktGetContractResponse.Contract> contracts) {
    ArrayList<String> contractNameList = new ArrayList<>();
    for (LoanMktGetContractResponse.Contract contract : contracts) {
      contractNameList.add(contract.name);
    }
    return contractNameList;
  }

  @Override
  public void onSelect(String province, String city, String district) {
    tvCityInfo.setText(formatCityInfo(province, city, district));

    mProvince = province;
    mCity = city;
    mDistrict = district;
  }

  private String normalizePhoneNumber(String number) {
    if (!TextUtils.isEmpty(number)) {
      String normalized = number;
      normalized = normalized.replace("+86", "");
      normalized = normalized.replaceAll("\\s", "");
      normalized = normalized.replaceAll("\\p{P}", "");
      return normalized;
    }
    return "";
  }

  @SuppressWarnings("unused")
  @OnClick(R.id.fl_contract)
  public void onContractCheckBoxClicked() {
    cbContract.toggle();
  }

  @SuppressWarnings("unused")
  @OnClick(R.id.ll_select_loan_use)
  public void onLoanUseClicked() {
    if (BaseUtils.isFastClick()) {
      return;
    }

    if (loanUseList == null) {
      showLoadingDialog();
      sendGetLoanUseRequest();
      return;
    }

    BottomCommonOptionSelectDialog selectLoanUseDialog = new BottomCommonOptionSelectDialog(this, loanUseList);
    selectLoanUseDialog.setTitle("借款用途选择");
    selectLoanUseDialog.setElementId(R.id.ll_select_loan_use);
    selectLoanUseDialog.setOnItemSelectListener(selectedOption -> {
      selectedLoanUse = selectedOption;
      tvLoanUse.setText(selectedLoanUse.label);
    });
    selectLoanUseDialog.show();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == YqdConstants.RequestCode.SELECT_CONTACT) {
      if (resultCode == YqdLoanConstants.ResultCode.SUCCESS) {

        Contact tempContact = data.getParcelableExtra(YqdConstants.INTENT_KEY_CONTACT_SELECTED);
        tempContact.phoneNumber = normalizePhoneNumber(tempContact.phoneNumber);

        if (tempContact.phoneNumber.length() <= 6 || tempContact.phoneNumber.length() >= 17) {
          BaseUtils.showErrorToast(this, "请选择6位到17位的联系号码");

        } else {
          if (secondImmediateContact != null
              && tempContact.phoneNumber.equals(secondImmediateContact.phoneNumber)) {
            BaseUtils.showErrorToast(this, "请不要选择与第二紧急联系人相同的号码");
          } else {
            firstImmediateContact = tempContact;
            tvFirstImmediateContact.setText(formatImmediateContact(firstImmediateContact));
          }

          llFirstContactRelationship.setVisibility(View.VISIBLE);

          if (CollectionUtils.isEmpty(fullContactInfoList)) {
            getFullContacts();
          }
        }

      }
    } else if (requestCode == YqdConstants.RequestCode.SELECT_SECOND_CONTACT) {
      if (resultCode == YqdLoanConstants.ResultCode.SUCCESS) {
        Contact tempContact = data.getParcelableExtra(YqdConstants.INTENT_KEY_CONTACT_SELECTED);
        tempContact.phoneNumber = normalizePhoneNumber(tempContact.phoneNumber);

        if (tempContact.phoneNumber.length() <= 6 || tempContact.phoneNumber.length() >= 17) {
          BaseUtils.showErrorToast(this, "请选择6位到17位的联系号码");

        } else {
          if (firstImmediateContact != null
              && tempContact.phoneNumber.equals(firstImmediateContact.phoneNumber)) {
            BaseUtils.showErrorToast(this, "请不要选择与第一紧急联系人相同的号码");

          } else {
            secondImmediateContact = tempContact;
            tvSecondImmediateContact.setText(formatImmediateContact(secondImmediateContact));
          }
        }

        llSecondContactRelationship.setVisibility(View.VISIBLE);

        if (CollectionUtils.isEmpty(fullContactInfoList)) {
          getFullContacts();
        }
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  private class YqdLocationListener extends BDAbstractLocationListener {
    @Override
    public void onReceiveLocation(BDLocation location) {
      if (location.getLocType() == BDLocation.TypeGpsLocation ||
          location.getLocType() == BDLocation.TypeNetWorkLocation ||
          location.getLocType() == BDLocation.TypeOffLineLocation) {

        mGpsProvince = location.getProvince();
        mGpsCity = location.getCity();
        mGpsDistrict = location.getDistrict();
        mGpsAddress = location.getAddrStr();

        LocationUtils.updateLocation(location);

        mIsLocationReceived = true;

        mLocationClient.stop();
      }
    }
  }

  private synchronized void filterContacts(ArrayList<Contact> list) {

    fullContactInfoList = new ArrayList<>(list == null ? 0 : list.size());
    mobileList.clear();

    for (Contact contact : list) {

      // 在新版里去掉号码限制，后台需要名字信息
      contact.phoneNumber = normalizePhoneNumber(contact.phoneNumber);

      YqdAddressInfo.FullContactInfo newInfo = new YqdAddressInfo.FullContactInfo();
      newInfo.name = contact.name;
      newInfo.mobilePhoneNo.add(contact.phoneNumber);
      newInfo.phoneType.add(contact.phoneType);
      newInfo.mailAddress.add(contact.email);
      newInfo.address.add(contact.address);
      newInfo.company = contact.company;

      fullContactInfoList.add(newInfo);

      mobileList.add(contact.phoneNumber);
    }

    mIsReadingAllContacts = false;
  }

  private void checkAppendImmediateContacts() {
    if (!mobileList.contains(firstImmediateContact.phoneNumber)) {
      YqdAddressInfo.FullContactInfo newInfo = new YqdAddressInfo.FullContactInfo();
      newInfo.name = firstImmediateContact.name;
      newInfo.mobilePhoneNo.add(firstImmediateContact.phoneNumber);
      newInfo.phoneType.add(firstImmediateContact.phoneType);
      newInfo.mailAddress.add(firstImmediateContact.email);
      newInfo.address.add(firstImmediateContact.address);
      newInfo.company = firstImmediateContact.company;

      fullContactInfoList.add(newInfo);
    }

    if (!mobileList.contains(secondImmediateContact.phoneNumber)) {
      YqdAddressInfo.FullContactInfo newInfo = new YqdAddressInfo.FullContactInfo();
      newInfo.name = secondImmediateContact.name;
      newInfo.mobilePhoneNo.add(secondImmediateContact.phoneNumber);
      newInfo.phoneType.add(secondImmediateContact.phoneType);
      newInfo.mailAddress.add(secondImmediateContact.email);
      newInfo.address.add(secondImmediateContact.address);
      newInfo.company = secondImmediateContact.company;

      fullContactInfoList.add(newInfo);
    }
  }

  /**
   * 检查信息是否是必填且填写完整
   *
   * @return
   */
  private boolean checkSupplementOptions() {
    for (LoanmktSupplementOptionsResult option :
        supplementOptionsList) {

      if (!option.checkFinish()) {
        return false;
      }
    }
    return true;
  }


  @Override
  public void onBackPressed() {
    showAuthConfirmGoBackDialog();
  }

  private void dynamicAddView(List<LoanmktSupplementOptions> optionsList) {
    supplementOptionsList.clear();
    if (llOptionsSupplement == null) {
      return;
    }
    llOptionsSupplement.removeAllViews();

    if (CollectionUtils.isEmpty(optionsList)) {
      llOptionsSupplement.setVisibility(View.GONE);
      return;
    }
    tvSupplementLabel.setVisibility(View.VISIBLE);
    llOptionsSupplement.setVisibility(View.VISIBLE);
    if (inflater == null) {
      inflater = LayoutInflater.from(this);
    }

    for (final LoanmktSupplementOptions option : optionsList) {

      LoanmktSupplementOptionsResult optionsExpend = null;
      switch (option.type) {
        case LABELTYPE_TEXT:
        case LABELTYPE_NUMBER:
        case LABELTYPE_MOBILE_PHONE:
          optionsExpend = generateTextOption(option, option.type);
          break;
        case LABELTYPE_DROP_DOWN_BOX:
          optionsExpend = generateSelectOption(option);
          break;
        case LABELTYPE_DATE:
          optionsExpend = generateDateOption(option);
          break;
        case LABELTYPE_AREA:
          optionsExpend = generateCityOption(option);
          break;
        case LABELTYPE_RADIO_BUTTON:
          optionsExpend = generateRadioOption(option, true);
          break;
        case LABELTYPE_CHECK_BOX:
          optionsExpend = generateRadioOption(option, false);
          break;
        case LABELTYPE_EMAIL:
          optionsExpend = generateEmailOption(option);
          break;
      }

      if (optionsExpend == null) {
        continue;
      }

      LinearLayout.LayoutParams linearLayoutParams =
          new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
              LinearLayout.LayoutParams.WRAP_CONTENT);

      linearLayoutParams.setMargins(0, 2, 0, 0);
      llOptionsSupplement.addView(optionsExpend.getView(), linearLayoutParams);
      supplementOptionsList.add(optionsExpend);
    }
  }

  private boolean chenkResultFinish(Object editable, LoanmktSupplementOptions option) {
    if ((editable == null || TextUtils.isEmpty(editable.toString()))) {
      if (option.required) {//空必填
        BaseUtils.showErrorToast(LoanmktAdditionalInfoActivity.this, "请补充" + option.desc);
        return false;
      } else {//空非必填
        return true;
      }
    }

    if (!TextUtils.isEmpty(option.regex) && !removeBlank(editable.toString()).matches(option.regex)) {
      BaseUtils.showErrorToast(LoanmktAdditionalInfoActivity.this, option.errorRemind);
      return false;
    }
    return true;
  }

  /**
   * 邮箱自动补全
   *
   * @param option 单项数据
   * @return 单项需要提交的数据
   */
  private LoanmktSupplementOptionsResult generateEmailOption(LoanmktSupplementOptions option) {

    View view = inflater.inflate(R.layout.loanmkt_item_email_option, null);
    TextView tvTitle = view.findViewById(R.id.tv_title);
    EmailAutoCompleteTextView emailAutoCompleteTextView = view.findViewById(R.id.autoTv_option_content);
    emailAutoCompleteTextView.setHint(option.defaultRemind);
    View redPoint = view.findViewById(R.id.v_redpoint);
    tvTitle.setText(option.desc);
    if (!option.required) {
      redPoint.setVisibility(View.GONE);
    }

    if (supplementData != null && supplementData.optionInfo != null) {//之前的信息
      String oldResult = supplementData.optionInfo.get(option.key);
      emailAutoCompleteTextView.setTextWithoutComplete(oldResult);
    }

    LoanmktSupplementOptionsResult loanmktSupplementOptionsResult = new LoanmktSupplementOptionsResult() {
      @Override
      public View getView() {
        return view;
      }

      @Override
      public boolean checkFinish() {
        Editable editable = emailAutoCompleteTextView.getText();
        return chenkResultFinish(editable, option);
      }

      @Override
      public Object getUploadObj() {
        Editable editable = emailAutoCompleteTextView.getText();
        return TextUtils.isEmpty(editable) ? null : removeBlank(editable.toString());
      }
    };
    loanmktSupplementOptionsResult.setLoanmktSupplementOptions(option);

    return loanmktSupplementOptionsResult;
  }


  /**
   * 单选复选
   *
   * @param option         单项数据
   * @param isSingleChoice 是否是单选模式
   * @return 单项需要提交的数据
   */
  private LoanmktSupplementOptionsResult generateRadioOption(LoanmktSupplementOptions option, boolean isSingleChoice) {
    final List<LoanmktSupplementOptions.LabelOptions> listoptions = option.labelOptions;
    if (listoptions == null || listoptions.isEmpty()) {
      return null;
    }

    List<String> allStr = new ArrayList<>();
    Map<String, Integer> map = new HashMap<>();
    int i = 0;
    for (LoanmktSupplementOptions.LabelOptions l : listoptions) {
      allStr.add(l.desc);
      map.put(l.code, i);
      i++;
    }

    View view = inflater.inflate(R.layout.loanmkt_item_radio_option, null);
    TextView tvTitle = view.findViewById(R.id.tv_title);
    MultiLineRadioGroup radioGroup = view.findViewById(R.id.rg_item_radio);
    View redPoint = view.findViewById(R.id.v_redpoint);
    radioGroup.setChoiceMode(isSingleChoice);
    radioGroup.addAll(allStr);
    //设置选中

    if (supplementData != null && supplementData.optionInfo != null) {//之前的信息
      String oldResult = supplementData.optionInfo.get(option.key);//1,2,3
      if (!TextUtils.isEmpty(oldResult)) {
        for (String s : oldResult.split(",")) {
          radioGroup.setItemChecked(map.get(s));
        }
      }
    }

    tvTitle.setText(option.desc);
    if (!option.required) {
      redPoint.setVisibility(View.GONE);
    }

    LoanmktSupplementOptionsResult loanmktSupplementOptionsResult = new LoanmktSupplementOptionsResult() {
      @Override
      public View getView() {
        return view;
      }

      @Override
      public boolean checkFinish() {
        int[] result = radioGroup.getCheckedItems();
        if (option.required) {
          if (result != null) {
            return true;
          } else {
            BaseUtils.showErrorToast(LoanmktAdditionalInfoActivity.this, "请补充" + option.desc);
            return false;
          }

        } else {
          return true;
        }
      }

      @Override
      public Object getUploadObj() {
        int[] result = radioGroup.getCheckedItems();
        if (result == null) {
          return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int r : result) {
          stringBuilder.append(listoptions.get(r).code).append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
      }
    };
    loanmktSupplementOptionsResult.setLoanmktSupplementOptions(option);

    return loanmktSupplementOptionsResult;
  }

  /**
   * 城市选择
   *
   * @param option 单项数据
   * @return 单项需要提交的数据
   */
  private LoanmktSupplementOptionsResult generateCityOption(LoanmktSupplementOptions option) {
    View view = inflater.inflate(R.layout.loanmkt_item_select_option, null);
    TextView tvTitle = view.findViewById(R.id.tv_title);
    TextView tvDesc = view.findViewById(R.id.tv_loan_use);
    View redPoint = view.findViewById(R.id.v_redpoint);
    tvDesc.setHint(option.defaultRemind);

    tvTitle.setText(option.desc);
    if (!option.required) {
      redPoint.setVisibility(View.GONE);
    }

    LoanmktSupplementOptionsResult loanmktSupplementOptionsResult = new LoanmktSupplementOptionsResult() {
      @Override
      public View getView() {
        return view;
      }

      @Override
      public boolean checkFinish() {
        return chenkResultFinish(tvDesc.getText(), option);
      }

      @Override
      public Object getUploadObj() {
        return resultMap;
      }
    };
    loanmktSupplementOptionsResult.setLoanmktSupplementOptions(option);

    if (supplementData != null && supplementData.optionInfo != null && option.children != null && option.children.size() > 2) {//之前的信息
      String province = supplementData.optionInfo.get(option.children.get(0).key);
      String city = supplementData.optionInfo.get(option.children.get(1).key);
      String district = supplementData.optionInfo.get(option.children.get(2).key);

      loanmktSupplementOptionsResult.resultMap = new HashMap<>();
      loanmktSupplementOptionsResult.resultMap.put(option.children.get(0).key, province);
      loanmktSupplementOptionsResult.resultMap.put(option.children.get(1).key, city);
      loanmktSupplementOptionsResult.resultMap.put(option.children.get(2).key, district);

      tvDesc.setText(formatCityInfo(province, city, district));
    }

    view.setOnClickListener(v -> {
      if (BaseUtils.isFastClick()) {
        return;
      }

      Bottom3ColumnsSelectDialog cityDialog = new Bottom3ColumnsSelectDialog(this);
      cityDialog.setTitle("省市区选择");
      cityDialog.setElementId(option.key);
      cityDialog.setOnSelectedListener((province, city, district) -> {
            tvDesc.setText(formatCityInfo(province, city, district));
            if (option.children != null && option.children.size() > 2) {
              loanmktSupplementOptionsResult.resultMap = new HashMap<>();
              loanmktSupplementOptionsResult.resultMap.put(option.children.get(0).key, province);
              loanmktSupplementOptionsResult.resultMap.put(option.children.get(1).key, city);
              loanmktSupplementOptionsResult.resultMap.put(option.children.get(2).key, district);
            }
          }
      );

      if (provinceData != null && districtDataMap != null && cityDataMap != null) {
        cityDialog.setData(provinceData, cityDataMap, districtDataMap);
        cityDialog.show();
      } else {
        showLoadingDialog();
        sendGetAreaTreeRequest();
      }
    });
    return loanmktSupplementOptionsResult;
  }

  /**
   * 日期选择
   *
   * @param option 单项数据
   * @return 单项需要提交的数据
   */
  private LoanmktSupplementOptionsResult generateDateOption(LoanmktSupplementOptions option) {

    View view = inflater.inflate(R.layout.loanmkt_item_select_option, null);
    TextView tvTitle = view.findViewById(R.id.tv_title);
    TextView tvDesc = view.findViewById(R.id.tv_loan_use);
    View redPoint = view.findViewById(R.id.v_redpoint);
    tvDesc.setHint(option.defaultRemind);

    tvTitle.setText(option.desc);
    if (!option.required) {
      redPoint.setVisibility(View.GONE);
    }

    LoanmktSupplementOptionsResult loanmktSupplementOptionsResult = new LoanmktSupplementOptionsResult() {
      @Override
      public View getView() {
        return view;
      }

      @Override
      public boolean checkFinish() {
        return chenkResultFinish(tvDesc.getText(), option);
      }

      @Override
      public Object getUploadObj() {
        return resultStr;
      }
    };
    loanmktSupplementOptionsResult.setLoanmktSupplementOptions(option);

    if (supplementData != null && supplementData.optionInfo != null) {//之前的信息
      String oldResult = supplementData.optionInfo.get(option.key);
      if (!TextUtils.isEmpty(oldResult)) {
        loanmktSupplementOptionsResult.resultStr = oldResult;
        tvDesc.setText(stampToDate(oldResult));
      }
    }

    view.setOnClickListener(v -> {
      if (BaseUtils.isFastClick()) {
        return;
      }

      Calendar calender = Calendar.getInstance();
      DatePickerDialog dialog = new DatePickerDialog(this, THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
          tvDesc.setText(year + "年" + (monthOfYear + 1) + "月" + dayOfMonth + "日");
          loanmktSupplementOptionsResult.resultStr = dateToStamp(year, monthOfYear + 1, dayOfMonth);
        }
      }, calender.get(Calendar.YEAR), calender.get(Calendar.MONTH), calender.get(Calendar.DAY_OF_MONTH));
      dialog.show();
    });
    return loanmktSupplementOptionsResult;
  }

  /*
   * 将时间转换为时间戳
   */
  public String dateToStamp(int year, int monthOfYear, int dayOfMonth) {
    String res = "";
    try {
      String month;
      if (monthOfYear > 9) {
        month = monthOfYear + "";
      } else {
        month = 0 + "" + monthOfYear;
      }
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
      Date date = simpleDateFormat.parse(year + month + dayOfMonth);
      long ts = date.getTime();
      res = String.valueOf(ts);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return res;
  }

  /*
   * 将时间戳转换为时间
   */
  public String stampToDate(String s) {
    String res = "";
    try {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
      long lt = new Long(s);
      Date date = new Date(lt);
      res = simpleDateFormat.format(date);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return res;
  }

  /**
   * 文本输入
   *
   * @param option 单项数据
   * @return 单项需要提交的数据
   */
  private LoanmktSupplementOptionsResult generateTextOption(LoanmktSupplementOptions option, String inputType) {
    View view = inflater.inflate(R.layout.loanmkt_item_text_option, null);
    TextView tvTitle = view.findViewById(R.id.tv_title);
    EditText etOption = view.findViewById(R.id.et_option_content);
    View redPoint = view.findViewById(R.id.v_redpoint);
    etOption.setHint(option.defaultRemind);

    switch (inputType) {
      case LABELTYPE_MOBILE_PHONE:
      case LABELTYPE_NUMBER:
        etOption.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_VARIATION_NORMAL);
        break;
    }
    etOption.setSingleLine(false);

    tvTitle.setText(option.desc);
    if (!option.required) {
      redPoint.setVisibility(View.GONE);
    }

    if (supplementData != null && supplementData.optionInfo != null) {//之前的信息
      etOption.setText(supplementData.optionInfo.get(option.key));
    }

    LoanmktSupplementOptionsResult loanmktSupplementOptionsResult = new LoanmktSupplementOptionsResult() {
      @Override
      public View getView() {
        return view;
      }

      @Override
      public boolean checkFinish() {
        return chenkResultFinish(etOption.getText(), option);
      }

      @Override
      public Object getUploadObj() {
        Editable editable = etOption.getText();
        return TextUtils.isEmpty(editable) ? null : removeBlank(editable.toString());
      }
    };
    loanmktSupplementOptionsResult.setLoanmktSupplementOptions(option);
    return loanmktSupplementOptionsResult;
  }

  /**
   * 下拉选择
   *
   * @param option 单项数据
   * @return 单项需要提交的数据
   */
  private LoanmktSupplementOptionsResult generateSelectOption(LoanmktSupplementOptions option) {
    final List<LoanmktSupplementOptions.LabelOptions> listoptions = option.labelOptions;
    if (listoptions == null || listoptions.isEmpty()) {
      return null;
    }

    View view = inflater.inflate(R.layout.loanmkt_item_select_option, null);
    TextView tvTitle = view.findViewById(R.id.tv_title);
    TextView tvDesc = view.findViewById(R.id.tv_loan_use);
    View redPoint = view.findViewById(R.id.v_redpoint);
    tvDesc.setHint(option.defaultRemind);
    tvTitle.setText(option.desc);
    if (!option.required) {
      redPoint.setVisibility(View.GONE);
    }

    LoanmktSupplementOptionsResult loanmktSupplementOptionsResult = new LoanmktSupplementOptionsResult() {
      @Override
      public View getView() {
        return view;
      }

      @Override
      public boolean checkFinish() {
        return chenkResultFinish(resultStr, option);
      }

      @Override
      public Object getUploadObj() {
        return resultStr;
      }
    };
    loanmktSupplementOptionsResult.setLoanmktSupplementOptions(option);

    if (option.defaultOption != null) {
      tvDesc.setText(option.defaultOption.desc);
      loanmktSupplementOptionsResult.resultStr = option.defaultOption.code;
    }

    if (supplementData != null && supplementData.optionInfo != null && listoptions != null) {//之前的信息
      String thekey = supplementData.optionInfo.get(option.key);
      if (!TextUtils.isEmpty(thekey)) {
        loanmktSupplementOptionsResult.resultStr = thekey;

        for (LoanmktSupplementOptions.LabelOptions l : listoptions) {
          if (thekey.equals(l.code)) {
            tvDesc.setText(l.desc);
            break;
          }
        }
      }
    }

    view.setOnClickListener(v -> {
      if (BaseUtils.isFastClick()) {
        return;
      }

      BottomCommonOptionSelectDialog selectLoanUseDialog = new BottomCommonOptionSelectDialog(this, option.getCommonOption());
      selectLoanUseDialog.setTitle(option.desc + "选择");
      selectLoanUseDialog.setElementId(option.key);
      selectLoanUseDialog.setOnItemSelectListener(selectedOption -> {
        tvDesc.setText(selectedOption.label);
        loanmktSupplementOptionsResult.resultStr = selectedOption.value;
      });
      selectLoanUseDialog.show();
    });
    return loanmktSupplementOptionsResult;
  }

  private String removeBlank(String str) {
    if (str == null) {
      return str;
    } else {
      Pattern p = Pattern.compile("\\s*|\t|\r|\n");
      Matcher m = p.matcher(str);
      String result = m.replaceAll("");
      return result;
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    PhoneContactsManager.getInstance().unregisterListener(onGetContactListener);
  }
}