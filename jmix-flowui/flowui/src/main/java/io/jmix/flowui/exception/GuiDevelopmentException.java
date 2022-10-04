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
import io.jmix.flowui.xml.layout.ComponentLoader.ComponentContext;
import io.jmix.flowui.xml.layout.ComponentLoader.Context;

import javax.annotation.Nullable;
import java.util.Map;

public class GuiDevelopmentException extends DevelopmentException {

    protected String frameId;
    protected Context context;

    public GuiDevelopmentException(String message, String frameId) {
        super(message);
        this.frameId = frameId;
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

    @Nullable
    public String getFrameId() {
        if (frameId != null) {
            return frameId;
        } else if (context instanceof ComponentContext) {
            return ((ComponentContext) context).getFullFrameId();
        } else {
            return null;
        }
    }

    @Nullable
    public Context getContext() {
        return context;
    }

    @Override
    public String toString() {
        return super.toString() + (frameId != null ? ", frameId=" + frameId : "");
    }
}
