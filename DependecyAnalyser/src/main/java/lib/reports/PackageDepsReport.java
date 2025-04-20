package lib.reports;

import java.util.HashSet;
import java.util.Set;

public class PackageDepsReport {
    private final String packageName;
    private final Set<ClassDepsReport> classesAndInterfaces;

    public PackageDepsReport(String packageName, Set<ClassDepsReport> classesAndInterfaces) {
        this.packageName = packageName;
        this.classesAndInterfaces = classesAndInterfaces;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public Set<ClassDepsReport> getClassesAndInterfaces() {
        return new HashSet<>(this.classesAndInterfaces);
    }

    @Override
    public String toString() {
        return "[" +
                "packageName='" + this.packageName + '\'' +
                ", classesAndInterfaces=" + this.classesAndInterfaces +
                "\n]\n";
    }
}
