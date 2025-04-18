package lib.reports;

import java.util.Set;

public class ClassDepsReport {
    private final String className;
    private final String packageName;
    private final Set<String> dependencies;

    public ClassDepsReport(String className, String packageName, Set<String> dependencies) {
        this.className = className;
        this.packageName = packageName;
        this.dependencies = dependencies;
    }

    public String getClassName() {
        return this.className;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public Set<String> getDependencies() {
        return this.dependencies;
    }

    @Override
    public String toString() {
        return "[" +
                "className = '" + this.className + '\'' +
                ", packageName = '" + this.packageName + '\'' +
                ", dependencies = " + this.dependencies +
                ']';
    }
}