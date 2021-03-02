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

package io.jmix.charts.widget.client.amcharts.rpc;

import com.vaadin.shared.communication.ClientRpc;

import java.util.Date;

public interface JmixAmchartsSceneClientRpc extends ClientRpc {

    void draw(String chartJson);

    void updatePoints(String json);

    void zoomOut();

    void zoomToIndexes(int start, int end);

    void zoomToDates(Date start, Date end);

    void zoomOutValueAxes();

    void zoomOutValueAxisById(String id);

    void zoomOutValueAxisByIndex(int index);

    void zoomValueAxisToValuesById(String id, String startValue, String endValue);

    void zoomValueAxisToValuesByIndex(int index, String startValue, String endValue);

    void zoomValueAxisToDatesById(String id, Date start, Date end);

    void zoomValueAxisToDatesByIndex(int index, Date start, Date end);
}