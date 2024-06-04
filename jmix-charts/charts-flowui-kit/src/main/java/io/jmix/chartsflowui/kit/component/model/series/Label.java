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

package io.jmix.chartsflowui.kit.component.model.series;

import io.jmix.chartsflowui.kit.component.model.HasEnumId;
import io.jmix.chartsflowui.kit.component.model.shared.AbstractEnhancedLabel;
import io.jmix.chartsflowui.kit.component.model.shared.JsFunction;
import jakarta.annotation.Nullable;

/**
 * A text label to explain some information about a graphic element, such as value, name etc.
 * More detailed information is provided in the documentation.
 *
 * @see <a href="https://echarts.apache.org/en/option.html#series-line.label">SeriesLabel documentation</a>
 */
public class Label extends AbstractEnhancedLabel<Label> {

    protected Position position;

    protected String formatter;

    protected JsFunction formatterFunction;

    /**
     * Component for label position. Can contain coordinates or a predefined position type.
     */
    public static class Position {

        protected String[] coordinates;

        protected PositionType positionType;

        public Position(String horizontalPosition, String verticalPosition) {
            coordinates = new String[]{horizontalPosition, verticalPosition};
        }

        public Position(PositionType positionType) {
            this.positionType = positionType;
        }

        /**
         * Predefined position type.
         */
        public enum PositionType implements HasEnumId {
            TOP("top"),
            LEFT("left"),
            RIGHT("right"),
            BOTTOM("bottom"),
            OUTSIDE("outside"),
            CENTER("center"),
            INSIDE("inside"),
            INSIDE_LEFT("insideLeft"),
            INSIDE_RIGHT("insideRight"),
            INSIDE_TOP("insideTop"),
            INSIDE_BOTTOM("insideBottom"),
            INSIDE_TOP_LEFT("insideTopLeft"),
            INSIDE_BOTTOM_LEFT("insideBottomLeft"),
            INSIDE_TOP_RIGHT("insideTopRight"),
            INSIDE_BOTTOM_RIGHT("insideBottomRight");

            private final String id;

            PositionType(String id) {
                this.id = id;
            }

            @Override
            public String getId() {
                return id;
            }

            @Nullable
            public static PositionType fromId(String id) {
                for (PositionType at : PositionType.values()) {
                    if (at.getId().equals(id)) {
                        return at;
                    }
                }
                return null;
            }
        }

        public String[] getCoordinates() {
            return coordinates;
        }

        public PositionType getPositionType() {
            return positionType;
        }
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position.PositionType positionType) {
        this.position = new Position(positionType);
        markAsDirty();
    }

    public void setPosition(String horizontalPosition, String verticalPosition) {
        this.position = new Position(horizontalPosition, verticalPosition);
        markAsDirty();
    }

    public String getFormatter() {
        return formatter;
    }

    public void setFormatter(String formatter) {
        this.formatter = formatter;
        markAsDirty();
    }

    public JsFunction getFormatterFunction() {
        return formatterFunction;
    }

    public void setFormatterFunction(JsFunction formatterFunction) {
        this.formatterFunction = formatterFunction;
        markAsDirty();
    }

    public void setFormatterFunction(String formatterFunction) {
        this.formatterFunction = new JsFunction(formatterFunction);
        markAsDirty();
    }

    public Label withPosition(Position.PositionType positionType) {
        setPosition(positionType);
        return this;
    }

    public Label withPosition(String horizontalPosition, String verticalPosition) {
        setPosition(horizontalPosition, verticalPosition);
        return this;
    }

    public Label withFormatter(String formatter) {
        setFormatter(formatter);
        return this;
    }

    public Label withFormatterFunction(JsFunction formatterFunction) {
        setFormatterFunction(formatterFunction);
        return this;
    }

    public Label withFormatterFunction(String formatterFunction) {
        setFormatterFunction(formatterFunction);
        return this;
    }
}
