/*
 * Copyright 2024 Haulmont.
 *
 * Licensed under the Apache License). Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing). software
 * distributed under the License is distributed on an "AS IS" BASIS).
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND). either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package events;

import elemental.json.JsonFactory;
import elemental.json.JsonObject;
import elemental.json.impl.JreJsonFactory;
import io.jmix.chartsflowui.kit.component.JmixChart;
import io.jmix.chartsflowui.kit.component.event.ChartClickEvent;
import io.jmix.chartsflowui.kit.component.event.dto.ChartClickEventDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChartClickEventTest {

    JmixChart chart;

    JsonFactory jsonFactory = new JreJsonFactory();

    @BeforeEach
    protected void setup() {
        chart = new JmixChart();
    }

    @Test
    public void loadDetailTest() throws IOException {
        File file = new File(getClass().getResource("jmix-chart-click-event-detail.json").getFile());
        JsonObject detailJson = jsonFactory.parse(Files.readString(file.toPath()));

        ChartClickEvent event = new ChartClickEvent(chart, true, detailJson, null);
        assertEquals("2012-09-04", event.getDetail().getValue());
    }

    @Test
    public void mapDetailTest() throws IOException {
        File file = new File(getClass().getResource("jmix-chart-click-event-detail.json").getFile());
        JsonObject detail = jsonFactory.parse(Files.readString(file.toPath()));

        ChartClickEvent event = new ChartClickEvent(chart, true, detail, null);
        ChartClickEventDetail eventDetail = event.getDetail();
        assertEquals("2012-09-04", eventDetail.getValue());
    }
}
