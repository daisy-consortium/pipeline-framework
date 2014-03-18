package org.daisy.pipeline.job;

import java.util.Collection;

import org.daisy.common.xproc.XProcEngine;
import org.daisy.pipeline.job.fuzzy.FuzzyJobFactory;
import org.daisy.pipeline.job.priority.PrioritizableRunnable;
import org.daisy.pipeline.job.priority.PriorityThreadPoolExecutor;
import org.daisy.pipeline.job.priority.timetracking.TimeFunctions;
import org.daisy.pipeline.job.priority.timetracking.TimeTrackerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;

/**
 * DefaultJobExecutionService is the defualt way to execute jobs
 */
public class DefaultJobExecutionService implements JobExecutionService,
                ExecutionQueue {

        /** The Constant logger. */
        private static final Logger logger = LoggerFactory
                        .getLogger(DefaultJobExecutionService.class);
        /** The xproc engine. */
        private XProcEngine xprocEngine;

        //TODO: get these sizes from properties
        private PriorityThreadPoolExecutor executor = PriorityThreadPoolExecutor
                        .newFixedSizeThreadPoolExecutor(
                                        2,
                                        TimeTrackerFactory.newFactory(5,
                                                        TimeFunctions.newLinearTimeFunctionFactory()));

        /** Creates fuzzy jobs out of jobs and runnables */
        private FuzzyJobFactory fuzzyJobFactory = FuzzyJobFactory
                        .newFuzzyJobFactory();

        /**
         * Sets the x proc engine.
         *
         * @param xprocEngine
         *            the new x proc engine
         */
        public void setXProcEngine(XProcEngine xprocEngine) {
                this.xprocEngine = xprocEngine;
        }

        /**
         * Activate (OSGI)
         */
        public void activate() {
                logger.trace("Activating job execution service");
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.daisy.pipeline.job.JobExecutionService#submit(org.daisy.pipeline.
         * job.Job)
         */
        @Override
        public void submit(final Job job) {
                //logger.info("Submitting job");
                //Make the runnable ready to submit to the fuzzy-prioritized thread pool
                this.executor.execute(this.fuzzyJobFactory.newFuzzyJob(job,
                                this.getRunnable(job)));
        }

        Runnable getRunnable(final Job job) {
                return new ThreadWrapper(new Runnable() {

                        @Override
                        public void run() {

                                try {
                                        logger.info("Starting to log to job's log file too:"
                                                        + job.getId().toString());
                                        MDC.put("jobid", job.getId().toString());
                                        job.run(xprocEngine);
                                        MDC.remove("jobid");
                                        logger.info("Stopping logging to job's log file");
                                } catch (Exception e) {
                                        throw new RuntimeException(e.getCause());
                                }

                        }
                });
        }

        /**
         * This class offers a solution to avoid memory leaks due to
         * the missuse of ThreadLocal variables.
         * The actual run implementation may be a little bit naive regarding the interrupt handling
         *
         */
        private static class ThreadWrapper implements Runnable {

                private static final Logger logger = LoggerFactory
                                .getLogger(ThreadWrapper.class);
                private Runnable runnable;

                /**
                 * Constructs a new instance.
                 *
                 * @param runnable The runnable for this instance.
                 */
                public ThreadWrapper(Runnable runnable) {
                        this.runnable = runnable;
                }

                public void run() {
                        logger.info("Starting wrappedThread :"
                                        + Thread.currentThread().getName());
                        Thread t = new Thread(this.runnable);
                        t.start();
                        try {
                                t.join();
                        } catch (InterruptedException e) {
                                logger.warn("ThreadWrapper was interrupted...");
                        }
                }

        }

        protected static Optional<PrioritizableRunnable> find(JobId id,Collection<PrioritizableRunnable> tasks){
                PrioritizedJob job;
                for(PrioritizableRunnable r:tasks){
                        job=(PrioritizedJob) r;
                        if(job.get().getId().equals(id)){
                                return Optional.of(r);
                        }

                }
                return Optional.absent();
        }
        @Override
        public void moveUp(JobId id) {
                Optional<PrioritizableRunnable> r=find(id,this.getExecutor().asCollection());
                if(r.isPresent()){
                        this.getExecutor().moveUp(r.get());
                }
        }

        @Override
        public void moveDown(JobId id) {
                Optional<PrioritizableRunnable> r=find(id,this.getExecutor().asCollection());
                if(r.isPresent()){
                        this.getExecutor().moveDown(r.get());
                }

        }

        @Override
        public void cancel(JobId id) {
                Optional<PrioritizableRunnable> r=find(id,this.getExecutor().asCollection());
                if(r.isPresent()){
                        this.getExecutor().remove(r.get());
                }

        }

        @Override
        public Collection<PrioritizedJob> getQueue() {

                return Collections2.transform(this.getExecutor().asOrderedCollection(),
                                new Function<PrioritizableRunnable, PrioritizedJob>() {

                                        @Override
                                        public PrioritizedJob apply(PrioritizableRunnable runnable) {
                                                return (PrioritizedJob)runnable;
                                        }
                });
        }

        protected PriorityThreadPoolExecutor getExecutor(){
                return this.executor;
        }
}
