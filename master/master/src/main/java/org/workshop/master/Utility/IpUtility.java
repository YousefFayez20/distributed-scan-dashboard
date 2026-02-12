package org.workshop.master.Utility;

public class IpUtility {
    public static long ipToLong(String ip){
        String[] parts = ip.split("\\.");
        long result =0;
        for(String part :parts){
            result = (result<<8) |Integer.parseInt(part);
        }
        return result;
    }
    public static String longToIp(long ip) {
        return String.format("%d.%d.%d.%d",
                (ip >> 24) & 255,
                (ip >> 16) & 255,
                (ip >> 8) & 255,
                ip & 255);
    }
    public static long[] cidrToRange(String cidr){
        String[] parts = cidr.split("/");
        String baseIP = parts[0];
        int prefix = Integer.parseInt(parts[1]);
        long ipLong = ipToLong(baseIP);

        long mask = -1L << (32-prefix);

        long network = ipLong & mask;
        long broadcast = network + (~mask);

        return new long[]{network, broadcast};
    }
}
