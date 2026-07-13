/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.xml.layout.support;

import com.google.common.base.Strings;
import io.jmix.core.MessageTools;
import io.jmix.flowui.kit.xml.layout.support.BaseLoaderSupport;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Facilitates parsing data from XML.
 */
@Component("flowui_LoaderSupport")
public class LoaderSupport {

    protected MessageTools messageTools;

    public LoaderSupport(MessageTools messageTools) {
        this.messageTools = messageTools;
    }

    /**
     * Returns an {@link Optional} with the attribute value from
     * the given element for the attribute with the given name,
     * otherwise an empty {@code Optional}. Also returns an empty
     * {@code Optional} if the attribute value is the empty string.
     *
     * @param element       the element to obtain value
     * @param attributeName the name of the attribute value to be returned
     * @return an {@link Optional} with a present {@link String} value if the
     * specified attribute exists in the element, otherwise an empty {@link Optional}
     */
    public Optional<String> loadString(Element element, String attributeName) {
        return BaseLoaderSupport.loadString(element, attributeName);
    }

    /**
     * Returns an {@link Optional} with the attribute value from
     * the given element for the attribute with the given name,
     * otherwise an empty {@code Optional}.
     *
     * @param element       the element to obtain value
     * @param attributeName the name of the attribute value to be returned
     * @param emptyToNull   whether to return an empty {@code Optional} for empty string
     * @return an {@link Optional} with a present {@link String} value if the
     * specified attribute exists in the element, otherwise an empty {@link Optional}
     */
    public Optional<String> loadString(Element element, String attributeName, boolean emptyToNull) {
        return BaseLoaderSupport.loadString(element, attributeName, emptyToNull);
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
        return BaseLoaderSupport.loadBoolean(element, attributeName);
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
        return BaseLoaderSupport.loadInteger(element, attributeName);
    }

    /**
     * Returns an {@link Optional} with the attribute value from
     * the given element for the attribute with the given name,
     * otherwise an empty {@code Optional}.
     *
     * @param element       the element to obtain value
     * @param attributeName the name of the attribute value to be returned
     * @return an {@link Optional} with a present {@link Double} value if the
     * specified attribute exists in the element, otherwise an empty {@link Optional}
     */
    public Optional<Double> loadDouble(Element element, String attributeName) {
        return BaseLoaderSupport.loadDouble(element, attributeName);
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
        return BaseLoaderSupport.loadEnum(element, type, attributeName);
    }

    /**
     * Returns an {@link Optional} with the localized message defined in
     * the attribute value from the given element for the attribute with
     * the given name, otherwise an empty {@code Optional}. Also returns
     * an empty {@code Optional} if the attribute value is the empty string.
     *
     * @param element       the element to obtain value
     * @param attributeName the name of the attribute value to be returned
     * @param messageGroup  message group to use
     * @return an {@link Optional} with a present localized {@link String} value if the
     * specified attribute exists in the element, otherwise an empty {@link Optional}
     */
    public Optional<String> loadResourceString(Element element, String attributeName, String messageGroup) {
        return loadResourceString(element, attributeName, messageGroup, true);
    }

    /**
     * Returns an {@link Optional} with the localized message defined in
     * the attribute value from the given element for the attribute with
     * the given name, otherwise an empty {@code Optional}.
     *
     * @param element       the element to obtain value
     * @param attributeName the name of the attribute value to be returned
     * @param messageGroup  message group to use
     * @param emptyToNull   whether to return an empty {@code Optional} for empty string
     * @return an {@link Optional} with a present localized {@link String} value if the
     * specified attribute exists in the element, otherwise an empty {@link Optional}
     */
    public Optional<String> loadResourceString(Element element, String attributeName,
                                               String messageGroup, boolean emptyToNull) {
        return loadString(element, attributeName, emptyToNull)
                .map(stringValue -> loadResourceString(stringValue, messageGroup));
    }

    public void loadString(Element element, String attributeName, Consumer<String> setter) {
        BaseLoaderSupport.loadString(element, attributeName, setter);
    }

    public void loadBoolean(Element element, String attributeName, Consumer<Boolean> setter) {
        BaseLoaderSupport.loadBoolean(element, attributeName, setter);
    }

    public void loadInteger(Element element, String attributeName, Consumer<Integer> setter) {
        BaseLoaderSupport.loadInteger(element, attributeName, setter);
    }

    public void loadDouble(Element element, String attributeName, Consumer<Double> setter) {
        BaseLoaderSupport.loadDouble(element, attributeName, setter);
    }

    public <T extends Enum<T>> void loadEnum(Element element, Class<T> type, String attributeName,
                                             Consumer<T> setter) {
        BaseLoaderSupport.loadEnum(element, type, attributeName, setter);
    }

    public void loadResourceString(Element element, String attributeName, String messageGroup, Consumer<String> setter) {
        loadResourceString(element, attributeName, messageGroup)
                .ifPresent(setter);
    }

    @Nullable
    protected String loadResourceString(@Nullable String message, String messageGroup) {
        if (Strings.isNullOrEmpty(message)) {
            return message;
        }

        return messageTools.loadString(messageGroup, message);
    }
}
