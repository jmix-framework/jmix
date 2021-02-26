/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.core;

import com.haulmont.cuba.core.app.SchedulingService;
import com.haulmont.cuba.core.entity.ScheduledExecution;
import com.haulmont.cuba.core.entity.ScheduledTask;
import com.haulmont.cuba.core.entity.ScheduledTaskDefinedBy;
import com.haulmont.cuba.core.entity.SchedulingType;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.model.service.TestingService;
import com.haulmont.cuba.core.testsupport.CoreTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@CoreTest
@Tag("slowTests")
public class SchedulingTest {
    public static final String SERVER1 = "localhost:8080/cuba";

    @Autowired
    private SchedulingService schedulingService;

    @Autowired
    private TestingService testingService;

    @Autowired
    private TransactionalDataManager transactionalDataManager;

    @Test
    public void testTask() {
        clearScheduledTasks();

        ScheduledTask task = new ScheduledTask();
        task.setDefinedBy(ScheduledTaskDefinedBy.BEAN);
        task.setBeanName("cuba_TestingService");
        task.setMethodName("execute");
        task.setActive(true);
        task.setPeriod(5);
        task.setLogStart(true);
        task.setLogFinish(true);

        saveTask(task);

        runSchedulingFor(7);

        List<ScheduledExecution> list = getExecutionsList(task);
        assertEquals("Wrong number of executions: expected = 2, actual = " + list.size(), 2, list.size());
    }

    @Test
    public void testTaskCron() {

        clearScheduledTasks();

        ScheduledTask task = new ScheduledTask();
        task.setDefinedBy(ScheduledTaskDefinedBy.BEAN);
        task.setBeanName("cuba_TestingService");
        task.setMethodName("execute");
        task.setActive(true);
        task.setSchedulingType(SchedulingType.CRON);
        task.setCron("*/5 * * * * *");
        task.setLogStart(true);
        task.setLogFinish(true);

        saveTask(task);

        runSchedulingFor(7);

        List<ScheduledExecution> list = getExecutionsList(task);
        int executions = list.size();
        System.out.println(executions + " executions");
        assertTrue("Wrong number of executions: expected >= 1 and <= 2, actual =  " + executions, executions >= 1 && executions <= 2);

        runSchedulingFor(5);
        list = getExecutionsList(task);
        int executions2 = list.size();
        assertTrue("Wrong number of executions " + executions, executions2 >= executions + 1 && executions2 <= executions + 2);
    }

    @Test
    public void testTaskWithStartDate() {

        clearScheduledTasks();

        ScheduledTask task = new ScheduledTask();
        task.setDefinedBy(ScheduledTaskDefinedBy.BEAN);
        task.setBeanName("cuba_TestingService");
        task.setMethodName("execute");
        task.setActive(true);
        task.setPeriod(5);
        task.setStartDate(new Date(System.currentTimeMillis() + 3000));
        task.setLogStart(true);
        task.setLogFinish(true);

        saveTask(task);

        runSchedulingFor(15);

        List<ScheduledExecution> list = getExecutionsList(task);
        assertEquals("Wrong number of executions: expected = 3, actual = " + list.size(), 3, list.size());
    }


    @Test
    public void testFixedDelayTaskExecution() {
        clearScheduledTasks();
        ScheduledTask task = new ScheduledTask();
        task.setDefinedBy(ScheduledTaskDefinedBy.BEAN);
        task.setBeanName("cuba_TestingService");
        task.setMethodName("longRunningTask");
        task.setSchedulingType(SchedulingType.FIXED_DELAY);
        task.setActive(true);
        task.setPeriod(10);

        saveTask(task);

        schedulingService.setActive(true);

        sleep(40);
        schedulingService.setActive(false);

        List<ScheduledExecution> list = getExecutionsList(task);
        assertEquals("Wrong number of executions: : expected = 4, actual = " + list.size(), 4, list.size());
    }

    @Test
    public void testSingletonTaskFailover() {
        clearScheduledTasks();

        ScheduledTask task = new ScheduledTask();
        task.setDefinedBy(ScheduledTaskDefinedBy.BEAN);
        task.setBeanName("cuba_TestingService");
        task.setMethodName("execute");
        task.setActive(true);
        task.setPeriod(5);
        task.setSingleton(true);
        task.setTimeout(3);
        task.setPermittedServers(SERVER1);
        task.setLogStart(true);
        task.setLogFinish(true);

        saveTask(task);


        schedulingService.setActive(true);
        sleep(12);


        Date failTime = new Date();

        sleep(10);
        schedulingService.setActive(false);

        List<ScheduledExecution> list = getExecutionsList(task);
        assertTrue("Wrong number of executions: expected > 3, actual = " + list.size(), list.size() > 3);
        assertTrue("No executions after fail", list.get(list.size() - 1).getStartTime().compareTo(failTime) > 0);
    }

    private void saveTask(ScheduledTask task) {
        transactionalDataManager.save(task);
    }

    private void runSchedulingFor(int sec) {
        schedulingService.setActive(true);
        sleep(sec);
        schedulingService.setActive(false);
    }

    private List<ScheduledExecution> getExecutionsList(ScheduledTask task) {
        LoadContext loadContext = new LoadContext(ScheduledExecution.class);
        loadContext.setQueryString("select e from sys$ScheduledExecution e where e.task.id = :taskId order by e.startTime")
                .setParameter("taskId", task.getId());
        return transactionalDataManager.loadList(loadContext);
    }

    private void clearScheduledTasks() {
        testingService.clearScheduledTasks();
    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(seconds));
        } catch (InterruptedException e1) {
            throw new RuntimeException(e1);
        }
    }
}