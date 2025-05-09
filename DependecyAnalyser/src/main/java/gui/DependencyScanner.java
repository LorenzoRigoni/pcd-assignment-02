package gui;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.Resolvable;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class DependencyScanner {

    private final TypeSolver typeSolver;

    public DependencyScanner(String projectRootPath) {
        final File projectRootFile = new File(projectRootPath);

        final File sourceRoot;
        if (projectRootFile.isFile())
            sourceRoot = projectRootFile.getParentFile();
        else if (projectRootFile.isDirectory())
            sourceRoot = projectRootFile;
        else
            throw new IllegalArgumentException("Not valid path " + projectRootPath);

        this.typeSolver = new CombinedTypeSolver(
                new ReflectionTypeSolver(),
                new JavaParserTypeSolver(sourceRoot)
        );

        JavaSymbolSolver solver = new JavaSymbolSolver(typeSolver);
        StaticJavaParser.getConfiguration()
                .setSymbolResolver(solver);
    }

    public Observable<DependencyResult> analyze(String path) {
        final File file = new File(path);
        if (!file.exists())
            return Observable.error(new IllegalArgumentException("File not found at path " + path));

        final List<File> files;
        if (file.isFile() && path.endsWith(".java"))
            files = Collections.singletonList(file);
        else if (file.isDirectory())
            files = this.getJavaFiles(path);
        else
            return Observable.error(new IllegalArgumentException("No Java project/package/class found in " + path));

        return Observable.fromIterable(files)
                .subscribeOn(Schedulers.io())
                .flatMap(this::parseFileReactive)
                .flatMap(cu -> Observable.fromIterable(cu.findAll(ClassOrInterfaceDeclaration.class)))
                .map(this::extractResolvedDependencies)
                .observeOn(Schedulers.single());
    }

    private List<File> getJavaFiles(String folderPath) {
        try {
            return Files.walk(Paths.get(folderPath))
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private Observable<CompilationUnit> parseFileReactive(File file) {
        return Observable.fromCallable(() -> StaticJavaParser.parse(file))
                .onErrorResumeNext(throwable -> {
                    System.err.println("Errore nel parsing del file: " + throwable.getMessage());
                    return Observable.empty();
                });

    }

    private DependencyResult extractResolvedDependencies(ClassOrInterfaceDeclaration classDec) {
        String classFQN;
        try {
            classFQN = classDec.resolve().getQualifiedName();
        } catch (Exception e) {
            classFQN = classDec.getNameAsString(); // fallback se non risolto
        }

        Set<String> dependencies = new HashSet<>();

        // Analizza tutti i tipi usati nella classe
        classDec.findAll(ClassOrInterfaceType.class).forEach(type -> {
            try {
                ResolvedType resolvedType = JavaParserFacade.get(typeSolver).convertToUsage(type);
                if(resolvedType.isPrimitive() || resolvedType.isVoid())
                    return;

                if (resolvedType.isReferenceType()) {
                    String qualifiedName = resolvedType.asReferenceType().getQualifiedName();

                    if (toInclude(qualifiedName)) {
                        dependencies.add(qualifiedName);
                    }
                } else {
                    String name = resolvedType.describe();
                    if (toInclude(name)) {
                        dependencies.add(name);
                    }
                }

            } catch (Exception ignored) {
                // In caso non riuscisse a risolvere un tipo
            }
        });

        return new DependencyResult(classFQN, new ArrayList<>(dependencies));
    }

    public static class DependencyResult {
        public final String className;
        public final List<String> dependencies;

        public DependencyResult(String className, List<String> dependencies) {
            this.className = className;
            this.dependencies = dependencies;
        }
    }

    private boolean toInclude(String qualifiedName) {
        // Pacchetti da escludere
        List<String> excludedPackages = Arrays.asList(
                "java.lang", "java.util", "java.io", "java.math",
                "java.time", "java.text", "java.nio", "java.net",
                "javafx", "org.graphstream"
        );

        // Tipi da escludere
        Set<String> excludedTypes = new HashSet<>(Arrays.asList(
                "String", "Object", "Throwable", "Exception", "RuntimeException", "Error"
        ));

        // Escludi se il nome inizia con uno dei pacchetti
        for (String prefix : excludedPackages) {
            if (qualifiedName.startsWith(prefix)) {
                return false;
            }
        }

        // Escludi se il nome è un tipo specifico (senza pacchetto)
        if (excludedTypes.contains(qualifiedName)) {
            return false;
        }

        return true;
    }

}
