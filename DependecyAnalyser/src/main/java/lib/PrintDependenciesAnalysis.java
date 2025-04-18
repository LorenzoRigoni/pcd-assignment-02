package lib;

import java.nio.file.Path;

public class PrintDependenciesAnalysis {
    private static final DependencyAnalyserLib dependencyAnalyser = new DependencyAnalyserLib();
    private static final String CURRENT_PATH = System.getProperty("user.dir");

    public static void main(String[] args) {
        final Path classPath = Path.of(CURRENT_PATH + "\\src\\main\\java\\lib\\reports\\ClassDepsReport.java");

        dependencyAnalyser.getClassDependencies(classPath).onComplete(res -> {
            if(res.succeeded()) {
                System.out.println(res.result());
            } else
                System.out.println(res.cause().getMessage());

            System.exit(0);
        });
    }
}
