package io.jmix.quartz.util;

import io.jmix.core.impl.scanning.ClasspathScanCandidateDetector;
import io.jmix.core.impl.scanning.JmixModulesClasspathScanner;
import org.apache.commons.collections4.CollectionUtils;
import org.quartz.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component("quartz_QuartzJobClassFinder")
public class QuartzJobClassFinder {

    @Autowired
    private JmixModulesClasspathScanner classpathScanner;

    private List<String> quartzJobClassNames;

    /**
     * Returns FQN of classes implementing {@code org.quartz.Job} interface
     */
    public List<String> getQuartzJobClassNames() {
        if (CollectionUtils.isEmpty(quartzJobClassNames)) {
            quartzJobClassNames = new ArrayList<>(classpathScanner.getClassNames(QuartzJobDetector.class));
        }

        return quartzJobClassNames;
    }

    @Component("quartz_QuartzJobDetector")
    private static class QuartzJobDetector implements ClasspathScanCandidateDetector {
        @Override
        public boolean isCandidate(MetadataReader metadataReader) {
            return Arrays.asList(metadataReader.getClassMetadata().getInterfaceNames()).contains(Job.class.getName());
        }
    }

}
