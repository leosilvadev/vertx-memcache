/**
 * Copyright 2014 Groupon.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.groupon.vertx.memcache.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import io.vertx.core.json.JsonObject;
import org.junit.Before;
import org.junit.Test;

import com.groupon.vertx.memcache.MemcacheException;
import com.groupon.vertx.memcache.stream.MemcacheResponseType;

/**
 * Tests for <code>ModifyLineParser</code>.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.0.0
 */
public class ModifyLineParserTest {
    private ModifyLineParser parser;

    @Before
    public void setUp() throws Exception {
        parser = new ModifyLineParser();
    }

    @Test
    public void testIntegerResult() throws Exception {
        byte[] result = "12345".getBytes();
        assertTrue("Failed to parse value", parser.isResponseEnd(result));

        JsonObject response = parser.getResponse();
        assertEquals("Wrong status", "success", response.getString("status"));
        assertEquals("Value not parsed", 12345, (int) response.getInteger("data"));
    }

    @Test
    public void testNonIntegerResult() throws Exception {
        byte[] result = "hello".getBytes();
        try {
            parser.isResponseEnd(result);
            assertTrue("Failed to throw exception", false);
        } catch (MemcacheException me) {
            assertEquals("Unexpected exception", "Unexpected format in response", me.getMessage());
        }
    }

    @Test
    public void testNotFoundResult() throws Exception {
        byte[] result = MemcacheResponseType.NOT_FOUND.type;
        assertTrue("Failed to parse value", parser.isResponseEnd(result));

        JsonObject response = parser.getResponse();
        assertEquals("Wrong status", "success", response.getString("status"));
        assertNull("Value should be null", response.getString("data"));
    }

    @Test
    public void testErrorEndLine() {
        assertTrue("Failed to identify end", parser.isResponseEnd(MemcacheResponseType.ERROR.type));
        JsonObject response = parser.getResponse();
        assertEquals("Wrong status", "error", response.getString("status"));
        assertEquals("Wrong data", MemcacheResponseType.ERROR.name(), response.getString("message"));
    }

    @Test
    public void testClientErrorEndLine() {
        String clientError = "CLIENT ERROR message";
        assertTrue("Failed to identify end", parser.isResponseEnd(clientError.getBytes()));
        JsonObject response = parser.getResponse();
        assertEquals("Wrong status", "error", response.getString("status"));
        assertEquals("Wrong data", clientError, response.getString("message"));
    }

    @Test
    public void testServerErrorEndLine() {
        String serverError = "SERVER ERROR message";
        assertTrue("Failed to identify end", parser.isResponseEnd(serverError.getBytes()));
        JsonObject response = parser.getResponse();
        assertEquals("Wrong status", "error", response.getString("status"));
        assertEquals("Wrong data", serverError, response.getString("message"));
    }

    @Test
    public void testInvalidLine() {
        try {
            byte[] start = "foo".getBytes();
            parser.isResponseEnd(start);
            assertTrue("Failed to throw exception", false);
        } catch (MemcacheException me) {
            assertEquals("Unexpected exception", "Unexpected format in response", me.getMessage());
        }
    }
}
