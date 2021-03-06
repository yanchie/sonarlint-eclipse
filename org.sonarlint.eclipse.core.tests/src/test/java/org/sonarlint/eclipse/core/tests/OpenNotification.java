/*
 * SonarLint for Eclipse
 * Copyright (C) 2015-2020 SonarSource SA
 * sonarlint@sonarsource.com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarlint.eclipse.core.tests;

import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.sonarlint.eclipse.ui.internal.SonarLintImages;
import org.sonarlint.eclipse.ui.internal.popup.AbstractSonarLintPopup;

public class OpenNotification extends AbstractHandler {

  private static int counter = 0;

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    Display.getDefault().asyncExec(() -> {
      AbstractSonarLintPopup sonarLintPopup = new AbstractSonarLintPopup() {

        @Override
        protected String getMessage() {
          return "A very long message. A very long message. A very long message. A very long message. A very long message. A very long message. A very long message.";
        }

        @Override
        protected String getPopupShellTitle() {
          return "SonarCloud Notification" + counter++;
        }

        @Override
        protected Image getPopupShellImage(int maximumHeight) {
          return SonarLintImages.SONARCLOUD_SERVER_ICON_IMG;
        }

        @Override
        protected void createContentArea(Composite composite) {
          super.createContentArea(composite);

          addLink("Open in SonarCloud", e -> {
            this.close();
            try {
              PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL("https://google.fr"));
            } catch (PartInitException | MalformedURLException e1) {
              // ignore
            }
          });
        }

      };
      sonarLintPopup.setDelayClose(00);
      sonarLintPopup.open();
    });

    return null;
  }

}
