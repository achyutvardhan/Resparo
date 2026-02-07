package com.resparo.dev.util;

import com.resparo.dev.domain.OperatingSystem;

public final class OsdetectionProvider {

    private static  OperatingSystem systemOs;

    private static void detectOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            systemOs = OperatingSystem.MAC;
        } else if (os.contains("win")) {
            systemOs = OperatingSystem.WINDOWS;
        } else if (os.contains("nux") || os.contains("nix")) {
            systemOs = OperatingSystem.LINUX;
        } else {
            systemOs = OperatingSystem.UNKNOWN;
        }
    }

    public static  OperatingSystem getOS(){
        if (systemOs == null) {
            detectOS();
        }
        return systemOs;
    }
}