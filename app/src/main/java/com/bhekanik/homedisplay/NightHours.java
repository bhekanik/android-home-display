package com.bhekanik.homedisplay;

final class NightHours {
    private NightHours() {}

    static boolean isNight(int hour) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Hour must be between 0 and 23.");
        }
        return hour >= 22 || hour < 7;
    }
}
