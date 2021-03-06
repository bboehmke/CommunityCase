/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
package org.community.intellij.plugins.communitycase.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import org.community.intellij.plugins.communitycase.Util;
import org.community.intellij.plugins.communitycase.Vcs;
import org.community.intellij.plugins.communitycase.ui.UiUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Basic abstract action handler for all actions to extend.
 */
public abstract class BasicAction extends DumbAwareAction {

  protected BasicAction() {
    super();
  }

  protected BasicAction(String text) {
    super(text);
  }

  protected BasicAction(String text, String description, Icon icon) {
    super(text, description, icon);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    final Project project = event.getData(PlatformDataKeys.PROJECT);
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        FileDocumentManager.getInstance().saveAllDocuments();
      }
    });
    final VirtualFile[] vFiles = event.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
    assert vFiles != null : "The action is only available when files are selected";

    assert project != null;
    final Vcs vcs = Vcs.getInstance(project);
    if (!ProjectLevelVcsManager.getInstance(project).checkAllFilesAreUnder(vcs, vFiles)) {
      return;
    }
    final String actionName = getActionName();

    final List<VcsException> exceptions = new ArrayList<VcsException>();
    final boolean background = perform(project, vcs, exceptions, vFiles);
    if (!background) {
      vcs.runInBackground(new Task.Backgroundable(project, getActionName()) {

        public void run(@NotNull ProgressIndicator indicator) {
          Util.refreshFiles(project, Arrays.asList(vFiles));
          com.intellij.util.ui.UIUtil.invokeLaterIfNeeded(new Runnable() {
              public void run() {
                  UiUtil.showOperationErrors(project, exceptions, actionName);
              }
          });
        }
      });
    }
  }


  /**
   * Perform the action over set of files
   *
   * @param project       the context project
   * @param mksVcs        the vcs instance
   * @param exceptions    the list of exceptions to be collected.
   * @param files the files to be affected by the operation
   * @return true if the operation scheduled a background job, or cleanup is not needed
   */
  protected abstract boolean perform(@NotNull Project project,
                                     Vcs mksVcs,
                                     @NotNull List<VcsException> exceptions,
                                     @NotNull VirtualFile[] files);

  /**
   * Perform the action over set of files in background
   *
   * @param project       the context project
   * @param exceptions    the list of exceptions to be collected.
   * @param affectedFiles the files to be affected by the operation
   * @param action        the action to be run in background
   * @return true value
   */
  protected boolean toBackground(final Project project,
                                 Vcs vcs,
                                 final VirtualFile[] affectedFiles,
                                 final List<VcsException> exceptions,
                                 final Consumer<ProgressIndicator> action) {
    vcs.runInBackground(new Task.Backgroundable(project, getActionName()) {
      
      public void run(@NotNull ProgressIndicator indicator) {
        action.consume(indicator);
        Util.refreshFiles(project, Arrays.asList(affectedFiles));
        com.intellij.util.ui.UIUtil.invokeLaterIfNeeded(new Runnable() {
            public void run() {
                UiUtil.showOperationErrors(project, exceptions, getActionName());
            }
        });
      }
    });
    return true;
  }

  /**
   * recursively adds all the children of file to the files list, for which
   * this action makes sense ({@link #appliesTo(Project, VirtualFile)}
   * returns true)
   *
   * @param project the project subject of the action
   * @param files   result list
   * @param file    the file whose children should be added to the result list
   *                (recursively)
   */
  private void addChildren(@NotNull Project project, @NotNull List<VirtualFile> files, @NotNull VirtualFile file) {
    VirtualFile[] children = file.getChildren();
    for (VirtualFile child : children) {
      if (!child.isDirectory() && appliesTo(project, child)) {
        files.add(child);
      }
      else if (child.isDirectory() && isRecursive()) {
        addChildren(project, files, child);
      }
    }
  }

  /**
   * @return the name of action (it is used in a number of ui elements)
   */
  @NotNull
  protected abstract String getActionName();


  /**
   * @return true if the action could be applied recursively
   */
  @SuppressWarnings({"MethodMayBeStatic"})
  protected boolean isRecursive() {
    return true;
  }

  /**
   * Check if the action is applicable to the file. The default checks if the file is a directory
   *
   * @param project the context project
   * @param file    the file to check
   * @return true if the action is applicable to the virtual file
   */
  @SuppressWarnings({"MethodMayBeStatic", "UnusedDeclaration"})
  protected boolean appliesTo(@NotNull Project project, @NotNull VirtualFile file) {
    return !file.isDirectory();
  }

  /**
   * Disable the action if the event does not apply in this context.
   *
   * @param e The update event
   */
  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);
    Presentation presentation = e.getPresentation();
    Project project = e.getData(PlatformDataKeys.PROJECT);
    if (project == null) {
      presentation.setEnabled(false);
      presentation.setVisible(false);
      return;
    }

    VirtualFile[] vFiles = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
    if (vFiles == null || vFiles.length == 0) {
      presentation.setEnabled(false);
      presentation.setVisible(true);
      return;
    }
    Vcs vcs = Vcs.getInstance(project);
    boolean enabled = ProjectLevelVcsManager.getInstance(project).checkAllFilesAreUnder(vcs, vFiles) && isEnabled(project, vcs, vFiles);
    // only enable action if all the targets are under the vcs and the action supports all of them

    presentation.setEnabled(enabled);
    presentation.setVisible(enabled);
  }

  /**
   * Check if the action should be enabled for the set of the fils
   *
   * @param project the context project
   * @param vcs     the vcs to use
   * @param vFiles  the set of files
   * @return true if the action should be enabled
   */
  protected abstract boolean isEnabled(@NotNull Project project, @NotNull Vcs vcs, @NotNull VirtualFile... vFiles);

  /**
   * Save all files in the application (the operation creates write action)
   */
  public static void saveAll() {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        FileDocumentManager.getInstance().saveAllDocuments();
      }
    });
  }
}
