package com.beastblocks.provisionerjatt.examplejava;

import android.app.Activity;
import android.app.Dialog;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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

    static void show(Activity activity, ProvisioningSettings settings, Callbacks callbacks) {
        View content = activity.getLayoutInflater().inflate(R.layout.sheet_automation, null);
        EditText packageField = content.findViewById(R.id.package_name);
        EditText urlField = content.findViewById(R.id.apk_url);
        EditText serialField = content.findViewById(R.id.serial_number);
        TextView packageHelp = content.findViewById(R.id.package_help);
        TextView urlHelp = content.findViewById(R.id.apk_help);
        Button save = content.findViewById(R.id.save_automation);
        Button clearAutomation = content.findViewById(R.id.clear_automation);
        Button clearSerial = content.findViewById(R.id.clear_serial);
        TextView status = content.findViewById(R.id.automation_status);

        packageField.setText(settings.getPackageName());
        urlField.setText(settings.getApkUrl());
        serialField.setText(settings.getSerialNumber());

        if (settings.getAutomationConfigured()) {
            clearAutomation.setVisibility(View.VISIBLE);
            status.setVisibility(View.VISIBLE);
            if (settings.getHasSerialFilter()) {
                status.setText(activity.getString(
                    R.string.automation_on_serial,
                    settings.getSerialNumber()
                ));
            } else {
                status.setText(R.string.automation_on_all);
            }
        }
        if (settings.getHasSerialFilter()) {
            clearSerial.setVisibility(View.VISIBLE);
        }

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(content);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        save.setOnClickListener(v -> {
            String packageName = text(packageField);
            String apkUrl = text(urlField);
            String serial = text(serialField);
            boolean packageFilled = !packageName.isEmpty();
            boolean urlFilled = !apkUrl.isEmpty();
            boolean packageValid = ProvisioningSettings.Companion.isValidPackageName(packageName);
            boolean urlValid = ProvisioningSettings.Companion.isValidDownloadUrl(apkUrl);
            packageHelp.setText(packageFilled && !packageValid
                ? R.string.package_error
                : R.string.package_hint);
            urlHelp.setText(urlFilled && !urlValid ? R.string.apk_error : R.string.apk_hint);
            boolean canSave = (packageValid && urlValid && packageFilled && urlFilled)
                || (!serial.isEmpty() && !packageFilled && !urlFilled);
            if (!canSave) return;
            if (!callbacks.onSaveAutomation(packageName, apkUrl)) return;
            if (!callbacks.onSetSerial(serial)) return;
            dialog.dismiss();
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
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}
