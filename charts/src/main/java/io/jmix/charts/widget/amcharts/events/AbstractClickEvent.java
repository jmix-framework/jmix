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

package io.jmix.charts.widget.amcharts.events;


import io.jmix.charts.widget.amcharts.JmixAmchartsScene;

public abstract class AbstractClickEvent extends com.vaadin.ui.Component.Event {

    private static final long serialVersionUID = -549144399958392892L;

    private final int x;
    private final int y;
    private final int absoluteX;
    private final int absoluteY;

    public AbstractClickEvent(JmixAmchartsScene scene, int x, int y, int absoluteX, int absoluteY) {
        super(scene);
        this.x = x;
        this.y = y;
        this.absoluteX = absoluteX;
        this.absoluteY = absoluteY;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getAbsoluteX() {
        return absoluteX;
    }

    public int getAbsoluteY() {
        return absoluteY;
    }
}
