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

package rest_version;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import test_support.AbstractRestControllerFT;

import static test_support.RestTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

//todo component in BuildInfo
@Disabled
public class VersionControllerFT extends AbstractRestControllerFT {

    @Test
    public void getApiVersion() throws Exception {
        String url = baseUrl + "/version";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            assertEquals(HttpStatus.SC_OK, statusCode(response));

            String version = responseToString(response);
            assertNotNull(version);
            assertTrue(version.length() > 0);
        }
    }
}
