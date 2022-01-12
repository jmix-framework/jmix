package io.jmix.quartz.util;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class QuartzJobDetailsFinder {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Returns keys of {@link JobDetail} which registered in codebase as Spring beans
     */
    public List<JobKey> getJobDetailBeanKeys() {
        return applicationContext.getBeansOfType(JobDetail.class)
                .values().stream()
                .map(JobDetail::getKey)
                .collect(Collectors.toList());
    }

}
