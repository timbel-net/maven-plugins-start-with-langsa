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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "initialize", defaultPhase = LifecyclePhase.INITIALIZE)
@SuppressWarnings("unused")
public class StartWithLangsaPlugin extends AbstractMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  MavenProject project;

  @Override
  public void execute() {
    var basedir = project.getBasedir().getAbsolutePath();
    var gitDir = Paths.get(basedir, ".git");

    if (Files.notExists(gitDir)) {
      getLog().warn("You are not set git yet. `git init` first!");
      return;
    }

    var gitTemplate = Paths.get(basedir, ".git", ".template.txt");
    if (Files.notExists(gitTemplate)) {
      createGitTemplate(gitDir, gitTemplate);
      getLog().info("Generated beautiful git commit.message 🎉");
    }

    var gitHookCommitMsg = Paths.get(basedir, ".git", "hooks", "commit-msg");
    if (Files.notExists(gitHookCommitMsg)) {
      createGitHookCommitMsg(gitHookCommitMsg);
      getLog().info("Generated annoying git hooks(commit-msg) 🐶");
    }

  }

  private void createGitHookCommitMsg(Path gitHookCommitMsg) {
    var hooks = this.getClass().getResource("/git-hooks-commit-msg");
    if (hooks == null) {
      return;
    }

    try (var content = hooks.openStream()) {
      Files.write(gitHookCommitMsg, content.readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
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
      throw new RuntimeException(e);
    }
  }

}
