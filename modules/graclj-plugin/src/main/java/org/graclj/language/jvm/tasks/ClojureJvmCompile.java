package org.graclj.language.jvm.tasks;

import org.graclj.platform.jvm.ClojureJvmPlatform;
import org.graclj.platform.jvm.toolchain.ClojureJvmToolChain;
import org.graclj.platform.jvm.toolchain.ClojureJvmToolChainRegistry;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public class ClojureJvmCompile extends SourceTask {
    private ClojureJvmPlatform platform;
    private ClojureJvmToolChainRegistry toolChainRegistry;
    private File destinationDir;
    private FileCollection classpath;

    // @Input
    public ClojureJvmPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(ClojureJvmPlatform platform) {
        this.platform = platform;
    }

    public ClojureJvmToolChain getToolChain() {
        return toolChainRegistry.getForPlatform(getPlatform());
    }

    public void setToolChainRegistry(ClojureJvmToolChainRegistry toolChainRegistry) {
        this.toolChainRegistry = toolChainRegistry;
    }

    @OutputDirectory
    public File getDestinationDir() {
        return this.destinationDir;
    }

    public void setDestinationDir(File destinationDir) {
        this.destinationDir = destinationDir;
    }

    @InputFiles
    public FileCollection getClasspath() {
        return this.classpath;
    }

    public void setClasspath(FileCollection configuration) {
        this.classpath = configuration;
    }

    @TaskAction
    public void compile() {
        FileCollection compiler = getToolChain().getCompiler();
        getProject().copy(spec -> {
            spec.from(getSource());
            spec.into(getTemporaryDir());
        });

        getProject().javaexec(spec -> {
            spec.classpath(compiler.getFiles());
            spec.classpath(getClasspath());

            // Clojure compiler requires source dirs and class dirs to be on classpath
            spec.classpath(getTemporaryDir());
            spec.classpath(getDestinationDir());

            spec.setMain("clojure.main");
            spec.args("--main", "org.graclj.tools.clojure");

            // Location to get source from
            spec.args(getTemporaryDir());

            // Location to write class files out to
            spec.args(getDestinationDir().getAbsolutePath());
        });
    }
}
