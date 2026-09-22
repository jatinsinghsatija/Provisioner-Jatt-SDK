package com.beastblocks.provisionerjatt.examplejava;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.beastblocks.provisionerjattsdk.domain.ProvisioningSettings;

final class AutomationSheet {
    interface Callbacks {
        boolean onSaveAutomation(String packageName, String apkUrl);
        boolean onSetSerial(String serial);
        boolean onClearAutomation();
        boolean onClearSerial();
    }

    private AutomationSheet() {}

    static void show(Context context, ProvisioningSettings settings, Callbacks callbacks) {
        Dialog dialog = new Dialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.sheet_automation, null);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        EditText packageName = view.findViewById(R.id.package_name);
        EditText apkUrl = view.findViewById(R.id.apk_url);
        EditText serialNumber = view.findViewById(R.id.serial_number);
        TextView packageSupport = view.findViewById(R.id.package_support);
        TextView urlSupport = view.findViewById(R.id.url_support);
        Button save = view.findViewById(R.id.btn_save);
        Button clearAutomation = view.findViewById(R.id.btn_clear_automation);
        Button clearSerial = view.findViewById(R.id.btn_clear_serial);
        TextView automationStatus = view.findViewById(R.id.automation_status);

        packageName.setText(settings.getPackageName());
        apkUrl.setText(settings.getApkUrl());
        serialNumber.setText(settings.getSerialNumber());

        Runnable refresh = () -> {
            String pkg = text(packageName);
            String url = text(apkUrl);
            String serial = text(serialNumber);
            boolean packageFilled = !pkg.isBlank();
            boolean urlFilled = !url.isBlank();
            boolean packageValid = ProvisioningSettings.Companion.isValidPackageName(pkg);
            boolean urlValid = ProvisioningSettings.Companion.isValidDownloadUrl(url);
            packageSupport.setText(packageFilled && !packageValid ? R.string.package_error : R.string.package_hint);
            urlSupport.setText(urlFilled && !urlValid ? R.string.apk_error : R.string.apk_hint);
            boolean canSave = (packageValid && urlValid && packageFilled && urlFilled)
                || (!serial.isBlank() && !packageFilled && !urlFilled);
            save.setEnabled(canSave);
        };
        TextWatcher watcher = new SimpleWatcher(refresh);
        packageName.addTextChangedListener(watcher);
        apkUrl.addTextChangedListener(watcher);
        serialNumber.addTextChangedListener(watcher);
        refresh.run();

        if (settings.getAutomationConfigured()) {
            clearAutomation.setVisibility(View.VISIBLE);
            automationStatus.setVisibility(View.VISIBLE);
            automationStatus.setText(
                settings.getHasSerialFilter()
                    ? context.getString(R.string.automation_on_serial, settings.getSerialNumber())
                    : context.getString(R.string.automation_on_all)
            );
        }
        if (settings.getHasSerialFilter()) {
            clearSerial.setVisibility(View.VISIBLE);
        }

        save.setOnClickListener(v -> {
            boolean autoOk = callbacks.onSaveAutomation(text(packageName), text(apkUrl));
            boolean serialOk = callbacks.onSetSerial(text(serialNumber));
            if (autoOk && serialOk) dialog.dismiss();
        });
        clearAutomation.setOnClickListener(v -> {
            if (callbacks.onClearAutomation()) dialog.dismiss();
        });
        clearSerial.setOnClickListener(v -> {
            if (callbacks.onClearSerial()) dialog.dismiss();
        });
        dialog.show();
    }

    private static String text(EditText field) {
        Editable value = field.getText();
        return value == null ? "" : value.toString().trim();
    }

    private static final class SimpleWatcher implements TextWatcher {
        private final Runnable onChange;

        SimpleWatcher(Runnable onChange) {
            this.onChange = onChange;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) {
            onChange.run();
        }
    }
}
