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
import org.sputnikdev.bluetooth.manager.DiscoveredAdapter;
import org.sputnikdev.bluetooth.manager.DiscoveredDevice;
import org.sputnikdev.bluetooth.manager.transport.Adapter;
import org.sputnikdev.bluetooth.manager.transport.BluetoothObjectFactory;
import org.sputnikdev.bluetooth.manager.transport.Characteristic;
import org.sputnikdev.bluetooth.manager.transport.Device;
import tinyb.BluetoothAdapter;
import tinyb.BluetoothDevice;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothGattService;
import tinyb.BluetoothManager;
import tinyb.BluetoothType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * A Bluetooth Manager Transport abstraction layer implementation based on TinyB library.
 * @author Vlad Kolotov
 */
public class TinyBFactory implements BluetoothObjectFactory {

    public static final String TINYB_PROTOCOL_NAME = "tinyb";

    private static final Logger LOGGER = LoggerFactory.getLogger(TinyBFactory.class);

    private static final ExecutorService NOTIFICATION_SERVICE = Executors.newCachedThreadPool();

    /**
     * Loads TinyB native libraries from classpath by copying them to a temp folder.
     * @return true if all libraries succesefully loaded, false otherwise
     */
    public static boolean loadNativeLibraries() {
        if (NativesLoader.isSupportedEnvironment()) {
            try {
                System.load(NativesLoader.prepare("libtinyb.so")); // $COVERAGE-IGNORE$
                System.load(NativesLoader.prepare("libjavatinyb.so")); // $COVERAGE-IGNORE$
                return true; // $COVERAGE-IGNORE$
            } catch (Throwable e) {
                LOGGER.info("Could not load TinyB native libraries.", e);
                return false;
            }
        }
        LOGGER.info("TinyB: environemnt is not supported. Only Linux OS; x86, x86_64 and arm6 architectures; "
            + "are supported.");
        return false;
    }

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
        if (device == null || !device.getConnected()) {
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
    public List<DiscoveredAdapter> getDiscoveredAdapters() {
        return BluetoothManager.getBluetoothManager().getAdapters().stream().map(
                TinyBFactory::convert).collect(Collectors.toList());
    }

    @Override
    public List<DiscoveredDevice> getDiscoveredDevices() {
        return BluetoothManager.getBluetoothManager().getDevices().stream().map(
                TinyBFactory::convert).collect(Collectors.toList());
    }

    @Override
    public String getProtocolName() {
        return TINYB_PROTOCOL_NAME;
    }

    @Override
    public void configure(Map<String, Object> config) { /* do nothing for now */ }

    /**
     * Disposing TinyB factory by closing/disposing all adapters, devices and services.
     */
    public void dispose() {
        try {
            BluetoothManager.getBluetoothManager().stopDiscovery();
        } catch (Exception ignore) { }
        BluetoothManager.getBluetoothManager().getServices().forEach(TinyBFactory::closeSilently);
        BluetoothManager.getBluetoothManager().getDevices().forEach(TinyBFactory::closeSilently);
        BluetoothManager.getBluetoothManager().getAdapters().forEach(TinyBFactory::closeSilently);
    }

    static void notifySafely(Runnable noticator, Logger logger, String errorMessage) {
        getNotificationService().submit(() -> {
            try {
                noticator.run();
            } catch (Exception ex) {
                logger.error(errorMessage, ex);
            }
        });
    }

    private static ExecutorService getNotificationService() {
        return NOTIFICATION_SERVICE;
    }

    private static void closeSilently(AutoCloseable autoCloseable) {
        try {
            autoCloseable.close();
        } catch (Exception ignore) { /* do nothing */ }
    }

    private static DiscoveredDevice convert(BluetoothDevice device) {
        return new DiscoveredDevice(new URL(TINYB_PROTOCOL_NAME,
                device.getAdapter().getAddress(), device.getAddress()),
                device.getName(), device.getAlias(), device.getRSSI(),
                device.getBluetoothClass(),
                //TODO implement proper determination of the device type
                device.getBluetoothClass() == 0);
    }

    private static DiscoveredAdapter convert(BluetoothAdapter adapter) {
        return new DiscoveredAdapter(new URL(TINYB_PROTOCOL_NAME,
                adapter.getAddress(), null),
                adapter.getName(), adapter.getAlias());
    }
}
