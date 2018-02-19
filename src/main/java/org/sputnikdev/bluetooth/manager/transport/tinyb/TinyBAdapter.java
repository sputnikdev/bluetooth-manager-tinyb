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
        adapter.setAlias(alias);
    }

    @Override
    public boolean isPowered() {
        return adapter.getPowered();
    }

    @Override
    public void enablePoweredNotifications(Notification<Boolean> notification) {
        adapter.enablePoweredNotifications(powered -> {
            TinyBFactory.notifySafely(() -> {
                notification.notify(powered);
            }, LOGGER, "Powered notification execution error");
        });
    }

    @Override
    public void disablePoweredNotifications() {
        adapter.disablePoweredNotifications();
    }

    @Override
    public void setPowered(boolean powered) {
        adapter.setPowered(powered);
    }

    @Override
    public boolean isDiscovering() {
        return adapter.getDiscovering();
    }

    @Override
    public void enableDiscoveringNotifications(Notification<Boolean> notification) {
        adapter.enableDiscoveringNotifications(value -> {
            TinyBFactory.notifySafely(() -> {
                notification.notify(value);
            }, LOGGER, "Discovering notification execution error");
        });
    }

    @Override
    public void disableDiscoveringNotifications() {
        adapter.disableDiscoveringNotifications();
    }

    @Override
    public boolean startDiscovery() {
        adapter.setRssiDiscoveryFilter(-100);
        return adapter.startDiscovery();
    }

    @Override
    public boolean stopDiscovery() {
        return adapter.stopDiscovery();
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
