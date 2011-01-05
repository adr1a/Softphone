package Multimedia;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.media.*;
import javax.media.format.VideoFormat;

import com.sun.media.vfw.*;
import com.sun.media.protocol.vfw.VFWCapture;
import com.sun.media.util.WindowUtil;


public class VFWAuto {

    public void detectaDispositivos() {
        Vector devices = (Vector) CaptureDeviceManager.getDeviceList(null).clone();
        Enumeration enu = devices.elements();

        while (enu.hasMoreElements()) {
            CaptureDeviceInfo cdi = (CaptureDeviceInfo) enu.nextElement();
            String name = cdi.getName();
            if (name.startsWith("vfw:"))
                CaptureDeviceManager.removeDevice(cdi);
        }
	
        int nDevices = 0;
        for (int i = 0; i < 10; i++) {
            String name = VFWCapture.capGetDriverDescriptionName(i);
            if (name != null && name.length() > 1) {
                System.err.println("Found device " + name);
                System.err.println("Querying device. Please wait...");
                com.sun.media.protocol.vfw.VFWSourceStream.autoDetect(i);
                nDevices++;
            }
        }
    }

}