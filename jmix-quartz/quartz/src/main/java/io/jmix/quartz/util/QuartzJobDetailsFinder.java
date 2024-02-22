package io.jmix.quartz.util;

import io.jmix.core.JmixKindBean;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component("quartz_QuartzJobDetailsFinder")
public class QuartzJobDetailsFinder {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Returns {@link JobKey}'s of {@link JobDetail} which registered in codebase as Spring beans. Such {@link JobDetail}s considered
     * as "predefined" and cannot be removed from the Quartz engine via user interface.
     *
     * @see io.jmix.quartz.model.JobSource
     */
    public List<JobKey> getJobDetailBeanKeys() {
        return applicationContext.getBeansOfType(JobDetail.class)
                .values().stream()
                .map(JobDetail::getKey)
                .collect(Collectors.toList());
    }

    public Set<String> getPredefinedJobKeys() {
        return applicationContext.getBeansOfType(JmixKindBean.class)
                .values().stream()
                .filter(p -> ((JmixKindBean) p).getKind().equals("predefinedJobs"))
                .map(JmixKindBean::getValue)
                .collect(Collectors.toSet());
    }

}
