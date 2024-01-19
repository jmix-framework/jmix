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

package io.jmix.chartsflowui.kit.component.model.shared;

import io.jmix.chartsflowui.kit.component.model.HasEnumId;
import jakarta.annotation.Nullable;

public interface HasSymbols<T> {

    Symbol getSymbol();

    void setSymbol(String icon);

    void setSymbol(SymbolType symbolType);

    @SuppressWarnings("unchecked")
    default T withSymbol(String icon) {
        setSymbol(icon);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    default T withSymbol(SymbolType symbolType) {
        setSymbol(symbolType);
        return (T) this;
    }

    Integer getSymbolSize();

    void setSymbolSize(Integer symbolSize);

    @SuppressWarnings("unchecked")
    default T withSymbolSize(Integer symbolSize) {
        setSymbolSize(symbolSize);
        return (T) this;
    }

    Integer getSymbolRotate();

    void setSymbolRotate(Integer symbolRotate);

    @SuppressWarnings("unchecked")
    default T withSymbolRotate(Integer symbolRotate) {
        setSymbolRotate(symbolRotate);
        return (T) this;
    }

    Boolean getSymbolKeepAspect();

    void setSymbolKeepAspect(Boolean symbolKeepAspect);

    @SuppressWarnings("unchecked")
    default T withSymbolKeepAspect(Boolean symbolKeepAspect) {
        setSymbolKeepAspect(symbolKeepAspect);
        return (T) this;
    }

    String[] getSymbolOffset();

    void setSymbolOffset(String xOffset, String yOffset);

    @SuppressWarnings("unchecked")
    default T withSymbolOffset(String xOffset, String yOffset) {
        setSymbolOffset(xOffset, yOffset);
        return (T) this;
    }

    enum SymbolType implements HasEnumId {
        CIRCLE("circle"),
        RECTANGLE("rect"),
        ROUND_RECTANGLE("roundRect"),
        TRIANGLE("triangle"),
        DIAMOND("diamond"),
        PIN("pin"),
        ARROW("arrow"),
        NONE("none");

        private final String id;

        SymbolType(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static SymbolType fromId(String id) {
            for (SymbolType at : SymbolType.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    class Symbol {

        protected String icon;

        protected SymbolType type;

        public Symbol(String icon) {
            this.icon = icon;
        }

        public Symbol(SymbolType type) {
            this.type = type;
        }

        public String getIcon() {
            return icon;
        }

        public SymbolType getType() {
            return type;
        }
    }
}
