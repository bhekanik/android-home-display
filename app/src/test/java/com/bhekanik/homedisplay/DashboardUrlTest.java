package com.bhekanik.homedisplay;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class DashboardUrlTest {
    @Test public void acceptsLanDashboardAndTrimsPastedWhitespace() {
        assertEquals("http://192.0.2.10:8123/lovelace/home", DashboardUrl.validate("  http://192.0.2.10:8123/lovelace/home  "));
    }
    @Test public void acceptsHttpsAndIpv6Hosts() {
        assertEquals("https://home.example/dashboard#room", DashboardUrl.validate("https://home.example/dashboard#room"));
        assertEquals("http://[::1]:8123/", DashboardUrl.validate("http://[::1]:8123/"));
    }
    @Test public void rejectsNonWebSchemesAndEmbeddedCredentials() {
        for (String address : new String[] {"javascript:alert(1)", "file:///etc/hosts", "intent://settings", "data:text/html,test", "https://user:password@home.example/"}) {
            assertThrows(address, IllegalArgumentException.class, () -> DashboardUrl.validate(address));
        }
    }
    @Test public void rejectsEmptyMalformedHostsAndInvalidPorts() {
        for (String address : new String[] {"", " ", "http://", "https://bad host/", "http://home.example:0", "http://home.example:65536", "http://home.example:-2"}) {
            assertThrows(address, IllegalArgumentException.class, () -> DashboardUrl.validate(address));
        }
        assertThrows(IllegalArgumentException.class, () -> DashboardUrl.validate(null));
    }
}
