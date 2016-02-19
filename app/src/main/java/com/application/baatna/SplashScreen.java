package com.application.baatna;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.baatna.utils.BTracker;
import com.application.baatna.utils.BaatnaLocationCallback;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.FacebookConnect;
import com.application.baatna.utils.FacebookConnectCallback;
import com.application.baatna.utils.UploadManager;
import com.application.baatna.utils.UploadManagerCallback;
import com.application.baatna.views.Home;
import com.application.baatna.views.UserLoginActivity;
import com.facebook.Session;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by apoorvarora on 20/02/16.
 */
public class SplashScreen extends Activity implements FacebookConnectCallback, UploadManagerCallback, BaatnaLocationCallback {

    int width;
    int height;
    private SharedPreferences prefs;
    private ProgressDialog z_ProgressDialog;
    private boolean dismissDialog = false;

    /** Constant, randomly selected */
    public final int RESULT_FACEBOOK_LOGIN_OK = 1432; // Random numbers
    public final int RESULT_GOOGLE_LOGIN_OK = 1434;

    // RelativeLayout loginPage;
    // RelativeLayout signUpPage;

    final int DEFAULT_SHOWN = 87;
    final int LOGIN_SHOWN = 88;
    final int SIGNUP_SHOWN = 89;
    int mState = DEFAULT_SHOWN;
    public static final int ANIMATION_LOGIN = 200;

    private String APPLICATION_ID;
    private boolean destroyed = false;
    Location loc;

    private String error_responseCode = "";
    private String error_exception = "";
    private String error_stackTrace = "";

    private BaatnaApp zapp;
    Context context;
    private boolean windowHasFocus = false;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    String regId;
    int hardwareRegistered = 0;

    ImageView imgBg;

    private Animation animation1, animation2, animation3;
    private TranslateAnimation translation;

    private ViewPager mViewPager;
//    private RelativeLayout mSignupContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        prefs = getSharedPreferences("application_settings", 0);
        context = getApplicationContext();
        zapp = (BaatnaApp) getApplication();
        APPLICATION_ID = prefs.getString("app_id", "");

        width = getWindowManager().getDefaultDisplay().getWidth();
        height = getWindowManager().getDefaultDisplay().getHeight();

        imgBg = (ImageView) findViewById(R.id.baatna_background);

        try {
            int imageWidth = width;
            int imageHeight = height;

            Bitmap bgBitmap = CommonLib.getBitmap(this, R.drawable.baatna_background, imageWidth, imageHeight);
            imgBg.getLayoutParams().width = imageWidth;
            imgBg.getLayoutParams().height = imageHeight;
            imgBg.setImageBitmap(bgBitmap);

        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }


        imgBg.postDelayed(new Runnable() {
            @Override
            public void run() {
                startLocationCheck();
            }
        }, 2000);
        onCoordinatesIdentified(loc);

        //stuffs
        mViewPager = (ViewPager) findViewById(R.id.tour_view_pager);
//        mSignupContainer = (RelativeLayout) findViewById(R.id.signup_container);

        TourPagerAdapter mTourPagerAdpater = new TourPagerAdapter();
        ((ViewPager) mViewPager).setAdapter(mTourPagerAdpater);

        ((ViewPager) mViewPager).setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int position = ((ViewPager) mViewPager).getOffscreenPageLimit();

