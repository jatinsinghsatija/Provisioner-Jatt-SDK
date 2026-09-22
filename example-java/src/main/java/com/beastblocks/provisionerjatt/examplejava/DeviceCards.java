package com.beastblocks.provisionerjatt.examplejava;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.beastblocks.provisionerjattsdk.domain.ConnectionState;
import com.beastblocks.provisionerjattsdk.domain.DpcComponent;
import com.beastblocks.provisionerjattsdk.domain.ProvisioningStage;
import com.beastblocks.provisionerjattsdk.domain.TransportKind;
import com.beastblocks.provisionerjattsdk.ui.DeviceUiState;
import java.util.ArrayList;
import java.util.List;

final class DeviceCards {
    interface Listener {
        void onScan(String id);
        void onMakeOwner(String id, DpcComponent component);
        void onRemoveOwner(String id, DpcComponent component);
        void onRetry(String id);
        void onDisconnect(String id);
    }

    private DeviceCards() {}

    static void bind(LinearLayout container, List<DeviceUiState> devices, boolean automationConfigured, Listener listener) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        for (DeviceUiState device : devices) {
            container.addView(createCard(inflater, container, device, automationConfigured, listener));
        }
    }

    private static View createCard(
        LayoutInflater inflater,
        LinearLayout parent,
        DeviceUiState device,
        boolean automationConfigured,
        Listener listener
    ) {
        Context context = parent.getContext();
        View itemView = inflater.inflate(R.layout.item_device, parent, false);
        String id = SdkCalls.id(device.getTarget());
        itemView.setTag("device_" + id);

        TextView name = itemView.findViewById(R.id.device_name);
        TextView meta = itemView.findViewById(R.id.device_meta);
        ProgressBar connecting = itemView.findViewById(R.id.device_connecting);
        Button disconnect = itemView.findViewById(R.id.btn_disconnect);
        TextView status = itemView.findViewById(R.id.device_status);
        TextView message = itemView.findViewById(R.id.device_message);
        View automatedSection = itemView.findViewById(R.id.automated_section);
        ProgressBar automatedProgress = itemView.findViewById(R.id.automated_progress);
        TextView automatedLabel = itemView.findViewById(R.id.automated_label);
        View ownerRemovedSection = itemView.findViewById(R.id.owner_removed_section);
        TextView ownerRemovedBody = itemView.findViewById(R.id.owner_removed_body);
        Button redo = itemView.findViewById(R.id.btn_redo);
        View manualSection = itemView.findViewById(R.id.manual_section);
        Button scan = itemView.findViewById(R.id.btn_scan);
        View scanningRow = itemView.findViewById(R.id.scanning_row);
        LinearLayout dpcContainer = itemView.findViewById(R.id.dpc_container);
        View actionRow = itemView.findViewById(R.id.action_row);
        TextView manualResult = itemView.findViewById(R.id.manual_result);

        name.setText(device.getTarget().getDisplayName());
        List<String> bits = new ArrayList<>();
        bits.add(ProvisionerCopy.transportLabel(device.getTarget().getTransport()));
        if (device.getTarget().getSerial() != null) {
            bits.add(device.getTarget().getSerial());
        }
        bits.add(ProvisionerCopy.connectionLabel(context, device.getConnection()));
        meta.setText(String.join(" • ", bits));

        connecting.setVisibility(device.getConnection() == ConnectionState.CONNECTING ? View.VISIBLE : View.GONE);
        boolean wirelessConnected = device.getTarget().getTransport() == TransportKind.WIRELESS
            && device.getConnection() == ConnectionState.READY;
        disconnect.setVisibility(wirelessConnected ? View.VISIBLE : View.GONE);
        disconnect.setOnClickListener(v -> listener.onDisconnect(id));

        String statusText = ProvisionerCopy.connectionStatus(context, device);
        status.setVisibility(statusText == null ? View.GONE : View.VISIBLE);
        if (statusText != null) status.setText(statusText);

        if (device.getMessage() == null || device.getMessage().isEmpty()) {
            message.setVisibility(View.GONE);
        } else {
            message.setVisibility(View.VISIBLE);
            message.setText(device.getMessage());
        }

        automatedSection.setVisibility(View.GONE);
        manualSection.setVisibility(View.GONE);
        ownerRemovedSection.setVisibility(View.GONE);

        if (device.getConnection() != ConnectionState.READY) {
            return itemView;
        }

        ProvisioningStage stage = device.getAutomatedStage();
        if (stage instanceof ProvisioningStage.OwnerRemoved removed) {
            automatedSection.setVisibility(View.VISIBLE);
            automatedProgress.setVisibility(View.GONE);
            automatedLabel.setText(R.string.provision_removed);
            ownerRemovedSection.setVisibility(View.VISIBLE);
            ownerRemovedBody.setText(ProvisionerCopy.ownerRemovedBody(context, removed.getComponent()));
            redo.setOnClickListener(v -> listener.onRetry(id));
        } else if (automationConfigured) {
            automatedSection.setVisibility(View.VISIBLE);
            ownerRemovedSection.setVisibility(View.GONE);
            automatedProgress.setVisibility(ProvisionerCopy.automatedWorking(stage) ? View.VISIBLE : View.GONE);
            automatedLabel.setText(ProvisionerCopy.automatedLabel(context, stage));
        } else {
            bindManual(inflater, context, device, listener, manualSection, scan, scanningRow, dpcContainer, actionRow, manualResult);
        }
        return itemView;
    }

    private static void bindManual(
        LayoutInflater inflater,
        Context context,
        DeviceUiState device,
        Listener listener,
        View manualSection,
        Button scan,
        View scanningRow,
        LinearLayout dpcContainer,
        View actionRow,
        TextView manualResult
    ) {
        manualSection.setVisibility(View.VISIBLE);
        String id = SdkCalls.id(device.getTarget());
        boolean busy = device.getManual().getActionInProgress() != null;
        scan.setEnabled(!device.getManual().getScanning());
        scan.setOnClickListener(v -> listener.onScan(id));
        scanningRow.setVisibility(device.getManual().getScanning() ? View.VISIBLE : View.GONE);
        actionRow.setVisibility(busy ? View.VISIBLE : View.GONE);
        if (device.getManual().getResult() == null || device.getManual().getResult().isEmpty()) {
            manualResult.setVisibility(View.GONE);
        } else {
            manualResult.setVisibility(View.VISIBLE);
            manualResult.setText(device.getManual().getResult());
        }
        dpcContainer.removeAllViews();
        for (DpcComponent component : device.getManual().getComponents()) {
            View row = inflater.inflate(R.layout.item_dpc, dpcContainer, false);
            TextView dpcName = row.findViewById(R.id.dpc_name);
            Button makeOwner = row.findViewById(R.id.btn_make_owner);
            Button removeOwner = row.findViewById(R.id.btn_remove_owner);
            TextView hint = row.findViewById(R.id.dpc_hint);
            dpcName.setText(component.getFlattened());
            makeOwner.setEnabled(!busy);
            makeOwner.setOnClickListener(v -> confirm(context, true, component, id, listener));
            if (component.getTestOnly()) {
                removeOwner.setVisibility(View.VISIBLE);
                hint.setVisibility(View.GONE);
                removeOwner.setEnabled(!busy);
                removeOwner.setOnClickListener(v -> confirm(context, false, component, id, listener));
            } else {
                removeOwner.setVisibility(View.GONE);
                hint.setVisibility(View.VISIBLE);
            }
            dpcContainer.addView(row);
        }
    }

    private static void confirm(
        Context context,
        boolean makeOwner,
        DpcComponent component,
        String id,
        Listener listener
    ) {
        String title = context.getString(makeOwner ? R.string.confirm_make_owner : R.string.confirm_remove_owner);
        String body = context.getString(
            makeOwner ? R.string.confirm_make_body : R.string.confirm_remove_body,
            component.getFlattened()
        );
        new AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(body)
            .setPositiveButton(R.string.confirm, (dialog, which) -> {
                if (makeOwner) listener.onMakeOwner(id, component);
                else listener.onRemoveOwner(id, component);
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }
}
