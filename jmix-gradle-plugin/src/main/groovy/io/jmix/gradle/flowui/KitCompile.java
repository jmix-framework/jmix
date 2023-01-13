package io.jmix.gradle.flowui;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public class KitCompile extends DefaultTask {

    private static final String KIT_DIRECTORY = ".jmix/screen-designer/lib";

    public static final String CONFIGURATION_NAME = "kit";

    @OutputDirectory
    public File getOutputDirectory() {
        return getProject().file(KIT_DIRECTORY);
    }

    public KitCompile() {
        setDescription("Compile files for screen designer");
        setGroup("web");
    }

    @TaskAction
    public void compileKit() {
        Configuration kitConfiguration = getProject().getConfigurations().getAt(CONFIGURATION_NAME);

        getProject().copy(copySpec -> copySpec.from(kitConfiguration)
                .into(getOutputDirectory()));
    }
}
