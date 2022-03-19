/*
 * Copyright 2021 Haulmont.
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

package io.jmix.dashboards.model.visualmodel;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.dashboards.model.json.Exclude;
import io.jmix.dashboards.utils.DashboardLayoutUtils;

import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JmixEntity(name = "dshbrd_DashboardLayout")
public abstract class DashboardLayout {

    @Id
    @JmixProperty
    @JmixGeneratedValue
    protected UUID id;

    @JmixProperty
    protected List<DashboardLayout> children = new ArrayList<>();

    @JmixProperty
    @Exclude
    protected DashboardLayout parent;

    /**
     * The expand ratio of given layout in a parent layout.
     */
    @JmixProperty
    protected Integer weight = 1;

    @JmixProperty
    protected UUID expand;

    @JmixProperty
    protected String styleName;

    @JmixProperty
    protected Integer width = 100;

    @JmixProperty
    protected Integer height = 100;

    @JmixProperty
    protected String widthUnit = SizeUnit.PERCENTAGE.getId();

    @JmixProperty
    protected String heightUnit = SizeUnit.PERCENTAGE.getId();

    @JmixProperty
    @Exclude
    protected String caption;

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public List<DashboardLayout> getChildren() {
        return children;
    }

    public void setChildren(List<DashboardLayout> children) {
        this.children = children;
        this.children.forEach(child -> child.setParent(this));
    }

    public void addChild(DashboardLayout child) {
        children.add(child);
        child.setParent(this);
    }

    public void removeOwnChild(DashboardLayout child) {
        if (children.remove(child)) {
            child.setParent(null);
        }
    }

    public void removeChild(DashboardLayout child) {
        DashboardLayout parent = findParent(child);
        parent.removeOwnChild(child);
    }

    public void removeChild(UUID childId) {
        DashboardLayout parent = findParent(childId);
        parent.removeOwnChild(findLayout(childId));
    }

    public DashboardLayout findParent(DashboardLayout child) {
        return DashboardLayoutUtils.findParentLayout(this, child.getId());
    }

    public DashboardLayout findParent(UUID childId) {
        return DashboardLayoutUtils.findParentLayout(this, childId);
    }

    public DashboardLayout findLayout(UUID uuid) {
        return DashboardLayoutUtils.findLayout(this, uuid);
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public boolean isRoot() {
        return false;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public SizeUnit getWidthUnit() {
        return SizeUnit.fromId(widthUnit);
    }

    public void setWidthUnit(SizeUnit widthUnit) {
        this.widthUnit = widthUnit != null ? widthUnit.getId() : null;
    }

    public SizeUnit getHeightUnit() {
        return SizeUnit.fromId(heightUnit);
    }

    public void setHeightUnit(SizeUnit heightUnit) {
        this.heightUnit = heightUnit != null ? heightUnit.getId() : null;
    }

    public String getWidthWithUnits() {
        return getWidth() != null ? getWidth() + getWidthUnit().getId() : null;
    }

    public String getHeightWithUnits() {
        return getHeight() != null ? getHeight() + getHeightUnit().getId() : null;
    }

    public DashboardLayout getParent() {
        return parent;
    }

    public void setParent(DashboardLayout parent) {
        this.parent = parent;
    }

    public UUID getExpand() {
        return expand;
    }

    public void setExpand(UUID expand) {
        this.expand = expand;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
