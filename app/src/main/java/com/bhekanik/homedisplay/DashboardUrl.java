package com.bhekanik.homedisplay;

import java.net.URI;
import java.net.URISyntaxException;

final class DashboardUrl {
    private DashboardUrl() {}

    static String validate(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Enter a dashboard address.");
        }
        String trimmed = value.trim();
        try {
            URI uri = new URI(trimmed);
            String scheme = uri.getScheme();
            if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                throw new IllegalArgumentException("Use an http:// or https:// address.");
            }
            if (uri.getHost() == null || uri.getHost().isEmpty()) {
                throw new IllegalArgumentException("The address needs a valid host name or IP address.");
            }
            if (uri.getUserInfo() != null) {
                throw new IllegalArgumentException("Sign in on the dashboard; do not put credentials in its address.");
            }
            if (uri.getPort() == 0 || uri.getPort() > 65535) {
                throw new IllegalArgumentException("The port must be between 1 and 65535.");
            }
            return trimmed;
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("The dashboard address is not valid.", exception);
        }
    }
}
