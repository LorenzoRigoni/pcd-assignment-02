import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import lib.DependencyAnalyserLib;
import lib.reports.ClassDepsReport;
import org.junit.jupiter.api.*;

import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestDependencyAnalyserLib {
    private static final String CURRENT_PATH = System.getProperty("user.dir");

    private DependencyAnalyserLib dependencyAnalyserLib;
    private VertxTestContext testContext;

    @BeforeEach
    public void beforeEach() {
        final Vertx vertx = Vertx.vertx();
        this.dependencyAnalyserLib = new DependencyAnalyserLib(vertx);
        this.testContext = new VertxTestContext();
    }

    @Test
    public void testClassDependencyAnalyser() {
        final Path classSrcFile = Path.of(CURRENT_PATH + "\\src\\main\\java\\lib\\reports\\ClassDepsReport.java");

        this.dependencyAnalyserLib.getClassDependencies(classSrcFile).onComplete(res -> {
            if (res.succeeded()) {
                final ClassDepsReport report = res.result();
                assertEquals("ClassDepsReport", report.getClassOrInterfaceName());
                assertEquals("lib.reports", report.getPackageName());
                assertEquals(Set.of("Set, String"), report.getDependencies());
                this.testContext.completeNow();
            } else {
                this.testContext.failNow(res.cause());
            }
        });
    }

    @Test
    public void testPackageDependencyAnalyser() {
        assertTrue(true);
    }

    @Test
    public void testProjectDependencyAnalyser() {
        assertTrue(true);
    }
}