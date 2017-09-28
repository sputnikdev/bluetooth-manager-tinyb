package org.sputnikdev.bluetooth.manager.transport.tinyb;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class NativesLoaderTest {
    @Test
    public void load() throws Exception {
    }

    @Test
    public void getLibFolder() throws Exception {
        System.setProperty("os.name", "linux blah blah v1");
        System.setProperty("os.arch", "amd64");
        assertEquals("/native/linux/x86_64", NativesLoader.getLibFolder());
    }

    @Test
    public void isARM() throws Exception {
    }

    @Test
    public void isLinux() throws Exception {
    }

    @Test
    public void is64Bit() throws Exception {
    }

}