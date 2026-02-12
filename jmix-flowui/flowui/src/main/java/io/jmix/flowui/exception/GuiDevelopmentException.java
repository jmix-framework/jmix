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

package io.jmix.flowui.exception;

import io.jmix.core.DevelopmentException;
import io.jmix.flowui.xml.layout.ComponentLoader.Context;
import org.springframework.lang.Nullable;

import java.util.Map;

/**
 * Exception thrown during the development of UI, such as incorrect view layout markup.
 */
public class GuiDevelopmentException extends DevelopmentException {

    protected String originId;
    protected Context context;

    public GuiDevelopmentException(String message, @Nullable String originId) {
        super(message);
        this.originId = originId;
    }

    public GuiDevelopmentException(String message, Context context) {
        super(message);
        this.context = context;
    }

    public GuiDevelopmentException(String message, Context context, String paramKey, Object paramValue) {
        super(message, paramKey, paramValue);
        this.context = context;
    }

    public GuiDevelopmentException(String message, Context context, Map<String, Object> params) {
        super(message, params);
        this.context = context;
    }

    /**
     * Returns the origin identifier.
     *
     * @return the origin identifier
     */
    @Nullable
    public String getOriginId() {
        return originId != null
                ? originId
                : context != null
                ? context.getFullOriginId()
                : null;
    }

    /**
     * @deprecated Use {@link #getOriginId()} instead
     */
    @Deprecated(since = "2.3", forRemoval = true)
    @Nullable
    public String getFrameId() {
        return getOriginId();
    }

    @Nullable
    public Context getContext() {
        return context;
    }

    @Override
    public String toString() {
        String originId = getOriginId();
        return super.toString() +
                (originId != null ? ", originId=" + originId : "");
    }
}
