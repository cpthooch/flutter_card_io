package com.varunvairavan.fluttercardio;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;

import io.card.payment.CardIOActivity;
import io.card.payment.CardType;
import io.card.payment.CreditCard;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.embedding.engine.plugins.FlutterPlugin;

/**
 * FlutterCardIoPlugin
 */
public class FlutterCardIoPlugin implements MethodCallHandler, ActivityResultListener, FlutterPlugin, ActivityAware {
    private static final int MY_SCAN_REQUEST_CODE = 100;
    private static final String METHOD_CHANNEL_NAME = "flutter_card_io";

    private  FlutterPlugin.FlutterPluginBinding pluginBinding;
    private Activity activity;
    private ActivityPluginBinding activityBinding;
    private MethodChannel channel;
    private Result pendingResult;

    @Override
    public void onAttachedToEngine(FlutterPlugin.FlutterPluginBinding binding) {
        pluginBinding = binding;
    }

    @Override
    public void onDetachedFromEngine(FlutterPlugin.FlutterPluginBinding binding) {
        pluginBinding = null;
    }

    @Override
    public void onDetachedFromActivity() {
        clearPluginSetup();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
        onAttachedToActivity(binding);
    }

    @Override
    public void  onAttachedToActivity(ActivityPluginBinding binding) {
        activityBinding = binding;

        createPluginSetup(
                pluginBinding.getBinaryMessenger(),
                activityBinding.getActivity(),
                activityBinding);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity();
    }

    @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void createPluginSetup(
            BinaryMessenger messenger,
            Activity activity,
            ActivityPluginBinding activityBinding) {

        this.activity = activity;

        channel = new MethodChannel(messenger, METHOD_CHANNEL_NAME);
        channel.setMethodCallHandler(this);

        activityBinding.addActivityResultListener(this);
    }

    private void clearPluginSetup() {
        activity = null;
        activityBinding.removeActivityResultListener(this);
        activityBinding = null;
        channel.setMethodCallHandler(null);
        channel = null;
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (pendingResult != null) {
            result.error("ALREADY_ACTIVE", "Scan card is already active", null);
            return;
        }

        if (activity == null) {
            result.error("no_activity", "flutter_card_io plugin requires a foreground activity.", null);
            return;
        }

        pendingResult = result;

        if (call.method.equals("scanCard")) {
            Intent scanIntent = new Intent(activity, CardIOActivity.class);

            boolean requireExpiry = false;
            if (call.hasArgument("requireExpiry")) {
                requireExpiry = call.argument("requireExpiry");
            }

            boolean requireCVV = false;
            if (call.hasArgument("requireCVV")) {
                requireCVV = call.argument("requireCVV");
            }

            boolean requirePostalCode = false;
            if (call.hasArgument("requirePostalCode")) {
                requirePostalCode = call.argument("requirePostalCode");
            }

            boolean requireCardHolderName = false;
            if (call.hasArgument("requireCardHolderName")) {
                requireCardHolderName = call.argument("requireCardHolderName");
            }

            boolean restrictPostalCodeToNumericOnly = false;
            if (call.hasArgument("restrictPostalCodeToNumericOnly")) {
                restrictPostalCodeToNumericOnly = call.argument("restrictPostalCodeToNumericOnly");
            }

            boolean scanExpiry = true;
            if (call.hasArgument("scanExpiry")) {
                scanExpiry = call.argument("scanExpiry");
            }

            String scanInstructions = null;
            if (call.hasArgument("scanInstructions")) {
                scanInstructions = call.argument("scanInstructions");
            }

            boolean suppressManualEntry = false;
            if (call.hasArgument("suppressManualEntry")) {
                suppressManualEntry = call.argument("suppressManualEntry");
            }

            boolean suppressConfirmation = false;
            if (call.hasArgument("suppressConfirmation")) {
                suppressConfirmation = call.argument("suppressConfirmation");
            }

            boolean useCardIOLogo = false;
            if (call.hasArgument("useCardIOLogo")) {
                useCardIOLogo = call.argument("useCardIOLogo");
            }

            boolean hideCardIOLogo = false;
            if (call.hasArgument("hideCardIOLogo")) {
                hideCardIOLogo = call.argument("hideCardIOLogo");
            }

            boolean usePayPalActionbarIcon = true;
            if (call.hasArgument("usePayPalActionbarIcon")) {
                usePayPalActionbarIcon = call.argument("usePayPalActionbarIcon");
            }

            boolean keepApplicationTheme = false;
            if (call.hasArgument("keepApplicationTheme")) {
                keepApplicationTheme = call.argument("keepApplicationTheme");
            }

            // customize these values to suit your needs.
            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, requireExpiry); // default: false
            scanIntent.putExtra(CardIOActivity.EXTRA_SCAN_EXPIRY, scanExpiry);
            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, requireCVV); // default: false
            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, requirePostalCode); // default: false
            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CARDHOLDER_NAME, requireCardHolderName);
            scanIntent.putExtra(CardIOActivity.EXTRA_RESTRICT_POSTAL_CODE_TO_NUMERIC_ONLY, restrictPostalCodeToNumericOnly);
            scanIntent.putExtra(CardIOActivity.EXTRA_SCAN_INSTRUCTIONS, scanInstructions);
            scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, suppressManualEntry);
            scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_CONFIRMATION, suppressConfirmation);
            scanIntent.putExtra(CardIOActivity.EXTRA_USE_CARDIO_LOGO, useCardIOLogo);
            scanIntent.putExtra(CardIOActivity.EXTRA_HIDE_CARDIO_LOGO, hideCardIOLogo);
            scanIntent.putExtra(CardIOActivity.EXTRA_USE_PAYPAL_ACTIONBAR_ICON, usePayPalActionbarIcon);
            scanIntent.putExtra(CardIOActivity.EXTRA_KEEP_APPLICATION_THEME, keepApplicationTheme);

            // MY_SCAN_REQUEST_CODE is arbitrary and is only used within this activity.
            activity.startActivityForResult(scanIntent, MY_SCAN_REQUEST_CODE);
        } else {
            result.notImplemented();
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_SCAN_REQUEST_CODE) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

                Map<String, Object> response = new HashMap<>();
                response.put("cardholderName", scanResult.cardholderName);
                response.put("cardNumber", scanResult.cardNumber);
                String cardType = null;
                if (scanResult.getCardType() != CardType.UNKNOWN && scanResult.getCardType() != CardType.INSUFFICIENT_DIGITS) {
                    switch (scanResult.getCardType()) {
                        case AMEX:
                            cardType = "Amex";
                            break;
                        case DINERSCLUB:
                            cardType = "DinersClub";
                            break;
                        case DISCOVER:
                            cardType = "Discover";
                            break;
                        case JCB:
                            cardType = "JCB";
                            break;
                        case MASTERCARD:
                            cardType = "MasterCard";
                            break;
                        case VISA:
                            cardType = "Visa";
                            break;
                        case MAESTRO:
                            cardType = "Maestro";
                            break;
                        default:
                            break;
                    }
                }
                response.put("cardType", cardType);
                response.put("redactedCardNumber", scanResult.getRedactedCardNumber());
                response.put("expiryMonth", scanResult.expiryMonth);
                response.put("expiryYear", scanResult.expiryYear);
                response.put("cvv", scanResult.cvv);
                response.put("postalCode", scanResult.postalCode);
                pendingResult.success(response);
            } else {
                pendingResult.success(null);
            }
            pendingResult = null;
            return true;
        }
        return false;
    }
}
