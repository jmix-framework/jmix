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

package io.jmix.chartsflowui.kit.component.model;

import io.jmix.chartsflowui.kit.component.model.shared.AbstractTooltip;
import io.jmix.chartsflowui.kit.component.model.shared.TriggerOnMode;
import jakarta.annotation.Nullable;

/**
 * Tooltip component. Used to display some contextual information.
 * More detailed information is provided in the documentation.
 *
 * @see <a href="https://echarts.apache.org/en/option.html#tooltip">Tooltip documentation</a>
 */
public class Tooltip extends AbstractTooltip<Tooltip> {

    protected Boolean showContent;

    protected Boolean alwaysShowContent;

    protected TriggerOnMode triggerOn;

    protected Integer showDelay;

    protected Integer hideDelay;

    protected Boolean enterable;

    protected RenderMode renderMode;

    protected Boolean confine;

    protected Boolean appendToBody;

    protected String className;

    protected Double transitionDuration;

    protected OrderType order;

    /**
     * Render mode for tooltip.
     */
    public enum RenderMode implements HasEnumId {
        HTML("html"),
        RICH_TEXT("richText");

        private final String id;

        RenderMode(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static RenderMode fromId(String id) {
            for (RenderMode at : RenderMode.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }

            return null;
        }
    }

    /**
     * Tooltip order for multiple series.
     */
    public enum OrderType implements HasEnumId {
        SERIES_ASC("seriesAsc"),
        SERIES_DESC("seriesDesc"),
        VALUE_ASC("valueAsc"),
        VALUE_DESC("valueDesc");

        private final String id;

        OrderType(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static OrderType fromId(String id) {
            for (OrderType at : OrderType.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }

            return null;
        }
    }

    public Boolean getShowContent() {
        return showContent;
    }

    public void setShowContent(Boolean showContent) {
        this.showContent = showContent;
        markAsDirty();
    }

    public Boolean getAlwaysShowContent() {
        return alwaysShowContent;
    }

    public void setAlwaysShowContent(Boolean alwaysShowContent) {
        this.alwaysShowContent = alwaysShowContent;
        markAsDirty();
    }

    public TriggerOnMode getTriggerOn() {
        return triggerOn;
    }

    public void setTriggerOn(TriggerOnMode triggerOn) {
        this.triggerOn = triggerOn;
        markAsDirty();
    }

    public Integer getShowDelay() {
        return showDelay;
    }

    public void setShowDelay(Integer showDelay) {
        this.showDelay = showDelay;
        markAsDirty();
    }

    public Integer getHideDelay() {
        return hideDelay;
    }

    public void setHideDelay(Integer hideDelay) {
        this.hideDelay = hideDelay;
        markAsDirty();
    }

    public Boolean getEnterable() {
        return enterable;
    }

    public void setEnterable(Boolean enterable) {
        this.enterable = enterable;
        markAsDirty();
    }

    public RenderMode getRenderMode() {
        return renderMode;
    }

    public void setRenderMode(RenderMode renderMode) {
        this.renderMode = renderMode;
        markAsDirty();
    }

    public Boolean getConfine() {
        return confine;
    }

    public void setConfine(Boolean confine) {
        this.confine = confine;
        markAsDirty();
    }

    public Boolean getAppendToBody() {
        return appendToBody;
    }

    public void setAppendToBody(Boolean appendToBody) {
        this.appendToBody = appendToBody;
        markAsDirty();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
        markAsDirty();
    }

    public Double getTransitionDuration() {
        return transitionDuration;
    }

    public void setTransitionDuration(Double transitionDuration) {
        this.transitionDuration = transitionDuration;
        markAsDirty();
    }

    public OrderType getOrder() {
        return order;
    }

    public void setOrder(OrderType order) {
        this.order = order;
        markAsDirty();
    }

    public Tooltip withShowContent(Boolean showContent) {
        setShowContent(showContent);
        return this;
    }

    public Tooltip withAlwaysShowContent(Boolean alwaysShowContent) {
        setAlwaysShowContent(alwaysShowContent);
        return this;
    }

    public Tooltip withTriggerOn(TriggerOnMode triggerOn) {
        setTriggerOn(triggerOn);
        return this;
    }

    public Tooltip withShowDelay(Integer showDelay) {
        setShowDelay(showDelay);
        return this;
    }

    public Tooltip withHideDelay(Integer hideDelay) {
        setHideDelay(hideDelay);
        return this;
    }

    public Tooltip withEnterable(Boolean enterable) {
        setEnterable(enterable);
        return this;
    }

    public Tooltip withRenderMode(RenderMode renderMode) {
        setRenderMode(renderMode);
        return this;
    }

    public Tooltip withConfine(Boolean confine) {
        setConfine(confine);
        return this;
    }

    public Tooltip withAppendToBody(Boolean appendToBody) {
        setAppendToBody(appendToBody);
        return this;
    }

    public Tooltip withClassName(String className) {
        setClassName(className);
        return this;
    }

    public Tooltip withTransitionDuration(Double transitionDuration) {
        setTransitionDuration(transitionDuration);
        return this;
    }

    public Tooltip withOrder(OrderType order) {
        setOrder(order);
        return this;
    }
}
