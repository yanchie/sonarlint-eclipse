/*
 * SonarLint for Eclipse
 * Copyright (C) 2015 SonarSource
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonarlint.eclipse.ui.internal.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.sonarlint.eclipse.core.internal.SonarLintCorePlugin;

public class UpdateCommand extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    new Job("Update SonarLint") {

      @Override
      protected IStatus run(IProgressMonitor monitor) {
        SonarLintCorePlugin.getDefault().getRunner().tryUpdate();
        return Status.OK_STATUS;
      }
    }.schedule();
    return null;
  }

}
