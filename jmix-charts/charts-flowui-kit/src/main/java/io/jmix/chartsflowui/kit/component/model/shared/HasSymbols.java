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

/**
 * A component that has symbols.
 *
 * @param <T> origin class type
 */
public interface HasSymbols<T> {

    /**
     * @return symbol
     */
    Symbol getSymbol();

    /**
     * Sets a symbol or replaces an existing one.<br/>
     * Possible values:
     * <ul>
     *     <li>
     *         {@code image://%url%}, example: {@code image://http://example.website/a/b.png}
     *     </li>
     *     <li>
     *         {@code image://%dataURI%}, example: {@code image://data:image/gif;base64,...}
     *     </li>
     *     <li>
     *         {@code path://%svgPathData%}, example: {@code path://M30.9,53.2c16...}
     *     </li>
     * </ul>
     *
     * @param icon symbol to set
     */
    void setSymbol(String icon);

    /**
     * Sets predefined symbol type or replaces an existing one.
     *
     * @param symbolType symbol to set
     */
    void setSymbol(SymbolType symbolType);

    /**
     * @param icon symbol to set
     * @return this
     * @see HasSymbols#setSymbol(String)
     */
    @SuppressWarnings("unchecked")
    default T withSymbol(String icon) {
        setSymbol(icon);
        return (T) this;
    }

    /**
     * @param symbolType symbol to set
     * @return this
     * @see HasSymbols#setSymbol(SymbolType)
     */
    @SuppressWarnings("unchecked")
    default T withSymbol(SymbolType symbolType) {
        setSymbol(symbolType);
        return (T) this;
    }

    /**
     * @return symbol size
     */
    Integer getSymbolSize();

    /**
     * Sets a symbol size or replaces an existing one.
     *
     * @param symbolSize symbol size to set
     */
    void setSymbolSize(Integer symbolSize);

    /**
     * @param symbolSize symbol size to set
     * @return this
     * @see HasSymbols#setSymbolSize(Integer)
     */
    @SuppressWarnings("unchecked")
    default T withSymbolSize(Integer symbolSize) {
        setSymbolSize(symbolSize);
        return (T) this;
    }

    /**
     * @return rotate degree of symbol
     */
    Integer getSymbolRotate();

    /**
     * Sets a rotate degree of symbol or replaces an existing one. The negative value represents clockwise.
     *
     * @param symbolRotate rotate symbol to set
     */
    void setSymbolRotate(Integer symbolRotate);

    /**
     * @param symbolRotate rotate symbol to set
     * @return this
     * @see HasSymbols#setSymbolRotate(Integer)
     */
    @SuppressWarnings("unchecked")
    default T withSymbolRotate(Integer symbolRotate) {
        setSymbolRotate(symbolRotate);
        return (T) this;
    }

    /**
     * @return {@code true} if the aspect for symbols must be kept, {@code false} otherwise
     */
    Boolean getSymbolKeepAspect();

    /**
     * Sets the keeping of aspects for a symbol.
     *
     * @param symbolKeepAspect whether to keep aspect for symbols in the form of {@code path://%svgPathData%}
     */
    void setSymbolKeepAspect(Boolean symbolKeepAspect);

    /**
     * @param symbolKeepAspect whether to keep aspect for symbols in the form of {@code path://%svgPathData%}
     * @return this
     * @see HasSymbols#setSymbolKeepAspect(Boolean)
     */
    @SuppressWarnings("unchecked")
    default T withSymbolKeepAspect(Boolean symbolKeepAspect) {
        setSymbolKeepAspect(symbolKeepAspect);
        return (T) this;
    }

    /**
     * @return offset of symbol relative to original position
     */
    String[] getSymbolOffset();

    /**
     * Sets an offset of symbol relative to original position or replaces an existing one.<br/>
     * Possible values:
     * <ul>
     *     <li>
     *         Pixel values: {@code hasSymbols.setSymbolOffset("10", "10");}.
     *     </li>
     *     <li>
     *         Percentage values: {@code hasSymbols.setSymbolOffset("50%", "45%");}.
     *     </li>
     *     <li>
     *         Pixel and percentage values combination: {@code hasSymbols.setSymbolOffset("5", "50%");}.
     *     </li>
     * </ul>
     *
     * @param xOffset horizontal offset
     * @param yOffset vertical offset
     */
    void setSymbolOffset(String xOffset, String yOffset);

    /**
     * @param xOffset horizontal offset
     * @param yOffset vertical offset
     * @return this
     * @see HasSymbols#setSymbolOffset(String, String)
     */
    @SuppressWarnings("unchecked")
    default T withSymbolOffset(String xOffset, String yOffset) {
        setSymbolOffset(xOffset, yOffset);
        return (T) this;
    }

    /**
     * Predefined symbol types.
     */
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

    /**
     * A symbol that can be defined by a predefined type or icon.
     */
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
