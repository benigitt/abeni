package com.fragments;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.core.graphics.drawable.DrawableCompat;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.alaadcin.user.MyProfileActivity;
import com.alaadcin.user.R;
import com.alaadcin.user.SearchPickupLocationActivity;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.InternetConnection;
import com.general.files.StartActProcess;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MTextView;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {


    View view;
    MyProfileActivity myProfileAct;
    GeneralFunctions generalFunc;
    String userProfileJson = "";

    MaterialEditText fNameBox;
    MaterialEditText lNameBox;
    MaterialEditText emailBox;
    MaterialEditText mobileBox;
    MaterialEditText langBox;
    MaterialEditText currencyBox;

    MTextView homePlaceTxt, homePlaceHTxt;
    MTextView workPlaceTxt, workPlaceHTxt;

    LinearLayout placearea;
    InternetConnection internetConnection;
    boolean isemailverified = false;


    ImageView infoImg;
    MTextView verifytxt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);

        view = inflater.inflate(R.layout.fragment_profile, container, false);
        myProfileAct = (MyProfileActivity) getActivity();
        internetConnection = new InternetConnection(myProfileAct);
        generalFunc = myProfileAct.generalFunc;
        userProfileJson = myProfileAct.userProfileJson;


        if (generalFunc.getJsonValue("eEmailVerified", userProfileJson).equalsIgnoreCase("YES")) {
            isemailverified = true;

        }

        fNameBox = (MaterialEditText) view.findViewById(R.id.fNameBox);
        lNameBox = (MaterialEditText) view.findViewById(R.id.lNameBox);
        emailBox = (MaterialEditText) view.findViewById(R.id.emailBox);
        mobileBox = (MaterialEditText) view.findViewById(R.id.mobileBox);
        langBox = (MaterialEditText) view.findViewById(R.id.langBox);
        currencyBox = (MaterialEditText) view.findViewById(R.id.currencyBox);
        homePlaceTxt = (MTextView) view.findViewById(R.id.homePlaceTxt);
        homePlaceHTxt = (MTextView) view.findViewById(R.id.homePlaceHTxt);
        workPlaceTxt = (MTextView) view.findViewById(R.id.workPlaceTxt);
        workPlaceHTxt = (MTextView) view.findViewById(R.id.workPlaceHTxt);
        placearea = (LinearLayout) view.findViewById(R.id.placearea);

        // Deliver all

        verifytxt = (MTextView) view.findViewById(R.id.verifytxt);

        infoImg = (ImageView) view.findViewById(R.id.infoImg);
        infoImg.setOnClickListener(new setOnClickList());
        verifytxt.setOnClickListener(new setOnClickList());


        removeInput();
        setLabels();

        setData();

        myProfileAct.changePageTitle(generalFunc.retrieveLangLBl("", "LBL_PROFILE_TITLE_TXT"));
        homePlaceTxt.setOnClickListener(new setOnClickList());
        workPlaceTxt.setOnClickListener(new setOnClickList());

        checkPlaces();

        if (myProfileAct.isEdit) {
            placearea.setVisibility(View.GONE);
        }

        if (generalFunc.getJsonValue("APP_TYPE", userProfileJson).equals(Utils.CabGeneralType_UberX)) {
            placearea.setVisibility(View.GONE);
        }
