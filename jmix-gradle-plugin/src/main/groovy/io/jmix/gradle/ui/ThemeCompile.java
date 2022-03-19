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

import com.google.common.base.Splitter;
import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.handler.SCSSDocumentHandlerImpl;
import com.vaadin.sass.internal.handler.SCSSErrorHandler;
import com.yahoo.platform.yui.compressor.CssCompressor;
import io.jmix.gradle.JmixPlugin;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.GZIPOutputStream;

import static org.apache.commons.io.FileUtils.*;

public class ThemeCompile extends DefaultTask {

    public static final String VAADIN_STYLESHEETS_MANIFEST_KEY = "Vaadin-Stylesheets";
    public static final String COMPILE_CLASSPATH_CONFIGURATION = "compileClasspath";

    @Input
    protected List<String> themes = new ArrayList<>();

    @Input
    protected String scssDir = "src/main/themes";

    @Input
    protected String destDir = "";

    @Input
    protected boolean compress = true;
    @Input
    protected boolean cleanup = true;
    @Input
    protected boolean gzip = true;

    protected List<String> excludedThemes = new ArrayList<>();
    protected List<String> excludePaths = new ArrayList<>();
    protected List<String> doNotUnpackPaths = Arrays.asList(
            "VAADIN/themes/valo/*.css",
            "VAADIN/themes/valo/*.css.gz",
            "VAADIN/themes/valo/favicon.ico",
            "VAADIN/themes/valo/util/readme.txt",
            "META-INF/**"
    );

    public ThemeCompile() {
        setDescription("Compile SCSS styles in theme");
        setGroup("web");
    }

    @OutputDirectory
    public File getOutputDirectory() {
        if (destDir == null || destDir.isEmpty()) {
            return new File(getProject().getBuildDir(), "themes");
        }
        return new File(getProject().getProjectDir(), destDir);
    }

    @InputDirectory
    public File getSourceDirectory() {
        return getProject().file(scssDir);
    }

    public List<String> getThemes() {
        return themes;
    }

    public void setThemes(List<String> themes) {
        this.themes = themes;
    }

    public String getScssDir() {
        return scssDir;
    }

    public void setScssDir(String scssDir) {
        this.scssDir = scssDir;
    }

    public String getDestDir() {
        return destDir;
    }

    public void setDestDir(String destDir) {
        this.destDir = destDir;
    }

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    public boolean isCleanup() {
        return cleanup;
    }

    public void setCleanup(boolean cleanup) {
        this.cleanup = cleanup;
    }

