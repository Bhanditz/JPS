package org.jetbrains.jps

import junit.framework.TestCase
import org.codehaus.gant.GantBinding
import org.jetbrains.jps.idea.IdeaProjectLoader;

/**
 * @author nik
 */
class JpsBuildTestCase extends TestCase {
  
  def doTest(String projectPath, Closure initProject, Closure expectedOutput) {
    def binding = new GantBinding()
    binding.includeTool << Jps
    def project = new Project(binding)
    new IdeaProjectLoader().loadFromPath(project, projectPath)
    initProject(project)
    def target = FileUtil.createTempDirectory("targetDir")
    project.targetFolder = target.absolutePath
    project.clean()
    project.makeAll()
    project.buildArtifacts()

    def root = new FileSystemItem(name: "<root>")
    initFileSystemItem(root, expectedOutput)
    root.assertDirectoryEqual(target, "");
  }

  def initFileSystemItem(FileSystemItem item, Closure initializer) {
    def meta = new InitializingExpando()
    meta.dir = {String name, Closure content ->
      def dir = new FileSystemItem(name: name, directory: true)
      initFileSystemItem(dir, content)
      item << dir
    }
    meta.archive = {String name, Closure content ->
      def archive = new FileSystemItem(name: name, archive: true)
      initFileSystemItem(archive, content)
      item << archive
    }
    meta.file = {Object[] args ->
      item << new FileSystemItem(name: args[0], content: args.length > 1 ? args[1] : null)
    }

    initializer.delegate = meta
    initializer.setResolveStrategy Closure.DELEGATE_FIRST
    initializer()
  }

}