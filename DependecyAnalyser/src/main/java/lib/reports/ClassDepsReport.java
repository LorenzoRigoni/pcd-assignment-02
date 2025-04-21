package lib.reports;

import java.util.HashSet;
import java.util.Set;

public class ClassDepsReport {
    private final String classOrInterfaceName;
    private final String packageName;
    private final Set<String> dependencies;

    public ClassDepsReport(String classOrInterfaceName, String packageName, Set<String> dependencies) {
        this.classOrInterfaceName = classOrInterfaceName;
        this.packageName = packageName;
        this.dependencies = dependencies;
    }

    public String getClassOrInterfaceName() {
        return this.classOrInterfaceName;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public Set<String> getDependencies() {
        return new HashSet<>(this.dependencies);
    }

    @Override
    public String toString() {
        return "\n[" +
                "classOrInterfaceName = '" + this.classOrInterfaceName + '\'' +
                ", packageName = '" + this.packageName + '\'' +
                ", dependencies = " + this.dependencies +
                "]";
    }
}