package com.pehrs.vespa.yql.plugin.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.jetbrains.idea.maven.execution.MavenRunner;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;

public class IdeProjectUtils {

  public static boolean isApplicationSrcDir(VirtualFile dirNode) {
    VirtualFile[] children = dirNode.getChildren();
    return Arrays.stream(children)
        .anyMatch(vf -> vf.getName().equals("services.xml") && !vf.isDirectory())
        && Arrays.stream(children)
        .anyMatch(vf -> vf.getName().equals("schemas") && vf.isDirectory());
  }

  public static boolean isVespaJavaAppProject(Project project) {
    VirtualFile projectRoot = project.getProjectFile().getParent().getParent();
    return isVespaJavaAppProject(projectRoot);
  }

  public static boolean isVespaJavaAppProject(VirtualFile projectRoot) {
    VirtualFile[] children = projectRoot.getChildren();

    Optional<VirtualFile> applicationFound =
        Arrays.stream(children).filter(vf -> vf.getName().equals("src"))
            .flatMap(vf -> Arrays.stream(vf.getChildren())
                .filter(child -> child.getName().equals("main")))
            .flatMap(src -> Arrays.stream(src.getChildren())
                .filter(vf -> vf.getName().equals("application"))
            )
            .findFirst();

    return applicationFound.map(vf -> isApplicationSrcDir(vf)).orElse(false);
  }

  public static Optional<VirtualFile> getJavaProjectAppDir(VirtualFile projectRoot) {
    return Arrays.stream(projectRoot.getChildren())
        .filter(rootChild -> rootChild.getName().equals("target"))
        .flatMap(rootChild -> Arrays.stream(rootChild.getChildren())
            .filter(targetChild -> targetChild.getName().equals("application"))
        ).findFirst();

  }

  public static VirtualFile getProjectRootDir(Project project) {
    return project.getProjectFile().getParent().getParent();
  }

  public static Optional<VirtualFile> getServicesXmlFile(VirtualFile projectRoot) {
    return Arrays.stream(projectRoot.getChildren())
        .filter(rootChild -> rootChild.getName().equals("src"))
        .flatMap(root -> Arrays.stream(root.getChildren())
            .filter(targetChild -> targetChild.getName().equals("main"))
            .flatMap(main -> Arrays.stream(main.getChildren())
                .filter(mainChild -> mainChild.getName().equals("application"))
                .flatMap(app -> Arrays.stream(app.getChildren())
                    .filter(appChild -> appChild.getName().equals("services.xml"))
                )
            )
        ).findFirst();

  }

  public static void mavenRunPackage(Project project, Runnable onComplete) {
    VirtualFile projectRoot = project.getProjectFile().getParent().getParent();
    YqlAppSettingsState appSettings = YqlAppSettingsState.getInstance();
    MavenRunnerParameters parameters = new MavenRunnerParameters(
        true,
        projectRoot.getCanonicalPath(),
        (String) null,
        Arrays.stream(appSettings.mavenParameters.split(" ")).toList(),
        (Collection<String>) null
    );
    MavenRunnerSettings settings = new MavenRunnerSettings();
    MavenRunner runner = MavenRunner.getInstance(project);
    runner.run(parameters, settings, () -> {
      onComplete.run();
    });
  }
}
