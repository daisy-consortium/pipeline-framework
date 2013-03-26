package org.daisy.pipeline.modules;

import java.net.URI;
import java.util.Collection;

public interface ModuleBuilder {

	Module build();

	ModuleBuilder withName(String name);

	ModuleBuilder withLoader(ResourceLoader loader);

	ModuleBuilder withVersion(String version);

	ModuleBuilder withTitle(String title);

	ModuleBuilder withComponents(
			Collection<? extends Component> components);
	ModuleBuilder withEntities(
			Collection<? extends Entity> entities);

	ModuleBuilder withComponent(URI uri, String path);

}