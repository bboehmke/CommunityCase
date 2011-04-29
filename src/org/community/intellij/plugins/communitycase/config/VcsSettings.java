package org.community.intellij.plugins.communitycase.config;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class VcsSettings {
  private final VcsProjectSettings myVcsProjectSettings;
  private final VcsApplicationSettings myVcsApplicationSettings;

  private VcsSettings(VcsProjectSettings vcsProjectSettings, VcsApplicationSettings vcsApplicationSettings) {
    this.myVcsProjectSettings=vcsProjectSettings;
    this.myVcsApplicationSettings=vcsApplicationSettings;
  }

  public static VcsSettings getInstance(Project project) {
    VcsProjectSettings vps=VcsProjectSettings.getInstance(project);
    VcsApplicationSettings vas=VcsApplicationSettings.getInstance();
    return new VcsSettings(vps,vas);
  }

  @NotNull
  public String getBranchFilter() {
    return myVcsProjectSettings.getBranchFilter();
  }

  public void setBranchFilter(String branchFilter) {
    myVcsProjectSettings.setBranchFilter(branchFilter);
  }

  public void setPathFilter(String pathFilter) {
    myVcsProjectSettings.setPathFilter(pathFilter);
  }

  @NotNull
  public String getPathFilter() {
    return myVcsProjectSettings.getPathFilter();
  }

  public boolean isPreserveKeepFiles() {
    return myVcsProjectSettings.isPreserveKeepFiles();
  }

  public void setPreserveKeepFiles(boolean preserveKeepFiles) {
    myVcsProjectSettings.setPreserveKeepFiles(preserveKeepFiles);
  }

  public boolean isUseReservedCheckoutForFiles() {
    return myVcsProjectSettings.isUseReservedCheckoutForFiles();
  }

  public void setUseReservedCheckoutForFiles(boolean useReserved) {
    myVcsProjectSettings.setUseReservedCheckoutForFiles(useReserved);
  }

  public boolean isUseReservedCheckoutForDirectories() {
    return myVcsProjectSettings.isUseReservedCheckoutForDirectories();
  }

  public void setUseReservedCheckoutForDirectories(boolean useReserved) {
    myVcsProjectSettings.setUseReservedCheckoutForDirectories(useReserved);
  }

  /**
   * @return the default executable name depending on the platform
   */
  public String getDefaultPathToExecutable() {
    return myVcsApplicationSettings.getDefaultPathToExecutable();
  }

  /**
   * @return get last set path or null
   */
  public String getPathToExecutable() {
    return myVcsApplicationSettings.getPathToExecutable();
  }

  /**
   * Change last set path to executable
   *
   * @param path the path
   */
  public void setPathToExecutable(String path) {
    myVcsApplicationSettings.setPathToExecutable(path);
  }

  public boolean getShowDirectories() {
    return true;
  }

  //todo wc we should probably remove all settings below this line, they probably don't work with ClearCase

  public ConversionPolicy getLineSeparatorsConversion() {
    return myVcsProjectSettings.getLineSeparatorsConversion();
  }

  public boolean getAskBeforeLineSeparatorConversion() {
    return myVcsProjectSettings.getAskBeforeLineSeparatorConversion();
  }

  public void setAskBeforeLineSeparatorConversion(boolean askBeforeLineSeparatorConversion) {
    myVcsProjectSettings.setAskBeforeLineSeparatorConversion(askBeforeLineSeparatorConversion);
  }

  public void setLineSeparatorsConversion(ConversionPolicy lineSeparatorsConversion) {
    myVcsProjectSettings.setLineSeparatorsConversion(lineSeparatorsConversion);
  }

  public UpdateType getUpdateType() {
    return myVcsProjectSettings.getUpdateType();
  }

  public UpdateChangesPolicy updateChangesPolicy() {
    return myVcsProjectSettings.updateChangesPolicy();
  }

  public void setUpdateType(UpdateType updateType) {
    myVcsProjectSettings.setUpdateType(updateType);
  }

  public void setUpdateChangesPolicy(UpdateChangesPolicy value) {
    myVcsProjectSettings.setUpdateChangesPolicy(value);
  }

  /**
   * The way the local changes are saved before update if user has selected auto-stash
   */
  public enum UpdateChangesPolicy {
    /**
     * Stash changes
     */
    STASH,
    /**
     * Shelve changes
     */
    SHELVE,
    /**
     * Keep files in working tree
     */
    KEEP
  }

  /**
   * The type of update to perform
   */
  public enum UpdateType {
    /**
     * Use default specified in the config file for the branch
     */
    BRANCH_DEFAULT,
    /**
     * Merge fetched commits with local branch
     */
    MERGE,
    /**
     * Rebase local commits upon the fetched branch
     */
    REBASE
  }

  /**
   * The CRLF conversion policy
   */
  public enum ConversionPolicy {
    /**
     * No conversion is performed
     */
    NONE,
    /**
     * The files are converted to project line separators
     */
    PROJECT_LINE_SEPARATORS
  }

}