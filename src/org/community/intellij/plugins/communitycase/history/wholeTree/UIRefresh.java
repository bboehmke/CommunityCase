/*
 * Copyright 2000-2010 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.community.intellij.plugins.communitycase.history.wholeTree;

import com.intellij.openapi.vcs.CalledInAwt;
import com.intellij.openapi.vfs.VirtualFile;
import org.community.intellij.plugins.communitycase.history.browser.SymbolicRefs;

/**
* @author irengrig
*/
public interface UIRefresh {
  @CalledInAwt
  void linesReloaded();
  @CalledInAwt
  void detailsLoaded();
  void acceptException(final Exception e);

  void reportSymbolicRefs(VirtualFile root, SymbolicRefs symbolicRefs);
}
