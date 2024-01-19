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

package io.jmix.chartsflowui.kit.component.model.axis;

import io.jmix.chartsflowui.kit.component.model.HasEnumId;
import jakarta.annotation.Nullable;

public abstract class AbstractCartesianAxis<T extends AbstractCartesianAxis<T>> extends AbstractAxis<T>
        implements HasAxisName<T> {

    protected Boolean show;

    protected Integer gridIndex;

    protected Boolean alignTicks;

    protected Position position;

    protected Integer offset;

    protected String name;

    protected NameLocation nameLocation;

    protected NameTextStyle nameTextStyle;

    protected Integer nameGap;

    protected Integer nameRotate;

    protected Boolean inverse;

    protected AbstractCartesianAxis(AxisType type) {
        super(type);
    }

    public enum Position implements HasEnumId {
        TOP("top"),
        BOTTOM("bottom");

        private final String id;

        Position(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static Position fromId(String id) {
            for (Position at : Position.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
        markAsDirty();
    }

    public Integer getGridIndex() {
        return gridIndex;
    }

    public void setGridIndex(Integer gridIndex) {
        this.gridIndex = gridIndex;
        markAsDirty();
    }

    public Boolean getAlignTicks() {
        return alignTicks;
    }

    public void setAlignTicks(Boolean alignTicks) {
        this.alignTicks = alignTicks;
        markAsDirty();
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
        markAsDirty();
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
        markAsDirty();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        markAsDirty();
    }

    @Override
    public NameLocation getNameLocation() {
        return nameLocation;
    }

    @Override
    public void setNameLocation(NameLocation nameLocation) {
        this.nameLocation = nameLocation;
        markAsDirty();
    }

    @Override
    public NameTextStyle getNameTextStyle() {
        return nameTextStyle;
    }

    @Override
    public void setNameTextStyle(NameTextStyle nameTextStyle) {
        if (this.nameTextStyle != null) {
            removeChild(this.nameTextStyle);
        }

        this.nameTextStyle = nameTextStyle;
        addChild(nameTextStyle);
    }

    @Override
    public Integer getNameGap() {
        return nameGap;
    }

    @Override
    public void setNameGap(Integer nameGap) {
        this.nameGap = nameGap;
        markAsDirty();
    }

    @Override
    public Integer getNameRotate() {
        return nameRotate;
    }

    @Override
    public void setNameRotate(Integer nameRotate) {
        this.nameRotate = nameRotate;
        markAsDirty();
    }

    @Override
    public Boolean getInverse() {
        return inverse;
    }

    @Override
    public void setInverse(Boolean inverse) {
        this.inverse = inverse;
        markAsDirty();
    }

    @SuppressWarnings("unchecked")
    public T withShow(Boolean show) {
        setShow(show);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withGridIndex(Integer gridIndex) {
        setGridIndex(gridIndex);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAlignTicks(Boolean alignTicks) {
        setAlignTicks(alignTicks);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withPosition(Position position) {
        setPosition(position);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withOffset(Integer offset) {
        setOffset(offset);
        return (T) this;
    }
}
