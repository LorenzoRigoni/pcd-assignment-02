import lib.DependecyAnalyserLib;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestDependecyAnalyserLib {
    private DependecyAnalyserLib dependecyAnalyserLib;

    @BeforeEach
    public void beforeEach() {
        this.dependecyAnalyserLib = new DependecyAnalyserLib();
    }

    @Test
    public void testClassDependencyAnalyser() {
        assertTrue(true);
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