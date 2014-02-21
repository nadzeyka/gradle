/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.api.internal.artifacts.ivyservice;

import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.Module;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.util.GUtil;

import java.util.Map;

public class IvyUtil {

    public static ModuleRevisionId createModuleRevisionId(Module module) {
        return createModuleRevisionId(module.getGroup(), module.getName(), module.getVersion());
    }

    public static ModuleRevisionId createModuleRevisionId(Dependency dependency) {
        return createModuleRevisionId(dependency.getGroup(), dependency.getName(), dependency.getVersion());
    }

    public static ModuleRevisionId createModuleRevisionId(String group, String name, String version) {
        return ModuleRevisionId.newInstance(emptyStringIfNull(group), name, emptyStringIfNull(version));
    }

    public static ModuleRevisionId createModuleRevisionId(ModuleVersionIdentifier id) {
        return ModuleRevisionId.newInstance(id.getGroup(), id.getName(), id.getVersion());
    }

    public static ModuleRevisionId createModuleRevisionId(ModuleRevisionId revId, String version) {
        return ModuleRevisionId.newInstance(revId, version);
    }

    private static String emptyStringIfNull(String value) {
        return GUtil.elvis(value, "");
    }

    public static ModuleRevisionId createModuleRevisionId(String org, String name, String branch, String rev, Map extraAttributes) {
        return createModuleRevisionId(org, name, branch, rev, extraAttributes, true);
    }

    public static ModuleRevisionId createModuleRevisionId(String org, String name, String branch, String revConstraint, Map extraAttributes, boolean replaceNullBranchWithDefault) {
        return ModuleRevisionId.newInstance(org, name, branch, revConstraint, extraAttributes, replaceNullBranchWithDefault);
    }

    public static ModuleId createModuleId(String org, String name) {
        return ModuleId.newInstance(org, name);
    }
}
