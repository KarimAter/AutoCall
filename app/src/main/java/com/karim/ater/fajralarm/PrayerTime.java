package com.karim.ater.fajralarm;

public class PrayerTime {
    private timings timings;

    public PrayerTime(PrayerTime.timings timings) {
        this.timings = timings;
    }

    PrayerTime.timings getTimings() {
        return timings;
    }

    public class timings {
        private String Fajr;
        private String Sunrise;

        public timings(String fajr, String sunrise) {
            Fajr = fajr;
            Sunrise = sunrise;
        }

        String getFajr() {
            return Fajr;
        }

        public String getSunrise() {
            return Sunrise;
        }
    }
}
