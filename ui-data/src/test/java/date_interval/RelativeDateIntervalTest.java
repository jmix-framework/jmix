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

package date_interval;

import date_interval.screen.RelativeDateIntervalTestScreen;
import io.jmix.core.CoreConfiguration;
import io.jmix.core.DataManager;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.ui.Screens;
import io.jmix.ui.UiConfiguration;
import io.jmix.ui.app.propertyfilter.dateinterval.model.RelativeDateInterval;
import io.jmix.ui.testassist.junit.UiTest;
import io.jmix.uidata.UiDataConfiguration;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import test_support.UiDataTestConfiguration;
import test_support.entity.Project;

import java.util.Date;

import static io.jmix.ui.app.propertyfilter.dateinterval.model.RelativeDateInterval.Operation.GREATER_OR_EQUAL;
import static org.junit.jupiter.api.Assertions.assertEquals;

@UiTest(screenBasePackages = {"date_interval.screen", "io.jmix.ui.testassist.app.main"}, mainScreenId = "testMainScreen")
@ContextConfiguration(classes = {CoreConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class,
        UiConfiguration.class, UiDataConfiguration.class, UiDataTestConfiguration.class})
public class RelativeDateIntervalTest {

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        Date currentDate = new Date();
        Date nextDate = DateUtils.addMonths(currentDate, -1);

        Project project1 = dataManager.create(Project.class);
        project1.setStartDate(currentDate);

        Project project2 = dataManager.create(Project.class);
        project2.setStartDate(nextDate);

        dataManager.save(project1, project2);
    }

    @AfterEach
    void cleanup() {
        jdbcTemplate.update("delete from TEST_UIDATA_PROJECT");
    }

    @Test
    protected void relativeDateIntervalTest(Screens screens) {
        RelativeDateIntervalTestScreen screen = screens.create(RelativeDateIntervalTestScreen.class);
        screen.show();

        assertEquals(2, screen.getItems().size());

        screen.dateFilter.setValue(new RelativeDateInterval(GREATER_OR_EQUAL, "START_OF_YESTERDAY"));
        screen.dateFilter.apply();

        assertEquals(1, screen.getItems().size());
    }
}
