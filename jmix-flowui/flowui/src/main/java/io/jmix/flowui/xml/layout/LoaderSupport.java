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

package io.jmix.flowui.xml.layout;

import com.google.common.base.Strings;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Facilitates parsing data from XML.
 */
@Component("flowui_LoaderSupport")
public class LoaderSupport {

    /**
     * Returns an {@link Optional} with the attribute value from
     * the given element for the attribute with the given name,
     * otherwise an empty {@code Optional}.
     *
     * @param element       the element to obtain value
     * @param attributeName the name of the attribute value to be returned
     * @return an {@link Optional} with a present {@link String} value if the
     * specified attribute exists in the element, otherwise an empty {@link Optional}
     */
    public Optional<String> loadString(Element element, String attributeName) {
        String attributeValue = element.attributeValue(attributeName);
        return Optional.ofNullable(Strings.emptyToNull(attributeValue));
    }

    /**
     * Returns an {@link Optional} with the attribute value from
     * the given element for the attribute with the given name,
     * otherwise an empty {@code Optional}.
     *
     * @param element       the element to obtain value
     * @param attributeName the name of the attribute value to be returned
     * @return an {@link Optional} with a present {@link Boolean} value if the
     * specified attribute exists in the element, otherwise an empty {@link Optional}
     */
    public Optional<Boolean> loadBoolean(Element element, String attributeName) {
        return loadString(element, attributeName)
                .map(Boolean::parseBoolean);
    }

    /**
     * Returns an {@link Optional} with the attribute value from
     * the given element for the attribute with the given name,
     * otherwise an empty {@code Optional}.
     *
     * @param element       the element to obtain value
     * @param attributeName the name of the attribute value to be returned
     * @return an {@link Optional} with a present {@link Integer} value if the
     * specified attribute exists in the element, otherwise an empty {@link Optional}
     */
    public Optional<Integer> loadInteger(Element element, String attributeName) {
        return loadString(element, attributeName)
                .map(Integer::parseInt);
    }

    /**
     * Returns an {@link Optional} with the attribute value from
     * the given element for the attribute with the given name,
     * otherwise an empty {@code Optional}.
     *
     * @param element       the element to obtain value
     * @param type          the enum type to be converted to
     * @param attributeName the name of the attribute value to be returned
     * @return an {@link Optional} with a present {@link Enum} value if the
     * specified attribute exists in the element, otherwise an empty {@link Optional}
     */
    public <T extends Enum<T>> Optional<T> loadEnum(Element element, Class<T> type, String attributeName) {
        return loadString(element, attributeName)
                .map(stringValue -> Enum.valueOf(type, stringValue));
    }
}
