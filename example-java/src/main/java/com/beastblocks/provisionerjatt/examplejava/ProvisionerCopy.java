package com.beastblocks.provisionerjatt.examplejava;

import android.content.Context;
import com.beastblocks.provisionerjattsdk.domain.ConnectionState;
import com.beastblocks.provisionerjattsdk.domain.DpcComponent;
import com.beastblocks.provisionerjattsdk.domain.ProvisioningStage;
import com.beastblocks.provisionerjattsdk.domain.TransportKind;
import com.beastblocks.provisionerjattsdk.ui.DeviceUiState;

final class ProvisionerCopy {
    private ProvisionerCopy() {}

    static String connectionLabel(Context context, ConnectionState state) {
        int id = switch (state) {
            case DISCOVERED -> R.string.label_discovered;
            case PERMISSION_REQUIRED -> R.string.label_usb_permission;
            case CONNECTING -> R.string.label_connecting;
            case UNAUTHORIZED -> R.string.label_authorization;
            case READY -> R.string.label_ready;
            case DISCONNECTED -> R.string.label_disconnected;
            case FAILED -> R.string.label_failed;
        };
        return context.getString(id);
    }

    static String connectionStatus(Context context, DeviceUiState device) {
        return switch (device.getConnection()) {
            case DISCOVERED -> {
                if (device.getTarget().getTransport() == TransportKind.USB) {
                    yield context.getString(R.string.usb_discovered);
                } else if (device.getPreviouslyPaired()) {
                    yield context.getString(R.string.previously_paired);
                } else {
                    yield context.getString(R.string.waiting_wireless);
                }
            }
            case PERMISSION_REQUIRED -> context.getString(R.string.waiting_usb_permission);
            case CONNECTING -> {
                if (device.getPreviouslyPaired()
                    && device.getTarget().getTransport() == TransportKind.WIRELESS) {
                    yield context.getString(R.string.reconnecting);
                } else {
                    yield context.getString(R.string.opening_adb);
                }
            }
            case UNAUTHORIZED -> context.getString(R.string.authorize_computer);
            case READY -> null;
            case DISCONNECTED -> context.getString(R.string.disconnected);
            case FAILED -> context.getString(R.string.connection_failed);
        };
    }

    static String transportLabel(TransportKind kind) {
        String raw = kind.name().toLowerCase();
        return raw.substring(0, 1).toUpperCase() + raw.substring(1);
    }

    static String automatedLabel(Context context, ProvisioningStage stage) {
        if (stage instanceof ProvisioningStage.Idle) {
            return context.getString(R.string.stage_idle);
        }
        if (stage instanceof ProvisioningStage.Authenticating) {
            return context.getString(R.string.stage_authenticating);
        }
        if (stage instanceof ProvisioningStage.Scanning) {
            return context.getString(R.string.stage_scanning);
        }
        if (stage instanceof ProvisioningStage.Downloading) {
            return context.getString(R.string.stage_downloading);
        }
        if (stage instanceof ProvisioningStage.Pushing) {
            return context.getString(R.string.stage_pushing);
        }
        if (stage instanceof ProvisioningStage.Installing) {
            return context.getString(R.string.stage_installing);
        }
        if (stage instanceof ProvisioningStage.SettingOwner) {
            return context.getString(R.string.stage_owner);
        }
        if (stage instanceof ProvisioningStage.Launching) {
            return context.getString(R.string.stage_launching);
        }
        if (stage instanceof ProvisioningStage.Retrying retrying) {
            return context.getString(R.string.stage_retrying, retrying.getReason());
        }
        if (stage instanceof ProvisioningStage.Succeeded succeeded) {
            return context.getString(R.string.stage_succeeded, succeeded.getComponent().getFlattened());
        }
        if (stage instanceof ProvisioningStage.Failed failed) {
            if (failed.getFactoryResetGuidance()) {
                if (failed.getReason().toLowerCase().contains("factory reset")) {
                    return failed.getReason();
                }
                return context.getString(R.string.stage_factory, failed.getReason());
            }
            return context.getString(R.string.stage_failed, failed.getReason());
        }
        if (stage instanceof ProvisioningStage.OwnerRemoved) {
            return context.getString(R.string.provision_removed);
        }
        return context.getString(R.string.stage_idle);
    }

    static boolean automatedWorking(ProvisioningStage stage) {
        return stage instanceof ProvisioningStage.Authenticating
            || stage instanceof ProvisioningStage.Scanning
            || stage instanceof ProvisioningStage.Downloading
            || stage instanceof ProvisioningStage.Pushing
            || stage instanceof ProvisioningStage.Installing
            || stage instanceof ProvisioningStage.SettingOwner
            || stage instanceof ProvisioningStage.Launching
            || stage instanceof ProvisioningStage.Retrying;
    }

    static String ownerRemovedBody(Context context, DpcComponent component) {
        return context.getString(R.string.provision_removed_body, component.getFlattened());
    }
}
