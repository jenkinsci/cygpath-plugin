package hudson.plugins.cygpath;

import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.FreeStyleBuild;
import hudson.tasks.Shell;
import org.jvnet.hudson.test.HudsonTestCase;

/**
 * @author Kohsuke Kawaguchi
 */
public class CygpathTest extends HudsonTestCase {
    /**
     * Verify that it does no harm on Unix.
     */
    public void testOnUnix() throws Exception {
        if(Hudson.isWindows())  return; // can't test

        FreeStyleProject p = createFreeStyleProject();
        p.getBuildersList().add(new Shell("echo abc"));
        assertBuildStatusSuccess(p.scheduleBuild2(0).get());
    }

    /**
     * Verify that it does the conversion on Cygwin
     */
    public void testOnCygwin() throws Exception {
        if(!Hudson.isWindows())  return; // can't test

        hudson.getDescriptorByType(Shell.DescriptorImpl.class).setShell("/bin/sh");
        FreeStyleProject p = createFreeStyleProject();
        p.getBuildersList().add(new Shell("echo abc"));
        FreeStyleBuild b = assertBuildStatusSuccess(p.scheduleBuild2(0).get());
        // verify that a conversion happened
        assertLogContains("cygwin",b);
    }
}
