package com.beastblocks.provisionerjatt.examplejava;

import android.graphics.Outline;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import com.beastblocks.provisionerjattsdk.ProvisionerClient;
import com.beastblocks.provisionerjattsdk.ProvisionerJatt;
import com.beastblocks.provisionerjattsdk.domain.ConnectionState;
import com.beastblocks.provisionerjattsdk.domain.DpcComponent;
import com.beastblocks.provisionerjattsdk.ui.DeviceUiState;
import com.beastblocks.provisionerjattsdk.ui.ProvisionerUiState;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ComponentActivity implements DeviceCards.Listener {
    private enum Tab { CONNECTED, DISCOVERED }

    private ProvisionerClient client;
    private Tab selectedTab = Tab.CONNECTED;
    private boolean wideLayout;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private final Runnable pump = new Runnable() {
        @Override
        public void run() {
            if (client != null) {
                render(client.getState().getValue());
            }
            ui.postDelayed(this, 250);
        }
    };

    private TextView tabDiscovered;
    private TextView tabConnected;
    private View serialBanner;
    private TextView serialBannerTitle;
    private View emptyState;
    private ProgressBar emptyProgress;
    private TextView emptyTitle;
    private TextView emptyBody;
    private LinearLayout deviceList;
    private TextView titleDiscovered;
    private TextView titleConnected;
    private View emptyDiscovered;
    private View emptyConnected;
    private ProgressBar emptyDiscoveredProgress;
    private ProgressBar emptyConnectedProgress;
    private TextView emptyDiscoveredTitle;
    private LinearLayout listDiscovered;
    private LinearLayout listConnected;
    private View listDiscoveredScroll;
    private View listConnectedScroll;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        client = ProvisionerJatt.get();
        client.attach(this, this, null);
        setContentView(R.layout.activity_main);
        bindViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ui.post(pump);
    }

    @Override
    protected void onStop() {
        ui.removeCallbacks(pump);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (client != null) {
            client.detach(this);
        }
        super.onDestroy();
    }

    private void bindViews() {
        clipCircle(findViewById(R.id.toolbar_logo));
        findViewById(R.id.btn_automate).setOnClickListener(v -> openAutomation());
        serialBanner = findViewById(R.id.serial_banner);
        serialBannerTitle = findViewById(R.id.serial_banner_title);

        tabDiscovered = findViewById(R.id.tab_discovered);
        tabConnected = findViewById(R.id.tab_connected);
        emptyState = findViewById(R.id.empty_state);
        emptyProgress = findViewById(R.id.empty_progress);
        emptyTitle = findViewById(R.id.empty_title);
        emptyBody = findViewById(R.id.empty_body);
        deviceList = findViewById(R.id.device_list);
        wideLayout = deviceList == null;

        if (wideLayout) {
            titleDiscovered = findViewById(R.id.title_discovered);
            titleConnected = findViewById(R.id.title_connected);
            emptyDiscovered = findViewById(R.id.empty_discovered);
            emptyConnected = findViewById(R.id.empty_connected);
            emptyDiscoveredProgress = findViewById(R.id.empty_discovered_progress);
            emptyConnectedProgress = findViewById(R.id.empty_connected_progress);
            emptyDiscoveredTitle = findViewById(R.id.empty_discovered_title);
            listDiscovered = findViewById(R.id.list_discovered);
            listConnected = findViewById(R.id.list_connected);
            listDiscoveredScroll = findViewById(R.id.list_discovered_scroll);
            listConnectedScroll = findViewById(R.id.list_connected_scroll);
        } else {
            tabDiscovered.setOnClickListener(v -> {
                selectedTab = Tab.DISCOVERED;
                if (client != null) render(client.getState().getValue());
            });
            tabConnected.setOnClickListener(v -> {
                selectedTab = Tab.CONNECTED;
                if (client != null) render(client.getState().getValue());
            });
        }
    }

    private void openAutomation() {
        if (client == null) return;
        AutomationSheet.show(this, client.getState().getValue().getSettings(), new AutomationSheet.Callbacks() {
            @Override
            public boolean onSaveAutomation(String packageName, String apkUrl) {
                if (packageName.isBlank() && apkUrl.isBlank()) return true;
                return client.setAutomation(packageName, apkUrl);
            }

            @Override
            public boolean onSetSerial(String serial) {
                if (serial.isBlank()) return client.clearSerial();
                return client.setSerial(serial);
            }

            @Override
            public boolean onClearAutomation() {
                return client.clearAutomation();
            }

            @Override
            public boolean onClearSerial() {
                return client.clearSerial();
            }
        });
    }

    private void render(ProvisionerUiState state) {
        if (state == null) return;
        List<DeviceUiState> discovered = new ArrayList<>();
        List<DeviceUiState> connected = new ArrayList<>();
        for (DeviceUiState device : state.getDevices()) {
            if (device.getConnection() == ConnectionState.READY) connected.add(device);
            else discovered.add(device);
        }
        boolean hasSerial = state.getSettings().getHasSerialFilter();
        serialBanner.setVisibility(hasSerial ? View.VISIBLE : View.GONE);
        if (hasSerial) {
            serialBannerTitle.setText(getString(R.string.serial_lock_title, state.getSettings().getSerialNumber()));
        }

        boolean automation = state.getSettings().getAutomationConfigured();
        if (wideLayout) {
            titleDiscovered.setText(getString(R.string.pane_discovered, discovered.size()));
            titleConnected.setText(getString(R.string.pane_connected, connected.size()));
            bindPane(discovered, state.getScanning(), automation, listDiscovered, listDiscoveredScroll, emptyDiscovered, emptyDiscoveredProgress, emptyDiscoveredTitle, true);
            bindPane(connected, state.getScanning(), automation, listConnected, listConnectedScroll, emptyConnected, emptyConnectedProgress, findViewById(R.id.empty_connected_title), false);
            return;
        }

        tabDiscovered.setText(getString(R.string.tab_discovered, discovered.size()));
        tabConnected.setText(getString(R.string.tab_connected, connected.size()));
        boolean discoveredSelected = selectedTab == Tab.DISCOVERED;
        tabDiscovered.setBackgroundResource(discoveredSelected ? R.drawable.bg_tab_selected : 0);
        tabConnected.setBackgroundResource(discoveredSelected ? 0 : R.drawable.bg_tab_selected);
        tabDiscovered.setTextColor(getColor(discoveredSelected ? R.color.brand_orange : R.color.text_secondary));
        tabConnected.setTextColor(getColor(discoveredSelected ? R.color.text_secondary : R.color.brand_orange));

        List<DeviceUiState> visible = discoveredSelected ? discovered : connected;
        emptyState.setTag(discoveredSelected ? "devices_empty" : "connected_empty");
        if (visible.isEmpty()) {
            deviceList.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            emptyProgress.setVisibility(state.getScanning() ? View.VISIBLE : View.GONE);
            if (discoveredSelected) {
                emptyTitle.setText(state.getScanning() ? R.string.empty_discovered_scanning : R.string.empty_discovered_title);
                emptyBody.setText(R.string.empty_discovered_body);
            } else {
                emptyTitle.setText(R.string.empty_connected_title);
                emptyBody.setText(R.string.empty_connected_body);
            }
        } else {
            emptyState.setVisibility(View.GONE);
            deviceList.setVisibility(View.VISIBLE);
            DeviceCards.bind(deviceList, visible, automation, this);
        }
    }

    private void bindPane(
        List<DeviceUiState> devices,
        boolean scanning,
        boolean automationConfigured,
        LinearLayout list,
        View listScroll,
        View empty,
        ProgressBar progress,
        TextView emptyTitle,
        boolean discovered
    ) {
        if (devices.isEmpty()) {
            listScroll.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
            progress.setVisibility(scanning ? View.VISIBLE : View.GONE);
            if (discovered) {
                emptyTitle.setText(scanning ? R.string.empty_discovered_scanning : R.string.empty_discovered_title);
            }
        } else {
            empty.setVisibility(View.GONE);
            listScroll.setVisibility(View.VISIBLE);
            DeviceCards.bind(list, devices, automationConfigured, this);
        }
    }

    private static void clipCircle(ImageView view) {
        view.setClipToOutline(true);
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline outline) {
                outline.setOval(0, 0, v.getWidth(), v.getHeight());
            }
        });
        view.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> v.invalidateOutline());
    }

    @Override
    public void onScan(String id) {
        SdkCalls.scan(client, id);
    }

    @Override
    public void onMakeOwner(String id, DpcComponent component) {
        SdkCalls.makeDeviceOwner(client, id, component);
    }

    @Override
    public void onRemoveOwner(String id, DpcComponent component) {
        SdkCalls.removeOwner(client, id, component);
    }

    @Override
    public void onRetry(String id) {
        SdkCalls.retryProvisioning(client, id);
    }

    @Override
    public void onDisconnect(String id) {
        SdkCalls.disconnectWireless(client, id);
    }
}
