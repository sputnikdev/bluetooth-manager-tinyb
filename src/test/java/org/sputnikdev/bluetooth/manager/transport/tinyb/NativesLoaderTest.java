package org.sputnikdev.bluetooth.manager.transport.tinyb;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NativesLoaderTest {

    @Test
    public void tesetIsSupportedEnvironment() {
        System.setProperty("os.name", "linux blah blah v1");
        assertTrue(NativesLoader.isSupportedEnvironment());
        System.setProperty("os.name", "Bindows blah blah v0.1");
        assertFalse(NativesLoader.isSupportedEnvironment());
    }

    @Test
    public void testPrepare() throws Exception {
        System.setProperty("os.name", "Linux");
        System.setProperty("os.arch", "x86_64");
        File tempLibFile = new File(NativesLoader.prepare("libjavatinyb.so"));
        assertTrue(tempLibFile.exists());
        assertEquals("libjavatinyb.so", tempLibFile.getName());
        assertEquals(new File(System.getProperty("java.io.tmpdir")), tempLibFile.getParentFile().getParentFile());
    }

    @Test(expected = IllegalStateException.class)
    public void testLoadUnsupportedOS() throws IOException {
        System.setProperty("os.name", "Bindows");
        NativesLoader.prepare("libjavatinyb.so");
    }


    @Test
    public void testGetLibFolder() throws Exception {
        System.setProperty("os.name", "linux blah blah v1");
        System.setProperty("os.arch", "amd64");
        assertEquals("/native/linux/x86_64", NativesLoader.getLibFolder());
        System.setProperty("os.arch", "armblahblah");
        assertEquals("/native/arm/armv6", NativesLoader.getLibFolder());
    }

    @Test
    public void testIsARM() throws Exception {
        System.setProperty("os.arch", "Armv6");
        assertTrue(NativesLoader.isARM());
    }

    @Test
    public void testIsLinux() throws Exception {
        System.setProperty("os.name", "linux blah blah v1");
        assertTrue(NativesLoader.isLinux());
        System.setProperty("os.name", "Bindows blah blah v0.1");
        assertFalse(NativesLoader.isLinux());
    }

    @Test
    public void testIs64Bit() throws Exception {
        System.setProperty("os.arch", "x86_64");
        assertTrue(NativesLoader.is64Bit());
        System.setProperty("os.arch", "Amd64");
        assertTrue(NativesLoader.is64Bit());
        System.setProperty("os.arch", "x86");
        assertFalse(NativesLoader.is64Bit());
    }

}