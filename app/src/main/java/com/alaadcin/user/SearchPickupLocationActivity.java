package com.alaadcin.user;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
import com.general.files.GetLocationUpdates;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.utils.Logger;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class SearchPickupLocationActivity extends AppCompatActivity implements OnMapReadyCallback, GetAddressFromLocation.AddressFound, GetLocationUpdates.LocationUpdates, GoogleMap.OnCameraMoveStartedListener, OnCameraIdleListener {

    public boolean isAddressEnable = false;
    MTextView titleTxt;
    ImageView backImgView;

    GeneralFunctions generalFunc;
    MButton btn_type2;
    int btnId;

    MTextView placeTxtView;

    boolean isPlaceSelected = false;
    LatLng placeLocation;
    Marker placeMarker;

    SupportMapFragment map;
    GoogleMap gMap;

    GetAddressFromLocation getAddressFromLocation;
    LinearLayout placeArea;
    MTextView homePlaceTxt;
    MTextView workPlaceTxt;

    String userHomeLocationLatitude_str;
    String userHomeLocationLongitude_str;
    String userWorkLocationLatitude_str;
    String userWorkLocationLongitude_str;
    String home_address_str;
    String work_address_str;
    GetLocationUpdates getLastLocation;
    String iUserFavAddressId = "";
    String userProfileJson = "";
    JSONArray userFavouriteAddressArr;
    String eType = "";
    boolean isAddressStore = false;
    boolean isbtnclick = false;
    private String TAG = SearchPickupLocationActivity.class.getSimpleName();
    private Location userLocation;
    private Marker locMarker;
    private boolean isFirstLocation = true;
    ImageView pinImgView;
    MTextView locationImage;
    private SearchPickupLocationActivity listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_pickup_location);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        userFavouriteAddressArr = generalFunc.getJsonArray("UserFavouriteAddress", userProfileJson);


        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        placeTxtView = (MTextView) findViewById(R.id.placeTxtView);

        homePlaceTxt = (MTextView) findViewById(R.id.homePlaceTxt);
        workPlaceTxt = (MTextView) findViewById(R.id.workPlaceTxt);
        placeArea = (LinearLayout) findViewById(R.id.placeArea);
        pinImgView = (ImageView) findViewById(R.id.pinImgView);
        locationImage = (MTextView) findViewById(R.id.locationImage);


        titleTxt.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        titleTxt.setSelected(true);
        titleTxt.setSingleLine(true);


        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapV2);

        getAddressFromLocation = new GetAddressFromLocation(getActContext(), generalFunc);
        getAddressFromLocation.setAddressList(this);


        setLabels();

        map.getMapAsync(SearchPickupLocationActivity.this);

        backImgView.setOnClickListener(new setOnClickAct());
        locationImage.setOnClickListener(new setOnClickAct());
        btnId = Utils.generateViewId();
        btn_type2.setId(btnId);

        btn_type2.setOnClickListener(new setOnClickAct());
        (findViewById(R.id.pickUpLocSearchArea)).setOnClickListener(new setOnClickAct());
        homePlaceTxt.setOnClickListener(new setOnClickAct());
        workPlaceTxt.setOnClickListener(new setOnClickAct());

