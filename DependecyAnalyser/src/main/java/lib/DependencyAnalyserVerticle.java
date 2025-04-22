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
                    System.out.println("Report of Java file " + classReport.result().getClassOrInterfaceName());
                    System.out.println(classReport.result() + "\n");
                    System.out.println("Report of package " + packageReport.result().getPackageName());
                    System.out.println(packageReport.result() + "\n");
                    System.out.println("Report of project " + projectReport.result().getProjectName());
                    System.out.println(projectReport.result());
                    startPromise.complete();
                })
                .onFailure(System.err::println);
    }
}
