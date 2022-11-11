/*
 * Copyright 2019 Haulmont.
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

package io.jmix.gradle.ui;

import io.jmix.gradle.ClassPathUtil;
import io.jmix.gradle.JmixPlugin;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class WidgetsDebug extends WidgetsTask {

    @Input
    protected String widgetSetsDir = "";
    @Input
    protected String widgetSetClass = "";
    @Input
    protected Map<String, Object> compilerArgs = new HashMap<>();

    @Input
    protected boolean printCompilerClassPath = false;

    @Input
    protected boolean shortClassPath = true;

    @Input
    protected String logLevel = "INFO";

    @Input
    protected String xmx = "-Xmx768m";
    @Input
    protected String xss = "-Xss8m";

    public WidgetsDebug() {
        setDescription("Debug GWT widgetset");
        setGroup("debug");
        // set default task dependsOn
        dependsOn(getProject().getTasks().getByPath(JavaPlugin.CLASSES_TASK_NAME));
    }

    @TaskAction
    public void debugWidgets() {
        if (widgetSetClass == null || widgetSetClass.isEmpty()) {
            throw new IllegalStateException("Please specify \"String widgetSetClass\" for debug widgetset");
        }

        if (widgetSetsDir == null || widgetSetsDir.isEmpty()) {
            widgetSetsDir = getDefaultBuildDir().getAbsolutePath();
        }

        File widgetSetsDirectory = new File(widgetSetsDir);
        if (widgetSetsDirectory.exists()) {
            FileUtils.deleteQuietly(widgetSetsDirectory);
        }
        //noinspection ResultOfMethodCallIgnored
        widgetSetsDirectory.mkdir();

        List<File> compilerClassPath = collectClassPathEntries();
        List<String> gwtCompilerArgs = collectCompilerArgs();
        List<String> gwtCompilerJvmArgs = collectCompilerJvmArgs();

        if (Os.isFamily(Os.FAMILY_WINDOWS) && shortClassPath) {
            File javaTmp = getProject().file("build/tmp/");
            if (javaTmp.exists()) {
                FileUtils.deleteQuietly(javaTmp);
            }
            javaTmp.mkdirs();

            File classPathFile = getProject().file("build/tmp/debug-widget-set-classpath.dat");
            ClassPathUtil.createFormattedClassPathFile(classPathFile, compilerClassPath);

            gwtCompilerJvmArgs.add("@" + classPathFile.getAbsolutePath());

            getProject().javaexec(spec -> {
                spec.getMainClass().set("com.google.gwt.dev.codeserver.CodeServer");
                spec.setArgs(gwtCompilerArgs);
                spec.setJvmArgs(gwtCompilerJvmArgs);
            });

            FileUtils.deleteQuietly(classPathFile);
        } else {
            getProject().javaexec(javaExecSpec -> {
                javaExecSpec.getMainClass().set("com.google.gwt.dev.codeserver.CodeServer");
                javaExecSpec.setClasspath(getProject().files(compilerClassPath));
                javaExecSpec.setArgs(gwtCompilerArgs);
                javaExecSpec.setJvmArgs(gwtCompilerJvmArgs);
            });
        }
    }

    @InputFiles
    public FileCollection getSourceFiles() {
        return super.getSourceFiles();
    }

    @OutputDirectory
    public File getOutputDirectory() {
        if (widgetSetsDir == null || widgetSetsDir.isEmpty()) {
            return getDefaultBuildDir();
        }
        return new File(widgetSetsDir);
    }

    @Internal
    protected File getDefaultBuildDir() {
        return new File(getProject().getBuildDir(), "/web-debug/VAADIN/widgetsets");
    }

    protected List<File> collectClassPathEntries() {
        List<File> compilerClassPath = new ArrayList<>();

        // import runtime dependencies such as servlet-api
        Configuration runtimeConfiguration = getProject().getConfigurations().findByName("runtime");
        if (runtimeConfiguration != null) {
            for (ResolvedArtifact artifact : runtimeConfiguration.getResolvedConfiguration().getResolvedArtifacts()) {
                compilerClassPath.add(artifact.getFile());
            }
        }

        Configuration compileConfiguration = getProject().getConfigurations().findByName("compile");
        if (compileConfiguration != null) {
            for (Project dependencyProject : collectProjectsWithDependency("vaadin-shared")) {
                SourceSet dependencyMainSourceSet = getSourceSet(dependencyProject, "main");

                compilerClassPath.addAll(dependencyMainSourceSet.getJava().getSrcDirs());
                compilerClassPath.addAll(getClassesDirs(dependencyMainSourceSet));
                compilerClassPath.add(dependencyMainSourceSet.getOutput().getResourcesDir());

                getProject().getLogger().debug(">> Widget set building Module: {}", dependencyProject.getName());
            }
        }

        SourceSet mainSourceSet = getSourceSet(getProject(), "main");

        compilerClassPath.addAll(mainSourceSet.getJava().getSrcDirs());
        compilerClassPath.addAll(getClassesDirs(mainSourceSet));
        compilerClassPath.add(mainSourceSet.getOutput().getResourcesDir());

        List<File> compileClassPathArtifacts = StreamSupport
                .stream(mainSourceSet.getCompileClasspath().spliterator(), false)
                .filter(f -> includedArtifact(f.getName()) && !compilerClassPath.contains(f))
                .collect(Collectors.toList());
        compilerClassPath.addAll(compileClassPathArtifacts);

        Configuration widgetsConfiguration =
                getProject().getConfigurations().findByName(JmixPlugin.WIDGETS_CONFIGURATION_NAME);

        if (widgetsConfiguration != null) {
            List<File> widgetsDeps = widgetsConfiguration.getResolvedConfiguration().getFiles().stream()
                    .filter(f -> includedArtifact(f.getName()) && !compilerClassPath.contains(f))
                    .collect(Collectors.toList());

            compilerClassPath.addAll(widgetsDeps);
        }

        if (getProject().getLogger().isEnabled(LogLevel.DEBUG)) {
            StringBuilder sb = new StringBuilder();
            for (File classPathEntry : compilerClassPath) {
                sb.append('\t')
                        .append(classPathEntry.getAbsolutePath())
                        .append('\n');
            }
            getProject().getLogger().debug("GWT Compiler ClassPath: \n{}", sb.toString());
            getProject().getLogger().debug("");
        } else if (printCompilerClassPath) {
            StringBuilder sb = new StringBuilder();
            for (File classPathEntry : compilerClassPath) {
                sb.append('\t')
                        .append(classPathEntry.getAbsolutePath())
                        .append('\n');
            }

            System.out.println("GWT Compiler ClassPath: \n" + sb.toString());
            System.out.println();
        }

        return compilerClassPath;
    }

    protected List<String> collectCompilerArgs() {
        List<String> args = new ArrayList<>();

        args.addAll(Arrays.asList("-logLevel", logLevel));
        args.addAll(Arrays.asList("-workDir", getProject().file(widgetSetsDir).getAbsolutePath()));

        for (File srcDir : getSourceSet(getProject(), "main").getJava().getSrcDirs()) {
            if (srcDir.exists()) {
                args.addAll(Arrays.asList("-src", srcDir.getAbsolutePath()));
            }
        }

        for (Project dependencyProject : collectProjectsWithDependency("vaadin-client")) {
            for (File srcDir : getSourceSet(dependencyProject, "main").getJava().getSrcDirs()) {
                if (srcDir.exists()) {
                    args.add("-src");
                    args.add(srcDir.getAbsolutePath());
                }
            }
        }

        // support overriding of default parameters
        Map<String, Object> gwtCompilerArgs = new HashMap<>();
        gwtCompilerArgs.put("-XmethodNameDisplayMode", "FULL");
        if (compilerArgs != null) {
            gwtCompilerArgs.putAll(compilerArgs);
        }

        for (Map.Entry<String, Object> entry : gwtCompilerArgs.entrySet()) {
            args.add(entry.getKey());
            args.add(String.valueOf(entry.getValue()));
        }

        args.add(widgetSetClass);

        if (getProject().getLogger().isInfoEnabled()) {
            System.out.println("GWT Compiler args: ");
            System.out.println('\t');
            System.out.println(args);
        }

        return args;
    }

    protected List<String> collectCompilerJvmArgs() {
        compilerJvmArgs.add(xmx);
        compilerJvmArgs.add(xss);

        if (getProject().getLogger().isInfoEnabled()) {
            System.out.println("JVM Args:");
            System.out.println('\t');
            System.out.println(compilerJvmArgs);
        }

        return new LinkedList<>(compilerJvmArgs);
    }

    public void setWidgetSetsDir(String widgetSetsDir) {
        this.widgetSetsDir = widgetSetsDir;
    }

    public String getWidgetSetsDir() {
        return widgetSetsDir;
    }

    public void setWidgetSetClass(String widgetSetClass) {
        this.widgetSetClass = widgetSetClass;
    }

    public String getWidgetSetClass() {
        return widgetSetClass;
    }

    public void setCompilerArgs(Map<String, Object> compilerArgs) {
        this.compilerArgs = compilerArgs;
    }

    public Map<String, Object> getCompilerArgs() {
        return compilerArgs;
    }

    public void setPrintCompilerClassPath(boolean printCompilerClassPath) {
        this.printCompilerClassPath = printCompilerClassPath;
    }

    public boolean isPrintCompilerClassPath() {
        return printCompilerClassPath;
    }

    public boolean isShortClassPath() {
        return shortClassPath;
    }

    public void setShortClassPath(boolean shortClassPath) {
        this.shortClassPath = shortClassPath;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setXmx(String xmx) {
        this.xmx = xmx;
    }

    public String getXmx() {
        return xmx;
    }

    public void setXss(String xss) {
        this.xss = xss;
    }

    public String getXss() {
        return xss;
    }
}
