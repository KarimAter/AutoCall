package com.karim.ater.fajralarm;

public class PrayerTime {
    public class timings {
        String Fajr;
        String Sunrise;

        public timings(String fajr, String sunrise) {
            Fajr = fajr;
            Sunrise = sunrise;
        }

        public String getFajr() {
            return Fajr;
        }

        public void setFajr(String fajr) {
            Fajr = fajr;
        }

        public String getSunrise() {
            return Sunrise;
        }

        public void setSunrise(String sunrise) {
            Sunrise = sunrise;
        }
    }

    timings timings;

    public PrayerTime(PrayerTime.timings timings) {
        this.timings = timings;
    }

    public PrayerTime.timings getTimings() {
        return timings;
    }

    public void setTimings(PrayerTime.timings timings) {
        this.timings = timings;
    }

    //    public String getTimings() {
//        return timings;
//    }
//
//    public void setTimings(String timings) {
//        this.timings = timings;
//    }
////
////    public PrayerTime(String Fajr, String date) {
////        this.Fajr = Fajr;
////        this.date = date;
////    }
//
//
//    public PrayerTime(String timings) {
//        this.timings = timings;
//    }

    public PrayerTime() {
    }

//    public String getFajr() {
//        return Fajr;
//    }
//
//    public void setFajr(String fajr) {
//        this.Fajr = fajr;
//    }
//
//    public String getDate() {
//        return date;
//    }
//
//    public void setDate(String date) {
//        this.date = date;
//    }
}
