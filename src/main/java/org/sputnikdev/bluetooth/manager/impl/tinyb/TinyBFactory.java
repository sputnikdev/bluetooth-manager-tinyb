package org.sputnikdev.bluetooth.manager.impl.tinyb;

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

import java.util.List;
import java.util.stream.Collectors;

import org.sputnikdev.bluetooth.URL;
import org.sputnikdev.bluetooth.manager.impl.Adapter;
import org.sputnikdev.bluetooth.manager.impl.BluetoothObjectFactory;
import org.sputnikdev.bluetooth.manager.impl.Characteristic;
import org.sputnikdev.bluetooth.manager.impl.Device;
import tinyb.BluetoothAdapter;
import tinyb.BluetoothDevice;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothGattService;
import tinyb.BluetoothManager;
import tinyb.BluetoothType;

/**
 *
 * @author Vlad Kolotov
 */
public class TinyBFactory implements BluetoothObjectFactory {

    public static final String TINYB_PROTOCOL_NAME = "tinyb";

    public TinyBFactory() { }

    @Override
    public Adapter getAdapter(URL url) {
        BluetoothAdapter adapter = (BluetoothAdapter) BluetoothManager.getBluetoothManager().getObject(
                BluetoothType.ADAPTER, null, url.getAdapterAddress(), null);
        return adapter != null ? new TinyBAdapter(adapter) : null;
    }

    @Override
    public Device getDevice(URL url) {
        BluetoothAdapter adapter = (BluetoothAdapter) BluetoothManager.getBluetoothManager().getObject(
                BluetoothType.ADAPTER, null, url.getAdapterAddress(), null);
        if (adapter == null) {
            return null;
        }
        BluetoothDevice device = (BluetoothDevice) BluetoothManager.getBluetoothManager().getObject(
                BluetoothType.DEVICE, null, url.getDeviceAddress(), adapter);
        return device != null ? new TinyBDevice(device) : null;
    }

    @Override
    public Characteristic getCharacteristic(URL url) {
        BluetoothAdapter adapter = (BluetoothAdapter) BluetoothManager.getBluetoothManager().getObject(
                BluetoothType.ADAPTER, null, url.getAdapterAddress(), null);
        if (adapter == null) {
            return null;
        }
        BluetoothDevice device = (BluetoothDevice) BluetoothManager.getBluetoothManager().getObject(
                BluetoothType.DEVICE, null, url.getDeviceAddress(), adapter);
        if (device == null) {
            return null;
        }
        BluetoothGattService service = (BluetoothGattService) BluetoothManager.getBluetoothManager().getObject(
                        BluetoothType.GATT_SERVICE, null, url.getServiceUUID(), device);
        if (service == null) {
            return null;
        }
        BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic)
                BluetoothManager.getBluetoothManager().getObject(
                        BluetoothType.GATT_CHARACTERISTIC, null, url.getCharacteristicUUID(), service);
        return characteristic != null ? new TinyBCharacteristic(characteristic) : null;
    }

    @Override
    public List<Adapter> getDiscoveredAdapters() {
        return BluetoothManager.getBluetoothManager().getAdapters().stream().map(
                TinyBAdapter::new).collect(Collectors.toList());
    }

    @Override
    public List<Device> getDiscoveredDevices() {
        return BluetoothManager.getBluetoothManager().getDevices().stream().map(
                TinyBDevice::new).collect(Collectors.toList());
    }

    @Override
    public String getProtocolName() {
        return TINYB_PROTOCOL_NAME;
    }
}
