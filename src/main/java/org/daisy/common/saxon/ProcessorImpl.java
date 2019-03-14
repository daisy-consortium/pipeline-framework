package org.daisy.common.saxon;

import java.util.Set;
import java.util.HashSet;

import javax.xml.transform.URIResolver;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.s9api.Processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "saxon-processor",
	service = { Processor.class }
)
public class ProcessorImpl extends Processor {
	
	private URIResolver uriResolver;
	private final HashSet<ExtensionFunctionDefinition> xpathExtensionFunctions
		= new HashSet<ExtensionFunctionDefinition>();
	
	@Reference(
		name = "ExtensionFunctionDefinition",
		unbind = "removeFunction",
		service = ExtensionFunctionDefinition.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void addFunction(ExtensionFunctionDefinition function) {
		logger.debug("Adding extension function definition {}", function.getFunctionQName().toString());
		xpathExtensionFunctions.add(function);
	}
	
	public void removeFunction(ExtensionFunctionDefinition function) {
		logger.debug("Removing extension function definition {}", function.getFunctionQName().toString());
		xpathExtensionFunctions.remove(function);
	}
	
	@Reference(
		name = "URIResolver",
		unbind = "-",
		service = URIResolver.class,
		cardinality = ReferenceCardinality.OPTIONAL,
		policy = ReferencePolicy.STATIC
	)
	public void setURIResolver(URIResolver resolver) {
		uriResolver = resolver;
	}
	
	public ProcessorImpl() {
		super(false);
	}
	
	@Activate
	public void activate() {
		Configuration config = getUnderlyingConfiguration();
		if (uriResolver != null)
			config.setURIResolver(uriResolver);
		for (Object function : xpathExtensionFunctions)
			config.registerExtensionFunction((ExtensionFunctionDefinition)function);
	}
	
	private static final Logger logger = LoggerFactory.getLogger(ProcessorImpl.class);
	
}
