package ai.langsa.maven.plugins;

import static java.nio.file.Files.notExists;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "initialize", defaultPhase = LifecyclePhase.VALIDATE)
@SuppressWarnings("unused")
public class StartWithLangsaPlugin extends AbstractMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  MavenProject project;

  @Override
  public void execute() {
    var basedir = project.getBasedir().getAbsolutePath();
    var gitDir = Paths.get(basedir, ".git");

    if (notExists(gitDir)) {
      getLog().warn("You are not set git yet. `git init` first!");
      return;
    }

    var isChangeVersion = notMatchVersion(gitDir.resolve(".version"));
    var gitTemplate = Paths.get(basedir, ".git", ".template.txt");
    if (isChangeVersion || notExists(gitTemplate)) {
      createGitTemplate(gitDir, gitTemplate);
      getLog().info("Generated beautiful git commit.message 🎉");
    }

    createHook("pre-commit", isChangeVersion);
    createHook("commit-msg", isChangeVersion);
  }

  private boolean notMatchVersion(Path version) {
    try {
      var v = Files.readString(version);
      if (v.equals(project.getVersion())) {
        return false;
      }
    } catch (IOException e) {
      // no work
    }

    try {
      Files.write(version, project.getVersion().getBytes());
    } catch (IOException e) {
      // no work
    }

    return true;
  }

  private void createHook(String hook, boolean isChangeVersion) {
    var basedir = project.getBasedir().getAbsolutePath();
    var hookTargetFile = Paths.get(basedir, ".git", "hooks", hook);
    if (isChangeVersion || notExists(hookTargetFile)) {
      createGitHookFile(hook, hookTargetFile);
      getLog().info("Generated annoying git hooks(" + hook + ") 🐶");
    }
  }

  private void createGitHookFile(String hook, Path hookFilePath) {
    var hookSource = this.getClass().getResource("/git-hooks-" + hook + (isWin() ? ".cmd" : ""));
    if (hookSource == null) {
      return;
    }

    try (var content = hookSource.openStream()) {
      Files.write(hookFilePath, content.readAllBytes());

      if (!hookFilePath.toFile().setExecutable(true, true)) {
        throw new IllegalStateException(hook + " hook file does not executable permission.");
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private void createGitTemplate(Path gitDir, Path gitTemplate) {
    var template = this.getClass().getResource("/git-commit-template.txt");
    if (template == null) {
      return;
    }

    try (var content = template.openStream()) {
      Files.write(gitTemplate, content.readAllBytes());

      var runtime = Runtime.getRuntime();
      runtime.exec(
        new String[]{"git", "config", "commit.template", ".git/.template.txt"},
          null,
          gitDir.toFile()
      );
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private boolean isWin() {
    return System.getProperty("os.name").startsWith("Windows");
  }
}
