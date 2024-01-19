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

package io.jmix.chartsflowui.kit.component.model.series.mark;

public class Coordinate {

    protected Double[] numberCoordinates;

    protected String[] stringCoordinates;

    public Coordinate(Double... numberCoordinates) {
        this.numberCoordinates = numberCoordinates;
    }

    public Coordinate(String... stringCoordinates) {
        this.stringCoordinates = stringCoordinates;
    }

    public Double[] getNumberCoordinates() {
        return numberCoordinates;
    }

    public String[] getStringCoordinates() {
        return stringCoordinates;
    }
}
