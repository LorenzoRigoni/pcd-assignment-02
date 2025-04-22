package lib;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import lib.reports.ClassDepsReport;
import lib.reports.PackageDepsReport;
import lib.reports.ProjectDepsReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DependencyAnalyserLib {
    private final Vertx vertx;

    public DependencyAnalyserLib(Vertx vertx) {
        this.vertx = vertx;
    }

    /**
     * Get the name, the package name and the dependencies of a class or an interface in Java.
     *
     * @param classSrcFile the absolute path of the class/interface
     * @return a Future result that contains the name, the package name and the dependencies
     */
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

    /**
     * Get the package name and all the reports of the classes contained in the package.
     *
     * @param packageSrcFolder the absolute path of the package
     * @return a Future result that contains the package name and all the reports of the classes contained in the package.
     */
    public Future<PackageDepsReport> getPackageDependencies(Path packageSrcFolder) {
        final Promise<PackageDepsReport> packageReportPromise = Promise.promise();

        this.getFilesPaths(packageSrcFolder)
                .compose(paths -> {
                    final List<Future<ClassDepsReport>> classesFutures = new ArrayList<>();

                    paths.forEach(p -> classesFutures.add(this.getClassDependencies(p)));

                    return Future.all(classesFutures);
                }).onSuccess(cF -> {
                    final Set<ClassDepsReport> classesReports = new HashSet<>();

                    for (int i = 0; i < cF.size(); i++) {
                        classesReports.add(cF.resultAt(i));
                    }

                    final String packageName = classesReports.stream()
                            .findFirst()
                            .map(ClassDepsReport::getPackageName)
                            .orElse("java");

                    packageReportPromise.complete(new PackageDepsReport(packageName, classesReports));
                }).onFailure(packageReportPromise::fail);

        return packageReportPromise.future();
    }

    /**
     * Get the project name and all the reports of the packages contained in the Java project.
     *
     * @param projectSrcFolder the absolute path of the Java project.
     * @return a Future result that contains the project name and all the reports of the packages contained in the Java project
     */
    public Future<ProjectDepsReport> getProjectDependencies(Path projectSrcFolder) {
        final Promise<ProjectDepsReport> projectReportPromise = Promise.promise();

        this.getProjectEntryPoint(projectSrcFolder)
                .compose(this::getPackagesPaths)
                .compose(paths -> {
                    final List<Future<PackageDepsReport>> packagesFutures = new ArrayList<>();

                    paths.forEach(p -> packagesFutures.add(this.getPackageDependencies(p)));

                    return Future.all(packagesFutures);
                }).onSuccess(cF -> {
                    final Set<PackageDepsReport> packagesReports = new HashSet<>();

                    for (int i = 0; i < cF.size(); i++) {
                        packagesReports.add(cF.resultAt(i));
                    }

                    final String projectName = projectSrcFolder.getFileName().toString();

                    projectReportPromise.complete(new ProjectDepsReport(projectName, packagesReports));
                }).onFailure(projectReportPromise::fail);

        return projectReportPromise.future();
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
        return this.vertx.executeBlocking(() -> StaticJavaParser.parse(sourceCode), true);
    }

    private Future<Map<String, Map<String, Set<String>>>> visitAST(CompilationUnit compilationUnit) {
        final Promise<Map<String, Map<String, Set<String>>>> visitPromise = Promise.promise();
        // [Class Name -> [Package -> Dependencies]]

        this.vertx.executeBlocking(() -> {
            final Map<String, Set<String>> packageWithDependencies = new HashMap<>();
            final Set<String> types = new HashSet<>();

            new ClassOrInterfaceVisitor().visit(compilationUnit, types);

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

    private Future<Set<Path>> getFilesPaths(Path packagePath) {
        final Promise<Set<Path>> pathsPromise = Promise.promise();

        this.vertx.executeBlocking(() -> {
            try (final Stream<Path> paths = Files.walk(packagePath)) {
                return paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".java"))
                        .collect(Collectors.toSet());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).onSuccess(pathsPromise::complete)
        .onFailure(pathsPromise::fail);

        return pathsPromise.future();
    }

    private Future<Path> getProjectEntryPoint(Path projectSrcFolder) {
        final Promise<Path> projectEntryPointPromise = Promise.promise();

        this.vertx.executeBlocking(() -> {
            try (final Stream<Path> paths = Files.walk(projectSrcFolder)) {
                Optional<Path> srcFolderPath = paths
                        .filter(Files::isDirectory)
                        .filter(p -> p.getFileName().toString().equals("java"))
                        .findFirst();
                if (srcFolderPath.isPresent())
                    return srcFolderPath.get();
                else
                    throw new RuntimeException("Could not find Java source folder");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).onSuccess(projectEntryPointPromise::complete)
        .onFailure(projectEntryPointPromise::fail);

        return projectEntryPointPromise.future();
    }

    private Future<Set<Path>> getPackagesPaths(Path projectEntryPoint) {
        final Promise<Set<Path>> pathsPromise = Promise.promise();

        this.vertx.executeBlocking(() -> {
            try (final Stream<Path> paths = Files.walk(projectEntryPoint)) {
                return paths
                        .filter(Files::isDirectory)
                        .filter(p -> !p.equals(projectEntryPoint))
                        .collect(Collectors.toSet());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).onSuccess(pathsPromise::complete)
        .onFailure(pathsPromise::fail);

        return pathsPromise.future();
    }
}