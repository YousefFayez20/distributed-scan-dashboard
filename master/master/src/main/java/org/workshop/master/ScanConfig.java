package org.workshop.master;

public final class ScanConfig {
    private ScanConfig(){
    }
    public static final String CIDR = "192.168.1.0/24";
    public static final int INTERVAL_IN_SECONDS = 20;
    public static final int[] PORTS = {8080,443,80};
}
