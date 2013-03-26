package org.daisy.pipeline.job.context;

import java.io.IOException;

import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobURIUtils;
import org.daisy.pipeline.job.ResourceCollection;
import org.daisy.pipeline.script.BoundXProcScript;

final class MappingJobContext extends AbstractJobContext {

	public MappingJobContext(JobId id, String niceName,BoundXProcScript boundScript,ResourceCollection collection) throws IOException{
		super(id,niceName,boundScript,JobURIUtils.newURIMapper(id));
		XProcDecorator decorator=XProcDecorator.from(this.getScript(),this.getMapper(),collection);
		setInput(decorator.decorate(this.getInputs()));
		setOutput(decorator.decorate(this.getOutputs()));

	}

}