package com.alaadcin.user;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.MTextView;

import java.util.HashMap;

public class StaticPageActivity extends AppCompatActivity {

    public String static_page_id = "1";
    public String staticData = "";
    public String vTitle = "";
    MTextView titleTxt;
    ImageView backImgView;
    GeneralFunctions generalFunc;
    ProgressBar loading;
    ErrorView errorView;
    LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_static_page);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        loading = (ProgressBar) findViewById(R.id.loading);
        errorView = (ErrorView) findViewById(R.id.errorView);
        container = (LinearLayout) findViewById(R.id.container);

        static_page_id = getIntent().getStringExtra("staticpage");
        staticData = getIntent().getStringExtra("staticData");
        vTitle = getIntent().getStringExtra("vTitle");
        setLabels();

        backImgView.setOnClickListener(new setOnClickList());

        if (!Utils.checkText(staticData)) {
            loadAboutUsData();
        }
    }


    public void setLabels() {
        if (Utils.checkText(staticData)) {
            titleTxt.setText(vTitle);
            loadAboutUsDetail(staticData);
        } else {
            if (static_page_id.equalsIgnoreCase("1")) {
                titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ABOUT_US_HEADER_TXT"));

            } else if (static_page_id.equalsIgnoreCase("33")) {
                titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PRIVACY_POLICY_TEXT"));

            } else if (static_page_id.equals("4")) {
                titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TERMS_AND_CONDITION"));

            } else {
                titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DETAILS"));
            }
        }
    }

    public void loadAboutUsData() {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(View.VISIBLE);
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "staticPage");
        parameters.put("iPageId", static_page_id);
        parameters.put("appType", Utils.app_type);
        parameters.put("iMemberId", generalFunc.getMemberId());
        if (generalFunc.getMemberId().equalsIgnoreCase("")) {
            parameters.put("vLangCode", generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY));
        }

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {


                if (responseString != null && !responseString.equals("")) {
                    String tPageDesc = generalFunc.getJsonValue("page_desc", responseString);
                    loadAboutUsDetail(tPageDesc);
                } else {
                    generateErrorView();
                }
            }
        });
        exeWebServer.execute();
    }

    public void loadAboutUsDetail(String tPageDesc) {
        closeLoader();

        WebView view = new WebView(this);
        view.setVerticalScrollBarEnabled(false);
        view.setBackgroundColor(getResources().getColor(R.color.appThemeColor_bg_parent_1));
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        view.setLongClickable(false);
        view.setHapticFeedbackEnabled(false);

        container.addView(view);

        view.loadDataWithBaseURL(null, generalFunc.wrapHtml(view.getContext(), tPageDesc), "text/html", "UTF-8", null);
    }

    public void closeLoader() {
        if (loading.getVisibility() == View.VISIBLE) {
            loading.setVisibility(View.GONE);
        }
    }

    public void generateErrorView() {

        closeLoader();

        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                loadAboutUsData();
            }
        });
    }

    public Context getActContext() {
        return StaticPageActivity.this;
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(getActContext());
            switch (view.getId()) {
                case R.id.backImgView:
                    StaticPageActivity.super.onBackPressed();
                    break;

            }
        }
    }

}
