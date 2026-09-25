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
import io.jmix.quartz.model.JobDataParameterModel;
import io.jmix.quartz.model.JobModel;
import io.jmix.quartz.model.ScheduleType;
import io.jmix.quartz.model.TriggerModel;
import io.jmix.quartz.service.QuartzService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.quartz.autoconfigure.QuartzProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Verifies that a transactional job save does not deadlock with the scheduler polling thread
 * while another job is actively firing (see the deadlock note in plan item A4: an in-JVM
 * TRIGGER_ACCESS semaphore combined with uncommitted job store writes hangs the application
 * on databases where writers block readers, such as HSQLDB).
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                CoreConfiguration.class,
                DataConfiguration.class,
                EclipselinkConfiguration.class,
                QuartTestApplication.class
        }
)
//the job store properties are contributed by QuartzEnvironmentPostProcessor in real applications,
//environment post-processors do not run for plain @ContextConfiguration tests;
//JMX is disabled because this context is the second one in the test JVM and MBean names would collide
@TestPropertySource(properties = {
        "spring.quartz.properties.org.quartz.jobStore.class=io.jmix.quartz.impl.JmixLocalDataSourceJobStore",
        "spring.quartz.properties.org.quartz.jobStore.useDBLocks=true",
        "spring.jmx.enabled=false"})
public class QuartzJobStoreLockingTest {

    static final String JOB_GROUP = "lockingGroup";
    static final String TRIGGER_GROUP = "lockingTriggerGroup";

    @Autowired
    private QuartzService quartzService;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private UnconstrainedDataManager dataManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private QuartzProperties quartzProperties;

    @AfterEach
    void tearDown() throws SchedulerException {
        for (String jobName : List.of("lockingBusyJob", "lockingJobA", "lockingJobB")) {
            scheduler.deleteJob(JobKey.jobKey(jobName, JOB_GROUP));
        }
    }

    @Test
    void testDbLockingPropertyApplied() {
        Assertions.assertEquals("true", quartzProperties.getProperties().get("org.quartz.jobStore.useDBLocks"),
                "actual quartz properties: " + quartzProperties.getProperties());
    }

    @Test
    void testTransactionalRenameWhileAnotherJobIsFiring() throws Exception {
        //a job firing every 100 ms keeps the scheduler thread constantly acquiring triggers
        JobModel busyJob = buildJobModel("lockingBusyJob");
        quartzService.updateQuartzJob(busyJob, List.of(), List.of(buildBusyTriggerModel()), false);

        //the job being renamed is paused and its trigger never fires
        JobModel jobModel = buildJobModel("lockingJobA");
        quartzService.updateQuartzJob(jobModel, List.of(), List.of(buildFarFutureTriggerModel()), false);
        quartzService.pauseJob("lockingJobA", JOB_GROUP);

        //the rename flow of JobModelDetailView: delete under the old key and update in one transaction
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        CompletableFuture<Void> save = CompletableFuture.runAsync(() -> {
            String oldName = "lockingJobA";
            String newName = "lockingJobB";
            for (int i = 0; i < 300; i++) {
                String from = oldName;
                String to = newName;
                transactionTemplate.executeWithoutResult(status -> {
                    quartzService.deleteJob(from, JOB_GROUP);
                    JobModel renamed = buildJobModel(to);
                    quartzService.updateQuartzJob(renamed, List.<JobDataParameterModel>of(),
                            List.of(buildFarFutureTriggerModel()), true);
                });
                String swap = oldName;
                oldName = newName;
                newName = swap;
            }
        });

        try {
            save.get(60, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            //the deadlock cannot be broken from within the JVM (even a database shutdown blocks on the stuck
            //session), so save the evidence and halt - otherwise the test run hangs forever on context close
            reportDeadlockAndHalt();
        }
    }

    private void reportDeadlockAndHalt() {
        String message = "The save transaction deadlocked with the scheduler thread";
        String threadDump = buildThreadDump();
        try {
            Path dumpFile = Path.of(System.getProperty("java.io.tmpdir"), "quartz-locking-test-deadlock.txt");
            Files.writeString(dumpFile, threadDump);
            System.err.println(message + ", thread dump: " + dumpFile);
        } catch (IOException ioException) {
            System.err.println(message + ":\n" + threadDump);
        }
        Runtime.getRuntime().halt(1);
    }

    private JobModel buildJobModel(String jobName) {
        JobModel jobModel = dataManager.create(JobModel.class);
        jobModel.setJobName(jobName);
        jobModel.setJobGroup(JOB_GROUP);
        jobModel.setJobClass(QuartTestApplication.MyQuartzJob.class.getName());
        return jobModel;
    }

    private TriggerModel buildBusyTriggerModel() {
        TriggerModel triggerModel = dataManager.create(TriggerModel.class);
        triggerModel.setTriggerName("lockingBusyTrigger");
        triggerModel.setTriggerGroup(TRIGGER_GROUP);
        triggerModel.setScheduleType(ScheduleType.SIMPLE);
        triggerModel.setRepeatInterval(100L);
        return triggerModel;
    }

    private TriggerModel buildFarFutureTriggerModel() {
        TriggerModel triggerModel = dataManager.create(TriggerModel.class);
        triggerModel.setTriggerName("lockingRenamedTrigger");
        triggerModel.setTriggerGroup(TRIGGER_GROUP);
        triggerModel.setScheduleType(ScheduleType.CRON_EXPRESSION);
        triggerModel.setCronExpression("0 0 0 * * ?");
        triggerModel.setStartDate(Date.from(LocalDateTime.now().plus(1, ChronoUnit.HOURS)
                .atZone(ZoneId.systemDefault()).toInstant()));
        return triggerModel;
    }

    private String buildThreadDump() {
        return Arrays.stream(ManagementFactory.getThreadMXBean().dumpAllThreads(true, true))
                .map(ThreadInfo::toString)
                .collect(Collectors.joining());
    }
}
