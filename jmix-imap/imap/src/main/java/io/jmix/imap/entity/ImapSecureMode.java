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

package io.jmix.imap.entity;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

import jakarta.annotation.Nullable;

public enum ImapSecureMode implements EnumClass<String> {

    STARTTLS("starttls"),
    TLS("tls");

    private final String id;

    ImapSecureMode(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static ImapSecureMode fromId(String id) {
        for (ImapSecureMode at : ImapSecureMode.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}