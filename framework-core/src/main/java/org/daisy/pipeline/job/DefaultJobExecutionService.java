package org.daisy.pipeline.job;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.daisy.common.xproc.XProcEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


// TODO: Auto-generated Javadoc
/**
 * DefaultJobExecutionService is the defualt way to execute jobs
 */
public class DefaultJobExecutionService implements JobExecutionService {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(DefaultJobExecutionService.class);

	/** The xproc engine. */
	private XProcEngine xprocEngine;

	/**
	 * Sets the x proc engine.
	 *
	 * @param xprocEngine the new x proc engine
	 */
	public void setXProcEngine(XProcEngine xprocEngine) {
		// TODO make it dynamic
		this.xprocEngine = xprocEngine;
	}


	private final ExecutorService executor = Executors.newFixedThreadPool(5);

	/**
	 * Activate (OSGI)
	 */
	public void activate(){
		logger.trace("Activating job execution service");
	}

	/* (non-Javadoc)
	 * @see org.daisy.pipeline.job.JobExecutionService#submit(org.daisy.pipeline.job.Job)
	 */
	@Override
	public void submit(final Job job) {
		executor.submit(new Runnable() {

			@Override
			public void run() {
				try{
				logger.info("Starting to log to job's log file too:"+job.getId().toString());
				MDC.put("jobid", job.getId().toString());
				job.run(xprocEngine);
				MDC.remove("jobid");
				logger.info("Stopping to log to job's log file");
				}catch (Exception e) {
					throw new RuntimeException(e.getCause());
				}
			}
		});
	}
}