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

public class RadiusAxis extends AbstractPolarAxis<RadiusAxis> implements HasAxisName<RadiusAxis> {

    protected String name;

    protected NameLocation nameLocation;

    protected NameTextStyle nameTextStyle;

    protected Integer nameGap;

    protected Integer nameRotate;

    protected Boolean inverse;

    public RadiusAxis() {
        super(AxisType.VALUE);
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
}
