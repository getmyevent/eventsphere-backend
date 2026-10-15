package com.eventsphere.common.util;

public final class MobileNormalizer {

    private MobileNormalizer() {}

    public static String normalize(String rawMobile) {
        if (rawMobile == null) return "";
        String digits = rawMobile.replaceAll("\\D", "");
        if (digits.startsWith("91") && digits.length() > 10) {
            digits = digits.substring(2);
        }
        if (digits.startsWith("0") && digits.length() > 10) {
            digits = digits.substring(1);
        }
        return digits;
    }
}
