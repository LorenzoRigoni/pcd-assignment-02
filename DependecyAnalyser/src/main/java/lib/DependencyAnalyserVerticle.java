package lib;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lib.reports.ClassDepsReport;
import lib.reports.PackageDepsReport;
import lib.reports.ProjectDepsReport;

import java.nio.file.Path;

public class DependencyAnalyserVerticle extends AbstractVerticle {
    private static final String CURRENT_PATH = System.getProperty("user.dir");
    private static final Path CLASS_PATH = Path.of(CURRENT_PATH + "\\src\\main\\java\\lib\\reports\\ClassDepsReport.java");
    private static final Path PACKAGE_PATH = Path.of(CURRENT_PATH + "\\src\\main\\java\\lib\\reports");
    private static final Path PROJECT_PATH = Path.of(CURRENT_PATH);

    @Override
    public void start(Promise<Void> startPromise) {
        final DependencyAnalyserLib dependencyAnalyser = new DependencyAnalyserLib(this.vertx);

        final Future<ClassDepsReport> classReport = dependencyAnalyser.getClassDependencies(CLASS_PATH);
        final Future<PackageDepsReport> packageReport = dependencyAnalyser.getPackageDependencies(PACKAGE_PATH);
        final Future<ProjectDepsReport> projectReport = dependencyAnalyser.getProjectDependencies(PROJECT_PATH);

        Future.join(classReport, packageReport, projectReport)
                .onSuccess(res -> {
                    logClassReport(classReport.result());
                    System.out.println();
                    logPackageReport(packageReport.result());
                    System.out.println();
                    logProjectReport(projectReport.result());
                    startPromise.complete();
                })
                .onFailure(System.err::println);
    }

    private void logClassReport(ClassDepsReport classReport) {
        System.out.println("-----------------------------------");
        System.out.println("Report of Java file " + classReport.getClassOrInterfaceName());
        System.out.println("Class or interface name: " + classReport.getClassOrInterfaceName());
        System.out.println("Package name: " + classReport.getPackageName());
        System.out.println("Dependencies: " + classReport.getDependencies());
        System.out.println("-----------------------------------");
    }

    private void logPackageReport(PackageDepsReport packageReport) {
        System.out.println("--------------------------------------");
        System.out.println("Report of Java package " + packageReport.getPackageName());
        for (ClassDepsReport classReport : packageReport.getClassesAndInterfaces())
            logClassReport(classReport);
        System.out.println("--------------------------------------");
    }

    private void logProjectReport(ProjectDepsReport projectReport) {
        System.out.println("-----------------------------------------");
        System.out.println("Report of Java project " + projectReport.getProjectName());
        for (PackageDepsReport packageReport : projectReport.getPackages())
            logPackageReport(packageReport);
        System.out.println("-----------------------------------------");
    }
}
