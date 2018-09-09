package org.sputnikdev.bluetooth.manager.transport.tinyb;

/*-
 * #%L
 * org.sputnikdev:bluetooth-manager-tinyb
 * %%
 * Copyright (C) 2017 Sputnik Dev
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sputnikdev.bluetooth.URL;
import org.sputnikdev.bluetooth.manager.transport.Adapter;
import org.sputnikdev.bluetooth.manager.transport.Device;
import org.sputnikdev.bluetooth.manager.transport.Notification;
import tinyb.BluetoothAdapter;
import tinyb.BluetoothDevice;
import tinyb.BluetoothException;
import tinyb.TransportType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class representing TinyB adapters.
 * @author Vlad Kolotov
 */
class TinyBAdapter implements Adapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TinyBAdapter.class);

    private final URL url;
    private final BluetoothAdapter adapter;

    TinyBAdapter(URL url, BluetoothAdapter adapter) {
        this.url = url;
        this.adapter = adapter;
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public String getAlias() {
        return adapter.getAlias();
    }

    @Override
    public String getName() {
        return adapter.getName();
    }

    @Override
    public void setAlias(String alias) {
        LOGGER.debug("Set alias: {} : {}", url, alias);
        adapter.setAlias(alias);
    }

    @Override
    public boolean isPowered() {
        return adapter.getPowered();
    }

    @Override
    public void enablePoweredNotifications(Notification<Boolean> notification) {
        LOGGER.debug("Enable powered notifications: {}", url);
        adapter.enablePoweredNotifications(powered -> {
            TinyBFactory.notifySafely(() -> {
                notification.notify(powered);
            }, LOGGER, "Powered notification execution error");
        });
    }

    @Override
    public void disablePoweredNotifications() {
        LOGGER.debug("Disable powered notifications: {}", url);
        adapter.disablePoweredNotifications();
    }

    @Override
    public void setPowered(boolean powered) {
        LOGGER.debug("Set powered: {} : {}", url, powered);
        adapter.setPowered(powered);
    }

    @Override
    public boolean isDiscovering() {
        return adapter.getDiscovering();
    }

    @Override
    public void enableDiscoveringNotifications(Notification<Boolean> notification) {
        LOGGER.debug("Enable discovering notifications: {}", url);
        adapter.enableDiscoveringNotifications(value -> {
            TinyBFactory.notifySafely(() -> {
                notification.notify(value);
            }, LOGGER, "Discovering notification execution error");
        });
    }

    @Override
    public void disableDiscoveringNotifications() {
        LOGGER.debug("Disable discovering notifications: {}", url);
        adapter.disableDiscoveringNotifications();
    }

    @Override
    public boolean startDiscovery() {
        LOGGER.debug("Starting discovery: {}", url);
        // NOTE: there is a bug in Bluez: https://www.spinics.net/lists/linux-bluetooth/msg67229.html
        // which causes "GDBus.Error:org.bluez.Error.Failed: Software caused connection abort"
        // Bluez debug log:
        // src/device.c:att_connect_cb() connect error: Function not implemented (38)
        // TinyB transport debug log:
        // Software caused connection abort (103)
        // it is provoked by setting any discovery filter
        // however, if filter is not set (or reset), then not all devices are getting discovered
        // so it is a trade off between having all devices discovered and stable connection establishing
        //adapter.setRssiDiscoveryFilter(-100);
        try {
            adapter.setDiscoveryFilter(Collections.emptyList(), 0, 0, TransportType.AUTO);
        } catch (Exception ex) {
            // some adapters are reported not to support this, hence ignore and log it
            // GDBus.Error:org.bluez.Error.NotSupported
            LOGGER.warn("Adapter does not support filtering: {}. Reason: {}.", url, ex.getMessage());
        }
        return adapter.startDiscovery();
    }

    @Override
    public boolean stopDiscovery() {
        LOGGER.debug("Stopping discovery: {}", url);
        try {
            return adapter.stopDiscovery();
        } catch (BluetoothException ex) {
            if (adapter.getDiscovering()
                    && "GDBus.Error:org.bluez.Error.Failed: No discovery started".equals(ex.getMessage())) {
                // workaround for a Bluez bug
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Device> getDevices() {
        List<BluetoothDevice> devices = adapter.getDevices();
        List<Device> result = new ArrayList<>(devices.size());
        for (BluetoothDevice device : devices) {
            if (device.getRSSI() != 0) {
                result.add(new TinyBDevice(url.copyWithDevice(device.getAddress()), device));
            }
        }
        return Collections.unmodifiableList(result);
    }

    protected static void dispose(BluetoothAdapter adapter) {
        LOGGER.debug("Disposing adapter: {}", adapter.getAddress());
        TinyBFactory.runSilently(adapter::stopDiscovery);
        adapter.getDevices().forEach(TinyBDevice::dispose);
        TinyBFactory.runSilently(adapter::disableDiscoveringNotifications);
        TinyBFactory.runSilently(adapter::disablePoweredNotifications);
        TinyBFactory.runSilently(adapter::disableDiscoverableNotifications);
        TinyBFactory.runSilently(adapter::disablePairableNotifications);
    }

}
