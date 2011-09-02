package org.daisy.common.xproc.calabash;

import java.net.URI;

import javax.xml.transform.URIResolver;

import org.daisy.calabash.XProcConfigurationFactory;
import org.daisy.common.xproc.XProcEngine;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcPipeline;
import org.daisy.common.xproc.XProcPipelineInfo;
import org.daisy.common.xproc.XProcResult;
import org.xml.sax.EntityResolver;

//TODO check thread safety
public final class CalabashXProcEngine implements XProcEngine {

	private URIResolver uriResolver = null;
	private EntityResolver entityResolver = null;
	private XProcConfigurationFactory configFactory = null;

	public CalabashXProcEngine() {
		// FIXME: default entity resolver
		entityResolver = new CalabashXprocEntityResolver();
	}

	@Override
	public XProcPipeline load(URI uri) {
		if (configFactory == null) {
			throw new IllegalStateException(
					"Calabash configuration factory unavailable");
		}
		return new CalabashXProcPipeline(uri, configFactory, uriResolver,
				entityResolver);
	}

	@Override
	public XProcPipelineInfo getInfo(URI uri) {
		return load(uri).getInfo();
	}

	@Override
	public XProcResult run(URI uri, XProcInput data) {
		return load(uri).run(data);
	}

	public void setConfigurationFactory(XProcConfigurationFactory configFactory) {
		this.configFactory = configFactory;
	}

	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}

	public void setUriResolver(URIResolver uriResolver) {
		this.uriResolver = uriResolver;
	}

}
