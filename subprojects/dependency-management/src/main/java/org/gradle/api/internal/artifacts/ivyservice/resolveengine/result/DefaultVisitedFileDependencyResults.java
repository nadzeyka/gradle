/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.artifacts.ivyservice.resolveengine.result;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.SetMultimap;
import org.gradle.api.artifacts.FileCollectionDependency;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.ArtifactSet;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.CompositeArtifactSet;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.FileDependencyArtifactSet;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.ResolvedArtifactSet;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.SelectedFileDependencyResults;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.VisitedFileDependencyResults;
import org.gradle.api.internal.artifacts.transform.VariantSelector;
import org.gradle.api.specs.Spec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultVisitedFileDependencyResults implements VisitedFileDependencyResults {
    private final SetMultimap<Long, FileDependencyArtifactSet> filesByNodeId;
    private final Map<FileCollectionDependency, FileDependencyArtifactSet> rootFiles;

    public DefaultVisitedFileDependencyResults(SetMultimap<Long, FileDependencyArtifactSet> filesByNodeId, Map<FileCollectionDependency, FileDependencyArtifactSet> rootFiles) {
        this.filesByNodeId = filesByNodeId;
        this.rootFiles = rootFiles;
    }

    @Override
    public SelectedFileDependencyResults select(Spec<? super ComponentIdentifier> componentFilter, VariantSelector selector) {
        // Wrap each file dependency in a set that performs variant selection and transformation
        // Also merge together the artifact sets for each configuration node
        ImmutableMap.Builder<Long, ResolvedArtifactSet> filesByConfigBuilder = ImmutableMap.builder();
        for (Long key : filesByNodeId.keySet()) {
            Set<FileDependencyArtifactSet> artifactsForConfiguration = filesByNodeId.get(key);
            List<ResolvedArtifactSet> selectedArtifacts = new ArrayList<ResolvedArtifactSet>(artifactsForConfiguration.size());
            for (ArtifactSet artifactSet : artifactsForConfiguration) {
                selectedArtifacts.add(artifactSet.select(componentFilter, selector));
            }
            filesByConfigBuilder.put(key, CompositeArtifactSet.of(selectedArtifacts));
        }
        ImmutableMap<Long, ResolvedArtifactSet> filesByConfig = filesByConfigBuilder.build();

        ResolvedArtifactSet allFiles = CompositeArtifactSet.of(filesByConfig.values());

        ImmutableMap.Builder<FileCollectionDependency, ResolvedArtifactSet> rootFilesBuilder = ImmutableMap.builder();
        for (Map.Entry<FileCollectionDependency, FileDependencyArtifactSet> entry : rootFiles.entrySet()) {
            rootFilesBuilder.put(entry.getKey(), entry.getValue().select(componentFilter, selector));
        }

        return new DefaultFileDependencyResults(rootFilesBuilder.build(), allFiles, filesByConfig);
    }

    private static class DefaultFileDependencyResults implements SelectedFileDependencyResults {
        private final Map<FileCollectionDependency, ResolvedArtifactSet> rootFiles;
        private final Map<Long, ResolvedArtifactSet> filesByConfiguration;
        private final ResolvedArtifactSet allArtifacts;

        DefaultFileDependencyResults(Map<FileCollectionDependency, ResolvedArtifactSet> rootFiles, ResolvedArtifactSet allArtifacts, Map<Long, ResolvedArtifactSet> filesByConfiguration) {
            this.rootFiles = rootFiles;
            this.allArtifacts = allArtifacts;
            this.filesByConfiguration = filesByConfiguration;
        }

        @Override
        public Map<FileCollectionDependency, ResolvedArtifactSet> getFirstLevelFiles() {
            return rootFiles;
        }

        @Override
        public ResolvedArtifactSet getArtifacts(long id) {
            ResolvedArtifactSet artifacts = filesByConfiguration.get(id);
            return artifacts == null ? ResolvedArtifactSet.EMPTY : artifacts;
        }

        @Override
        public ResolvedArtifactSet getArtifacts() {
            return allArtifacts;
        }
    }
}
