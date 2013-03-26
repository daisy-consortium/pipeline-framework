package org.daisy.pipeline.job.result;

import java.io.File;

import java.net.URI;

import java.util.Collection;
import java.util.List;

import javax.xml.transform.Result;

import com.google.common.base.Supplier;

import org.daisy.common.transform.DynamicResultSupplier;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.common.xproc.XProcPortInfo;

import org.daisy.pipeline.job.context.JobContext;
import org.daisy.pipeline.job.context.URITranslatorHelper;
import org.daisy.pipeline.job.context.XProcDecorator;
import org.daisy.pipeline.job.context.XProcDecorator.TranslatableOption;
import org.daisy.pipeline.job.util.IOHelper;
import org.daisy.pipeline.job.util.URIMapper;
import org.daisy.pipeline.script.XProcScript;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class ResultSetFactory {


	public static ResultSet newResultSet(JobContext ctxt,URIMapper mapper){
		//go through the outputs write them add the uri's to the 
		//result object
		ResultSet.Builder builder = new ResultSet.Builder();
		ResultSetFactory.collectOutputs(ctxt.getScript(),ctxt.getOutputs(), mapper,builder);

		//go through the output options and add them, this is a bit more tricky 
		//as you have to check if the files exist
		//if your working with an anyURIDir then scan the directory to 
		//get all the files inside.
		ResultSetFactory.collectOptions(ctxt.getScript(), ctxt.getInputs(), mapper,builder);
		return builder.build();
	}

	static synchronized void collectOutputs(XProcScript script,XProcOutput outputs,URIMapper mapper,ResultSet.Builder builder){
		for (XProcPortInfo info: script.getXProcPipelineInfo().getOutputPorts()){
			Supplier<Result> prov= outputs.getResultSupplier(info.getName());

			if(prov==null)
				continue;

			List<JobResult> results=null;
			if(prov instanceof DynamicResultSupplier){
				results=buildJobResult((DynamicResultSupplier) prov,mapper);
			}else{
				results=buildJobResult(prov,mapper);
			}
			builder.addResults(info.getName(),results);
		}

	}
	//Non dymamic just one result will be returned in fact
	static List<JobResult> buildJobResult(Supplier<Result> supplier,URIMapper mapper){
		List<JobResult> jobs= Lists.newLinkedList();
		URI path=URI.create(supplier.get().getSystemId());
		jobs.add(singleResult(path,mapper));
		return jobs;
	}
	static List<JobResult> buildJobResult(DynamicResultSupplier supplier,URIMapper mapper){
		List<JobResult> jobs= Lists.newLinkedList();
		for( Result res: supplier.providedResults()){
			URI path=URI.create(res.getSystemId());
			jobs.add(singleResult(path,mapper));
		}
		return jobs;

	}

	static JobResult singleResult(URI path, URIMapper mapper){
		return new JobResult.Builder().withPath(path).withIdx(mapper.unmapOutput(path).toString()).build();
	};

	static void collectOptions(XProcScript script,XProcInput inputs,URIMapper mapper,ResultSet.Builder builder){
		Collection<XProcOptionInfo> optionInfos = Lists.newLinkedList(script.getXProcPipelineInfo().getOptions());
		//options which are translatable and outputs	
		Collection<XProcOptionInfo> options= Collections2.filter(optionInfos,URITranslatorHelper.getTranslatableOutputOptionsFilter(script));
		for(XProcOptionInfo option: options){
			if(inputs.getOptions().get(option.getName())==null)
				continue;
			//is file
			if(XProcDecorator.TranslatableOption.ANY_FILE_URI.getName().equals(script.getOptionMetadata(option.getName()).getType())){
				URI path=URI.create(inputs.getOptions().get(option.getName()));
				JobResult result= singleResult(path,mapper);
				builder.addResult(option.getName(),result);
				//is dir
			}else if (XProcDecorator.TranslatableOption.ANY_DIR_URI.getName().equals(script.getOptionMetadata(option.getName()).getType())){
				String dir=inputs.getOptions().get(option.getName());
				List<URI> ls=IOHelper.treeFileList(URI.create(dir));
				for(URI path: ls){
					JobResult result= singleResult(path,mapper);
					builder.addResult(option.getName(),result);
				}
			}
		}

	}


	
}