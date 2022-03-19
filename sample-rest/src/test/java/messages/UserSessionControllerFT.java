/*
 * Copyright 2020 Haulmont.
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

package messages;

import test_support.AbstractRestControllerFT;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static test_support.RestTestUtils.sendPutWithHeaders;
import static test_support.RestTestUtils.statusCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserSessionControllerFT extends AbstractRestControllerFT {

    @Test
    public void setSessionLocale() throws Exception {
        setSessionLocale(
                "en",
                oauthToken,
                response -> assertEquals(HttpStatus.SC_OK, statusCode(response))
        );
    }

    @Test
    public void setSessionLocaleUnauthorized() throws Exception {
        setSessionLocale(
                "en",
                null,
                response -> assertEquals(HttpStatus.SC_UNAUTHORIZED, statusCode(response))
        );
    }

    @Test
    public void setSessionLocaleUnsupported() throws Exception {
        setSessionLocale(
                "a string representing unsupported locale",
                oauthToken,
                response -> assertEquals(HttpStatus.SC_UNPROCESSABLE_ENTITY, statusCode(response))
        );
    }

    private void setSessionLocale(String locale,
                                  @Nullable String token,
                                  Consumer<CloseableHttpResponse> assertionsCallback) throws Exception {
        String url = baseUrl + "/user-session/locale";

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.ACCEPT_LANGUAGE, locale);

        try (CloseableHttpResponse response = sendPutWithHeaders(url, token, "", null, headers)) {
            assertionsCallback.accept(response);
        }
    }
}
