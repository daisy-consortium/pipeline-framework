package org.daisy.pipeline.persistence.jobs;

import java.io.Serializable;

import javax.persistence.Embeddable;

import javax.xml.transform.Source;

import org.daisy.common.base.Provider;

@Embeddable
public class PersistentSource implements Source,Serializable,Provider<Source>{
	static final long serialVersionUID=98749124L;

	private String systemId;

	/**
	 * Constructs a new instance.
	 */
	public PersistentSource() {
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param systemId The systemId for this instance.
	 */
	public PersistentSource(String systemId) {
		this.systemId = systemId;
	}

	@Override
	public String getSystemId() {
		return systemId;
	}

	@Override
	public void setSystemId(String systemId) {
		this.systemId=systemId;
	}

	@Override
	public Source provide() {
		return this;
	}

}
