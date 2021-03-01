/*
 * Copyright 2021 Haulmont.
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

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package io.jmix.hibernate.impl.types.uuid;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor;

import java.util.UUID;

import static java.lang.System.arraycopy;

/**
 * Descriptor for {@link UUID} handling.
 */
public class JmixUUIDTypeDescriptor extends UUIDTypeDescriptor {
    public static final JmixUUIDTypeDescriptor INSTANCE = new JmixUUIDTypeDescriptor();

    @SuppressWarnings({"unchecked"})
    public <X> X unwrap(UUID value, Class<X> type, WrapperOptions options) {
        if (value == null) {
            return null;
        }

        if (String.class.isAssignableFrom(type)) {
            return (X) ToStringTransformer.INSTANCE.transform(value);
        }

        return super.unwrap(value, type, options);
    }

    public <X> UUID wrap(X value, WrapperOptions options) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return ToStringTransformer.INSTANCE.parse(value);
        }

        return super.wrap(value, options);
    }

    public static class ToStringTransformer implements ValueTransformer {
        public static final ToStringTransformer INSTANCE = new ToStringTransformer();

        public String transform(UUID uuid) {
            return uuid.toString().replace("-", "");
        }

        public UUID parse(Object value) {
            String uuid = (String) value;
            if (uuid.contains("-")) {
                return UUID.fromString(uuid);
            } else {
                return UUID.fromString(prepareForUUID(uuid));
            }
        }

        protected String prepareForUUID(String value) {
            char[] uuidArr = new char[36];
            char[] uuid = value.toCharArray();

            arraycopy(uuid, 0, uuidArr, 0, 8);
            uuidArr[8] = '-';
            arraycopy(uuid, 8, uuidArr, 9, 4);
            uuidArr[13] = '-';
            arraycopy(uuid, 12, uuidArr, 14, 4);
            uuidArr[18] = '-';
            arraycopy(uuid, 16, uuidArr, 19, 4);
            uuidArr[23] = '-';
            arraycopy(uuid, 20, uuidArr, 24, 12);

            return new String(uuidArr);
        }
    }
}
