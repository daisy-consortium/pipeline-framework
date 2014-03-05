package org.daisy.pipeline.job;

import org.daisy.common.properties.PropertyPublisher;
import org.daisy.common.properties.PropertyPublisherFactory;
import org.daisy.pipeline.clients.Client;

public class JobManagerFactory {
        private JobStorage storage;
        private JobExecutionService executionService;
        private RuntimeConfigurator runtimeConfigurator;
        
        public JobManager createFor(Client client){
                return new DefaultJobManager(this.storage.filterBy(client),
                                executionService,new JobContextFactory(this.runtimeConfigurator,client));
        }

        /**
         * @param storage the storage to set
         */
        public void setStorage(JobStorage storage) {
                //TODO: check null
                this.storage = storage;
        }

        /**
         * @param executionService the executionService to set
         */
        public void setExecutionService(JobExecutionService executionService) {
                //TODO:check null
                this.executionService = executionService;
        }

        public void setRuntimeConfigurator(RuntimeConfigurator configurator){
                //TODO: check null
                //              logger.debug("setting monitor factory");
                this.runtimeConfigurator=configurator;
        }

        //FIXME: probably move these two methods somewhere else, maybe a dummy class for the framework just tu publish this.
        public void setPropertyPublisherFactory(PropertyPublisherFactory propertyPublisherFactory){
                PropertyPublisher propertyPublisher=propertyPublisherFactory.newPropertyPublisher();    
                //the property publishing step goes here
                propertyPublisher.publish("org.daisy.pipeline.iobase" ,System.getProperty("org.daisy.pipeline.iobase","" ),this.getClass());
                propertyPublisher.publish("org.daisy.pipeline.home" ,System.getProperty("org.daisy.pipeline.home","" ),this.getClass());
                propertyPublisher.publish("org.daisy.pipeline.logdir",System.getProperty("org.daisy.pipeline.logdir","" ),this.getClass());
        }

        public void unsetPropertyPublisherFactory(PropertyPublisherFactory propertyPublisherFactory){
                PropertyPublisher propertyPublisher=propertyPublisherFactory.newPropertyPublisher();    
                //the property unpublishing step goes here
                propertyPublisher.unpublish("org.daisy.pipeline.iobase" ,  this.getClass());
                propertyPublisher.unpublish("org.daisy.pipeline.home"   ,  this.getClass());
                propertyPublisher.unpublish("org.daisy.pipeline.logdir" ,  this.getClass());

        }
}
