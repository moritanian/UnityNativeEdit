package com.bkmin.android;
import com.unity3d.player.*;

import android.app.Activity;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import org.json.JSONException;
import org.json.JSONObject;

import com.unity3d.player.UnityPlayerNativeActivity;

public class NativeEditPlugin {
    public static Activity unityActivity;
    public static RelativeLayout mainLayout;
    private static ViewGroup	topViewGroup;
    private static int		keyboardHeight = 0;
    private static String   unityName = "";
    private static String MSG_SHOW_KEYBOARD = "ShowKeyboard";


    public static final String LOG_TAG = "NativeEditPlugin";

    private static View getLeafView(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup)view;
            for (int i = 0; i < vg.getChildCount(); ++i) {
                View chview = vg.getChildAt(i);
                View result = getLeafView(chview);
                if (result != null)
                    return result;
            }
            return null;
        }
        else {
            Log.i(LOG_TAG, "Found leaf view");
            return view;
        }
    }

    @SuppressWarnings("unused")
    public static void InitPluginMsgHandler(final String _unityName)
    {
        unityActivity = UnityPlayer.currentActivity;
        unityName = _unityName;

        unityActivity.runOnUiThread(new Runnable() {
            public void run() {
                if (mainLayout != null)
                    topViewGroup.removeView(mainLayout);

                final ViewGroup rootView = (ViewGroup) unityActivity.findViewById (android.R.id.content);

                rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        Rect r = new Rect();
                        rootView.getWindowVisibleDisplayFrame(r);
                        int screenHeight = rootView.getRootView().getHeight();

                        // r.bottom is the position above soft keypad or device button.
                        // if keypad is shown, the r.bottom is smaller than that before.
                        int newKeyboardHeight = screenHeight - r.bottom;
                        if( keyboardHeight == newKeyboardHeight){
                            return;
                        }
                        keyboardHeight = newKeyboardHeight;
                        boolean bKeyOpen = (keyboardHeight > screenHeight * 0.15);

                        float fKeyHeight = (float) keyboardHeight / (float) screenHeight;

                        JSONObject json = new JSONObject();
                        try {
                            json.put("msg", MSG_SHOW_KEYBOARD);
                            json.put("show", bKeyOpen);
                            json.put("keyheight", fKeyHeight);
                        } catch (JSONException e) {
                        }
                        SendUnityMessage(json);
                    }
                });

                View topMostView = getLeafView(rootView);
                topViewGroup = (ViewGroup) topMostView.getParent();
                mainLayout = new RelativeLayout(unityActivity);
                RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);
                topViewGroup.addView(mainLayout, rlp);

                rootView.setOnSystemUiVisibilityChangeListener
                        (new View.OnSystemUiVisibilityChangeListener() {
                            @Override
                            public void onSystemUiVisibilityChange(int visibility) {
                                rootView.setSystemUiVisibility(
                                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                            }
                        });
            }
        });
    }

    @SuppressWarnings("unused")
    public static void ClosePluginMsgHandler()
    {
        unityActivity.runOnUiThread(new Runnable() {
            public void run() {
                topViewGroup.removeView(mainLayout);
            }
        });
    }

    public static void SendUnityMessage(JSONObject jsonMsg)
    {
        UnityPlayer.UnitySendMessage(unityName, "OnMsgFromPlugin", jsonMsg.toString());
    }

    @SuppressWarnings("unused")
    public static String SendUnityMsgToPlugin(final int nSenderId, final String jsonMsg) {
        final Runnable task = new Runnable() {
            public void run() {
                EditBox.processRecvJsonMsg(nSenderId, jsonMsg);
            }
        };
        unityActivity.runOnUiThread(task);
        return new JSONObject().toString();
    }
}
