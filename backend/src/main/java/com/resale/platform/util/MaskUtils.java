package com.resale.platform.util;

public final class MaskUtils {

    private MaskUtils() {
    }

    public static String maskAccount(String account) {
        if (account == null || account.length() < 3) {
            return "***";
        }
        if (account.contains("@")) {
            int atIndex = account.indexOf("@");
            if (atIndex > 2) {
                return account.substring(0, 2) + "***" + account.substring(atIndex);
            }
        }
        return account.substring(0, 2) + "***" + account.substring(account.length() - 1);
    }

    public static String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 7) {
            return "***";
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }
}
