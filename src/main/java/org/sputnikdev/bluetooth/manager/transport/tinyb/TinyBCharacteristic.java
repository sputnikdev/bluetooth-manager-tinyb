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
import org.sputnikdev.bluetooth.manager.transport.Characteristic;
import org.sputnikdev.bluetooth.manager.transport.CharacteristicAccessType;
import org.sputnikdev.bluetooth.manager.transport.Notification;
import tinyb.BluetoothDevice;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothGattService;
import tinyb.BluetoothNotification;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.sputnikdev.bluetooth.manager.transport.CharacteristicAccessType.*;

/**
 *
 * @author Vlad Kolotov
 */
class TinyBCharacteristic implements Characteristic {

    private enum AccessTypeMapping {
        broadcast(BROADCAST),
        read(READ),
        write_without_response(WRITE_WITHOUT_RESPONSE),
        write(WRITE),
        notify(NOTIFY),
        indicate(INDICATE),
        authenticated_signed_writes(AUTHENTICATED_SIGNED_WRITES),

        reliable_write(null),
        writable_auxiliaries(null),
        encrypt_read(null),
        encrypt_write(null),
        encrypt_authenticated(null),
        encrypt_authenticated_write(null),
        secure_read(null),
        secure_write(null);

        private CharacteristicAccessType accessType;

        AccessTypeMapping(CharacteristicAccessType accessType) {
            this.accessType = accessType;
        }
    }

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
    public Set<CharacteristicAccessType> getFlags() {
        // the very first properties are known:
        // https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.attribute.gatt.characteristic_declaration.xml
        // the rest them to be clarified
        //TODO find out what are they, extended properties (CharacteristicAccessType.EXTENDED_PROPERTIES)?
        /*
        "broadcast"
        "read"
        "write-without-response"
        "write"
        "notify"
        "indicate"
        "authenticated-signed-writes"

        "reliable-write"
        "writable-auxiliaries"
        "encrypt-read"
        "encrypt-write"
        "encrypt-authenticated-read"
        "encrypt-authenticated-write"
        "secure-read" (Server only)
        "secure-write" (Server only)
         */
        String[] flags = characteristic.getFlags();
        return Stream.of(flags)
                .filter(Objects::nonNull)
                .map(f -> AccessTypeMapping.valueOf(f.toLowerCase().replaceAll("-", "_")).accessType)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isNotifying() {
        return characteristic.getNotifying();
    }

    @Override
    public byte[] readValue() {
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
    public boolean writeValue(byte[] bytes) {
        return characteristic.writeValue(bytes);
    }

    @Override
    public void dispose() { }
}
