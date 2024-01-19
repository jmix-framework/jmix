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

package io.jmix.chartsflowui.kit.component.model.datazoom;

public class InsideDataZoom extends AbstractDataZoom<InsideDataZoom> {

    protected Boolean disabled;

    protected Boolean zoomOnMouseWheel;

    protected Boolean moveOnMouseMove;

    protected Boolean moveOnMouseWheel;

    protected Boolean preventDefaultMouseMove;

    public InsideDataZoom() {
        super(DataZoomType.INSIDE);
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
        markAsDirty();
    }

    public Boolean getZoomOnMouseWheel() {
        return zoomOnMouseWheel;
    }

    public void setZoomOnMouseWheel(Boolean zoomOnMouseWheel) {
        this.zoomOnMouseWheel = zoomOnMouseWheel;
        markAsDirty();
    }

    public Boolean getMoveOnMouseMove() {
        return moveOnMouseMove;
    }

    public void setMoveOnMouseMove(Boolean moveOnMouseMove) {
        this.moveOnMouseMove = moveOnMouseMove;
        markAsDirty();
    }

    public Boolean getMoveOnMouseWheel() {
        return moveOnMouseWheel;
    }

    public void setMoveOnMouseWheel(Boolean moveOnMouseWheel) {
        this.moveOnMouseWheel = moveOnMouseWheel;
        markAsDirty();
    }

    public Boolean getPreventDefaultMouseMove() {
        return preventDefaultMouseMove;
    }

    public void setPreventDefaultMouseMove(Boolean preventDefaultMouseMove) {
        this.preventDefaultMouseMove = preventDefaultMouseMove;
        markAsDirty();
    }

    public InsideDataZoom withDisabled(Boolean disabled) {
        setDisabled(disabled);
        return this;
    }

    public InsideDataZoom withZoomOnMouseWheel(Boolean zoomOnMouseWheel) {
        setZoomOnMouseWheel(zoomOnMouseWheel);
        return this;
    }

    public InsideDataZoom withMoveOnMouseMove(Boolean moveOnMouseMove) {
        setMoveOnMouseMove(moveOnMouseMove);
        return this;
    }

    public InsideDataZoom withMoveOnMouseWheel(Boolean moveOnMouseWheel) {
        setMoveOnMouseWheel(moveOnMouseWheel);
        return this;
    }

    public InsideDataZoom withPreventDefaultMouseMove(Boolean preventDefaultMouseMove) {
        setPreventDefaultMouseMove(preventDefaultMouseMove);
        return this;
    }
}
