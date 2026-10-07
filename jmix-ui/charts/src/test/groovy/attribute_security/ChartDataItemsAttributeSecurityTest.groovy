/*
 * Copyright 2026 Haulmont.
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

package attribute_security

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import io.jmix.charts.model.chart.impl.AbstractChart
import io.jmix.charts.model.chart.impl.GanttChartModelImpl
import io.jmix.charts.model.chart.impl.SerialChartModelImpl
import io.jmix.charts.serialization.ChartDataItemsSerializer
import io.jmix.core.AccessConstraintsRegistry
import io.jmix.core.AccessLogger
import io.jmix.core.AccessManager
import io.jmix.core.Metadata
import io.jmix.core.MetadataTools
import io.jmix.core.accesscontext.EntityAttributeContext
import io.jmix.core.constraint.AccessConstraint
import io.jmix.core.entity.KeyValueEntity
import io.jmix.core.impl.keyvalue.KeyValueMetaClass
import io.jmix.core.impl.keyvalue.KeyValueMetaProperty
import io.jmix.core.metamodel.model.MetaProperty
import io.jmix.core.metamodel.model.Range
import io.jmix.ui.data.impl.EntityDataItem
import io.jmix.ui.data.impl.ListDataProvider
import serialization.TestChartSerializer
import spock.lang.Specification

class ChartDataItemsAttributeSecurityTest extends Specification {

    Set<String> deniedViewProperties = []

    TestChartSerializer chartSerializer

    def setup() {
        def registry = new AccessConstraintsRegistry()
        registry.register(new AccessConstraint<EntityAttributeContext>() {
            @Override
            Class<EntityAttributeContext> getContextType() {
                return EntityAttributeContext
            }

            @Override
            void applyTo(EntityAttributeContext context) {
                if (deniedViewProperties.contains(context.propertyPath.toString())) {
                    context.setViewDenied()
                }
            }
        })

        def accessManager = new AccessManager()
        accessManager.@registry = registry
        accessManager.@accessLogger = Stub(AccessLogger)

        def itemsSerializer = new ChartDataItemsSerializer()
        itemsSerializer.metadata = Stub(Metadata) {
            getClass(_) >> { KeyValueEntity entity -> entity.instanceMetaClass }
        }
        itemsSerializer.metadataTools = new MetadataTools()
        itemsSerializer.accessManager = accessManager

        chartSerializer = new TestChartSerializer()
        chartSerializer.@itemsSerializer = itemsSerializer
    }

    def "Values of entity attributes the user cannot view are not sent"() {
        given:
        deniedViewProperties << "value"

        def item = createEntity(category: "Sky", value: 75, count: 3)
        def chart = new SerialChartModelImpl()
                .setCategoryField("category")
                .setAdditionalFields(["value", "count"])
                .setDataProvider(new ListDataProvider([new EntityDataItem(item)]))

        when:
        def row = serializeDataProvider(chart).get(0).asJsonObject

        then:
        row.get("category").asString == "Sky"
        !row.has("value")
        row.get("count").asInt == 3
    }

    def "Gantt segment values of entity attributes the user cannot view are not sent"() {
        given:
        deniedViewProperties << "duration"

        def chart = createGanttChart()

        when:
        def segment = serializeDataProvider(chart).get(0).asJsonObject
                .getAsJsonArray("segments").get(0).asJsonObject

        then:
        segment.get("start").asInt == 1
        !segment.has("duration")
    }

    def "Gantt segments are not sent if the user cannot view the segments attribute"() {
        given:
        deniedViewProperties << "segments"

        def chart = createGanttChart()

        when:
        def row = serializeDataProvider(chart).get(0).asJsonObject

        then:
        row.get("category").asString == "Task"
        row.getAsJsonArray("segments").isEmpty()
    }

    protected GanttChartModelImpl createGanttChart() {
        def segment = createEntity(start: 1, duration: 2)
        def task = createEntity(category: "Task", segments: [segment])

        return new GanttChartModelImpl()
                .setCategoryField("category")
                .setSegmentsField("segments")
                .setStartField("start")
                .setDurationField("duration")
                .setDataProvider(new ListDataProvider([new EntityDataItem(task)]))
    }

    protected JsonArray serializeDataProvider(AbstractChart chart) {
        String json = chartSerializer.serialize(chart)
        return JsonParser.parseString(json).asJsonObject.getAsJsonArray("dataProvider")
    }

    protected KeyValueEntity createEntity(Map<String, Object> values) {
        def entityMetaClass = new KeyValueMetaClass()
        values.keySet().each { name ->
            entityMetaClass.addProperty(
                    new KeyValueMetaProperty(entityMetaClass, name, Object, Stub(Range), MetaProperty.Type.DATATYPE))
        }

        def entity = new KeyValueEntity()
        entity.instanceMetaClass = entityMetaClass
        values.each { name, value -> entity.setValue(name, value) }
        return entity
    }
}
