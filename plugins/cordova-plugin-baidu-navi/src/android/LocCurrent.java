package com.mawujun.navi;

/**
 * Created by mawujun on 2017/4/21.
 */

public class LocCurrent {
    public static Double longitude;
    public static Double latitude;

    public static Long getLoc_time() {
        return loc_time;
    }

    public static void setLoc_time(Long loc_time) {
        LocCurrent.loc_time = loc_time;
    }

    public static Long loc_time;

    public static Double getLatitude() {
        return latitude;
    }

    public static void setLatitude(Double latitude) {
        LocCurrent.latitude = latitude;
    }



    public static Double getLongitude() {
        return longitude;
    }

    public static void setLongitude(Double longitude) {
        LocCurrent.longitude = longitude;
    }


}
