package gui;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
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
        this.typeSolver = new CombinedTypeSolver(
                new ReflectionTypeSolver(), // Per classi standard Java
                new JavaParserTypeSolver(new File(projectRootPath)) // Per le classi nel progetto
        );

        JavaSymbolSolver solver = new JavaSymbolSolver(typeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(solver);
    }

    public Observable<DependencyResult> analyze(String folderPath) {
        return Observable.fromIterable(getJavaFiles(folderPath))
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

    private DependencyResult extractResolvedDependencies(ClassOrInterfaceDeclaration clazz) {
        String classFQN;
        try {
            classFQN = clazz.resolve().getQualifiedName();
        } catch (Exception e) {
            classFQN = clazz.getNameAsString(); // fallback se non risolto
        }

        Set<String> dependencies = new HashSet<>();

        // Analizza tutti i tipi usati nella classe
        clazz.findAll(Type.class).forEach(type -> {
            try {
                ResolvedType resolvedType = JavaParserFacade.get(typeSolver).convertToUsage(type);
                if (resolvedType.isReferenceType()) {
                    dependencies.add(resolvedType.asReferenceType().getQualifiedName());
                } else {
                    dependencies.add(resolvedType.describe());
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
}