    public boolean isGzip() {
        return gzip;
    }

    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }

    @TaskAction
    public void compileThemes() throws IOException {
        File stylesDirectory = getSourceDirectory();
        if (!stylesDirectory.exists()) {
            throw new FileNotFoundException(String.format("Unable to find SCSS themes root directory: %s",
                    stylesDirectory.getAbsolutePath()));
        }

        File themesTmp = new File(getProject().getBuildDir(), "tmp/themes");
        if (themesTmp.exists()) {
            deleteDirectory(themesTmp);
        } else {
            forceMkdirParent(themesTmp);
        }
        forceMkdir(themesTmp);

        File vaadinThemesRoot = new File(themesTmp, "VAADIN/themes");
        forceMkdir(vaadinThemesRoot);

        List<String> themes = new ArrayList<>(this.themes);

        if (themes.isEmpty()) {
            getLogger().info("[ThemeCompile] scan directory '{}' for themes",
                    stylesDirectory.getAbsolutePath());

            File[] themeFolders = stylesDirectory.listFiles(pathname ->
                    pathname.isDirectory() && !pathname.getName().startsWith(".")
            );
            if (themeFolders != null) {
                Arrays.stream(themeFolders)
                        .map(File::getName)
                        .forEach(themes::add);
            }
        }

        unpackVaadinAddonsThemes(themesTmp);
        unpackThemesDependencies(themesTmp, vaadinThemesRoot);

        for (String themeDirName : themes) {
            buildTheme(themeDirName, stylesDirectory, vaadinThemesRoot);
        }

        File destinationDirectory = getOutputDirectory();

        copyResources(themesTmp, destinationDirectory);

        if (cleanup) {
            // remove empty directories
            removeEmptyDirs(destinationDirectory);
        }

        for (String themeName : excludedThemes) {
            File themeDestDir = new File(destinationDirectory, themeName);
            getLogger().info("[ThemeCompile] excluded theme '{}'", themeName);

            deleteQuietly(themeDestDir);
        }

        for (String path : excludePaths) {
            File pathFile = new File(destinationDirectory, path);
            getLogger().info("[ThemeCompile] excluded path '{}'", path);

            deleteQuietly(pathFile);
        }
    }

    protected void unpackVaadinAddonsThemes(File themesTmp) {
        Configuration compileConfiguration = getProject().getConfigurations().findByName(COMPILE_CLASSPATH_CONFIGURATION);
        if (compileConfiguration == null) {
            return;
        }

        Set<ResolvedArtifact> resolvedArtifacts = compileConfiguration.getResolvedConfiguration().getResolvedArtifacts();

        resolvedArtifacts.stream()
                .map(ResolvedArtifact::getFile)
                .filter(f -> f.exists() && f.isFile() && f.getName().endsWith(".jar"))
                .forEach(jarFile -> {
                    try (InputStream is = new FileInputStream(jarFile);
                         JarInputStream jarStream = new JarInputStream(is)) {

                        String vaadinStylesheets = getVaadinStylesheets(jarStream);

                        if (vaadinStylesheets != null) {
                            getLogger().info("[ThemeCompile] unpack Vaadin addon styles {}", jarFile.getName());

                            getProject().copy(copySpec ->
                                    copySpec.from(getProject().zipTree(jarFile))
                                            .into(themesTmp)
                                            .include("VAADIN/**"));
                        }
                    } catch (IOException e) {
                        throw new GradleException("Unable to read JAR with theme", e);
                    }
                });
    }

    protected void unpackThemesDependencies(File themesTmp, File vaadinThemesRoot) {
        Configuration themesConf = getProject().getConfigurations().findByName(JmixPlugin.THEMES_CONFIGURATION_NAME);
        if (themesConf != null) {
            List<File> themeArchives = collectThemeArchives(themesConf);

            for (File archive : themeArchives) {
                if (archive.getName().startsWith("vaadin-themes")) {
                    getLogger().info("[ThemeCompile] unpack vaadin-themes artifact {}", archive.getName());

                    getProject().copy(copySpec ->
                            copySpec.from(getProject().zipTree(archive))
                                    .into(themesTmp)
                                    .include("VAADIN/**")
                                    .setExcludes(doNotUnpackPaths)
                    );
                } else {
                    getLogger().info("[ThemeCompile] unpack themes artifact {}", archive.getName());

                    getProject().copy(copySpec ->
                            copySpec.from(getProject().zipTree(archive))
                                    .into(vaadinThemesRoot)
                                    .setExcludes(doNotUnpackPaths)
                    );
                }
            }
        }
    }

    protected List<File> collectThemeArchives(Configuration themesConf) {
        Set<ResolvedDependency> firstLevelModuleDependencies =
                themesConf.getResolvedConfiguration().getFirstLevelModuleDependencies();

        List<File> files = new ArrayList<>();
        Set<ResolvedArtifact> passedArtifacts = new HashSet<>();

        for (ResolvedDependency dependency : firstLevelModuleDependencies) {
            collectThemeArchives(dependency, passedArtifacts, files);
        }

        return files;
    }

    protected void collectThemeArchives(ResolvedDependency dependency, Set<ResolvedArtifact> passedArtifacts,
                                        List<File> files) {
        for (ResolvedDependency child : dependency.getChildren()) {
            collectThemeArchives(child, passedArtifacts, files);
        }

        for (ResolvedArtifact artifact : dependency.getModuleArtifacts()) {
            if (passedArtifacts.contains(artifact)) {
                continue;
            }

            passedArtifacts.add(artifact);
            files.add(artifact.getFile());
        }
    }

    protected void buildTheme(String themeDirName, File stylesDirectory, File vaadinThemesRoot) throws FileNotFoundException {
        getLogger().info("[ThemeCompile] build theme '{}'", themeDirName);

        File themeDir = new File(stylesDirectory, themeDirName);
        if (!themeDir.exists()) {
            throw new FileNotFoundException("Unable to find theme directory: " + themeDir.getAbsolutePath());
        }

        File themeBuildDir = new File(vaadinThemesRoot, themeDirName);

        getLogger().info("[ThemeCompile] copy theme '{}' to build directory", themeDir.getName());
        // copy theme to build directory
        getProject().copy(copySpec ->
                copySpec.from(themeDir)
                        .into(themeBuildDir)
                        .exclude(element -> {
                            return element.getFile().getName().startsWith(".");
                        }));

        generateAddonIncludes(themeBuildDir);

        getLogger().info("[ThemeCompile] compile theme '{}'", themeDir.getName());

        File scssFile = new File(themeBuildDir, "styles.scss");
        File cssFile = new File(themeBuildDir, "styles.css");

        compileScss(scssFile, cssFile);

        if (compress) {
            performCssCompression(themeDir, cssFile);
        }

        if (gzip) {
            createGzipCss(themeBuildDir, cssFile);
        }

        getLogger().info("[ThemeCompile] successfully compiled theme '{}'", themeDir.getName());
    }

    protected void compileScss(File scssFile, File cssFile) {
        ScssContext.UrlMode urlMode = ScssContext.UrlMode.MIXED;
        SCSSErrorHandler errorHandler = new SCSSErrorHandler() {
            boolean[] hasErrors = new boolean[]{false};

            @Override
            public void error(CSSParseException e) throws CSSException {
                getLogger().error("[ThemeCompile] Error when parsing file \n{} on line {}, column {}",
                        e.getURI(), e.getLineNumber(), e.getColumnNumber(), e);

                hasErrors[0] = true;
            }

            @Override
            public void fatalError(CSSParseException e) throws CSSException {
                getLogger().error("[ThemeCompile] Error when parsing file \n{} on line {}, column {}",
                        e.getURI(), e.getLineNumber(), e.getColumnNumber(), e);

                hasErrors[0] = true;
            }

            @Override
            public void warning(CSSParseException e) throws CSSException {
                getLogger().error("[ThemeCompile] Warning when parsing file \n{} on line {}, column {}",
                        e.getURI(), e.getLineNumber(), e.getColumnNumber(), e);
            }

            @Override
            public void traverseError(Exception e) {
                getLogger().error("[ThemeCompile] Error on SCSS traverse", e);

                hasErrors[0] = true;
            }

            @Override
            public void traverseError(String message) {
                getLogger().error("[ThemeCompile] {}", message);

                hasErrors[0] = true;
            }

            @Override
            public boolean isErrorsDetected() {
                return super.isErrorsDetected() || hasErrors[0];
            }
        };
        errorHandler.setWarningsAreErrors(false);

        try {
            ScssStylesheet scss =
                    ScssStylesheet.get(scssFile.getAbsolutePath(), null, new SCSSDocumentHandlerImpl(), errorHandler);

            if (scss == null) {
                throw new GradleException("Unable to find SCSS file " + scssFile.getAbsolutePath());
            }

            scss.compile(urlMode);

            Writer writer = new FileWriter(cssFile);
            scss.write(writer, false);
            writer.close();
        } catch (Exception e) {
            throw new GradleException("Unable to build theme " + scssFile.getAbsolutePath(), e);
        }

        if (errorHandler.isErrorsDetected()) {
            throw new GradleException("Unable to build theme " + scssFile.getAbsolutePath());
        }
    }

    protected void createGzipCss(File themeBuildDir, File cssFile) {
        getLogger().info("[ThemeCompile] compress css file 'styles.css'");

        File cssGzFile = new File(themeBuildDir, "styles.css.gz");

        try (FileInputStream uncompressedStream = new FileInputStream(cssFile);
             GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(cssGzFile))) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = uncompressedStream.read(buffer)) > 0) {
                gzos.write(buffer, 0, len);
            }

            gzos.finish();
        } catch (IOException e) {
            throw new GradleException("Unable to GZIP theme CSS", e);
        }
    }

    protected void performCssCompression(File themeDir, File cssFile) {
        getLogger().info("[ThemeCompile] compress theme '{}'", themeDir.getName());

        File compressedFile = new File(cssFile.getAbsolutePath() + ".compressed");

        try (FileReader cssReader = new FileReader(cssFile);
             Writer out = new BufferedWriter(new FileWriter(compressedFile))) {
            CssCompressor compressor = new CssCompressor(cssReader);
            compressor.compress(out, 0);
        } catch (IOException e) {
            throw new GradleException("Unable to minify CSS theme " + themeDir.getName(), e);
        }

        if (compressedFile.exists()) {
            try {
                FileUtils.forceDelete(cssFile);
            } catch (IOException e) {
                throw new GradleException("Unable to delete CSS file " + cssFile.getAbsolutePath(), e);
            }

            boolean renamed = compressedFile.renameTo(cssFile);
            if (!renamed) {
                throw new GradleException("Unable to move file " + cssFile.getAbsolutePath());
            }
        }
    }

    protected void generateAddonIncludes(File themeBuildDir) {
        File addonIncludesFile = new File(themeBuildDir, "addons.scss");
        if (addonIncludesFile.exists()) {
            getLogger().info("[ThemeCompile] there is the customized addons.scss file in the project");
            // can be completely overridden in project
            return;
        }

        getLogger().info("[ThemeCompile] include styles from addons for '{}'", themeBuildDir.getName());

        StringBuilder includesBuilder = new StringBuilder();
        includesBuilder.append("/* This file is automatically managed and will be overwritten */\n\n");

        Set<ResolvedArtifact> addedArtifacts = new HashSet<>();
        Set<String> includedPaths = new HashSet<>();
        List<String> includeMixins = new ArrayList<>();

        ConfigurationContainer configurations = getProject().getConfigurations();

        Configuration themesConfiguration = configurations.findByName(JmixPlugin.THEMES_CONFIGURATION_NAME);
        if (themesConfiguration != null) {
            collectAddonIncludes(themesConfiguration, includesBuilder, addedArtifacts, includeMixins, includedPaths);
        }
        Configuration compileConfiguration = configurations.findByName(COMPILE_CLASSPATH_CONFIGURATION);
        if (compileConfiguration != null) {
            collectAddonIncludes(compileConfiguration, includesBuilder, addedArtifacts, includeMixins, includedPaths);
        }

        // print mixins
        includesBuilder.append("\n@mixin addons {\n");
        for (String mixin : includeMixins) {
            includesBuilder.append("  @include ").append(mixin).append(";\n");
        }
        includesBuilder.append('}');

        try {
            FileUtils.write(addonIncludesFile, includesBuilder.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new GradleException("Unable to write addons.scss in " + themeBuildDir.getAbsolutePath(), e);
        }
    }

    protected void collectAddonIncludes(Configuration configuration, StringBuilder appComponentsIncludeBuilder,
                                        Set<ResolvedArtifact> addedArtifacts, List<String> includeMixins, Set<String> includedPaths) {
        Set<ResolvedDependency> dependencies = configuration.getResolvedConfiguration().getFirstLevelModuleDependencies();
        Set<ResolvedDependency> visitedDependencies = new HashSet<>();

        walkDependencies(dependencies, visitedDependencies, addedArtifacts, artifact -> {
            File file = artifact.getFile();

            try (FileInputStream is = new FileInputStream(file);
                 JarInputStream jarStream = new JarInputStream(is)) {

                String vaadinStylesheets = getVaadinStylesheets(jarStream);
                if (vaadinStylesheets != null) {
                    includeVaadinStyles(vaadinStylesheets, includeMixins, includedPaths, appComponentsIncludeBuilder);
                }
            } catch (IOException e) {
                throw new GradleException("Unable to read SCSS theme includes", e);
            }
        });
    }

    protected void includeVaadinStyles(String vaadinStylesheets, List<String> includeMixins, Set<String> includedPaths,
                                       StringBuilder includeBuilder) {
        List<String> vAddonIncludes = Splitter.on(",")
                .omitEmptyStrings()
                .trimResults()
                .splitToList(vaadinStylesheets);

        for (String include : new LinkedHashSet<>(vAddonIncludes)) {
            if (!include.startsWith("/")) {
                include = '/' + include;
            }

            if (includedPaths.contains(include)) {
                continue;
            }

            includedPaths.add(include);

            getLogger().info("[ThemeCompile] include vaadin addons styles '{}'", include);

            if (include.endsWith(".css")) {
                includeBuilder.append(String.format("@import url(\"../../..%s\");\n", include));
            } else {
                String mixin = include.substring(include.lastIndexOf("/") + 1,
                        include.length() - ".scss".length());

                includeBuilder.append(String.format("@import \"../../..%s\";\n", include));

                includeMixins.add(mixin);
            }
        }
    }

    protected void walkDependencies(Set<ResolvedDependency> dependencies, Set<ResolvedDependency> visitedDependencies, Set<ResolvedArtifact> addedArtifacts,
                                    Consumer<ResolvedArtifact> artifactAction) {
        for (ResolvedDependency dependency : dependencies) {
            if (!visitedDependencies.contains(dependency)) {
                visitedDependencies.add(dependency);
                walkDependencies(dependency.getChildren(), visitedDependencies, addedArtifacts, artifactAction);

                for (ResolvedArtifact artifact : dependency.getModuleArtifacts()) {
                    if (addedArtifacts.contains(artifact)) {
                        continue;
                    }

                    addedArtifacts.add(artifact);

                    if (artifact.getFile().getName().endsWith(".jar")) {
                        artifactAction.accept(artifact);
                    }
                }
            }
        }
    }

    @Nullable
    protected String getVaadinStylesheets(JarInputStream jarStream) {
        Manifest mf = jarStream.getManifest();
        if (mf != null && mf.getMainAttributes() != null) {
            return mf.getMainAttributes().getValue(VAADIN_STYLESHEETS_MANIFEST_KEY);
        }
        return null;
    }

    protected void copyResources(File themesBuildDir, File themesDestDir) {
        getProject().copy(copySpec ->
                copySpec.from(themesBuildDir)
                        .into(themesDestDir)
                        .exclude(element -> {
                            String name = element.getFile().getName();
                            return name.startsWith(".") || name.endsWith(".scss");
                        }));
    }

    protected void removeEmptyDirs(File themesDestDir) {
        recursiveVisitDir(themesDestDir, f -> {
            String[] list = f.list();
            if (list == null) {
                return;
            }

            if (list.length == 0) {
                Path relativePath = themesDestDir.toPath().relativize(f.toPath());

                getLogger().debug("[CubaWebScssThemeCreation] remove empty dir {} in '{}'", relativePath,
                        themesDestDir.getName());

                try {
                    FileUtils.deleteDirectory(f);
                } catch (IOException e) {
                    throw new GradleException("Unable to delete empty dir", e);
                }
            }
        });
    }

    protected void recursiveVisitDir(File dir, Consumer<File> apply) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File f : files) {
            if (f.exists() && f.isDirectory()) {
                recursiveVisitDir(f, apply);
                apply.accept(f);
            }
        }
    }
}
