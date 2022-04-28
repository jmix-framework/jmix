package io.jmix.flowui.sys;

import com.google.common.base.Strings;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.flowui.action.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

public class ActionsConfiguration extends AbstractScanConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ActionsConfiguration.class);

    protected ApplicationContext applicationContext;
    protected MetadataReaderFactory metadataReaderFactory;

    protected List<String> basePackages = Collections.emptyList();
    protected List<ActionDefinition> explicitDefinitions = Collections.emptyList();

    @Autowired
    public ActionsConfiguration(ApplicationContext applicationContext, AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        this.applicationContext = applicationContext;
        this.metadataReaderFactory = metadataReaderFactory;
    }

    public List<String> getBasePackages() {
        return basePackages;
    }

    public void setBasePackages(List<String> basePackages) {
        checkNotNullArgument(basePackages);

        this.basePackages = basePackages;
    }

    public List<ActionDefinition> getExplicitDefinitions() {
        return explicitDefinitions;
    }

    public void setExplicitDefinitions(List<ActionDefinition> explicitDefinitions) {
        checkNotNullArgument(explicitDefinitions);

        this.explicitDefinitions = explicitDefinitions;
    }

    public List<ActionDefinition> getActions() {
        log.trace("Scanning packages {}", basePackages);

        Stream<ActionDefinition> scannedActionsStream = basePackages.stream()
                .flatMap(this::scanPackage)
                .filter(this::isCandidateUiController)
                .map(this::extractActionDefinition);

        return Stream.concat(scannedActionsStream, explicitDefinitions.stream())
                .collect(Collectors.toList());
    }

    protected ActionDefinition extractActionDefinition(MetadataReader metadataReader) {
        Map<String, Object> actionTypeAnn =
                metadataReader.getAnnotationMetadata().getAnnotationAttributes(ActionType.class.getName());

        String valueAttr = null;
        if (actionTypeAnn != null) {
            valueAttr = (String) actionTypeAnn.get(ActionType.VALUE_ATTRIBUTE);
        }

        String className = metadataReader.getClassMetadata().getClassName();
        String actionTypeId = Strings.isNullOrEmpty(valueAttr) ? className : valueAttr;

        return new ActionDefinition(actionTypeId, className);
    }

    protected boolean isCandidateUiController(MetadataReader metadataReader) {
        return metadataReader.getClassMetadata().isConcrete()
                && metadataReader.getAnnotationMetadata().hasAnnotation(ActionType.class.getName());
    }

    @Override
    protected MetadataReaderFactory getMetadataReaderFactory() {
        return metadataReaderFactory;
    }

    @Override
    protected ResourceLoader getResourceLoader() {
        return applicationContext;
    }

    @Override
    protected Environment getEnvironment() {
        return applicationContext.getEnvironment();
    }
}