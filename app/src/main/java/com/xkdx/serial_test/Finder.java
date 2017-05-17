package com.xkdx.serial_test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.Vector;


/**
 * Created by Administrator on 2017/1/18.
 */

public class Finder {
    private static final String TAG = "SerialPort";
    private Vector<Driver> mDrivers = null;

    public Finder() {
    }

    Vector<Driver> getDrivers() throws IOException {
        if(this.mDrivers == null) {
            this.mDrivers = new Vector();
            LineNumberReader r = new LineNumberReader(new FileReader("/proc/tty/drivers"));

            String l;
            while((l = r.readLine()) != null) {
                String drivername = l.substring(0, 21).trim();
                String[] w = l.split(" +");
                if(w.length >= 5 && w[w.length - 1].equals("serial")) {
                    this.mDrivers.add(new Finder.Driver(drivername, w[w.length - 4]));
                }
            }

            r.close();
        }

        return this.mDrivers;
    }

    public String[] getAllDevices() {
        Vector devices = new Vector();

        try {
            Iterator e = this.getDrivers().iterator();

            while(e.hasNext()) {
                Finder.Driver e1 = (Finder.Driver)e.next();
                Iterator itdev = e1.getDevices().iterator();

                while(itdev.hasNext()) {
                    String device = ((File)itdev.next()).getName();
                    String value = String.format("%s (%s)", new Object[]{device, e1.getName()});
                    devices.add(value);
                }
            }
        } catch (IOException var7) {
            var7.printStackTrace();
        }

        return (String[])devices.toArray(new String[devices.size()]);
    }

    public String[] getAllDevicesPath() {
        Vector devices = new Vector();

        try {
            Iterator itdriv = this.getDrivers().iterator();

            while(itdriv.hasNext()) {
                Finder.Driver e = (Finder.Driver)itdriv.next();
                Iterator itdev = e.getDevices().iterator();

                while(itdev.hasNext()) {
                    String device = ((File)itdev.next()).getAbsolutePath();
                    devices.add(device);
                }
            }
        } catch (IOException var6) {
            var6.printStackTrace();
        }

        return (String[])devices.toArray(new String[devices.size()]);
    }

    public class Driver {
        private String mDriverName;
        private String mDeviceRoot;
        Vector<File> mDevices = null;

        public Driver(String name, String root) {
            this.mDriverName = name;
            this.mDeviceRoot = root;
        }

        public Vector<File> getDevices() {
            if(this.mDevices == null) {
                this.mDevices = new Vector();
                File dev = new File("/dev");
                File[] files = dev.listFiles();

                for(int i = 0; i < files.length; ++i) {
                    if(files[i].getAbsolutePath().startsWith(this.mDeviceRoot)) {
                        this.mDevices.add(files[i]);
                    }
                }
            }

            return this.mDevices;
        }

        public String getName() {
            return this.mDriverName;
        }
    }
}
