package io.jmix.quartz.util;

import io.jmix.core.impl.scanning.ClasspathScanCandidateDetector;
import io.jmix.core.impl.scanning.JmixModulesClasspathScanner;
import org.apache.commons.collections4.CollectionUtils;
import org.quartz.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component("quartz_QuartzJobClassFinder")
public class QuartzJobClassFinder {

    private static final Logger log = LoggerFactory.getLogger(QuartzJobClassFinder.class);

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
            try {
                return !metadataReader.getClassMetadata().isInterface()
                        && !metadataReader.getClassMetadata().isAbstract()
                        && Job.class.isAssignableFrom(Class.forName(metadataReader.getClassMetadata().getClassName()));
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                log.trace("Class not found", e);
                return false;
            }
        }
    }

}
