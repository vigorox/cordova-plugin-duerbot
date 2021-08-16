package com.liteputer.cordova.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import com.baidu.duer.bot.BotMessageProtocol;
import com.baidu.duer.botsdk.*;
import com.baidu.duer.botsdk.BotSdk;
import com.baidu.duer.botsdk.IBotMessageListener;

import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.Fragment;
import android.content.BroadcastReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class DuerBotPlugin extends CordovaPlugin {
    DuerBot duerBot;
    CallbackContext initCallbackContext;
    CallbackContext addHandlerCallbackContext;
    private static final String TAG = "DuerBotPlugin";

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        if (action.equals("register")) {
            String botId = data.getString(0);
            String signatureKey = data.getString(1);
            Context context = this.cordova.getActivity().getApplicationContext();
            initCallbackContext = callbackContext;
            this.duerBot = new DuerBot(botId, signatureKey, context, initCallbackContext);
            PluginResult pluginresult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginresult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginresult);
            return true;
        } else if (action.equals("speak")) {
            String txt = data.getString(0);
            this.duerBot.speak(txt);
            return true;
        } else if (action.equals("addDuerBotHandler")) {
            addHandlerCallbackContext = callbackContext;
            this.duerBot.addDuerBotHandler(addHandlerCallbackContext);
            PluginResult pluginresult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginresult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginresult);
            return true;
        } else if (action.equals("removeDuerBotHandler")) {
            this.duerBot.removeDuerBotHandler(callbackContext);
            return true;
        } else if (action.equals("isRegistered")) {
            if (this.duerBot != null && this.duerBot.isRegistered == true) {
                callbackContext.success();
                ;
            } else {
                if (this.duerBot == null) {
                    callbackContext.error("Duerbot is not inited yet!");
                } else {
                    callbackContext.error("Duerbot is not registered yet!");
                }
            }
            return true;
        } else {
            return false;
        }
    }
}

class DuerBot {
    private static final String TAG = "DuerBot";
    public DuerBotHandler duerBotHandler;
    public Boolean isRegistered = false;

    public DuerBot(String botId, String signatureKey, Context context, CallbackContext callbackContext) {
        ContextUtil.setContext(context);
        String random1 = "random1" + Math.random();
        String random2 = "random2" + Math.random();

        BotSdk.getInstance().init(context);
        BotSdk.enableLog(true);
        BotSdk.getInstance().register(BotMessageListener.getInstance(), botId, random1,
                BotSDKUtils.sign(random1, signatureKey), random2, BotSDKUtils.sign(random2, signatureKey));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BotConstants.ACTION_REGISTER_SUCCESS);
        LocalBroadcastManager.getInstance(context).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    switch (intent.getAction()) {
                        case BotConstants.ACTION_REGISTER_SUCCESS:
                            if (callbackContext != null) {
                                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, true);
                                pluginResult.setKeepCallback(false);
                                callbackContext.sendPluginResult(pluginResult);
                            }
                            isRegistered = true;
                            Log.i(TAG, "DuerBot init success");
                            break;
                        default:
                            Log.i(TAG, "unknown action:" + intent.getAction());
                            break;
                    }
                }
            }
        }, intentFilter);

    }

    public void addDuerBotHandler(CallbackContext callbackContext) {
        this.duerBotHandler = new DuerBotHandler(callbackContext);

        BotMessageListener.getInstance().addCallback(this.duerBotHandler);
        BotSdk.getInstance().setDialogStateListener(this.duerBotHandler);

        UiContextPayload payload = new UiContextPayload();
        payload.setEnableGeneralUtterances(false);
        BotSdk.getInstance().updateUiContext(payload);
    }

    public void removeDuerBotHandler(CallbackContext callbackContext) {
        BotMessageListener.getInstance().removeCallback(this.duerBotHandler);
        BotSdk.getInstance().setDialogStateListener(null);
    }

    public void speak(String txt) {
        BotSdk.getInstance().speak(txt, false);
    }

}

class DuerBotHandler implements IBotIntentCallback, IDialogStateListener {
    private final String TAG = "DuerBotHandler";
    private CallbackContext callbackContext;

    public DuerBotHandler(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }

    @Override
    public void handleIntent(BotIntent intent, String customData) {
        // String intentResult = getString(R.string.result_intent) +
        // "\n指令名称:%s\n槽位信息：%s";
        Log.d(TAG, "handleIntent");
        return;
    }

    @Override
    public void onClickLink(String url, HashMap<String, String> paramMap) {
        Log.d(TAG, "url=" + url);
        if (BotConstants.UNKNOWN_URL.equals((url))) {
            String query = paramMap.get("query");
            if (query != null && callbackContext != null) {
                Log.d(TAG, query);
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, query);
                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }
        }
    }

    @Override
    public void onHandleScreenNavigatorEvent(int event) {
        Log.d(TAG, "onHandleScreenNavigatorEvent");
    }

    @Override
    public void onDialogStateChanged(DialogState dialogState) {
        Log.d(TAG, "onDialogStateChanged");
    }
}

class BotConstants {
    public static final String UNKNOWN_URL = "http://sdk.bot.dueros.ai?action=unknown_utterance";
    public static final String ACTION_REGISTER_SUCCESS = "com.baidu.duer.test_botsdk.register_success";

}