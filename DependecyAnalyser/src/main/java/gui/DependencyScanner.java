package gui;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class DependencyScanner {

    public Observable<DependencyResult> analyzeDependencies(String folderPath) {
        return Observable.fromIterable(getJavaFiles(folderPath))
                .subscribeOn(Schedulers.io())
                .flatMap(file -> Observable.fromCallable(() -> parseFile(file))
                        .onErrorResumeNext(error -> Observable.empty()))
                .flatMap(cu -> Observable.fromIterable(cu.findAll(ClassOrInterfaceDeclaration.class)))
                .map(clazz -> extractDependencies(clazz))
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

    private static CompilationUnit parseFile(File file) throws IOException {
        return StaticJavaParser.parse(file);
    }

    private static DependencyResult extractDependencies(ClassOrInterfaceDeclaration clazz) {
        String className = clazz.getNameAsString();
        Set<String> dependencies = new HashSet<>();

        // Estende o implementa
        clazz.getExtendedTypes().forEach(t -> dependencies.add(t.getNameAsString()));
        clazz.getImplementedTypes().forEach(t -> dependencies.add(t.getNameAsString()));

        // Tipi usati nei campi
        clazz.findAll(com.github.javaparser.ast.body.FieldDeclaration.class).forEach(field -> {
            dependencies.add(field.getElementType().asString());
        });

        // Tipi usati nei metodi (parametri e ritorni)
        clazz.findAll(com.github.javaparser.ast.body.MethodDeclaration.class).forEach(method -> {
            dependencies.add(method.getType().asString());
            method.getParameters().forEach(param -> dependencies.add(param.getType().asString()));
        });

        // Tipi usati nel corpo della classe (più approfondito, ma approssimato)
        clazz.findAll(com.github.javaparser.ast.type.Type.class).forEach(t -> {
            dependencies.add(t.asString());
        });

        return new DependencyResult(className, new ArrayList<>(dependencies));
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
