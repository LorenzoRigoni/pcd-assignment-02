package lib.reports;

import java.util.HashSet;
import java.util.Set;

public class ProjectDepsReport {
    private final String projectName;
    private final Set<PackageDepsReport> packages;

    public ProjectDepsReport(String projectName, Set<PackageDepsReport> packages) {
        this.projectName = projectName;
        this.packages = packages;
    }

    public String getProjectName() {
        return this.projectName;
    }

    public Set<PackageDepsReport> getPackages() {
        return new HashSet<>(this.packages);
    }
}