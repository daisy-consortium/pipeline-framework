package org.daisy.pipeline.persistence.job;

import java.net.URI;

import javax.xml.namespace.QName;

import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobResult;
import org.daisy.pipeline.job.JobUUIDGenerator;

import org.daisy.pipeline.persistence.Database;
import org.daisy.pipeline.persistence.job.PersistentOptionResult;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PersistentOptionResultTest {
	Database db;
	PersistentOptionResult pi1;
	String name="result";
	URI path=URI.create("file:/tmp/file.xml");
	String idx="file.xml";
	JobResult result;
	JobId id1;
	@Before	
	public void setUp(){
		System.setProperty("org.daisy.pipeline.iobase",System.getProperty("java.io.tmpdir"));
		result= new JobResult.Builder().withPath(path).withIdx(idx).build();
		db=DatabaseSupplier.getDatabase();
		id1= new JobUUIDGenerator().generateId();
		pi1=new PersistentOptionResult(id1,result, new QName(name));
		db.addObject(pi1);
	}	

	@After
	public void tearDown(){
		db.deleteObject(pi1);
	}	

	@Test
	public void optionName() throws Exception{
		PersistentOptionResult stored=db.getEntityManager().find(PersistentOptionResult.class,new PersistentOptionResult.PK(id1,idx));
		Assert.assertEquals(name,stored.getOptionName().toString());
	}
	
	@Test
	public void jobResult() throws Exception{
		PersistentOptionResult stored=db.getEntityManager().find(PersistentOptionResult.class,new PersistentOptionResult.PK(id1,idx));
		Assert.assertEquals(result,stored.getJobResult());
	}
	
}