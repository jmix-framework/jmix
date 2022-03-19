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

package datatypes;

import com.jayway.jsonpath.ReadContext;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Test;
import test_support.AbstractRestControllerFT;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static test_support.RestTestUtils.parseResponse;
import static test_support.RestTestUtils.sendGet;

public class DatatypesControllerFT extends AbstractRestControllerFT {

    @Test
    public void getDatatypes() throws Exception {
        String url = baseUrl + "/metadata/datatypes";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            ReadContext ctx = parseResponse(response);
            assertTrue(ctx.read("$.length()", Integer.class) > 1);

            assertEquals("yyyy-MM-dd'T'HH:mm:ss.SSS", ctx.read("$[?(@.id == 'dateTime')].format", List.class).get(0));
            assertEquals(0, ctx.read("$[?(@.id == 'string')].format", List.class).size());

            assertEquals("0.####", ctx.read("$[?(@.id == 'decimal')].format", List.class).get(0));
            assertEquals(".", ctx.read("$[?(@.id == 'decimal')].decimalSeparator", List.class).get(0));
        }
    }
}
