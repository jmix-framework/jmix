/*
 * Copyright 2022 Haulmont.
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

import elemental.json.JsonFactory;
import elemental.json.JsonObject;
import elemental.json.impl.JreJsonFactory;
import io.jmix.chartsflowui.kit.component.JmixChart;
import io.jmix.chartsflowui.kit.component.event.JmixChartClickEvent;
import io.jmix.chartsflowui.kit.component.event.dto.JmixChartClickEventDetail;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JmixChartClickEventTest {

    @Test
    public void loadDetailTest() throws IOException {
        JmixChart chart = new JmixChart();
        JsonFactory jsonFactory = new JreJsonFactory();
        File file = new File(getClass().getResource("jmix-chart-click-event-detail.json").getFile());
        JsonObject detail = jsonFactory.parse(Files.readString(file.toPath()));


        JmixChartClickEvent event = new JmixChartClickEvent(chart, true, detail, "0");
        assertEquals("2012-09-04", event.loadDetail().getValue());
    }

    @Test
    public void mapDetailTest() throws IOException {
        JmixChart chart = new JmixChart();
        JsonFactory jsonFactory = new JreJsonFactory();
        File file = new File(getClass().getResource("jmix-chart-click-event-detail.json").getFile());
        JsonObject detail = jsonFactory.parse(Files.readString(file.toPath()));


        JmixChartClickEvent event = new JmixChartClickEvent(chart, true, detail, "0");
        JmixChartClickEventDetail eventDetail = event.mapDetail(JmixChartClickEventDetail.class);
        assertEquals("2012-09-04", eventDetail.getValue());
    }

    @Test
    public void mapTestDetailTest() throws IOException {
        JmixChart chart = new JmixChart();
        JsonFactory jsonFactory = new JreJsonFactory();
        File file = new File(getClass().getResource("jmix-chart-test-event-detail.json").getFile());
        JsonObject detail = jsonFactory.parse(Files.readString(file.toPath()));


        JmixChartClickEvent event = new JmixChartClickEvent(chart, true, detail, "0");
        JmixChartTestEventDetail eventDetail = event.mapDetail(JmixChartTestEventDetail.class);
        assertEquals("bar", eventDetail.getTestMap().get("foo"));
        assertEquals(4, eventDetail.getNumbers().get(1));
        assertEquals(false, eventDetail.getTestDTO().getBoolField());
    }
}