// Deliver all

        if (!isemailverified) {

            infoImg.setVisibility(View.GONE);
            verifytxt.setVisibility(View.GONE);
        }

        return view;
    }

    public void setLabels() {
        fNameBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_FIRST_NAME_HEADER_TXT"));
        lNameBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_LAST_NAME_HEADER_TXT"));
        emailBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_EMAIL_LBL_TXT"));
        mobileBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_MOBILE_NUMBER_HEADER_TXT"));
        langBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_LANGUAGE_TXT"));
        currencyBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_CURRENCY_TXT"));
        homePlaceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_HOME_PLACE_TXT"));
        workPlaceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_WORK_PLACE_TXT"));
        homePlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOME_PLACE"));
        workPlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_WORK_PLACE"));
        ((MTextView) view.findViewById(R.id.placesTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_PLACES_HEADER_TXT"));

// deliver all
        verifytxt.setText(generalFunc.retrieveLangLBl("", "LBL_VERIFY"));
    }

    // deliver all
    public void openRsendMailDailog() {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            if (btn_id == 0) {
                generateAlert.closeAlertBox();
            } else {
                //yes
                generateAlert.closeAlertBox();
                sendVerificationSMS();
            }

        });
        generateAlert.setContentMessage(generalFunc.retrieveLangLBl("", "LBL_VERIFICATION_PENDING"), generalFunc.retrieveLangLBl("", "LBL_VERIFICATION_PENDING_MSG"));
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_RESEND_EMAIL"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_NO"));
        generateAlert.showAlertBox();
    }

    public void removeInput() {
        Utils.removeInput(fNameBox);
        Utils.removeInput(lNameBox);
        Utils.removeInput(emailBox);
        Utils.removeInput(mobileBox);
        Utils.removeInput(langBox);
        Utils.removeInput(currencyBox);

        fNameBox.setHideUnderline(true);
        lNameBox.setHideUnderline(true);
        emailBox.setHideUnderline(true);
        mobileBox.setHideUnderline(true);
        langBox.setHideUnderline(true);
        currencyBox.setHideUnderline(true);
    }

    public void setData() {

        JSONArray currencyList_arr = generalFunc.getJsonArray(generalFunc.retrieveValue(Utils.CURRENCY_LIST_KEY));
        if (currencyList_arr != null && currencyList_arr.length() < 2) {
            currencyBox.setVisibility(View.GONE);
        }
        fNameBox.setText(generalFunc.getJsonValue("vName", userProfileJson));
        lNameBox.setText(generalFunc.getJsonValue("vLastName", userProfileJson));
        emailBox.setText(generalFunc.getJsonValue("vEmail", userProfileJson));
        currencyBox.setText(generalFunc.getJsonValue("vCurrencyPassenger", userProfileJson));
        mobileBox.setText("+" + generalFunc.getJsonValue("vPhoneCode", userProfileJson) + generalFunc.getJsonValue("vPhone", userProfileJson));

        fNameBox.getLabelFocusAnimator().start();
        lNameBox.getLabelFocusAnimator().start();
        emailBox.getLabelFocusAnimator().start();
        mobileBox.getLabelFocusAnimator().start();
        langBox.getLabelFocusAnimator().start();
        currencyBox.getLabelFocusAnimator().start();


        if (generalFunc.getJsonValue("ENABLE_OPTION_UPDATE_CURRENCY", userProfileJson).equalsIgnoreCase("No")) {
            currencyBox.setVisibility(View.VISIBLE);
            currencyBox.setEnabled(false);
            currencyBox.setClickable(false);
        }

        setLanguage();
    }

    public void checkPlaces() {

        String home_address_str = generalFunc.retrieveValue("userHomeLocationAddress");
        String work_address_str = generalFunc.retrieveValue("userWorkLocationAddress");


        final Drawable img_delete = getResources().getDrawable(R.mipmap.ic_edit);
        final Drawable img_edit = getResources().getDrawable(R.mipmap.ic_pluse);
        final Drawable img_home_place = getResources().getDrawable(R.mipmap.ic_home);
        final Drawable img_work_place = getResources().getDrawable(R.mipmap.ic_work);

        int color = Color.parseColor("#909090");
        DrawableCompat.setTint(img_delete, color);
        DrawableCompat.setTint(img_edit, color);


        if (home_address_str != null) {
//            homePlaceTxt.setText("" + home_address_str);

            homePlaceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOME_PLACE"));
            homePlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            homePlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            homePlaceTxt.setTextColor(getResources().getColor(R.color.gray));
            homePlaceHTxt.setText("" + home_address_str);
            homePlaceHTxt.setVisibility(View.VISIBLE);
            homePlaceTxt.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, img_delete, null);
            homePlaceHTxt.setTextColor(getResources().getColor(R.color.black));

            homePlaceTxt.setOnTouchListener(new View.OnTouchListener() {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if (generalFunc.isRTLmode()) {
                        if (event.getAction() == MotionEvent.ACTION_UP && homePlaceTxt.getCompoundDrawables()[DRAWABLE_LEFT] != null) {
                            if (event.getRawX() <= (homePlaceTxt.getLeft() + homePlaceTxt.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width())) {
                                Bundle bn = new Bundle();
                                bn.putString("isHome", "true");
                                new StartActProcess(myProfileAct.getActContext()).startActForResult(myProfileAct.getProfileFrag(), SearchPickupLocationActivity.class,
                                        Utils.ADD_HOME_LOC_REQ_CODE, bn);
                                return true;
                            }
                        }
                    } else {
                        if (event.getAction() == MotionEvent.ACTION_UP && homePlaceTxt.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
                            if (event.getRawX() >= (homePlaceTxt.getRight() - homePlaceTxt.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                                Bundle bn = new Bundle();
                                bn.putString("isHome", "true");
                                new StartActProcess(myProfileAct.getActContext()).startActForResult(myProfileAct.getProfileFrag(), SearchPickupLocationActivity.class,
                                        Utils.ADD_HOME_LOC_REQ_CODE, bn);
                                return true;
                            }
                        }
                    }

                    return false;
                }
            });
        } else {
            homePlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOME_PLACE"));
            homePlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            homePlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            homePlaceTxt.setText("" + generalFunc.retrieveLangLBl("", "LBL_ADD_HOME_PLACE_TXT"));
            homePlaceTxt.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, img_edit, null);
            homePlaceTxt.setTextColor(getResources().getColor(R.color.gray));
        }

        if (work_address_str != null) {

            workPlaceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_WORK_PLACE"));
            workPlaceHTxt.setText("" + work_address_str);
            workPlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            workPlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            workPlaceTxt.setTextColor(getResources().getColor(R.color.gray));
            workPlaceHTxt.setVisibility(View.VISIBLE);
            workPlaceTxt.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, img_delete, null);
            workPlaceHTxt.setTextColor(getResources().getColor(R.color.black));

            workPlaceTxt.setOnTouchListener(new View.OnTouchListener() {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                @Override
                public boolean onTouch(View v, MotionEvent event) {


                    if (generalFunc.isRTLmode()) {
                        if (event.getAction() == MotionEvent.ACTION_UP && workPlaceTxt.getCompoundDrawables()[DRAWABLE_LEFT] != null) {
                            if (event.getRawX() <= (workPlaceTxt.getLeft() + workPlaceTxt.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width())) {
                                Bundle bn = new Bundle();
                                bn.putString("isWork", "true");
                                new StartActProcess(myProfileAct.getActContext()).startActForResult(myProfileAct.getProfileFrag(), SearchPickupLocationActivity.class,
                                        Utils.ADD_WORK_LOC_REQ_CODE, bn);
                                return true;
                            }
                        }
                    } else {
                        if (event.getAction() == MotionEvent.ACTION_UP && workPlaceTxt.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
                            if (event.getRawX() >= (workPlaceTxt.getRight() - workPlaceTxt.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                                Bundle bn = new Bundle();
                                bn.putString("isWork", "true");
                                new StartActProcess(myProfileAct.getActContext()).startActForResult(myProfileAct.getProfileFrag(), SearchPickupLocationActivity.class,
                                        Utils.ADD_WORK_LOC_REQ_CODE, bn);
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });
        } else {
            workPlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_WORK_PLACE"));
            workPlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            workPlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            workPlaceTxt.setText("" + generalFunc.retrieveLangLBl("", "LBL_ADD_WORK_PLACE_TXT"));
            workPlaceTxt.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, img_edit, null);
            workPlaceTxt.setTextColor(getResources().getColor(R.color.gray));

        }
    }

    public void setLanguage() {
        JSONArray languageList_arr = generalFunc.getJsonArray(generalFunc.retrieveValue(Utils.LANGUAGE_LIST_KEY));

        if (languageList_arr == null) {
            return;
        }

        if (languageList_arr.length() < 2) {
            langBox.setVisibility(View.GONE);
        }

        for (int i = 0; i < languageList_arr.length(); i++) {
            JSONObject obj_temp = generalFunc.getJsonObject(languageList_arr, i);

            if ((generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY)).equals(generalFunc.getJsonValueStr("vCode", obj_temp))) {

                langBox.setText(generalFunc.getJsonValueStr("vTitle", obj_temp));
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utils.ADD_HOME_LOC_REQ_CODE && resultCode == myProfileAct.RESULT_OK && data != null) {

            HashMap<String, String> storeData = new HashMap<>();

            if (generalFunc.isLocationEnabled()) {

                storeData.put("userHomeLocationLatitude", "" + data.getStringExtra("Latitude"));
                storeData.put("userHomeLocationLongitude", "" + data.getStringExtra("Longitude"));
                storeData.put("userHomeLocationAddress", "" + data.getStringExtra("Address"));

                generalFunc.storeData(storeData);
                homePlaceTxt.setText(data.getStringExtra("Address"));
                checkPlaces();

            } else {

                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                if (place.getAddress() == null || place.getLatLng() == null) {
                    return;
                }

                homePlaceTxt.setText(place.getAddress());
                LatLng placeLocation = place.getLatLng();
                storeData.put("userHomeLocationLatitude", "" + placeLocation.latitude);
                storeData.put("userHomeLocationLongitude", "" + placeLocation.longitude);
                storeData.put("userHomeLocationAddress", "" + place.getAddress());
                generalFunc.storeData(storeData);

                checkPlaces();


            }

        } else if (requestCode == Utils.ADD_WORK_LOC_REQ_CODE && resultCode == myProfileAct.RESULT_OK && data != null) {
            HashMap<String, String> storeData = new HashMap<>();
            if (generalFunc.isLocationEnabled()) {
                storeData.put("userWorkLocationLatitude", "" + data.getStringExtra("Latitude"));
                storeData.put("userWorkLocationLongitude", "" + data.getStringExtra("Longitude"));
                storeData.put("userWorkLocationAddress", "" + data.getStringExtra("Address"));
                generalFunc.storeData(storeData);

                workPlaceTxt.setText(data.getStringExtra("Address"));

                checkPlaces();
            } else {


                Place place = PlaceAutocomplete.getPlace(getActivity(), data);

                if (place.getAddress() == null || place.getLatLng() == null) {
                    return;
                }
                workPlaceTxt.setText(place.getAddress());
                LatLng placeLocation = place.getLatLng();
                storeData.put("userWorkLocationLatitude", "" + placeLocation.latitude);
                storeData.put("userWorkLocationLongitude", "" + placeLocation.longitude);
                storeData.put("userWorkLocationAddress", "" + place.getAddress());
                generalFunc.storeData(storeData);
                checkPlaces();

            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Utils.hideKeyboard(getActivity());
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Bundle bn = new Bundle();

            if (!internetConnection.isNetworkConnected()) {
                generalFunc.showMessage(homePlaceTxt, generalFunc.retrieveLangLBl("", "LBL_NO_INTERNET_TXT"));
                return;
            }

            if (i == R.id.homePlaceTxt) {
                bn.putString("isHome", "true");
                new StartActProcess(myProfileAct.getActContext()).startActForResult(myProfileAct.getProfileFrag(), SearchPickupLocationActivity.class,
                        Utils.ADD_HOME_LOC_REQ_CODE, bn);
            } else if (i == R.id.workPlaceTxt) {
                bn.putString("isWork", "true");
                new StartActProcess(myProfileAct.getActContext()).startActForResult(myProfileAct.getProfileFrag(), SearchPickupLocationActivity.class,
                        Utils.ADD_WORK_LOC_REQ_CODE, bn);
            } else if (i == verifytxt.getId() || i == infoImg.getId()) {
                openRsendMailDailog();
            }
        }
    }


    public void sendVerificationSMS() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "sendVerificationEmail");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getContext(), parameters);
        exeWebServer.setDataResponseListener(responseString -> {
            if (responseString != null && !responseString.equals("")) {
                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
            } else {
                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
            }
        });
        exeWebServer.execute();
    }
}
