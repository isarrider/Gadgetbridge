/*  Copyright (C) 2026 The Gadgetbridge contributors

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.externalevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class WatchAppCompanionRegistryTest {
    private static final String DEVICE = "AA:BB:CC:DD:EE:01";
    private static final String OTHER = "AA:BB:CC:DD:EE:02";

    private final WatchAppCompanionRegistry registry = WatchAppCompanionRegistry.getInstance();

    @Before
    public void clear() {
        registry.clear();
    }

    @Test
    public void routesOnlyTheBoundDeviceAndApp() {
        assertTrue(registry.bind(DEVICE, "0391257", "com.example.one"));
        assertTrue(registry.bind(DEVICE, "42", "com.example.two"));
        assertEquals("com.example.one", registry.packageFor(DEVICE, "391257"));
        assertEquals("com.example.two", registry.packageFor(DEVICE, "42"));
        assertNull(registry.packageFor(OTHER, "391257"));
        assertNull(registry.packageFor(DEVICE, "43"));
    }

    @Test
    public void appIdIsNotAPackageAndDoesNotAuthorizeOne() {
        assertFalse(registry.bind(DEVICE, "391257", "391257"));
        assertFalse(registry.bind(DEVICE, "org.example.watchapp", "com.example.one"));
        assertFalse(registry.bind(DEVICE, "391257", "not a package"));
        assertFalse(registry.bind("", "391257", "com.example.one"));
        assertFalse(registry.bind(null, "391257", "com.example.one"));
        assertNull(registry.packageFor(DEVICE, "391257"));
    }

    @Test
    public void laterCompanionReplacesTheRecipient() {
        assertTrue(registry.bind(DEVICE, "7", "com.example.one"));
        assertTrue(registry.bind(DEVICE, "7", "com.example.two"));
        assertEquals("com.example.two", registry.packageFor(DEVICE, "7"));
    }
}
