package org.daisy.pipeline.job;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.pipeline.job.context.URITranslatorHelper;
import org.daisy.pipeline.script.XProcScript;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class URITranslatorHelperTest   {
	String testFile="dir/file.xml";

	XProcScript mscript;
	QName  optIn      ;
        QName  optReg     ;
        QName  optOutFile ;
        QName  optOutDir  ;
        QName  optOutNA  ;



	@Before 
	public void setUp(){

		mscript= new Mock.ScriptGenerator.Builder().withOptionOutputsNA(1).withOptionInputs(1).withOptionOther(1).withOptionOutputsDir(1).withOptionOutputsFile(1).build().generate();
		//options names
		optIn      = Mock.ScriptGenerator.getOptionInputName(0);
		optReg     = Mock.ScriptGenerator.getRegularOptionName(0);
		optOutFile = Mock.ScriptGenerator.getOptionOutputFileName(0);
		optOutDir  = Mock.ScriptGenerator.getOptionOutputDirName(0);
		optOutNA  = Mock.ScriptGenerator.getOptionOutputNAName(0);
	}
	@Test
	public void notEmpty(){
		Assert.assertTrue(URITranslatorHelper.notEmpty("hola"));
		Assert.assertFalse(URITranslatorHelper.notEmpty(""));
		Assert.assertFalse(URITranslatorHelper.notEmpty(null));
	}


	/**
	 * Tests 'getTranslatableOptionFilter'.
	 *
	 * @see org.daisy.pipeline.job.context.URITranslatorHelper#getTranslatableOptionFilter(XProcScript)
	 */
	@Test
	public void getTranslatableOptionFilter() throws Exception {

		List<XProcOptionInfo> infos=Lists.newLinkedList(mscript.getXProcPipelineInfo().getOptions());
		Collection<XProcOptionInfo> filtered=Collections2.filter(infos,URITranslatorHelper.getTranslatableOptionFilter(mscript));	
		Assert.assertEquals(4,filtered.size());
		//check we have the ones we expect
		Set<QName> names= Sets.newHashSet();
		names.add(optIn);
		names.add(optOutDir);
		names.add(optOutFile);
		names.add(optOutNA);
		for(XProcOptionInfo inf:filtered)
			Assert.assertTrue(String.format("Name %s should've been filtered out",inf.getName()),names.contains(inf.getName()));

			
	}


	/**
	 * Tests 'getOutputOptionFilter'.
	 *
	 * @see org.daisy.pipeline.job.context.URITranslatorHelper#getOutputOptionFilter(XProcScript)
	 */
	@Test
	public void getOutputOptionFilter() throws Exception {
		List<XProcOptionInfo> infos=Lists.newLinkedList(mscript.getXProcPipelineInfo().getOptions());
		Collection<XProcOptionInfo> filtered=Collections2.filter(infos,URITranslatorHelper.getOutputOptionFilter(mscript));	

		Assert.assertEquals(2,filtered.size());
		//check we have the ones we expect
		Set<QName> names= Sets.newHashSet();
		names.add(optOutDir);
		names.add(optOutFile);
		for(XProcOptionInfo inf:filtered)
			Assert.assertTrue(String.format("Name %s should've been filtered",inf.getName()),names.contains(inf.getName()));
	}

	/**
	 * Tests 'getTranslatableOutputOptionsFilter'.
	 *
	 * @see org.daisy.pipeline.job.context.URITranslatorHelper#getTranslatableOutputOptionsFilter(XProcScript)
	 */
	@Test
	public void getTranslatableOutputOptionsFilter() throws Exception {
		List<XProcOptionInfo> infos=Lists.newLinkedList(mscript.getXProcPipelineInfo().getOptions());
		Collection<XProcOptionInfo> filtered=Collections2.filter(infos,URITranslatorHelper.getTranslatableOutputOptionsFilter(mscript));	

		Assert.assertEquals(2,filtered.size());
		//check we have the ones we expect
		Set<QName> names= Sets.newHashSet();
		names.add(optOutDir);
		names.add(optOutFile);
		for(XProcOptionInfo inf:filtered)
			Assert.assertTrue(String.format("Name %s should've been filtered",inf.getName()),names.contains(inf.getName()));
	}

	/**
	 * Tests 'getTranslatableInputOptionsFilter'.
	 *
	 * @see org.daisy.pipeline.job.context.URITranslatorHelper#getTranslatableInputOptionsFilter(XProcScript)
	 */
	@Test
	public void getTranslatableInputOptionsFilter() throws Exception {
		List<XProcOptionInfo> infos=Lists.newLinkedList(mscript.getXProcPipelineInfo().getOptions());
		Collection<XProcOptionInfo> filtered=Collections2.filter(infos,URITranslatorHelper.getTranslatableInputOptionsFilter(mscript));	
		Assert.assertEquals(2,filtered.size());
		Assert.assertEquals(Lists.newArrayList(filtered).get(0).getName(),optIn);
		Assert.assertEquals(Lists.newArrayList(filtered).get(1).getName(),optOutNA);

	}
	
	@Test 
	public void getDynamicResultSupplierPartsNullSupplier() throws Exception{

		String outName = Mock.ScriptGenerator.getOutputName(0);
		String[] parts= URITranslatorHelper.getDynamicResultSupplierParts(outName,null,"");
		Assert.assertEquals(outName+"/"+outName,parts[0]);
		Assert.assertEquals(".xml",parts[1]);
		
	}
	@Test 
	public void getDynamicResultSupplierPartsEmpty() throws Exception{

		String outName = Mock.ScriptGenerator.getOutputName(0);
		String[] parts= URITranslatorHelper.getDynamicResultSupplierParts(outName,Mock.getResultSupplier(""),"");
		Assert.assertEquals(outName+"/"+outName,parts[0]);
		Assert.assertEquals(".xml",parts[1]);
		
	}
		
	@Test 
	public void getDynamicResultSupplierPartsFile() throws Exception{

		String outName = Mock.ScriptGenerator.getOutputName(0);
		String[] parts= URITranslatorHelper.getDynamicResultSupplierParts(outName,Mock.getResultSupplier("dir/file.opf"),"");
		Assert.assertEquals("dir/file",parts[0]);
		Assert.assertEquals(".opf",parts[1]);
		
	}

	@Test 
	public void getDynamicResultSupplierPartsFileNoExtension() throws Exception{

		String outName = Mock.ScriptGenerator.getOutputName(0);
		String[] parts= URITranslatorHelper.getDynamicResultSupplierParts(outName,Mock.getResultSupplier("dir/file"),"");
		Assert.assertEquals("dir/file",parts[0]);
		Assert.assertEquals("",parts[1]);
		
	}

	@Test 
	public void getDynamicResultSupplierPartsFileNoExtensionAndDotsInPath() throws Exception{

		String outName = Mock.ScriptGenerator.getOutputName(0);
		String[] parts= URITranslatorHelper.getDynamicResultSupplierParts(outName,Mock.getResultSupplier("di.r/file"),"");
		Assert.assertEquals("di.r/file",parts[0]);
		Assert.assertEquals("",parts[1]);
		
	}

	@Test 
	public void getDynamicResultSupplierPartsDir() throws Exception{

		String outName = Mock.ScriptGenerator.getOutputName(0);
		String[] parts= URITranslatorHelper.getDynamicResultSupplierParts(outName,Mock.getResultSupplier("dir/"),"");
		Assert.assertEquals("dir/"+outName,parts[0]);
		Assert.assertEquals(".xml",parts[1]);
		
	}
	
}
