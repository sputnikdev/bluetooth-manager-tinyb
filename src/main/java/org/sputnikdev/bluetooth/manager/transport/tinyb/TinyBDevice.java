package org.sputnikdev.bluetooth.manager.transport.tinyb;

/*-
 * #%L
 * org.sputnikdev:bluetooth-manager
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

import org.sputnikdev.bluetooth.URL;
import org.sputnikdev.bluetooth.manager.transport.Notification;
import org.sputnikdev.bluetooth.manager.transport.Device;
import org.sputnikdev.bluetooth.manager.transport.Service;
import tinyb.BluetoothDevice;
import tinyb.BluetoothGattService;
import tinyb.BluetoothNotification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Vlad Kolotov
 */
class TinyBDevice implements Device {

    private final BluetoothDevice device;

    TinyBDevice(BluetoothDevice device) {
        this.device = device;
    }

    @Override
    public URL getURL() {
        return new URL(TinyBFactory.TINYB_PROTOCOL_NAME, device.getAdapter().getAddress(), device.getAddress());
    }

    @Override
    public int getBluetoothClass() {
        return device.getBluetoothClass();
    }

    @Override
    public boolean disconnect() {
        return device.disconnect();
    }

    @Override
    public boolean connect() {
        return device.connect();
    }

    @Override
    public String getName() {
        return device.getName();
    }

    @Override
    public String getAlias() {
        return device.getAlias();
    }

    @Override
    public void setAlias(String alias) {
        device.setAlias(alias);
    }

    @Override
    public boolean isBlocked() {
        return device.getBlocked();
    }

    @Override
    public boolean isBleEnabled() {
        //TODO get proper state of manufacturer advertisement
        return getBluetoothClass() == 0;
    }

    @Override
    public void enableBlockedNotifications(Notification<Boolean> notification) {
        device.enableBlockedNotifications(new BluetoothNotification<Boolean>() {
            @Override public void run(Boolean value) {
                notification.notify(value);
            }
        });
    }

    @Override
    public void disableBlockedNotifications() {
        device.disableBlockedNotifications();
    }

    @Override
    public void setBlocked(boolean blocked) {
        device.setBlocked(blocked);
    }

    @Override
    public short getRSSI() {
        return device.getRSSI();
    }

    @Override
    public void enableRSSINotifications(Notification<Short> notification) {
        device.enableRSSINotifications(new BluetoothNotification<Short>() {
            @Override public void run(Short value) {
                notification.notify(value);
            }
        });
    }

    @Override
    public void disableRSSINotifications() {
        device.disableRSSINotifications();
    }

    @Override
    public boolean isConnected() {
        return device.getConnected();
    }

    @Override
    public void enableConnectedNotifications(Notification<Boolean> notification) {
        device.enableConnectedNotifications(new BluetoothNotification<Boolean>() {
            @Override public void run(Boolean value) {
                notification.notify(value);
            }
        });
    }

    @Override
    public void disableConnectedNotifications() {
        device.disableConnectedNotifications();
    }

    @Override
    public boolean isServicesResolved() {
        return device.getServicesResolved();
    }

    @Override
    public void enableServicesResolvedNotifications(Notification<Boolean> notification) {
        device.enableServicesResolvedNotifications(new BluetoothNotification<Boolean>() {
            @Override public void run(Boolean value) {
                notification.notify(value);
            }
        });
    }

    @Override
    public void disableServicesResolvedNotifications() {
        device.disableServicesResolvedNotifications();
    }

    @Override
    public List<Service> getServices() {
        List<BluetoothGattService> services = device.getServices();
        List<Service> result = new ArrayList<>(services.size());
        for (BluetoothGattService nativeService : services) {
            result.add(new TinyBService(nativeService));
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public void dispose() { }
}
