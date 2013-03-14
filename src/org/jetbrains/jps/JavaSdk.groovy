package org.jetbrains.jps

/**
 * @author nik
 */
class JavaSdk extends Sdk implements IJavaSdk {
  String jdkPath

  def JavaSdk(Project project, String name, String jdkPath, Closure initializer) {
    super(project, name, initializer)
    this.jdkPath = jdkPath
  }

  String getJavacExecutable() {
    return jdkPath + File.separator + "bin" + File.separator + "javac";
  }

  String getJavaExecutable() {
    return jdkPath + File.separator + "bin" + File.separator + "java";
  }

  def getRuntimeRoots() {
    return getClasspathRoots(ClasspathKind.TEST_RUNTIME);
  }
}
