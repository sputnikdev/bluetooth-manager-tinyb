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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;

/**
 * An utility class that loads tinyb native libraries from classpath by copying them into the temp directory.
 * @author Vlad Kolotov
 */
class NativesLoader {

    static boolean load(String library) {
        BufferedInputStream tinyBClasspathStream = null;
        try {
            tinyBClasspathStream = new BufferedInputStream(NativesLoader.class.getResourceAsStream(library), 1000);
            String fileName = new File(library).getName();
            //String libFileName = FilenameUtils.getBaseName(library);

            File tempDirectory = File.createTempFile("tinyb", "libs");
            tempDirectory.delete();
            tempDirectory.mkdir();
            try {
                tempDirectory.deleteOnExit();
            } catch (Exception ex) {
                // old jvms
            }

            File lib = new File(tempDirectory, fileName);
            if (lib.createNewFile()) {
                FileUtils.copyInputStreamToFile(tinyBClasspathStream, lib);
                System.load(lib.getAbsolutePath());
                return true;
            }

        } catch (Exception ex) {
            return false;
        } finally {
            IOUtils.closeQuietly(tinyBClasspathStream);
        }
        return false;
    }

    static String getLibFolder() {
        if (isARM()) {
            return "/native/arm/armv6";
        } else if (isLinux()) {
            return is64Bit() ? "/native/linux/x86_64" : "/native/linux/x86_32";
        }
        throw new IllegalStateException("Unsupported OS: " + getOsName());
    }

    static boolean isARM() {
        return getOsArch().startsWith("arm");
    }

    static boolean isLinux() {
        return getOsName().startsWith("linux");
    }

    static boolean is64Bit() {
        String osArch = getOsArch();
        return osArch.startsWith("x86_64") || osArch.startsWith("amd64");
    }

    private static String getOsName() {
        return System.getProperty("os.name").toLowerCase();
    }

    private static String getOsArch() {
        return System.getProperty("os.arch").toLowerCase();
    }

}