            @Override
            public void onPageSelected(int arg0) {

                // startPosition = arg0;

                LinearLayout dotsContainer = (LinearLayout) findViewById(R.id.tour_dots);

                int index = 5;
                for (int count = 0; count < index; count++) {
                    ImageView dots = (ImageView) dotsContainer.getChildAt(count);

                    if (count == arg0)
                        dots.setImageResource(R.drawable.tour_image_dots_selected);
                    else
                        dots.setImageResource(R.drawable.tour_image_dots_unselected);
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
        fixSizes();
        animate();
        UploadManager.addCallback(this);
        updateDotsContainer();
    }

    private void animate() {
        try {
            final View tourDots = findViewById(R.id.tour_dots);

//            mSignupContainer.setVisibility(View.INVISIBLE);
            tourDots.setVisibility(View.INVISIBLE);

            translation = new TranslateAnimation(10f, 0F, 0f, 0F);
            translation.setDuration(200);
            translation.setFillAfter(true);
            translation.setInterpolator(new BounceInterpolator());

            animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_right);
            animation2.setDuration(500);
            animation2.restrictDuration(700);
            animation2.scaleCurrentDuration(1);
            animation2.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    tourDots.startAnimation(translation);
                }
            });

            animation3 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_bottom);
            animation3.setInterpolator(new DecelerateInterpolator());
            animation3.restrictDuration(700);
            animation3.scaleCurrentDuration(1);
            animation3.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    tourDots.setVisibility(View.VISIBLE);
                    tourDots.startAnimation(animation2);
                }
            });

            animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up_center);
            animation1.setDuration(700);
            animation1.restrictDuration(700);
            animation1.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (prefs.getInt("uid", 0) == 0) {
                        tourDots.setVisibility(View.VISIBLE);
                        tourDots.startAnimation(animation2);
//                        mSignupContainer.setVisibility(View.VISIBLE);
//                        mSignupContainer.startAnimation(animation3);
                    } else {
                        checkPlayServices();
                    }
                }
            });
            animation1.scaleCurrentDuration(1);
            mViewPager.startAnimation(animation1);
        } catch (Exception e) {
            imgBg.setVisibility(View.VISIBLE);
            findViewById(R.id.layout_login_separator).setVisibility(View.VISIBLE);
        }
    }

    private void checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        if (resultCode != ConnectionResult.SUCCESS) {


            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                CommonLib.ZLog("google-play-resultcode", resultCode);
                if (resultCode == 2 && !isFinishing()) {

                    if (windowHasFocus)
                        showDialog(PLAY_SERVICES_RESOLUTION_REQUEST);
                } else {
                    navigateToHome();
                }

            } else {
                navigateToHome();
            }

        } else {

            gcm = GoogleCloudMessaging.getInstance(getApplicationContext());

            regId = getRegistrationId(context);

            if (hardwareRegistered == 0) {
                // Call
                if (prefs.getInt("uid", 0) != 0 && !regId.equals("")) {
                    sendRegistrationIdToBackend();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("HARDWARE_REGISTERED", 1);
                    editor.commit();
                    hardwareRegistered=1;
                }
            }

            if (regId.isEmpty()) {
                CommonLib.ZLog("GCM", "RegID is empty");
                registerInBackground();
            } else {
                CommonLib.ZLog("GCM", "already registered : " + regId);
            }
            navigateToHome();
        }
    }

    public void startLocationCheck() {
        zapp.zll.forced = true;
        zapp.zll.addCallback(this);
        zapp.startLocationCheck();
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {

        final SharedPreferences prefs = getSharedPreferences("application_settings", 0);
        String registrationId = prefs.getString(CommonLib.PROPERTY_REG_ID, "");

        if (registrationId.isEmpty()) {
            CommonLib.ZLog("GCM", "Registration not found.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 1);
            return packageInfo.versionCode;

        } catch (Exception e) {
            CommonLib.ZLog("GCM", "EXCEPTION OCCURED" + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {

                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }

                    regId = gcm.register(CommonLib.GCM_SENDER_ID);
                    msg = "Device registered, registration ID=" + regId;
                    storeRegistrationId(context, regId);

                    if (prefs.getInt("uid", 0) != 0 && !regId.equals(""))
                        sendRegistrationIdToBackend();

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                CommonLib.ZLog("GCM msg", msg);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void storeRegistrationId(Context context, String regId) {

        prefs = getSharedPreferences("application_settings", 0);
        int appVersion = getAppVersion(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CommonLib.PROPERTY_REG_ID, regId);
        editor.putInt(CommonLib.PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private void sendRegistrationIdToBackend() {
        // new registerDeviceAtZomato().execute();
        UploadManager.updateRegistrationId(prefs.getString("access_token", ""), regId);
    }

    public void onCoordinatesIdentified(Location loc) {
        if(loc!=null){
            UploadManager.updateLocation(prefs.getString("access_token", ""),loc.getLatitude(), loc.getLongitude());

            float lat = (float) loc.getLatitude();
            float lon = (float)loc.getLongitude();
            Log.e("lat lon", lat + " " + lon);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putFloat("lat1", lat);
            editor.putFloat("lon1", lon);
            editor.commit();}
    }

    public void onLocationIdentified() {
    }

    public void onLocationNotIdentified() {
    }

    @Override
    public void onDifferentCityIdentified() {
    }

    @Override
    public void locationNotEnabled() {
    }

    @Override
    public void onLocationTimedOut() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onNetworkError() {
    }

    private void fixSizes() {

        mViewPager.getLayoutParams().height = 2 * height / 3;
//        mSignupContainer.getLayoutParams().height = height / 3 - width / 10 - width / 40;
    }

    @Override
    public void response(Bundle bundle) {

        error_exception = "";
        error_responseCode = "";
        error_stackTrace = "";
        boolean regIdSent = false;

        if (bundle.containsKey("error_responseCode"))
            error_responseCode = bundle.getString("error_responseCode");

        if (bundle.containsKey("error_exception"))
            error_exception = bundle.getString("error_exception");

        if (bundle.containsKey("error_stackTrace"))
            error_stackTrace = bundle.getString("error_stackTrace");

        try {

            int status = bundle.getInt("status");

            if (status == 0) {

                if (!error_exception.equals("") || !error_responseCode.equals("") || !error_stackTrace.equals(""))
                    ;// BTODO
                // sendFailedLogsToServer();

                if (bundle.getString("errorMessage") != null) {
                    String errorMessage = bundle.getString("errorMessage");
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.err_occurred, Toast.LENGTH_SHORT).show();
                }
                if (z_ProgressDialog != null && z_ProgressDialog.isShowing())
                    z_ProgressDialog.dismiss();
            } else {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("uid", bundle.getInt("uid"));
                if (bundle.containsKey("email"))
                    editor.putString("email", bundle.getString("email"));
                if (bundle.containsKey("description"))
                    editor.putString("description", bundle.getString("description"));
                if (bundle.containsKey("username"))
                    editor.putString("username", bundle.getString("username"));
                if (bundle.containsKey("thumbUrl"))
                    editor.putString("thumbUrl", bundle.getString("thumbUrl"));
                if (bundle.containsKey("profile_pic"))
                    editor.putString("profile_pic", bundle.getString("profile_pic"));
                if (bundle.containsKey("user_name"))
                    editor.putString("username", bundle.getString("username"));
                String token = bundle.getString("access_token");
                System.out.println(token);
                editor.putString("access_token", bundle.getString("access_token"));
                editor.putBoolean("verifiedUser", bundle.getBoolean("verifiedUser"));
                editor.commit();

                CommonLib.ZLog("login", "FACEBOOK");

                checkPlayServices();

                if (z_ProgressDialog != null && z_ProgressDialog.isShowing())
                    z_ProgressDialog.dismiss();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void facebookAction(View view) {
        BTracker.logGAEvent(this, BTracker.CATEGORY_WIDGET_ACTION, BTracker.ACTION_FACEBOOK_LOGIN_PRESSED, "");
//		z_ProgressDialog = ProgressDialog.show(Splash.this, null, getResources().getString(R.string.verifying_creds),
//				true, false);
        z_ProgressDialog = new ProgressDialog(SplashScreen.this,R.style.StyledDialog);
        z_ProgressDialog.setMessage(getResources().getString(R.string.verifying_creds));
        z_ProgressDialog.setCancelable(false);
        z_ProgressDialog.setIndeterminate(true);
        z_ProgressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        z_ProgressDialog.setCancelable(false);
        z_ProgressDialog.show();
        String regId = prefs.getString("registration_id", "");
        FacebookConnect facebookConnect = new FacebookConnect(SplashScreen.this, 1, APPLICATION_ID, true, regId);
        facebookConnect.execute();
        checkPlayServices();

    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        UploadManager.removeCallback(this);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        try {
            super.onActivityResult(requestCode, resultCode, intent);
            Session.getActiveSession().onActivityResult(this, requestCode, resultCode, intent);

        } catch (Exception w) {

            w.printStackTrace();

            try {
                com.facebook.Session fbSession = com.facebook.Session.getActiveSession();
                if (fbSession != null) {
                    fbSession.closeAndClearTokenInformation();
                }
                com.facebook.Session.setActiveSession(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (dismissDialog) {
            if (z_ProgressDialog != null) {
                z_ProgressDialog.dismiss();
            }
        }
    }

    @Override
    public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status,
    String stringId) {
        if (requestType == CommonLib.SIGNUP) {
            if (destroyed)
                return;
            if (status) {
                // make the login call now.
                // String name = ((TextView) signUpPage
                // .findViewById(R.id.login_username)).getText()
                // .toString();
                // if (name == null || name.equals(""))
                // return;
                // String email = ((TextView) signUpPage
                // .findViewById(R.id.login_email)).getText().toString();
                // if (email == null || email.equals(""))
                // return;
                // String password = ((TextView) signUpPage
                // .findViewById(R.id.login_password)).getText()
                // .toString();
                // if (password == null || password.equals(""))
                // return;
                // UploadManager.login(email, password);
                // } else {


            }
        } else if (requestType == CommonLib.LOGIN) {
            if (destroyed)
                return;
            if (status) {
                // save the access token.
                // {"access_token":"532460","user":{"user_id":0,"is_verified":0}}
                JSONObject responseJSON = null;
                try {
                    responseJSON = new JSONObject(String.valueOf(data));
                    SharedPreferences.Editor editor = prefs.edit();
                    if (responseJSON.has("access_token")) {
                        editor.putString("access_token", responseJSON.getString("access_token"));
                    }
                    if (responseJSON.has("HSLogin") && responseJSON.get("HSLogin") instanceof Boolean) {
                        editor.putBoolean("HSLogin", responseJSON.getBoolean("HSLogin"));
                    }
                    if (responseJSON.has("INSTITUTION_NAME")) {
                        editor.putString("INSTITUTION_NAME", responseJSON.getString("INSTITUTION_NAME"));
                    }
                    if (responseJSON.has("STUDENT_ID")) {
                        editor.putString("STUDENT_ID", responseJSON.getString("STUDENT_ID"));
                    }
                    if (responseJSON.has("user_id") && responseJSON.get("user_id") instanceof Integer) {
                        editor.putInt("uid", responseJSON.getInt("user_id"));
                    }
                    editor.commit();
                    checkPlayServices();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
        if (requestType == CommonLib.SIGNUP) {
            if (destroyed)
                return;
        }
    }

    private class TourPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {

            RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.splash_screen_pager_snippet, null);

            if (position == 0) {

                ImageView tour_logo = (ImageView) layout.findViewById(R.id.baatna_logo);
                ImageView tour_text_logo = (ImageView) layout.findViewById(R.id.baatna_text);
                TextView tour_text= (TextView) layout.findViewById(R.id.description);
                tour_logo.setVisibility(View.VISIBLE);
                tour_text_logo.setVisibility(View.VISIBLE);
                tour_text.setVisibility(View.VISIBLE);

                // setting image
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;

                    BitmapFactory.decodeResource(getResources(), R.drawable.logo, options);
                    options.inSampleSize = CommonLib.calculateInSampleSize(options, width, height);
                    options.inJustDecodeBounds = false;
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo, options);

                    tour_logo.setImageBitmap(bitmap);

                    BitmapFactory.Options options2 = new BitmapFactory.Options();
                    options2.inJustDecodeBounds = true;

                    BitmapFactory.decodeResource(getResources(), R.drawable.logo, options2);
                    options2.inSampleSize = CommonLib.calculateInSampleSize(options2, width, height);
                    options2.inJustDecodeBounds = false;
                    options2.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.baatna_splash_text, options2);
                    tour_text_logo.setImageBitmap(bitmap2);

                    tour_text.setText(getResources().getString(R.string.splash_description_1));
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    tour_logo.setBackgroundColor(getResources().getColor(R.color.transparent1));
                }
                findViewById(R.id.signup_container).setVisibility(View.GONE);
                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                imageParams.setMargins(15 * width / 50, 3 * width / 16, 15 * width / 50, 0);

            } else if (position == 1) {

                ImageView tour_logo = (ImageView) layout.findViewById(R.id.baatna_logo);
                ImageView tour_text_logo = (ImageView) layout.findViewById(R.id.baatna_text);
                TextView tour_text= (TextView) layout.findViewById(R.id.description);
                tour_logo.setVisibility(View.VISIBLE);
                tour_text_logo.setVisibility(View.GONE);
                tour_text.setVisibility(View.VISIBLE);
                tour_text.setText(getResources().getString(R.string.splash_description_2));

                tour_logo.getLayoutParams().width = 4 * width  / 5;
                tour_logo.getLayoutParams().height = 2 * width / 5;
                // setting image
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;

                    BitmapFactory.decodeResource(getResources(), R.drawable.tour_1, options);
                    options.inSampleSize = CommonLib.calculateInSampleSize(options, width, height);
                    options.inJustDecodeBounds = false;
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tour_1, options);

                    tour_logo.setImageBitmap(bitmap);

                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    tour_logo.setBackgroundColor(getResources().getColor(R.color.transparent1));
                }
                findViewById(R.id.signup_container).setVisibility(View.GONE);
                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                imageParams.setMargins(width / 20, width / 20, width / 20, 0);


            } else if (position == 2) {

                ImageView tour_logo = (ImageView) layout.findViewById(R.id.baatna_logo);
                ImageView tour_text_logo = (ImageView) layout.findViewById(R.id.baatna_text);
                TextView tour_text= (TextView) layout.findViewById(R.id.description);
                tour_logo.setVisibility(View.VISIBLE);
                tour_text_logo.setVisibility(View.GONE);
                tour_text.setVisibility(View.VISIBLE);
                tour_text.setText(getResources().getString(R.string.splash_description_3));
                tour_logo.getLayoutParams().width = 4 * width  / 5;
                tour_logo.getLayoutParams().height = 2 * width / 5;
                // setting image
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;

                    BitmapFactory.decodeResource(getResources(), R.drawable.tour_2, options);
                    options.inSampleSize = CommonLib.calculateInSampleSize(options, width, height);
                    options.inJustDecodeBounds = false;
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tour_2, options);

                    tour_logo.setImageBitmap(bitmap);

                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    tour_logo.setBackgroundColor(getResources().getColor(R.color.transparent1));
                }

                tour_logo.setVisibility(View.VISIBLE);
                findViewById(R.id.signup_container).setVisibility(View.GONE);
                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                imageParams.setMargins(15 * width / 50, 3 * width / 16, 15 * width / 50, 0);

            } else if (position == 3) {

                ImageView tour_logo = (ImageView) layout.findViewById(R.id.baatna_logo);
                ImageView tour_text_logo = (ImageView) layout.findViewById(R.id.baatna_text);
                TextView tour_text= (TextView) layout.findViewById(R.id.description);
                tour_logo.setVisibility(View.VISIBLE);
                tour_text_logo.setVisibility(View.GONE);
                tour_text.setVisibility(View.VISIBLE);
                tour_logo.getLayoutParams().width = 4 * width  / 5;
                tour_logo.getLayoutParams().height = 2 * width / 5;
                // setting image
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;

                    BitmapFactory.decodeResource(getResources(), R.drawable.tour_3, options);
                    options.inSampleSize = CommonLib.calculateInSampleSize(options, width, height);
                    options.inJustDecodeBounds = false;
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tour_3, options);

                    tour_logo.setImageBitmap(bitmap);

                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    tour_logo.setBackgroundColor(getResources().getColor(R.color.transparent1));
                }

                findViewById(R.id.signup_container).setVisibility(View.GONE);
                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                imageParams.setMargins(15 * width / 50, 3 * width / 16, 15 * width / 50, 0);

            } else if (position == 4) {

                ImageView tour_logo = (ImageView) layout.findViewById(R.id.baatna_logo);
                ImageView tour_text_logo = (ImageView) layout.findViewById(R.id.baatna_text);
                TextView tour_text= (TextView) layout.findViewById(R.id.description);
                tour_logo.setVisibility(View.VISIBLE);
                tour_text_logo.setVisibility(View.VISIBLE);
                tour_text.setVisibility(View.GONE);

                tour_logo.getLayoutParams().width = 4 * width  / 5;
                tour_logo.getLayoutParams().height = 2 * width / 5;
                // setting image
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;

                    BitmapFactory.decodeResource(getResources(), R.drawable.logo, options);
                    options.inSampleSize = CommonLib.calculateInSampleSize(options, width, height);
                    options.inJustDecodeBounds = false;
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo, options);

                    tour_logo.setImageBitmap(bitmap);

                    BitmapFactory.Options options2 = new BitmapFactory.Options();
                    options2.inJustDecodeBounds = true;

                    BitmapFactory.decodeResource(getResources(), R.drawable.logo, options2);
                    options2.inSampleSize = CommonLib.calculateInSampleSize(options2, width, height);
                    options2.inJustDecodeBounds = false;
                    options2.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.logo, options2);
                    tour_text_logo.setImageBitmap(bitmap2);


                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    tour_logo.setBackgroundColor(getResources().getColor(R.color.transparent1));
                }

                tour_logo.setVisibility(View.VISIBLE);

                findViewById(R.id.tour_dots).setVisibility(View.GONE);
                findViewById(R.id.tour_dots).setClickable(false);
                findViewById(R.id.signup_container).setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                imageParams.setMargins(15 * width / 50, 3 * width / 16, 15 * width / 50, 0);

            }
            collection.addView(layout, 0);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public void finishUpdate(ViewGroup arg0) {
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(ViewGroup arg0) {
        }

    }

    private void updateDotsContainer() {

        LinearLayout dotsContainer = (LinearLayout) findViewById(R.id.tour_dots);
        dotsContainer.removeAllViews();

        int index = 5;

        for (int count = 0; count < index; count++) {
            ImageView dots = new ImageView(getApplicationContext());

            if (count == 0) {
                dots.setImageResource(R.drawable.tour_image_dots_selected);
                dots.setPadding(width / 40, 0, width / 40, 0);

            } else {
                dots.setImageResource(R.drawable.tour_image_dots_unselected);
                dots.setPadding(0, 0, width / 40, 0);
            }

            final int c = count;
            dots.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        ((ViewPager) mViewPager).setCurrentItem(c);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            dotsContainer.addView(dots);
        }
    }

    public void navigateToHome() {
        if (prefs.getInt("uid", 0) != 0) {
            if (prefs.getBoolean("instutionLogin", true) && prefs.getBoolean("HSLogin", true)) {
                //did not login using hslogin before, navigate to user details page
                Intent intent = new Intent(this, UserLoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(this, Home.class);
                startActivity(intent);
                finish();
            }
        }
    }
}
