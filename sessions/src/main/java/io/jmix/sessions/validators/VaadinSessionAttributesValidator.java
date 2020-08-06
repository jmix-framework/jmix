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

package io.jmix.sessions.validators;

import java.util.Arrays;
import java.util.List;

public class VaadinSessionAttributesValidator implements SessionAttributePersistenceValidator {

    public final static String SERVLET_NAME = "jmixVaadinServlet";

    public final static String VAADIN_SESSION_KEY = "com.vaadin.server.VaadinSession." + SERVLET_NAME;
    public final static String VAADIN_SERVLET_LOCK_KEY = SERVLET_NAME + ".lock";

    private final static List<String> NON_PERSISTENT_ATTRIBUTES = Arrays.asList(VAADIN_SERVLET_LOCK_KEY, VAADIN_SESSION_KEY);

    @Override
    public boolean isPersistent(String attributeName, Object attributeValue) {
        return !NON_PERSISTENT_ATTRIBUTES.contains(attributeName);
    }
}
