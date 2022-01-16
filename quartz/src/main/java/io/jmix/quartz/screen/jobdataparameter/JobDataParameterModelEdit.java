package io.jmix.quartz.screen.jobdataparameter;

import io.jmix.quartz.model.JobDataParameterModel;
import io.jmix.ui.screen.EditedEntityContainer;
import io.jmix.ui.screen.StandardEditor;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;

@UiController("JobDataParameterModel.edit")
@UiDescriptor("job-data-parameter-model-edit.xml")
@EditedEntityContainer("jobDataParameterModelDc")
public class JobDataParameterModelEdit extends StandardEditor<JobDataParameterModel> {
}