//        generalFunc.showMessage(generalFunc.getCurrentView(SearchPickupLocationActivity.this), generalFunc.retrieveLangLBl("", "LBL_LONG_TOUCH_CHANGE_LOC_TXT"));


        checkLocations();

        if (userFavouriteAddressArr!=null && userFavouriteAddressArr.length() > 0) {

            for (int i = 0; i < userFavouriteAddressArr.length(); i++) {
                JSONObject dataItem = generalFunc.getJsonObject(userFavouriteAddressArr, i);

                if (generalFunc.getJsonValueStr("eType", dataItem).equalsIgnoreCase(eType)) {
                    iUserFavAddressId = generalFunc.getJsonValueStr("iUserFavAddressId", dataItem);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (getLastLocation != null) {
            getLastLocation.stopLocationUpdates();
        }
        releaseResources();
        super.onDestroy();
    }

    public void setLabels() {
        placeTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_SEARCH_LOC"));
        pinImgView.setVisibility(View.GONE);
        if (getIntent().getStringExtra("isPickUpLoc") != null && getIntent().getStringExtra("isPickUpLoc").equals("true")) {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SET_PICK_UP_LOCATION_TXT"));
        } else if (getIntent().getStringExtra("isHome") != null && getIntent().getStringExtra("isHome").equals("true")) {
            isAddressStore = true;
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_HOME_BIG_TXT"));
            homePlaceTxt.setText(generalFunc.retrieveLangLBl("Home Place", "LBL_HOME_PLACE"));
        } else if (getIntent().getStringExtra("isWork") != null && getIntent().getStringExtra("isWork").equals("true")) {
            isAddressStore = true;
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_WORK_HEADER_TXT"));
            workPlaceTxt.setText(generalFunc.retrieveLangLBl("Work Place", "LBL_WORK_PLACE"));
        } else {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SELECT_DESTINATION_HEADER_TXT"));
        }

        if (getIntent().getStringExtra("IS_FROM_SELECT_LOC") != null && getIntent().getStringExtra("IS_FROM_SELECT_LOC").equalsIgnoreCase("Yes")) {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_LOC"));
        }

        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_LOC"));
        locationImage.setText(generalFunc.retrieveLangLBl("", "LBL_CHANGE"));
    }


    public void checkLocations() {

        HashMap<String, String> data = new HashMap<>();
        data.put("userHomeLocationAddress", "");
        data.put("userHomeLocationLatitude", "");
        data.put("userHomeLocationLongitude", "");
        data.put("userWorkLocationAddress", "");
        data.put("userWorkLocationLatitude", "");
        data.put("userWorkLocationLongitude", "");
        data = generalFunc.retrieveValue(data);

        home_address_str = data.get("userHomeLocationAddress");

        userHomeLocationLatitude_str = data.get("userHomeLocationLatitude");
        userHomeLocationLongitude_str = data.get("userHomeLocationLongitude");

        work_address_str = data.get("userWorkLocationAddress");
        userWorkLocationLatitude_str = data.get("userWorkLocationLatitude");
        userWorkLocationLongitude_str = data.get("userWorkLocationLongitude");

        if (getIntent().getStringExtra("isHome") != null && getIntent().getStringExtra("isHome").equals("true")) {

            eType = "HOME";
            if (home_address_str != null && !home_address_str.equals("")) {
                homePlaceTxt.setVisibility(View.VISIBLE);
                placeArea.setVisibility(View.GONE);
                (findViewById(R.id.seperationLine)).setVisibility(View.VISIBLE);
            }
        }
        if (getIntent().getStringExtra("isWork") != null && getIntent().getStringExtra("isWork").equals("true")) {

            eType = "WORK";
            if (work_address_str != null && !work_address_str.equals("")) {
                workPlaceTxt.setVisibility(View.VISIBLE);
                placeArea.setVisibility(View.GONE);
                (findViewById(R.id.seperationLine)).setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.gMap = googleMap;

        setGoogleMapCameraListener(this);

        getLastLocation = new GetLocationUpdates(getActContext(), Utils.LOCATION_UPDATE_MIN_DISTANCE_IN_MITERS, false, this);

//        LatLng placeLocation = getLocationLatLng(true);
//
//        if (placeLocation != null) {
//            setCameraPosition(placeLocation);
//        }
        gMap.getUiSettings().setCompassEnabled(false);
    }

    private void setCameraPosition(LatLng location) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(
                new LatLng(location.latitude,
                        location.longitude))
                .zoom(Utils.defaultZomLevel).build();
        gMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    private LatLng getLocationLatLng(boolean setText) {
        LatLng placeLocation = null;

        String isPickUpLoc = getIntent().getStringExtra("isPickUpLoc");

        if (isPickUpLoc != null && isPickUpLoc.equals("true")) {
            if (getIntent().hasExtra("PickUpLatitude") && getIntent().hasExtra("PickUpLongitude")) {

                isAddressEnable = true;
                placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLatitude")),
                        generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLongitude")));

            }

            if (setText && getIntent().hasExtra("PickUpAddress")) {
                pinImgView.setVisibility(View.VISIBLE);
                isPlaceSelected = true;
                placeTxtView.setText("" + getIntent().getStringExtra("PickUpAddress"));
            }

        } else if (getIntent().getStringExtra("isDestLoc") != null && getIntent().hasExtra("DestLatitude") && getIntent().hasExtra("DestLongitude")) {

            isAddressEnable = true;
            placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("DestLatitude")),
                    generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("DestLongitude")));

            if (setText && getIntent().hasExtra("DestAddress")) {
                pinImgView.setVisibility(View.VISIBLE);
                isPlaceSelected = true;
                placeTxtView.setText("" + getIntent().getStringExtra("DestAddress"));
            }

        } else if (getIntent().getStringExtra("isHome") != null && getIntent().getStringExtra("isHome").equals("true") && home_address_str != null && !home_address_str.equals("")) {
            HashMap<String, String> data = new HashMap<>();
            data.put("userHomeLocationAddress", "");
            data.put("userHomeLocationLatitude", "");
            data.put("userHomeLocationLongitude", "");
            data = generalFunc.retrieveValue(data);
            if (data.get("userHomeLocationLatitude") != null && data.get("userHomeLocationLongitude") != null && !data.get("userHomeLocationLatitude").equalsIgnoreCase("")) {

                isAddressEnable = true;
                placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, data.get("userHomeLocationLatitude")),
                        generalFunc.parseDoubleValue(0.0, data.get("userHomeLocationLongitude")));

            }
            if (setText) {
                isPlaceSelected = true;
                pinImgView.setVisibility(View.VISIBLE);
                placeTxtView.setText("" + data.get("userHomeLocationAddress"));
            }
        } else if (getIntent().getStringExtra("isWork") != null && getIntent().getStringExtra("isWork").equals("true")) {
            HashMap<String, String> data = new HashMap<>();
            data.put("userWorkLocationAddress", "");
            data.put("userWorkLocationLatitude", "");
            data.put("userWorkLocationLongitude", "");
            data = generalFunc.retrieveValue(data);

            if (getIntent().getStringExtra("isWork") != null && getIntent().getStringExtra("isWork").equals("true") && work_address_str != null && !work_address_str.equals("")) {
                if (data.get("userWorkLocationLatitude") != null && data.get("userWorkLocationLongitude") != null) {

                    isAddressEnable = true;
                    placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, data.get("userWorkLocationLatitude")),
                            generalFunc.parseDoubleValue(0.0, data.get("userWorkLocationLongitude")));

                }
            }
            if (setText) {
                isPlaceSelected = true;
                pinImgView.setVisibility(View.VISIBLE);
                placeTxtView.setText("" + data.get("userWorkLocationAddress"));

            }
        } else if (userLocation != null) {
            placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, "" + userLocation.getLatitude()),
                    generalFunc.parseDoubleValue(0.0, "" + userLocation.getLongitude()));

        } else {


            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager = (LocationManager) getSystemService
                    (Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return placeLocation;
            }
            Location getLastLocation = locationManager.getLastKnownLocation
                    (LocationManager.PASSIVE_PROVIDER);
            if (getLastLocation != null) {
                LatLng UserLocation = new LatLng(generalFunc.parseDoubleValue(0.0, "" + getLastLocation.getLatitude()),
                        generalFunc.parseDoubleValue(0.0, "" + getLastLocation.getLongitude()));
                if (UserLocation != null) {
                    placeLocation = UserLocation;
                }
            }
        }


        return placeLocation;
    }

    private void addMarker(LatLng location) {
        if (placeMarker != null) {
            placeMarker.remove();
        }

        MarkerOptions marker_opt = new MarkerOptions().position(location);

        Drawable mDrawable = getActContext().getResources().getDrawable(R.drawable.pin_dest_select);
        mDrawable.setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.appThemeColor_2), PorterDuff.Mode.MULTIPLY));
        BitmapDescriptor markerIcon = getMarkerIconFromDrawable(mDrawable);

        marker_opt.icon(markerIcon).anchor(0.5f, 0.5f);
        // locMarker = this.gMap.addMarker(marker_opt);
        placeMarker = this.gMap.addMarker(marker_opt);

    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onLocationUpdate(Location location) {
        if (location == null) {
            return;
        }

        if (isFirstLocation == true) {
            placeLocation = getLocationLatLng(true);
            if (isAddressEnable && listener==null)
            {
                setGoogleMapCameraListener(this);
            }
            if (placeLocation != null) {
                setCameraPosition(new LatLng(placeLocation.latitude, placeLocation.longitude));
            } else {
                setCameraPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            }

            pinImgView.setVisibility(View.VISIBLE);
            isFirstLocation = false;
        }

        userLocation = location;
    }

    @Override
    public void onAddressFound(String address, double latitude, double longitude, String geocodeobject) {
        placeTxtView.setText(address);
        isPlaceSelected = true;
        this.placeLocation = new LatLng(latitude, longitude);

        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(this.placeLocation, 14.0f);

        if (gMap != null) {
            gMap.clear();
            if (isFirstLocation) {
                gMap.moveCamera(cu);
            }
            isFirstLocation = false;

            setGoogleMapCameraListener(this);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                pinImgView.setVisibility(View.VISIBLE);
                Place place = PlaceAutocomplete.getPlace(this, data);

                placeTxtView.setText(place.getAddress());
                isPlaceSelected = true;
                LatLng placeLocation = place.getLatLng();

                this.placeLocation = placeLocation;

                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(placeLocation, Utils.defaultZomLevel);

                if (gMap != null) {
                    gMap.clear();
                    placeMarker = gMap.addMarker(new MarkerOptions().position(placeLocation).title("" + place.getAddress()));
                    gMap.moveCamera(cu);
                }

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);

                generalFunc.showMessage(generalFunc.getCurrentView(SearchPickupLocationActivity.this),
                        status.getStatusMessage());
            } else if (requestCode == RESULT_CANCELED) {

            }
        } else if (requestCode == Utils.PLACE_CUSTOME_LOC_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                pinImgView.setVisibility(View.VISIBLE);

                placeTxtView.setText(data.getStringExtra("Address"));
                isPlaceSelected = true;
                isAddressEnable = true;
                LatLng placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, data.getStringExtra("Latitude")), generalFunc.parseDoubleValue(0.0, data.getStringExtra("Longitude")));

                this.placeLocation = placeLocation;

                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(placeLocation, Utils.defaultZomLevel);

                if (gMap != null && placeLocation != null) {
                    gMap.clear();
                    gMap.moveCamera(cu);
                }

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);

                generalFunc.showMessage(generalFunc.getCurrentView(SearchPickupLocationActivity.this),
                        status.getStatusMessage());
            } else if (requestCode == RESULT_CANCELED) {

            }
        }
    }

    public Context getActContext() {
        return SearchPickupLocationActivity.this;
    }


    public void releaseResources() {
        setGoogleMapCameraListener(null);
        this.gMap = null;
        getAddressFromLocation.setAddressList(null);
        getAddressFromLocation = null;
    }

    public void addHomeWorkAddress(String vAddress, String vLatitude, String vLongitude) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateUserFavouriteAddress");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("vAddress", vAddress);
        parameters.put("vLatitude", vLatitude);
        parameters.put("vLongitude", vLongitude);
        parameters.put("eType", eType);
        parameters.put("iUserFavAddressId", iUserFavAddressId);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);

        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                Logger.d("Response", "::" + responseString);
                btn_type2.setEnabled(true);
                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                    if (isDataAvail == true) {

                        String messgeJson = generalFunc.getJsonValue(Utils.message_str, responseString);
                        generalFunc.storeData(Utils.USER_PROFILE_JSON, messgeJson);

                        Bundle bn = new Bundle();
                        bn.putString("Address", placeTxtView.getText().toString());
                        bn.putString("Latitude", "" + placeLocation.latitude);
                        bn.putString("Longitude", "" + placeLocation.longitude);

                        if (getIntent().hasExtra("isFromMulti")) {
                            bn.putBoolean("isFromMulti", true);
                            bn.putInt("pos", getIntent().getIntExtra("pos", -1));
                        }

                        if (getIntent().hasExtra("isFromStopOver")) {
                            bn.putInt("pos", getIntent().getIntExtra("pos", -1));
                        }


                        if (getIntent().getStringExtra("isHome") != null && getIntent().getStringExtra("isHome").equals("true")) {
                            generalFunc.storeData("userHomeLocationLatitude", "" + placeLocation.latitude);
                            generalFunc.storeData("userHomeLocationLongitude", "" + placeLocation.longitude);
                            generalFunc.storeData("userHomeLocationAddress", "" + placeTxtView.getText().toString());
                        }
                        if (getIntent().getStringExtra("isWork") != null && getIntent().getStringExtra("isWork").equals("true")) {
                            generalFunc.storeData("userWorkLocationLatitude", "" + placeLocation.latitude);
                            generalFunc.storeData("userWorkLocationLongitude", "" + placeLocation.longitude);
                            generalFunc.storeData("userWorkLocationAddress", "" + placeTxtView.getText().toString());
                        }


                        new StartActProcess(getActContext()).setOkResult(bn);
                        backImgView.performClick();

                    } else {
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                    }
                } else {
                    isbtnclick = false;
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();

    }


    @Override
    public void onCameraMoveStarted(int i) {
        if (pinImgView.getVisibility() == View.VISIBLE) {
            if (!isAddressEnable) {
                placeTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_SELECTING_LOCATION_TXT"));
            }
        }

    }

    @Override
    public void onCameraIdle() {


        if (getAddressFromLocation == null) {
            return;
        }

        if (pinImgView.getVisibility() == View.GONE) {
            return;
        }

        LatLng center = null;
        if (gMap != null && gMap.getCameraPosition() != null) {
            center = gMap.getCameraPosition().target;
        }

        if (center == null) {
            return;
        }


        if (!isAddressEnable) {
            setGoogleMapCameraListener(null);
            getAddressFromLocation.setLocation(center.latitude, center.longitude);
            getAddressFromLocation.setLoaderEnable(true);
            getAddressFromLocation.execute();
        } else {
            isAddressEnable = false;
        }


    }

    public class onGoogleMapCameraChangeList implements GoogleMap.OnCameraChangeListener {

        @Override
        public void onCameraChange(CameraPosition cameraPosition) {


            if (getAddressFromLocation == null) {
                return;
            }

            LatLng center = null;
            if (gMap != null && gMap.getCameraPosition() != null) {
                center = gMap.getCameraPosition().target;
            }

            if (center == null) {
                return;
            }


            if (!isAddressEnable) {
                setGoogleMapCameraListener(null);
                getAddressFromLocation.setLocation(center.latitude, center.longitude);
                getAddressFromLocation.setLoaderEnable(true);
                getAddressFromLocation.execute();
            } else {
                isAddressEnable = false;
            }

            Logger.d("SearchPickup", "On camera change");


        }
    }

    public void setGoogleMapCameraListener(SearchPickupLocationActivity act) {
        listener =act;
        this.gMap.setOnCameraMoveStartedListener(act);
        this.gMap.setOnCameraIdleListener(act);

    }

    public class setOnClickAct implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(getActContext());
            int i = view.getId();
            if (i == R.id.backImgView) {
                SearchPickupLocationActivity.super.onBackPressed();

            } else if (i == R.id.pickUpLocSearchArea) {

                LatLng placeLocation = getLocationLatLng(false);
                Bundle bn = new Bundle();
                if (getIntent().hasExtra("locationArea")) {
                    bn.putString("locationArea", getIntent().getStringExtra("locationArea"));
                } else {
                    bn.putString("locationArea", "");
                }
                bn.putString("hideSetMapLoc", "");
                if (placeLocation != null) {

                    bn.putDouble("lat", placeLocation.latitude);
                    bn.putDouble("long", placeLocation.longitude);


                } else {
                    bn.putDouble("lat", 0.0);
                    bn.putDouble("long", 0.0);
                    bn.putString("address", "");

                }

                bn.putBoolean("isPlaceAreaShow", false);

                if (getIntent().hasExtra("isFromMulti")) {
                    bn.putBoolean("isFromMulti", true);
                    bn.putInt("pos", getIntent().getIntExtra("pos", -1));
                }
                if (getIntent().hasExtra("isFromStopOver")) {
                    bn.putInt("pos", getIntent().getIntExtra("pos", -1));
                }
                if (getIntent().hasExtra("stopOverPointsList")) {
                    bn.putSerializable("stopOverPointsList", getIntent().getSerializableExtra("stopOverPointsList"));
                }

                new StartActProcess(getActContext()).startActForResult(SearchLocationActivity.class,
                        bn, Utils.PLACE_CUSTOME_LOC_REQUEST_CODE);


            }
            else if (i == locationImage.getId()) {
                (findViewById(R.id.pickUpLocSearchArea)).performClick();
            }
            else if (i == btnId) {

                if (!isbtnclick) {


                    if (isPlaceSelected == false) {
                        generalFunc.showMessage(generalFunc.getCurrentView(SearchPickupLocationActivity.this),
                                generalFunc.retrieveLangLBl("Please set location.", "LBL_SET_LOCATION"));
                        return;
                    }

                    isbtnclick = true;

                    if (isAddressStore) {
                        addHomeWorkAddress(placeTxtView.getText().toString(), placeLocation.latitude + "", placeLocation.longitude + "");
                    } else {
                        Bundle bn = new Bundle();
                        bn.putString("Address", placeTxtView.getText().toString());
                        bn.putString("Latitude", "" + placeLocation.latitude);
                        bn.putString("Longitude", "" + placeLocation.longitude);

                        if (getIntent().hasExtra("isFromMulti")) {
                            bn.putBoolean("isFromMulti", true);
                            bn.putInt("pos", getIntent().getIntExtra("pos", -1));
                        }

                        if (getIntent().hasExtra("isFromStopOver")) {
                            bn.putInt("pos", getIntent().getIntExtra("pos", -1));
                        }

                        new StartActProcess(getActContext()).setOkResult(bn);
                        backImgView.performClick();

                    }
                }


            } else if (i == homePlaceTxt.getId()) {
                onAddressFound(home_address_str, generalFunc.parseDoubleValue(0.0, userHomeLocationLatitude_str),
                        generalFunc.parseDoubleValue(0.0, userHomeLocationLongitude_str), "");
            } else if (i == workPlaceTxt.getId()) {
                onAddressFound(work_address_str, generalFunc.parseDoubleValue(0.0, userWorkLocationLatitude_str),
                        generalFunc.parseDoubleValue(0.0, userWorkLocationLongitude_str), "");
            }
        }
    }

}