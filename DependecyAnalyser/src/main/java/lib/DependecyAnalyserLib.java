package lib;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lib.reports.ClassDepsReport;
import lib.reports.PackageDepsReport;
import lib.reports.ProjectDepsReport;

import java.nio.file.Path;

public class DependecyAnalyserLib {

    private final Vertx vertx;

    public DependecyAnalyserLib() {
        this.vertx = Vertx.vertx();
    }

    public Future<ClassDepsReport> getClassDependencies(Path classSrcFile) {
        throw new IllegalStateException();
    }

    public Future<PackageDepsReport> getPackageDependencies(Path packageSrcFolder) {
        throw new IllegalStateException();
    }

    public Future<ProjectDepsReport> getProjectDependencies(Path projectSrcFolder) {
        throw new IllegalStateException();
    }
}
