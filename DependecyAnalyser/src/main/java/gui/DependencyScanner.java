package gui;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class DependencyScanner {

    public Observable<DependencyResult> analyzeDependencies(String folderPath) {
        // Creiamo un Observable che esegue l'analisi per tutti i file Java nella cartella
        return Observable.fromIterable(getJavaFiles(folderPath))
                .subscribeOn(Schedulers.io()) // L'analisi avviene su un thread separato
                .flatMap(file -> Observable.fromCallable(() -> parseFile(file))
                        .onErrorComplete())  // Ignoriamo errori nel parsing
                .map(DependencyScanner::analyzeDependencies)
                .observeOn(Schedulers.single()); // Restituiamo i risultati sul thread principale
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

    private static DependencyResult analyzeDependencies(CompilationUnit cu) {
        Optional<ClassOrInterfaceDeclaration> declaration = cu.findFirst(ClassOrInterfaceDeclaration.class);
        if (!declaration.isPresent()) return new DependencyResult("Unknown", Collections.emptyList());

        ClassOrInterfaceDeclaration c = declaration.get(); // c è la classe

        String className = c.getNameAsString();

        Set<String> deps = new HashSet<>();

        // Superclassi e interfacce
        c.getExtendedTypes().forEach(t -> deps.add(t.getNameAsString()));
        c.getImplementedTypes().forEach(t -> deps.add(t.getNameAsString()));

        // Tipi dei campi
        c.findAll(com.github.javaparser.ast.body.FieldDeclaration.class).forEach(field -> {
            deps.add(field.getElementType().asString());
        });

        return new DependencyResult(className, new ArrayList<>(deps));
    }

    public static class DependencyResult {
        String className;
        List<String> dependencies;

        public DependencyResult(String className, List<String> dependencies) {
            this.className = className;
            this.dependencies = dependencies;
        }
    }
}
