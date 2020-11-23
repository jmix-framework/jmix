/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest.tests;

import com.jayway.jsonpath.ReadContext;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.jmix.samples.rest.tools.RestTestUtils.parseResponse;
import static io.jmix.samples.rest.tools.RestTestUtils.sendGet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 */
public class UserInfoControllerFT extends AbstractRestControllerFT {
    @Test
    public void getUserInfo() throws Exception {
        String url = baseUrl + "/userInfo";
        try (CloseableHttpResponse response = sendGet(url, oauthToken, null)) {
            ReadContext ctx = parseResponse(response);
            assertEquals("admin", ctx.read("$.username"));
            assertNotNull(ctx.read("locale"));
        }
    }
}
