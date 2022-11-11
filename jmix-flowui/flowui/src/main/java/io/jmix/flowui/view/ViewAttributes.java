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

package io.jmix.flowui.view;

import com.vaadin.flow.component.page.ExtendedClientDetails;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.sys.ExtendedClientDetailsProvider;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Class provides mechanism for setting/getting attributes that uses {@link VaadinSession} as store.
 * Class instance should be initialized with {@link View} id.
 */
@Component("flowui_ViewAttributes")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ViewAttributes {

    protected String viewId;

    protected ExtendedClientDetailsProvider extendedClientDetailsProvider;

    public ViewAttributes(String viewId) {
        Preconditions.checkNotNullArgument(viewId);

        this.viewId = viewId;
    }

    @Autowired
    public void setExtendedClientDetailsProvider(ExtendedClientDetailsProvider extendedClientDetailsProvider) {
        this.extendedClientDetailsProvider = extendedClientDetailsProvider;
    }

    /**
     * @return {@link View} id
     */
    public String getViewId() {
        return viewId;
    }

    /**
     * Sets attribute for the {@link View} ({@link #getViewId()}) in a current browser tab to Vaadin session.
     *
     * @param key   attribute name
     * @param value attribute value
     */
    public void setAttribute(String key, Object value) {
        Preconditions.checkNotEmptyString(key);

        Set<Attributes> attributes = getAttributes(viewId);

        Attributes attributesItem = getAttributesForWindowName(attributes, getWindowName())
                .orElse(new Attributes(getWindowName()));

        attributesItem.setAttribute(key, value);

        attributes.add(attributesItem);

        getVaadinSessionNN().setAttribute(viewId, attributes);
    }

    /**
     * @param key attribute name
     * @return attribute value or {@code null} otherwise
     */
    @Nullable
    public <T> T getAttribute(String key) {
        Preconditions.checkNotEmptyString(key);

        Set<Attributes> attributes = getAttributes(viewId);
        if (CollectionUtils.isEmpty(attributes)) {
            return null;
        }

        //noinspection unchecked
        return (T) getAttributesForWindowName(attributes, getWindowName())
                .map(value -> value.getAttribute(key))
                .orElse(null);
    }

    /**
     * Removes attribute by its name.
     *
     * @param key attribute name
     */
    public void removeAttribute(String key) {
        Preconditions.checkNotEmptyString(key);

        Set<Attributes> attributes = getAttributes(viewId);
        getAttributesForWindowName(attributes, getWindowName())
                .ifPresent(attr -> {
                    attr.removeAttribute(key);
                    if (attr.isEmpty()) {
                        attributes.remove(attr);
                    }
                    getVaadinSessionNN().setAttribute(viewId,
                            CollectionUtils.isEmpty(attributes) ? null : attributes);
                });
    }

    /**
     * Removes {@link Attributes} instance that contains all attributes for {@link View}.
     */
    public void removeAllAttributes() {
        Set<Attributes> viewAttributes = getAttributes(viewId);
        getAttributesForWindowName(viewAttributes, getWindowName())
                .ifPresent(attr -> {
                    viewAttributes.remove(attr);
                    getVaadinSessionNN().setAttribute(viewId,
                            CollectionUtils.isEmpty(viewAttributes) ? null : viewAttributes);
                });
    }

    protected VaadinSession getVaadinSessionNN() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            throw new IllegalStateException(ViewAttributes.class.getSimpleName() +
                    "does not work without defined Vaadin session");
        }
        return session;
    }

    protected Set<Attributes> getAttributes(String viewId) {
        VaadinSession session = getVaadinSessionNN();
        //noinspection unchecked
        return new HashSet<>(
                session.getAttribute(viewId) == null
                        ? Collections.emptySet()
                        : (Set<Attributes>) session.getAttribute(viewId));
    }

    protected Optional<Attributes> getAttributesForWindowName(Set<Attributes> attributes, String windowName) {
        return attributes.stream()
                .filter(attrItem -> attrItem.getWindowName().equals(windowName))
                .findFirst();
    }

    protected String getWindowName() {
        ExtendedClientDetails clientDetails = extendedClientDetailsProvider.getExtendedClientDetails();
        if (clientDetails == null) {
            throw new IllegalStateException("Cannot retrieve a window name");
        }
        return clientDetails.getWindowName();
    }

    /**
     * Class describes attributes map that corresponds to a certain view in a browser tab.
     */
    public static class Attributes {

        protected Map<String, Object> attributes;

        protected String windowName;

        public Attributes(String windowName) {
            Preconditions.checkNotEmptyString(windowName);
            this.windowName = windowName;
        }

        public String getWindowName() {
            return windowName;
        }

        public void setAttribute(String key, Object value) {
            Preconditions.checkNotEmptyString(key);
            if (attributes == null) {
                attributes = new HashMap<>();
            }
            attributes.put(key, value);
        }

        @Nullable
        public Object getAttribute(String key) {
            Preconditions.checkNotEmptyString(key);
            if (attributes == null || !attributes.containsKey(key)) {
                return null;
            }
            return attributes.get(key);
        }

        public void removeAttribute(String key) {
            Preconditions.checkNotEmptyString(key);
            if (attributes == null || !attributes.containsKey(key)) {
                return;
            }
            attributes.remove(key);
        }

        public boolean isEmpty() {
            return MapUtils.isEmpty(attributes);
        }

        @Override
        public int hashCode() {
            return windowName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || !obj.getClass().equals(this.getClass())) {
                return false;
            }
            return ((Attributes) obj).getWindowName().equals(windowName);
        }
    }
}
