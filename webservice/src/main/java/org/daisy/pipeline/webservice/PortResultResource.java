package org.daisy.pipeline.webservice;

import java.util.Collection;

import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobResult;

/**
 * The Class ResultResource.
 */
public class PortResultResource extends NamedResultResource{

	@Override
	protected Collection<JobResult> gatherResults(Job job, String name) {
		return job.getContext().getResults().getResults(name);
	}
}