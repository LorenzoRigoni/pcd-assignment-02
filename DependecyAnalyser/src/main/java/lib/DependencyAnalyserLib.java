package lib;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import lib.reports.ClassDepsReport;
import lib.reports.PackageDepsReport;
import lib.reports.ProjectDepsReport;

import java.nio.file.Path;
import java.util.*;

public class DependencyAnalyserLib {

    private final Vertx vertx;

    public DependencyAnalyserLib() {
        this.vertx = Vertx.vertx();
    }

    public Future<ClassDepsReport> getClassDependencies(Path classSrcFile) {
        final Promise<ClassDepsReport> classReportPromise = Promise.promise();

        this.readFile(classSrcFile)
                .compose(this::parseSourceCode)
                .compose(this::visitAST)
                .onSuccess(res -> {
                    final String className = res.keySet().stream().findFirst().orElse("");
                    final String packageName = res.get(className).keySet().stream().findFirst().orElse("java");
                    classReportPromise.complete(new ClassDepsReport(className, packageName, res.get(className).get(packageName)));
                })
                .onFailure(classReportPromise::fail);

        return classReportPromise.future();
    }

    public Future<PackageDepsReport> getPackageDependencies(Path packageSrcFolder) {
        return null;
    }

    public Future<ProjectDepsReport> getProjectDependencies(Path projectSrcFolder) {
        return null;
    }

    private Future<String> readFile(Path path) {
        final Promise<String> filePromise = Promise.promise();

        this.vertx.fileSystem().readFile(path.toString(), read -> {
            if (read.succeeded())
                filePromise.complete(read.result().toString("UTF-8"));
            else
                filePromise.fail(read.cause());
        });

        return filePromise.future();
    }

    private Future<CompilationUnit> parseSourceCode(String sourceCode) {
        return this.vertx.executeBlocking(() -> StaticJavaParser.parse(sourceCode), false);
    }

    private Future<Map<String, Map<String, Set<String>>>> visitAST(CompilationUnit compilationUnit) {
        final Promise<Map<String, Map<String, Set<String>>>> visitPromise = Promise.promise();
        // Class Name -> [Package -> Dependencies]

        this.vertx.executeBlocking(() -> {
            final Map<String, Set<String>> packageWithDependencies = new HashMap<>();
            final Set<String> types = new HashSet<>();

            new ASTVisitor().visit(compilationUnit, types);

            final String className = Objects.requireNonNull(compilationUnit.findAll(ClassOrInterfaceDeclaration.class)
                    .stream().findFirst().orElse(null)).getNameAsString();

            final String packageName = compilationUnit.getPackageDeclaration()
                    .map(NodeWithName::getNameAsString)
                    .orElse("java");

            packageWithDependencies.put(packageName, types);
            return Map.of(className, packageWithDependencies);
        }).onSuccess(visitPromise::complete)
        .onFailure(visitPromise::fail);

        return visitPromise.future();
    }
}
