package org.sputnikdev.bluetooth.manager.impl;

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
import tinyb.BluetoothDevice;
import tinyb.BluetoothException;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothGattService;
import tinyb.BluetoothNotification;

/**
 *
 * @author Vlad Kolotov
 */
class TinyBCharacteristic implements Characteristic {

    private final BluetoothGattCharacteristic characteristic;

    TinyBCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
    }

    @Override
    public URL getURL() {
        BluetoothGattService service = characteristic.getService();
        BluetoothDevice device = service.getDevice();
        return new URL(TinyBFactory.TINYB_PROTOCOL_NAME, device.getAdapter().getAddress(), device.getAddress(),
                service.getUUID(), characteristic.getUUID(), null);
    }

    @Override
    public String[] getFlags() {
        return characteristic.getFlags();
    }

    @Override
    public boolean isNotifying() {
        return characteristic.getNotifying();
    }

    @Override
    public byte[] readValue() throws BluetoothException {
        return characteristic.readValue();
    }

    @Override
    public void
    enableValueNotifications(Notification<byte[]> notification) {
        characteristic.enableValueNotifications(new BluetoothNotification<byte[]>() {
            @Override public void run(byte[] bytes) {
                notification.notify(bytes);
            }
        });
    }

    @Override
    public void disableValueNotifications() {
        characteristic.disableValueNotifications();
    }

    @Override
    public boolean writeValue(byte[] bytes) throws BluetoothException {
        return characteristic.writeValue(bytes);
    }
}
