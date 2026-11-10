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

package entity_inspector;

import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.datatoolsflowui.view.entityinspector.EntityInspectorDetailView;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import test_support.EntityInspectorUiTestConfiguration;
import test_support.TestFullAccessUiAuthenticator;
import test_support.entity.InspectorLobEntity;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UiTest(authenticator = TestFullAccessUiAuthenticator.class)
@SpringBootTest(classes = EntityInspectorUiTestConfiguration.class)
public class EntityInspectorLobFieldTest {

    @Autowired
    DataManager dataManager;
    @Autowired
    Metadata metadata;
    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    UrlParamSerializer urlParamSerializer;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanup() {
        jdbcTemplate.update("delete from TEST_INSPECTOR_LOB_ENTITY");
    }

    @Test
    void testLobFieldWithManyLinesHasLimitedHeight() {
        InspectorLobEntity entity = metadata.create(InspectorLobEntity.class);
        entity.setDescription(IntStream.rangeClosed(1, 20)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining("\n")));
        dataManager.save(entity);

        EntityInspectorDetailView view = navigateToInspector(entity);

        List<JmixTextArea> textAreas = UiComponentUtils.getComponents(view).stream()
                .filter(JmixTextArea.class::isInstance)
                .map(JmixTextArea.class::cast)
                .toList();
        assertEquals(1, textAreas.size());
        assertEquals("9.5em", textAreas.get(0).getMaxHeight());
    }

    private EntityInspectorDetailView navigateToInspector(InspectorLobEntity entity) {
        String entityName = metadata.getClass(InspectorLobEntity.class).getName();
        RouteParameters routeParameters = new RouteParameters(ImmutableMap.of(
                EntityInspectorDetailView.ROUTE_PARAM_NAME, urlParamSerializer.serialize(entityName),
                EntityInspectorDetailView.ROUTE_PARAM_ID, urlParamSerializer.serialize(entity.getId())
        ));
        navigationSupport.navigate(EntityInspectorDetailView.class, routeParameters);

        return UiTestUtils.getCurrentView();
    }
}
