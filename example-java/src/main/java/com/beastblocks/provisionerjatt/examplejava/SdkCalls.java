package com.beastblocks.provisionerjatt.examplejava;

import com.beastblocks.provisionerjattsdk.ProvisionerClient;
import com.beastblocks.provisionerjattsdk.domain.DeviceTarget;
import com.beastblocks.provisionerjattsdk.domain.DpcComponent;
import java.lang.reflect.Method;

/**
 * Kotlin {@code value class DeviceId} is inlined in the AAR, so Java must call
 * the generated method names rather than {@code getId()} / {@code scan(DeviceId)}.
 */
final class SdkCalls {
    private SdkCalls() {}

    static String id(DeviceTarget target) {
        return (String) call(target, "getId-ZvI0oWU");
    }

    static void scan(ProvisionerClient client, String deviceId) {
        call(client, "scan-DbfY-d0", deviceId);
    }

    static void makeDeviceOwner(ProvisionerClient client, String deviceId, DpcComponent component) {
        call(client, "makeDeviceOwner-8P2UQds", deviceId, component);
    }

    static void removeOwner(ProvisionerClient client, String deviceId, DpcComponent component) {
        call(client, "removeOwner-8P2UQds", deviceId, component);
    }

    static void retryProvisioning(ProvisionerClient client, String deviceId) {
        call(client, "retryProvisioning-DbfY-d0", deviceId);
    }

    static void disconnectWireless(ProvisionerClient client, String deviceId) {
        call(client, "disconnectWireless-DbfY-d0", deviceId);
    }

    private static Object call(Object target, String name, Object... args) {
        try {
            for (Method method : target.getClass().getMethods()) {
                if (method.getName().equals(name) && method.getParameterCount() == args.length) {
                    return method.invoke(target, args);
                }
            }
            throw new IllegalStateException("Missing SDK method " + name);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException(error);
        }
    }
}
