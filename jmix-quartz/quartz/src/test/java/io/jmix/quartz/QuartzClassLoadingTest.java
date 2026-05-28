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

package io.jmix.quartz;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.quartz.exception.QuartzJobSaveException;
import io.jmix.quartz.model.JobModel;
import io.jmix.quartz.service.QuartzService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                CoreConfiguration.class,
                DataConfiguration.class,
                EclipselinkConfiguration.class,
                QuartTestApplication.class
        }
)
public class QuartzClassLoadingTest {

    public static final String INITIALIZED_PROPERTY = "quartz.staticInitializerRan";

    @Autowired
    private QuartzService quartzService;

    @Autowired
    private UnconstrainedDataManager dataManager;

    @Autowired
    private Scheduler scheduler;

    @AfterEach
    void tearDown() throws Exception {
        System.clearProperty(INITIALIZED_PROPERTY);
        scheduler.deleteJob(JobKey.jobKey("securityReviewJob", "securityReviewGroup"));
    }

    @Test
    void testArbitraryJobClassNameIsLoadedBeforeServiceRejectsIt() {
        JobModel jobModel = dataManager.create(JobModel.class);
        jobModel.setJobName("securityReviewJob");
        jobModel.setJobGroup("securityReviewGroup");
        jobModel.setJobClass("test_support.QuartzStaticInitializerOnlyClass");

        Assertions.assertNull(System.getProperty(INITIALIZED_PROPERTY));

        try {
            quartzService.updateQuartzJob(jobModel, Collections.emptyList(), Collections.emptyList(), false);
        } catch (RuntimeException e) {
            Assertions.assertEquals(QuartzJobSaveException.class, e.getClass());
        }

        Assertions.assertNull(System.getProperty(INITIALIZED_PROPERTY));
    }
}
