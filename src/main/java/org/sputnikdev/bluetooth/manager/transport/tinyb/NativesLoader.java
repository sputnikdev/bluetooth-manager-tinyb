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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

/**
 * An utility class that loads tinyb native libraries from classpath by copying them into the temp directory.
 * @author Vlad Kolotov
 */
final class NativesLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativesLoader.class);

    private NativesLoader() { }

    static String prepare(String library) throws IOException, UnsupportedOperationException {
        if (!isLinux()) {
            throw new IllegalStateException("Operation system is not suported: " + getOsName());
        }

        String libraryPath = getLibFolder() + "/" + library;
        BufferedInputStream tinyBClasspathStream = null;

        try {
            tinyBClasspathStream = new BufferedInputStream(NativesLoader.class.getResourceAsStream(libraryPath), 1000);
            File lib = new File(createTempDirectory(), new File(library).getName());
            if (lib.createNewFile()) {
                FileUtils.copyInputStreamToFile(tinyBClasspathStream, lib);
                return lib.getAbsolutePath();
            }
            throw new IllegalStateException("Could not create a temporary file: " + lib.getAbsolutePath());
        } finally {
            IOUtils.closeQuietly(tinyBClasspathStream);
        }
    }

    static boolean isSupportedEnvironment() {
        LOGGER.debug("OS name: {}; OS arch: {}", getOsName(), getOsArch());
        LOGGER.debug("isLinux: {}; isARM6: {}; isX86_64: {}; isX86_32: {}",
                isLinux(), isARM6(), isX86_64(), isX86_32());
        //TODO add some checks for Bluez versions, e.g. that it is greater than v4.43
        return isLinux() && (isARM6() || isX86_64() || isX86_32());
    }

    static String getLibFolder() throws UnsupportedOperationException {
        if (isARM6()) {
            return "/native/arm/armv6";
        } else if (isX86_64()) {
            return "/native/linux/x86_64";
        } else if (isX86_32()) {
            return "/native/linux/x86_32";
        } else {
            throw new UnsupportedOperationException("Unsupported platform");
        }
    }

    static boolean isARM6() {
        return getOsArch().startsWith("arm");
    }

    static boolean isLinux() {
        return getOsName().startsWith("linux");
    }

    static boolean isX86_64() {
        String osArch = getOsArch();
        return osArch.startsWith("x86_64") || osArch.startsWith("amd64");
    }

    static boolean isX86_32() {
        String osArch = getOsArch();
        return "x86".equals(osArch) || osArch.startsWith("i686") || osArch.startsWith("i586") || osArch.startsWith("i486") || osArch.startsWith("i386");
    }

    private static String getOsName() {
        return System.getProperty("os.name").toLowerCase();
    }

    private static String getOsArch() {
        return System.getProperty("os.arch").toLowerCase();
    }

    private static File createTempDirectory() throws IOException {
        File tempDirectory = File.createTempFile("tinyb", "libs");
        tempDirectory.delete();
        tempDirectory.mkdir();
        try {
            tempDirectory.deleteOnExit();
        } catch (Exception ignored) {
            // old jvms
        }
        return tempDirectory;
    }

}
