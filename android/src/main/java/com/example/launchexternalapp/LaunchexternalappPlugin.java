package com.example.launchexternalapp;

import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.content.pm.PackageManager;

public class LaunchexternalappPlugin implements FlutterPlugin, MethodChannel.MethodCallHandler {

    private MethodChannel channel;
    private Context context;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        context = flutterPluginBinding.getApplicationContext();
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "launch_vpn");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        if (context == null) {
            result.error("ERROR", "Context is null", null);
            return;
        }

        switch (call.method) {
            case "getPlatformVersion":
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;

            case "isAppInstalled":
                String packageName = call.argument("package_name");
                if (TextUtils.isEmpty(packageName)) {
                    result.error("ERROR", "Empty or null package name", null);
                } else {
                    result.success(isAppInstalled(packageName));
                }
                break;

            case "openApp":
                String pkgName = call.argument("package_name");
                String openStore = call.argument("open_store");
                String arguments = call.argument("arguments");

                if (arguments != null) {
                    result.success(openApp(pkgName, openStore, arguments));
                } else {
                    result.success(openApp(pkgName, openStore));
                }
                break;

            default:
                result.notImplemented();
        }
    }

    private boolean isAppInstalled(String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    private String openApp(String packageName, String openStore) {
        try {
            if (isAppInstalled(packageName)) {
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                if (launchIntent != null) {
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(launchIntent);
                    return "app_opened";
                }
            } else if (!"false".equals(openStore)) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(android.net.Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
                context.startActivity(intent);
                return "navigated_to_store";
            }
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
        return "something went wrong";
    }

    private String openApp(String packageName, String openStore, String arguments) {
        try {
            if (isAppInstalled(packageName)) {
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                if (launchIntent != null) {
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (arguments != null) {
                        launchIntent.putExtra("arguments", arguments);
                    }
                    context.startActivity(launchIntent);
                    return "app_opened";
                }
            } else if (!"false".equals(openStore)) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(android.net.Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
                context.startActivity(intent);
                return "navigated_to_store";
            }
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
        return "something went wrong";
    }
}
