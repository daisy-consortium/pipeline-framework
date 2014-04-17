package org.daisy.pipeline.job.impl;

import java.io.IOException;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.AbstractJobContext;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobResources;
import org.daisy.pipeline.script.BoundXProcScript;

public class MappingJobContext extends AbstractJobContext {

	public MappingJobContext(Client client,JobId id, String niceName,BoundXProcScript boundScript,JobResources collection) throws IOException{
		super(client,id,niceName,boundScript,JobURIUtils.newURIMapper(id));
		XProcDecorator decorator=XProcDecorator.from(this.getScript(),this.getMapper(),collection);
		setInput(decorator.decorate(this.getInputs()));
		setOutput(decorator.decorate(this.getOutputs()));
		this.generateResults=true;

	}

}