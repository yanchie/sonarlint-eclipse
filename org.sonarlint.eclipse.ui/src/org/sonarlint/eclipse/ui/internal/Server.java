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
package org.sonarlint.eclipse.ui.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Server {
  public void start() throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress(63333), 0);
    server.createContext("/", new OpenFileRequestHandler());
    server.setExecutor(null); // creates a default executor
    server.start();
  }

  static class OpenFileRequestHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
        t.sendResponseHeaders(200, 0);
        OutputStream os = t.getResponseBody();
        os.close();

        Map<String, String> params = queryToMap(t.getRequestURI().getQuery());
        String projectName = params.get("projectName");
        String fileName = params.get("fileName");
        int lineNumber = Integer.parseInt(params.get("lineNumber"));

        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        if (project != null) {
          IResource resource = project.findMember(fileName);

          if (resource instanceof IFile) {
            IFile file = (IFile) resource;
            Display.getDefault().asyncExec(() -> {
              IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
              try {
                IEditorPart editorPart = IDE.openEditor(page, file);
                if (editorPart instanceof ITextEditor) {
                  ITextEditor textEditor = (ITextEditor) editorPart;
                  IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
                  int lineOffset = document.getLineOffset(lineNumber - 1);
                  textEditor.selectAndReveal(lineOffset, 0);
                  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().forceActive();
                }
              } catch (PartInitException | BadLocationException e) {
                e.printStackTrace();
              }
            });
          }
        }
    }

    public Map<String, String> queryToMap(String query) {
      Map<String, String> result = new HashMap<>();
      for (String param : query.split("&")) {
          String[] entry = param.split("=");
          if (entry.length > 1) {
              result.put(entry[0], entry[1]);
          }else{
              result.put(entry[0], "");
          }
      }
      return result;
  }
  }
}
