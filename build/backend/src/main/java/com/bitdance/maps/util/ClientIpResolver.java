package com.bitdance.maps.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public final class ClientIpResolver {

    private ClientIpResolver() {}

    public static String resolvePublicIp(HttpServletRequest request) {
        String[] candidates = {
            request.getHeader("X-Forwarded-For"),
            request.getHeader("X-Real-IP"),
            request.getRemoteAddr()
        };
        for (String candidate : candidates) {
            String ip = firstUsefulIp(candidate);
            if (StringUtils.hasText(ip) && !isLocalAddress(ip)) {
                return ip;
            }
        }
        return null;
    }

    private static String firstUsefulIp(String value) {
        if (!StringUtils.hasText(value)) return null;
        for (String part : value.split(",")) {
            String ip = part.trim();
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return null;
    }

    private static boolean isLocalAddress(String ip) {
        return ip.startsWith("127.")
            || ip.equals("::1")
            || ip.equals("0:0:0:0:0:0:0:1")
            || ip.startsWith("10.")
            || ip.startsWith("192.168.")
            || ip.matches("^172\\.(1[6-9]|2\\d|3[0-1])\\..*");
    }
}
