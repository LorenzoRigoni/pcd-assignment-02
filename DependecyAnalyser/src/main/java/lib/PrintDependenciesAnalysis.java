package lib;

import lib.reports.ClassDepsReport;

import java.nio.file.Path;

public class PrintDependenciesAnalysis {
    private static final DependencyAnalyserLib dependencyAnalyser = new DependencyAnalyserLib();
    private static final String CURRENT_PATH = System.getProperty("user.dir");

    public static void main(String[] args) {
        printClassDependencies(Path.of(CURRENT_PATH + "\\src\\main\\java\\lib\\reports\\ClassDepsReport.java"));
    }

    private static void printClassDependencies(Path classSrcFile) {
        dependencyAnalyser.getClassDependencies(classSrcFile).onComplete(res -> {
            if (res.succeeded()) {
                final ClassDepsReport report = res.result();
                System.out.println("Class name: " + report.getClassName());
                System.out.println("Package name: " + report.getPackageName());
                System.out.println("Dependencies: " + report.getDependencies());
            } else
                System.err.println(res.cause().getMessage());
        });
    }
}
