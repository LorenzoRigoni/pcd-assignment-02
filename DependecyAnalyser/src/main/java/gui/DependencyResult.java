package gui;

import java.util.List;

public class DependencyResult {
    public final String className;
    public final List<String> dependencies;

    public DependencyResult(String className, List<String> dependencies) {
        this.className = className;
        this.dependencies = dependencies;
    }
}
