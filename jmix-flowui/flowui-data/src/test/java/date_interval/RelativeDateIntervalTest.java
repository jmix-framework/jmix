/*
 * Copyright 2024 Haulmont.
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

import date_interval.view.RelativeDateIntervalTestView;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowuidata.dateinterval.model.RelativeDateInterval;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import test_support.FlowuiDataTestConfiguration;
import test_support.entity.Project;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UiTest(viewBasePackages = {"date_interval.view", "test_support.view"})
@SpringBootTest(classes = {FlowuiDataTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class RelativeDateIntervalTest {

    @Autowired
    protected SystemAuthenticator authenticator;
    @Autowired
    DataManager dataManager;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    ViewNavigators viewNavigators;

    @BeforeEach
    void setup() {
        Date currentDate = new Date();
        Date nextDate = DateUtils.addMonths(currentDate, -1);

        Project firstProject = dataManager.create(Project.class);
        firstProject.setStartDate(currentDate);

        Project secondsProject = dataManager.create(Project.class);
        secondsProject.setStartDate(nextDate);


        dataManager.save(firstProject, secondsProject);
    }

    @AfterEach
    void cleanup() {
        jdbcTemplate.update("delete from TEST_PROJECT");
    }

    @Test
    @DisplayName("Apply propertyFilter with RelativeDateInterval")
    protected void relativeDateIntervalTest() {
        viewNavigators.view(RelativeDateIntervalTestView.class).navigate();
        RelativeDateIntervalTestView relativeDateIntervalTestView = UiTestUtils.getCurrentView();

        assertEquals(2, relativeDateIntervalTestView.getItems().size());

        RelativeDateInterval startOfYesterdayRelativeDateInterval = new RelativeDateInterval(
                RelativeDateInterval.Operation.GREATER_OR_EQUAL,
                "START_OF_YESTERDAY"
        );
        PropertyFilter<? super RelativeDateInterval> propertyFilter = relativeDateIntervalTestView.dateFilter;
        propertyFilter.getValueComponent().setValue(startOfYesterdayRelativeDateInterval);
        propertyFilter.apply();

        assertEquals(1, relativeDateIntervalTestView.getItems().size());
    }
}
