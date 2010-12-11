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
package org.community.intellij.plugins.communitycase;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.ShortVcsRevisionNumber;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.SimpleHandler;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.StringTokenizer;

/**
 *  revision number
 */
public class RevisionNumber implements ShortVcsRevisionNumber {
  /**
   * the name of tip revision
   */
  @NonNls public static final String TIP = "LATEST";
  /**
   * the hash from 40 zeros representing not yet created commit
   */
  public static final String NOT_COMMITTED_HASH;

  static {
    char[] data = new char[40];
    Arrays.fill(data, '0');
    NOT_COMMITTED_HASH = new String(data);
  }

  /**
   * the revision number (40 character hashcode, tag, or reference). In some cases incomplete hashcode could be used.
   */
  @NotNull private final String myRevisionHash;
  /**
   * the date when revision created
   */
  @NotNull private final Date myTimestamp;

  /**
   * A constructor for TIP revision
   */
  public RevisionNumber() {
    // TODO review usages
    myRevisionHash = TIP;
    myTimestamp = new Date();
  }

  /**
   * A constructor from version. The current date is used.
   *
   * @param version the version number.
   */
  public RevisionNumber(@NonNls @NotNull String version) {
    // TODO review usages
    myRevisionHash = version;
    myTimestamp = new Date();
  }

  /**
   * A constructor from version and time
   *
   * @param version   the version number
   * @param timeStamp the time when the version has been created
   */
  public RevisionNumber(@NotNull String version, @NotNull Date timeStamp) {
    myTimestamp = timeStamp;
    myRevisionHash = version;
  }

  /**
   * {@inheritDoc}
   *
   * @see #getRev()
   */
  @NotNull
  public String asString() {
    return myRevisionHash;
  }

  @Override
  public String toShortString() {
    return toString();
  }

  /**
   * @return revision time
   */
  @NotNull
  public Date getTimestamp() {
    return myTimestamp;
  }

  /**
   * @return revision number
   */
  @NotNull
  public String getRev() {
    return myRevisionHash;
  }

  /**
   * @return the short revision number. The revision number likely unambiguously identify local revision, however in rare cases there could be conflicts.
   */
  @NotNull
  public String getShortRev() {
    if (myRevisionHash.length() == 0) return "";
    if (myRevisionHash.length() == 40) return myRevisionHash.substring(0, 8);
    if (myRevisionHash.length() > 40)  // revision string encoded with date too
    {
      return myRevisionHash.substring(myRevisionHash.indexOf("[") + 1, 8);
    }
    return myRevisionHash;
  }

  /**
   * {@inheritDoc}
   */
  public int compareTo(VcsRevisionNumber crev) {
    if (this == crev) return 0;

    if (crev instanceof RevisionNumber) {
      RevisionNumber other = (RevisionNumber)crev;
      if ((other.myRevisionHash != null) && myRevisionHash.equals(other.myRevisionHash)) {
        return 0;
      }

      if ((other.myRevisionHash.indexOf("[") > 0) && (other.myTimestamp != null)) {
        return myTimestamp.compareTo(other.myTimestamp);
      }

      // check for parent revs
      String otherName = null;
      String thisName = null;
      int otherParents = -1;
      int thisParent = -1;

      if (other.myRevisionHash.contains("~")) {
        int tildeIndex = other.myRevisionHash.indexOf('~');
        otherName = other.myRevisionHash.substring(0, tildeIndex);
        otherParents = Integer.parseInt(other.myRevisionHash.substring(tildeIndex));
      }

      if (myRevisionHash.contains("~")) {
        int tildeIndex = myRevisionHash.indexOf('~');
        thisName = myRevisionHash.substring(0, tildeIndex);
        thisParent = Integer.parseInt(myRevisionHash.substring(tildeIndex));
      }

      if (otherName == null && thisName == null) {
        final int result = myTimestamp.compareTo(other.myTimestamp);
        if (result == 0) {
          // it can NOT be 0 - it would mean that revisions are equal but they have different hash codes
          // but this is NOT correct. but we don't know here how to sort
          return myRevisionHash.compareTo(other.myRevisionHash);
        }
        return result;
      }
      else if (otherName == null) {
        return 1;  // I am an ancestor of the compared revision
      }
      else if (thisName == null) {
        return -1; // the compared revision is my ancestor
      }
      else {
        return thisParent - otherParents;  // higher relative rev numbers are older ancestors
      }
    }

    return -1;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if ((obj == null) || (obj.getClass() != getClass())) return false;

    RevisionNumber test = (RevisionNumber)obj;
    // TODO normalize revision string?
    return myRevisionHash.equals(test.myRevisionHash);
  }

  @Override
  public int hashCode() {
    return myRevisionHash.hashCode();
  }

  /**
   * @return a revision string that refers to the parent revision relatively
   *         to the current one. The  operator "~" is used. Note that in case of merges,
   *         the first revision of several will referred.
   */
  public String getParentRevisionStr() {
    String rev = myRevisionHash;
    int bracketIdx = rev.indexOf("[");
    if (bracketIdx > 0) {
      rev = myRevisionHash.substring(bracketIdx + 1, myRevisionHash.indexOf("]"));
    }

    int tildeIndex = rev.indexOf("~");
    if (tildeIndex > 0) {
      int n = Integer.parseInt(rev.substring(tildeIndex)) + 1;
      return rev.substring(0, tildeIndex) + "~" + n;
    }
    return rev + "~1";
  }

  /**
   * Create revision number from string
   *
   * @param rev a revision string
   * @return revision number (the same as {@link #RevisionNumber(String)}
   */
  public static RevisionNumber createRevision(String rev) {
    return new RevisionNumber(rev);
  }

  /**
   * Resolve revision number for the specified revision
   *
   * @param project a project
   * @param vcsRoot a vcs root
   * @param rev     a revision expression
   * @return a resolved revision number with correct time
   * @throws com.intellij.openapi.vcs.VcsException if there is a problem with running
   */
  public static RevisionNumber resolve(Project project, VirtualFile vcsRoot, @NonNls String rev) throws VcsException {
    SimpleHandler h = new SimpleHandler(project, vcsRoot, Command.REV_LIST);
    h.setRemote(true);
    h.setSilent(true);
    h.addParameters("--timestamp", "--max-count=1", rev);
    h.endOptions();
    StringTokenizer tokenizer = new StringTokenizer(h.run(), "\n\r \t", false);
    Date timestamp = Util.parseTimestamp(tokenizer.nextToken());
    return new RevisionNumber(tokenizer.nextToken(), timestamp);
  }

  @Override
  public String toString() {
    return myRevisionHash;
  }
}
