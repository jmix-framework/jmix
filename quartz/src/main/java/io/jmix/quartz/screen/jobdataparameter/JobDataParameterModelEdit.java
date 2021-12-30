package io.jmix.quartz.screen.jobdataparameter;

import io.jmix.ui.screen.*;
import io.jmix.quartz.model.JobDataParameterModel;

@UiController("JobDataParameterModel.edit")
@UiDescriptor("job-data-parameter-model-edit.xml")
@EditedEntityContainer("jobDataParameterModelDc")
public class JobDataParameterModelEdit extends StandardEditor<JobDataParameterModel> {
}