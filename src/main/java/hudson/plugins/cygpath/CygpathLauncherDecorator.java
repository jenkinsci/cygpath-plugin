/*
 * The MIT License
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Seiji Sogabe, Stephen Connolly
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.cygpath;

import hudson.LauncherDecorator;
import hudson.Extension;
import hudson.Launcher;
import hudson.Proc;
import hudson.FilePath;
import hudson.remoting.Channel;
import hudson.model.Node;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * If we are on Windows, convert the path of the executable via Cygwin.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class CygpathLauncherDecorator extends LauncherDecorator {
    public Launcher decorate(final Launcher base, Node node) {
        if(base.isUnix())   return base;    // no decoration on Unix

        return new Launcher(base) {
            @Override
            public Proc launch(String[] cmd, String[] env, InputStream in, OutputStream out, FilePath workDir) throws IOException {
                return base.launch(cygpath(cmd),env,in,out,workDir);
            }

            @Override
            public Proc launch(String[] cmd, boolean[] mask, String[] env, InputStream in, OutputStream out, FilePath workDir) throws IOException {
                return base.launch(cygpath(cmd),mask,env,in,out,workDir);
            }

            @Override
            public Channel launchChannel(String[] cmd, OutputStream out, FilePath workDir, Map<String, String> envVars) throws IOException, InterruptedException {
                return base.launchChannel(cygpath(cmd),out,workDir,envVars);
            }

            @Override
            public void kill(Map<String, String> modelEnvVars) throws IOException, InterruptedException {
                base.kill(modelEnvVars);
            }

            private String[] cygpath(String[] cmds) throws IOException {
                try {
                    String exe = cmds[0];
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    if(base.launch(new String[]{"cygpath","-w",exe},new String[0],out,null).join()==0)
                        // replace by the converted path
                        cmds[0] = out.toString();
                } catch (InterruptedException e) {
                    // handle the interrupt later
                    Thread.currentThread().interrupt();
                }
                return cmds;
            }
        };
    }

}
