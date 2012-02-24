package org.jetbrains.jps.runConf.java

import org.jetbrains.jps.JpsBuildTestCase
import org.jetbrains.jps.Project
import org.jetbrains.jps.RunConfiguration
import org.jetbrains.jps.util.FileUtil

class JavaAppLauncherTest extends JpsBuildTestCase {
  public void test_simple() {
    runAndAssertOutput("MainClass", null, { output ->
      assertTrue(output, output.indexOf("arg1" + System.getProperty("line.separator")) != -1)
      assertTrue(output, output.indexOf("arg2") != -1)
    })
  }

  public void test_properties() {
    runAndAssertOutput("MainClassProperties", {JavaBasedRunConfigurationLauncher launcher ->
        launcher.addSystemProperties(["my.prop1" : "val1", "my.prop2" : "val2"])
      }, { output ->
        assertTrue(output, output.indexOf("val1" + System.getProperty("line.separator")) != -1)
        assertTrue(output, output.indexOf("val2") != -1)
    })
  }

  public void test_env_vars() {
    runAndAssertOutput("MainClassEnvVars", null, { output ->
      assertTrue(output, output.indexOf("val1" + System.getProperty("line.separator")) != -1)
      assertTrue(output, output.indexOf("val2") != -1)
    })
  }

  public void test_jvm_args() {
    runAndAssertOutput("MainClassVmArgs", null, { output ->
      assertTrue(output, output.indexOf("val1" + System.getProperty("line.separator")) != -1)
      assertTrue(output, output.indexOf("val2") != -1)
      assertTrue(output, output.indexOf("plugins/appLauncher/testData/main-class-run-conf") != -1)
    })
  }

  public void test_args_with_spaces() {
    runAndAssertOutput("MainClassComplexArgs", null, { output ->
      assertTrue(output, output.indexOf("argument with space1" + System.getProperty("line.separator")) != -1)
      assertTrue(output, output.indexOf("argument with space2" + System.getProperty("line.separator")) != -1)
      assertTrue(output, output.indexOf("aa") != -1)
      assertTrue(output, output.indexOf("bb") != -1)
    })
  }

  public void test_run_ant_target_before_launch() {
    runAndAssertOutput("MainClass & Ant", null, { output ->
      assertTrue(output, output.indexOf("arg1" + System.getProperty("line.separator")) != -1)
      assertTrue(output, output.indexOf("arg2") != -1)
      assertTrue(output, output.indexOf("example2 called!") != -1)
      assertTrue(output, output.indexOf("plugins" + File.separator + "appLauncher" + File.separator + "testData" + File.separator + "main-class-run-conf") != -1)
    })
  }

  public void test_run_ant_target_before_launch_target_not_specified() {
    runAndAssertOutput("MainClass & Ant (target not specified)", null, { output ->
      assertTrue(output, output.indexOf("arg1" + System.getProperty("line.separator")) != -1)
      assertTrue(output, output.indexOf("arg2") != -1)
      assertTrue(output, output.indexOf("example2 called!") == -1)
    })
  }

  private void runAndAssertOutput(String runConfName, Closure launcherInitializer, Closure assertions) {
    Project project = loadProject("plugins/appLauncher/testData/main-class-run-conf", [:]);

    RunConfiguration runConf = project.runConfigurations[runConfName];

    project.targetFolder = createTempDir().absolutePath;

    project.makeAll();

    File outFile = createTempFile();

    JavaAppLauncher launcher = new JavaAppLauncher();
    launcher.setOutputFile(outFile);

    if (launcherInitializer != null) {
      launcherInitializer(launcher);
    }

    launcher.start(runConf);

    String output = FileUtil.loadFileText(outFile);
    assertions(output);
  }
}
