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

package io.jmix.email.authentication;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.jspecify.annotations.Nullable;

/**
 * OAuth2 client type a refresh token was issued to. Some providers bind the refresh token to the
 * client type: e.g. Microsoft Entra rejects redemption with a client secret for tokens obtained
 * by a public client flow such as device code (error AADSTS700025), and vice versa. The type is
 * stored together with the token so that redemption uses the matching client flavor.
 */
public enum OAuth2ClientType implements EnumClass<String> {

    /**
     * The token was issued to a confidential client (client secret was presented). Used by the
     * authorization code flow and manual token updates.
     */
    CONFIDENTIAL("CONFIDENTIAL"),

    /**
     * The token was issued to a public client (no client secret). Used by the device code flow.
     */
    PUBLIC("PUBLIC");

    private final String id;

    OAuth2ClientType(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static OAuth2ClientType fromId(String id) {
        for (OAuth2ClientType clientType : OAuth2ClientType.values()) {
            if (clientType.getId().equals(id)) {
                return clientType;
            }
        }
        return null;
    }
}
