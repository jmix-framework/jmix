/*
 * Copyright 2026 Haulmont.
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

package logging;

import io.jmix.email.ExceptionMessagesSupport;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionMessagesSupportTest {

    @Test
    void testChainIsJoinedOutermostFirst() {
        Exception root = new IOException("Connection refused");
        Exception middle = new IllegalStateException("Token endpoint unavailable", root);
        Exception outer = new RuntimeException("Connection test failed", middle);

        assertEquals("Connection test failed | Token endpoint unavailable | Connection refused",
                ExceptionMessagesSupport.flatten(outer));
    }

    @Test
    void testMultiLineMessageIsCollapsedToSingleLine() {
        Exception exception = new IOException("Token endpoint returned status 400:\n{\n  \"error\": \"invalid_grant\",\n"
                + "  \"error_description\": \"Token has been expired or revoked.\"\n}");

        assertEquals("Token endpoint returned status 400: { \"error\": \"invalid_grant\","
                        + " \"error_description\": \"Token has been expired or revoked.\" }",
                ExceptionMessagesSupport.flatten(exception));
    }

    @Test
    void testMessageRepeatedByWrapperIsDropped() {
        Exception root = new IOException("535-5.7.8 Username and Password not accepted");
        Exception wrapper = new RuntimeException(
                "Authentication failed: 535-5.7.8 Username and Password not accepted", root);

        assertEquals("Authentication failed: 535-5.7.8 Username and Password not accepted",
                ExceptionMessagesSupport.flatten(wrapper));
    }

    @Test
    void testMessagelessCauseIsRepresentedByClassName() {
        Exception outer = new IllegalStateException("Connection test failed", new NullPointerException());

        assertEquals("Connection test failed | NullPointerException",
                ExceptionMessagesSupport.flatten(outer));
    }
}
