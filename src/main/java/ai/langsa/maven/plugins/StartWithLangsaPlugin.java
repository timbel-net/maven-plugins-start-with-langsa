/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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

    var gitTemplate = Paths.get(basedir, ".git", ".template.txt");
    if (notExists(gitTemplate)) {
      createGitTemplate(gitDir, gitTemplate);
      getLog().info("Generated beautiful git commit.message 🎉");
    }

    createHook("pre-commit");
    createHook("commit-msg");
  }

  private void createHook(String hook) {
    var basedir = project.getBasedir().getAbsolutePath();
    var hookTargetFile = Paths.get(basedir, ".git", "hooks", hook);
    if (notExists(hookTargetFile)) {
      createGitHookFile(hook, hookTargetFile);
      getLog().info("Generated annoying git hooks(" + hook + ") 🐶");
    }
  }

  private void createGitHookFile(String hook, Path hookFilePath) {
    var hookSource = this.getClass().getResource("/git-hooks-" + hook);
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

}
