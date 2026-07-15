package com.github.emilienkia.klang.plugin;

import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl;
import com.intellij.util.ThrowableRunnable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.github.emilienkia.klang.plugin.language.psi.KlangFile;

/**
 * JUnit 5 (Jupiter) base class for K-lang PSI / resolution tests.
 *
 * <p>It boots a shared headless IntelliJ {@code Application} and a lightweight in-memory
 * project + {@link CodeInsightTestFixture} (created on the EDT). The K-lang language,
 * parser definition and reference contributor are picked up automatically from the
 * plugin's {@code META-INF/plugin.xml} on the test classpath.</p>
 *
 * <p>All PSI work must run on the EDT — use {@link #parse(String)} (which configures the
 * fixture on the EDT) and wrap any further PSI access in {@link #onEdt(ThrowableRunnable)}.</p>
 */
public abstract class KlangFixtureTestBase {

    protected CodeInsightTestFixture fixture;

    @BeforeEach
    final void setUpFixture() {
        onEdt(() -> {
            IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
            TestFixtureBuilder<IdeaProjectTestFixture> builder =
                    factory.createLightFixtureBuilder(LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR,
                            getClass().getSimpleName());
            fixture = factory.createCodeInsightFixture(builder.getFixture(),
                    new LightTempDirTestFixtureImpl(true));
            fixture.setUp();
        });
    }

    @AfterEach
    final void tearDownFixture() {
        onEdt(() -> fixture.tearDown());
    }

    /** Runs {@code runnable} on the EDT and rethrows any failure as an unchecked exception. */
    protected static void onEdt(ThrowableRunnable<? extends Throwable> runnable) {
        try {
            EdtTestUtil.runInEdtAndWait(runnable);
        } catch (Throwable t) {
            if (t instanceof RuntimeException re) throw re;
            if (t instanceof Error err) throw err;
            throw new RuntimeException(t);
        }
    }

    /**
     * Configures an in-memory {@code a.k} file with the given source and returns its
     * {@link KlangFile} root. Must be called on the EDT (e.g. inside {@link #onEdt}).
     */
    protected KlangFile parse(String source) {
        fixture.configureByText("a.k", source);
        return (KlangFile) fixture.getFile();
    }

    /**
     * Adds an extra {@code .k} file to the in-memory project (for multi-file tests) and
     * returns its {@link KlangFile} root. Must be called on the EDT.
     */
    protected KlangFile addFile(String path, String source) {
        return (KlangFile) fixture.addFileToProject(path, source);
    }
}



