/*
 * Copyright 2012 - present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.initializr.generator.spring.properties;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import io.spring.initializr.generator.buildsystem.SourceSet;
import io.spring.initializr.generator.project.contributor.ProjectContributor;
import io.spring.initializr.generator.spring.properties.ApplicationProperties.SectionKey;
import org.jspecify.annotations.Nullable;

/**
 * Base {@link ProjectContributor} that contributes application configuration files to a
 * project. A file is written per source set and Spring profile that has properties,
 * following the {@code src/{sourceSet}/resources/application[-{profile}]} convention. The
 * file for the main source set and the default profile is always written, even if it is
 * empty.
 *
 * @author Denis A. Altoé Falqueto
 * @see ApplicationProperties#section(SourceSet, String)
 */
public abstract class AbstractApplicationPropertiesContributor implements ProjectContributor {

	private final ApplicationProperties properties;

	private final String extension;

	protected AbstractApplicationPropertiesContributor(ApplicationProperties properties, String extension) {
		this.properties = properties;
		this.extension = extension;
	}

	@Override
	public void contribute(Path projectRoot) throws IOException {
		writeSection(projectRoot, SourceSet.MAIN, null, this.properties);
		for (Map.Entry<SectionKey, ApplicationProperties> entry : this.properties.getSections().entrySet()) {
			ApplicationProperties section = entry.getValue();
			if (!section.isEmpty()) {
				writeSection(projectRoot, entry.getKey().sourceSet(), entry.getKey().profile(), section);
			}
		}
	}

	/**
	 * Writes the given properties using the given writer.
	 * @param properties the properties to write
	 * @param writer the writer to use
	 */
	protected abstract void write(ApplicationProperties properties, PrintWriter writer);

	private void writeSection(Path projectRoot, SourceSet sourceSet, @Nullable String profile,
			ApplicationProperties properties) throws IOException {
		Path output = projectRoot.resolve(resolveFileName(sourceSet, profile));
		if (!Files.exists(output)) {
			Files.createDirectories(output.getParent());
			Files.createFile(output);
		}
		try (PrintWriter writer = new PrintWriter(Files.newOutputStream(output, StandardOpenOption.APPEND), false,
				StandardCharsets.UTF_8)) {
			write(properties, writer);
		}
	}

	private String resolveFileName(SourceSet sourceSet, @Nullable String profile) {
		String profileSuffix = (profile != null) ? "-" + profile : "";
		return "src/%s/resources/application%s.%s".formatted(sourceSet.getDirectoryName(), profileSuffix,
				this.extension);
	}

}
