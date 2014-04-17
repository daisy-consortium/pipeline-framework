package org.daisy.pipeline.job;

import java.io.File;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.JobContext;
import org.daisy.pipeline.job.JobContextFactory;
import org.daisy.pipeline.job.RuntimeConfigurator;
import org.daisy.pipeline.job.impl.JobURIUtils;
import org.daisy.pipeline.script.BoundXProcScript;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
@RunWith(MockitoJUnitRunner.class)
public class JobContextFactoryTest   {
        @Mock RuntimeConfigurator runtimeConfigurator;
        @Mock Client client;
        @Mock JobContext mCtxt;

        @Mock BoundXProcScript boundScript;

	String oldIoBase="";
	File tmpdir;
        
        @Before
        public void setUp(){
		oldIoBase=System.getProperty(JobURIUtils.ORG_DAISY_PIPELINE_IOBASE);	
		tmpdir= new File(System.getProperty("java.io.tmpdir"));
		System.setProperty(JobURIUtils.ORG_DAISY_PIPELINE_IOBASE,tmpdir.toString());	
        }
        @After
        public void tearDown(){
		if(oldIoBase!=null)
			System.setProperty(JobURIUtils.ORG_DAISY_PIPELINE_IOBASE,oldIoBase);	
        }
        @Test
        public void mappingContext(){
                String name="nice name";
                JobContextFactory factory=Mockito.spy(new JobContextFactory(runtimeConfigurator,client));
                Mockito.doReturn(mCtxt).when(factory).newMappingJobContext(name,boundScript,null);

                JobContext ctxt=factory.newJobContext(true,name,boundScript,null);
                Mockito.verify(factory,Mockito.times(1)).newMappingJobContext(name,boundScript,null);

        }

        @Test
        public void nonMappingContext(){
                String name="nice name";
                JobContextFactory factory=Mockito.spy(new JobContextFactory(runtimeConfigurator,client));
                Mockito.doReturn(mCtxt).when(factory).newJobContext(name,boundScript);

                JobContext ctxt=factory.newJobContext(false,name,boundScript,null);
                Mockito.verify(factory,Mockito.times(1)).newJobContext(name,boundScript);

        }
}
