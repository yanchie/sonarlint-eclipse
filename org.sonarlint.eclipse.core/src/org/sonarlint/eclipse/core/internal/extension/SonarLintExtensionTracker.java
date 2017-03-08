/*
 * SonarLint for Eclipse
 * Copyright (C) 2015-2017 SonarSource SA
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
package org.sonarlint.eclipse.core.internal.extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IFilter;
import org.sonarlint.eclipse.core.SonarLintLogger;
import org.sonarlint.eclipse.core.configurator.ProjectConfigurator;
import org.sonarlint.eclipse.core.resource.ISonarLintFileFilter;
import org.sonarlint.eclipse.core.resource.ISonarLintProjectFilter;
import org.sonarlint.eclipse.core.resource.ISonarLintProjectsProvider;

public class SonarLintExtensionTracker implements IExtensionChangeHandler {

  private static final String ATTR_CLASS = "class"; //$NON-NLS-1$

  private final SonarLintEP<ProjectConfigurator> configuratorEp = new SonarLintEP<>("org.sonarlint.eclipse.core.projectConfigurators"); //$NON-NLS-1$
  private final SonarLintEP<ISonarLintProjectsProvider> projectsProviderEp = new SonarLintEP<>("org.sonarlint.eclipse.core.projectsProvider"); //$NON-NLS-1$
  private final SonarLintEP<ISonarLintFileFilter> fileFilterEp = new SonarLintEP<>("org.sonarlint.eclipse.core.fileFilter"); //$NON-NLS-1$
  private final SonarLintEP<ISonarLintProjectFilter> projectFilterEp = new SonarLintEP<>("org.sonarlint.eclipse.core.projectFilter"); //$NON-NLS-1$

  private static class SonarLintEP<G> {

    private final String id;
    private final Collection<G> instances = new ArrayList<>();

    public SonarLintEP(String id) {
      this.id = id;
    }
  }

  private final Collection<SonarLintEP<?>> allEps = Arrays.asList(configuratorEp, projectsProviderEp, fileFilterEp, projectFilterEp);

  private ExtensionTracker tracker;

  public void start() {
    IExtensionRegistry reg = Platform.getExtensionRegistry();
    tracker = new ExtensionTracker(reg);
    IExtensionPoint[] epArray = allEps.stream().map(ep -> reg.getExtensionPoint(ep.id)).toArray(IExtensionPoint[]::new);
    IFilter filter = ExtensionTracker.createExtensionPointFilter(epArray);
    tracker.registerHandler(this, filter);
    for (IExtensionPoint ep : epArray) {
      for (IExtension ext : ep.getExtensions()) {
        addExtension(tracker, ext);
      }
    }
  }

  public void close() {
    if (tracker != null) {
      tracker.close();
      tracker = null;
    }
  }

  @Override
  public void addExtension(IExtensionTracker tracker, IExtension extension) {
    IConfigurationElement[] configs = extension.getConfigurationElements();
    for (final IConfigurationElement element : configs) {
      try {
        for (SonarLintEP ep : allEps) {
          if (ep.id.equals(extension.getExtensionPointUniqueIdentifier())) {
            Object instance = element.createExecutableExtension(ATTR_CLASS);
            ep.instances.add(instance);
            // register association between object and extension with the tracker
            tracker.registerObject(extension, instance, IExtensionTracker.REF_WEAK);
            break;
          }
        }
      } catch (CoreException e) {
        SonarLintLogger.get().error("Unable to load one SonarLint extension", e);
      }
    }
  }

  @Override
  public void removeExtension(IExtension extension, Object[] objects) {
    // stop using objects associated with the removed extension
    for (SonarLintEP ep : allEps) {
      if (ep.id.equals(extension.getExtensionPointUniqueIdentifier())) {
        ep.instances.removeAll(Arrays.asList(objects));
        break;
      }
    }
  }

  public Collection<ProjectConfigurator> getConfigurators() {
    return configuratorEp.instances;
  }

  public Collection<ISonarLintProjectsProvider> getProjectsProviders() {
    return projectsProviderEp.instances;
  }

  public Collection<ISonarLintProjectFilter> getProjectFilters() {
    return projectFilterEp.instances;
  }

  public Collection<ISonarLintFileFilter> getFileFilters() {
    return fileFilterEp.instances;
  }

}
