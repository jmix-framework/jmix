/*
 * Copyright 2022 Haulmont.
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

package test_support.listeners;

import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.event.EntityChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import test_support.entity.event_clearing.Project;
import test_support.entity.event_clearing.Task;

@Component("test_TaskChangedListener")
public class TestTaskChangedListener {

    private DataManager dataManager;

    public TestTaskChangedListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }


    @EventListener
    public void onTaskChangedEvent(EntityChangedEvent<Task> event) {
        Project project;
        if (event.getType() == EntityChangedEvent.Type.DELETED) {
            Id<Object> projectId = event.getChanges().getOldReferenceId("project");
            project = (Project) dataManager.load(projectId).one();
        } else {
            Task task = dataManager.load(event.getEntityId()).one();
            project = task.getProject();
        }

        Integer estimatedEfforts = project.getTasks().stream()
                .mapToInt(task -> task.getEstimatedEfforts() == null ? 0 : task.getEstimatedEfforts())
                .sum();

        project.setTotalEstimatedEfforts(estimatedEfforts);

        dataManager.save(project);//causes stackoverflow if holder.accumulatedList is not cleared at io.jmix.eclipselink.impl.EntityChangedEventManager.collect:124
    }

}