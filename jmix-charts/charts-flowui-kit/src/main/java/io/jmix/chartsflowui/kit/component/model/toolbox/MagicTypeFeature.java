/*
 * Copyright 2023 Haulmont.
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

package io.jmix.chartsflowui.kit.component.model.toolbox;

import io.jmix.chartsflowui.kit.component.model.ChartObservableObject;
import io.jmix.chartsflowui.kit.component.model.HasEnumId;
import jakarta.annotation.Nullable;

public class MagicTypeFeature extends AbstractFeature<MagicTypeFeature> {

    protected MagicType[] types;

    protected Title title;

    protected Icon icon;

    public enum MagicType implements HasEnumId {
        LINE("line"),
        BAR("bar"),
        STACK("stack");

        private final String id;

        MagicType(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static MagicType fromId(String id) {
            for (MagicType at : MagicType.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    public static class Title extends ChartObservableObject {

        protected String bar;

        protected String line;

        protected String stack;

        protected String tiled;

        public String getBar() {
            return bar;
        }

        public void setBar(String bar) {
            this.bar = bar;
            markAsDirty();
        }

        public String getLine() {
            return line;
        }

        public void setLine(String line) {
            this.line = line;
            markAsDirty();
        }

        public String getStack() {
            return stack;
        }

        public void setStack(String stack) {
            this.stack = stack;
            markAsDirty();
        }

        public String getTiled() {
            return tiled;
        }

        public void setTiled(String tiled) {
            this.tiled = tiled;
            markAsDirty();
        }

        public Title withBar(String bar) {
            setBar(bar);
            return this;
        }

        public Title withLine(String line) {
            setLine(line);
            return this;
        }

        public Title withStack(String stack) {
            setStack(stack);
            return this;
        }

        public Title withTiled(String tiled) {
            setTiled(tiled);
            return this;
        }
    }

    public static class Icon extends ChartObservableObject {

        protected String line;

        protected String bar;

        protected String stack;

        public String getLine() {
            return line;
        }

        public void setLine(String line) {
            this.line = line;
            markAsDirty();
        }

        public String getBar() {
            return bar;
        }

        public void setBar(String bar) {
            this.bar = bar;
            markAsDirty();
        }

        public String getStack() {
            return stack;
        }

        public void setStack(String stack) {
            this.stack = stack;
            markAsDirty();
        }

        public Icon withLine(String line) {
            setLine(line);
            return this;
        }

        public Icon withBar(String bar) {
            setBar(bar);
            return this;
        }

        public Icon withStack(String stack) {
            setStack(stack);
            return this;
        }
    }

    public MagicType[] getTypes() {
        return types;
    }

    public void setTypes(MagicType... types) {
        this.types = types;
        markAsDirty();
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        if (this.title != null) {
            removeChild(this.title);
        }

        this.title = title;
        addChild(title);
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        if (this.icon != null) {
            removeChild(this.icon);
        }

        this.icon = icon;
        addChild(icon);
    }

    public MagicTypeFeature withTypes(MagicType... types) {
        setTypes(types);
        return this;
    }

    public MagicTypeFeature withTitle(Title title) {
        setTitle(title);
        return this;
    }

    public MagicTypeFeature withIcon(Icon icon) {
        setIcon(icon);
        return this;
    }

    @Override
    protected String getFeatureName() {
        return "magicType";
    }
}
