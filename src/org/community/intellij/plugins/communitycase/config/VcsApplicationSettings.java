/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.community.intellij.plugins.communitycase.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NonNls;

import java.io.File;

/**
 * The application wide settings for the git
 */
@State(
  name = "Git.Application.Settings",
  storages = {@Storage(
    id = "Git.Application.Settings",
    file = "$APP_CONFIG$/vcs.xml")})
public class VcsApplicationSettings implements PersistentStateComponent<VcsApplicationSettings.State> {
  /**
   * the default executable
   */
  @NonNls static final String[] DEFAULT_WINDOWS_PATHS =
    {"C:\\Program Files\\ibm\\RationalSDLC\\ClearCase\\bin", "C:\\Program Files (x86)\\ibm\\RationalSDLC\\ClearCase\\bin"};
  /**
   * Windows executable name
   */
  @NonNls static final String DEFAULT_WINDOWS_GIT = "cleartool.exe";
  /**
   * Default UNIX paths
   */
  @NonNls static final String[] DEFAULT_UNIX_PATHS = {"/usr/local/bin", "/usr/bin", "/opt/local/bin", "/opt/bin"};
  /**
   * UNIX executable name
   */
  @NonNls static final String DEFAULT_UNIX_GIT = "cleartool";
  /**
   * The last used path to git
   */
  private String myPathToGit;

  public static VcsApplicationSettings getInstance() {
    return ServiceManager.getService(VcsApplicationSettings.class);
  }

  /**
   * @return the default executable name depending on the platform
   */
  public String defaultVcs() {
    if (myPathToGit == null) {
      String[] paths;
      String program;
      if (SystemInfo.isWindows) {
        program = DEFAULT_WINDOWS_GIT;
        paths = DEFAULT_WINDOWS_PATHS;
      }
      else {
        program = DEFAULT_UNIX_GIT;
        paths = DEFAULT_UNIX_PATHS;
      }
      for (String p : paths) {
        File f = new File(p, program);
        if (f.exists()) {
          myPathToGit = f.getAbsolutePath();
          break;
        }
      }
      if (myPathToGit == null) {
        // otherwise, hope it's in $PATH
        myPathToGit = program;
      }
    }
    return myPathToGit;
  }

  public State getState() {
    State s = new State();
    s.PATH_TO_GIT = myPathToGit;
    return s;
  }

  public void loadState(State state) {
    myPathToGit = state.PATH_TO_GIT == null ? defaultVcs() : state.PATH_TO_GIT;
  }

  /**
   * @return get last set path to git or null
   */
  public String getPathToVcs() {
    return myPathToGit == null ? defaultVcs() : myPathToGit;
  }

  /**
   * Change last set path to git (called on project settings save)
   * @param pathToGit the path to git
   */
  public void setPathToGit(String pathToGit) {
    myPathToGit = pathToGit;
  }

  /**
   * The settings state
   */
  public static class State {
    /**
     * The last saved path to git
     */
    public String PATH_TO_GIT;
  }
}
