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
import org.sputnikdev.bluetooth.DataConversionUtils;
import org.sputnikdev.bluetooth.URL;
import org.sputnikdev.bluetooth.manager.BluetoothAddressType;
import org.sputnikdev.bluetooth.manager.transport.Device;
import org.sputnikdev.bluetooth.manager.transport.Notification;
import org.sputnikdev.bluetooth.manager.transport.Service;
import tinyb.BluetoothDevice;
import tinyb.BluetoothGattService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A class representing TinyB devices.
 * @author Vlad Kolotov
 */
class TinyBDevice implements Device {

    private static final Logger LOGGER = LoggerFactory.getLogger(TinyBDevice.class);

    private final URL url;
    private final BluetoothDevice device;

    TinyBDevice(URL url, BluetoothDevice device) {
        this.url = url;
        this.device = device;
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public int getBluetoothClass() {
        LOGGER.debug("Getting bluetooth class: {}", url);
        return device.getBluetoothClass();
    }

    @Override
    public boolean disconnect() {
        LOGGER.debug("Disconnecting: {}", url);
        return device.disconnect();
    }

    @Override
    public boolean connect() {
        LOGGER.debug("Connecting: {}", url);
        return device.connect();
    }

    @Override
    public String getName() {
        LOGGER.debug("Getting name: {}", url);
        return device.getName();
    }

    @Override
    public String getAlias() {
        LOGGER.debug("Getting alias: {}", url);
        return device.getAlias();
    }

    @Override
    public void setAlias(String alias) {
        LOGGER.debug("Setting alias: {} / {}", url, alias);
        device.setAlias(alias);
    }

    @Override
    public boolean isBlocked() {
        LOGGER.debug("Getting blocked: {}", url);
        return device.getBlocked();
    }

    @Override
    public boolean isBleEnabled() {
        //TODO get proper state of manufacturer advertisement
        return getBluetoothClass() == 0;
    }

    @Override
    public void enableBlockedNotifications(Notification<Boolean> notification) {
        LOGGER.debug("Enable blocked notifications: {}", url);
        device.enableBlockedNotifications(value -> {
            TinyBFactory.notifySafely(() -> {
                notification.notify(value);
            }, LOGGER, "Blocked notification execution error");
        });
    }

    @Override
    public void disableBlockedNotifications() {
        LOGGER.debug("Disable blocked notifications: {}", url);
        device.disableBlockedNotifications();
    }

    @Override
    public void setBlocked(boolean blocked) {
        LOGGER.debug("Setting blocked: {} : {}", url, blocked);
        device.setBlocked(blocked);
    }

    @Override
    public short getRSSI() {
        LOGGER.debug("Getting RSSI: {}", url);
        return device.getRSSI();
    }

    @Override
    public short getTxPower() {
        LOGGER.debug("Getting TxPower: {}", url);
        return device.getTxPower();
    }

    @Override
    public void enableRSSINotifications(Notification<Short> notification) {
        LOGGER.debug("Enable RSSI notifications: {}", url);
        device.enableRSSINotifications(value -> {
            TinyBFactory.notifySafely(() -> {
                notification.notify(value);
            }, LOGGER, "RSSI notification execution error");
        });
    }

    @Override
    public void disableRSSINotifications() {
        LOGGER.debug("Disable RSSI notifications: {}", url);
        device.disableRSSINotifications();
    }

    @Override
    public boolean isConnected() {
        LOGGER.debug("Checking if device connected: {}", url);
        return device.getConnected();
    }

    @Override
    public void enableConnectedNotifications(Notification<Boolean> notification) {
        LOGGER.debug("Enable connected notifications: {}", url);
        device.enableConnectedNotifications(value -> {
            TinyBFactory.notifySafely(() -> {
                notification.notify(value);
            }, LOGGER, "Connected notification execution error");
        });
    }

    @Override
    public void disableConnectedNotifications() {
        LOGGER.debug("Disable connected notifications: {}", url);
        device.disableConnectedNotifications();
    }

    @Override
    public boolean isServicesResolved() {
        LOGGER.debug("Is services resolved?: {}", url);
        return device.getServicesResolved();
    }

    @Override
    public void enableServicesResolvedNotifications(Notification<Boolean> notification) {
        LOGGER.debug("Enable service resolved notifications: {}", url);
        device.enableServicesResolvedNotifications(value -> {
            TinyBFactory.notifySafely(() -> {
                notification.notify(value);
            }, LOGGER, "Services resolved notification execution error");
        });
    }

    @Override
    public void disableServicesResolvedNotifications() {
        LOGGER.debug("Disable service resolved notifications: {}", url);
        device.disableServicesResolvedNotifications();
    }

    @Override
    public List<Service> getServices() {
        LOGGER.debug("Getting resolved services: {}", url);
        if (!device.getConnected()) {
            return Collections.emptyList();
        }
        List<BluetoothGattService> services = device.getServices();
        List<Service> result = new ArrayList<>(services.size());
        for (BluetoothGattService nativeService : services) {
            result.add(new TinyBService(url.copyWithService(nativeService.getUUID()), nativeService));
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public Map<String, byte[]> getServiceData() {
        LOGGER.debug("Getting service data: {}", url);
        return device.getServiceData();
    }

    @Override
    public Map<Short, byte[]> getManufacturerData() {
        LOGGER.debug("Getting manufacturer data: {}", url);
        return device.getManufacturerData();
    }

    @Override
    public BluetoothAddressType getAddressType() {
        //TODO it is not yet implemented in TinyB, but quite possible to implement.
        return BluetoothAddressType.UNKNOWN;
    }

    @Override
    public void enableServiceDataNotifications(Notification<Map<String, byte[]>> notification) {
        LOGGER.debug("Enable service data notifications: {}", url);
        device.enableServiceDataNotifications(value -> {
            TinyBFactory.notifySafely(() -> {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Service data changed: {} : {}", url, value.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey,
                                    entry -> DataConversionUtils.convert(entry.getValue(), 16))));
                }
                notification.notify(value);
            }, LOGGER, "Service data notification execution error");
        });
    }

    @Override
    public void disableServiceDataNotifications() {
        LOGGER.debug("Disable service data notifications: {}", url);
        device.disableServiceDataNotifications();
    }

    @Override
    public void enableManufacturerDataNotifications(Notification<Map<Short, byte[]>> notification) {
        LOGGER.debug("Enable manufacturer data notifications: {}", url);
        device.enableManufacturerDataNotifications(value -> {
            TinyBFactory.notifySafely(() -> {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Manufacturer data changed: {} : {}", url, value.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey,
                                    entry -> DataConversionUtils.convert(entry.getValue(), 16))));
                }
                notification.notify(value);
            }, LOGGER, "Manufacturer data notification execution error");
        });
    }

    @Override
    public void disableManufacturerDataNotifications() {
        LOGGER.debug("Disable manufacturer data notifications: {}", url);
        device.disableManufacturerDataNotifications();
    }

    protected static void dispose(BluetoothDevice device) {
        LOGGER.debug("Disposing device: {}", device.getAddress());
        TinyBFactory.runSilently(device::disconnect);
        TinyBFactory.runSilently(device::disableBlockedNotifications);
        TinyBFactory.runSilently(device::disableConnectedNotifications);
        TinyBFactory.runSilently(device::disableRSSINotifications);
        TinyBFactory.runSilently(device::disableServicesResolvedNotifications);
        TinyBFactory.runSilently(device::disableManufacturerDataNotifications);
        TinyBFactory.runSilently(device::disablePairedNotifications);
        TinyBFactory.runSilently(device::disableServiceDataNotifications);
        TinyBFactory.runSilently(device::disableTrustedNotifications);
        TinyBFactory.runSilently(device::remove);
    }

}
