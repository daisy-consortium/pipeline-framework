package org.daisy.pipeline.persistence.job;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import com.google.common.base.Supplier;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.common.xproc.XProcResult;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.context.AbstractJobContext;
import org.daisy.pipeline.job.context.JobContextFactory;
import org.daisy.pipeline.job.result.JobResult;
import org.daisy.pipeline.job.result.ResultSet;
import org.daisy.pipeline.job.util.RuntimeConfigurable;
import org.daisy.pipeline.script.BoundXProcScript;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.XProcScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name="job_contexts")
public final class PersistentJobContext extends AbstractJobContext implements Serializable,RuntimeConfigurable{
	public static final long serialVersionUID=1L;
	private static final Logger logger = LoggerFactory.getLogger(PersistentJobContext.class);
	@Id
	@Column(name="job_id")
	String sId;
	
	String logFile;

	String scriptUri;

	String sNiceName;


	@Embedded
	PersistentMapper pMapper;

	@OneToMany(cascade = CascadeType.ALL,fetch=FetchType.EAGER)
	//@JoinColumn(name="job_id",referencedColumnName="job_id")
	@MapsId("job_id")
	List<PersistentInputPort> inputPorts= new ArrayList<PersistentInputPort>();

	@OneToMany(cascade = CascadeType.ALL,fetch=FetchType.EAGER)
	//@JoinColumn(name="job_id",referencedColumnName="job_id")
	@MapsId("job_id")
	List<PersistentOption> options= new ArrayList<PersistentOption>();

	@OneToMany(cascade = CascadeType.ALL,fetch=FetchType.EAGER)
	@MapsId("job_id")
	//@JoinColumn(name="job_id",referencedColumnName="job_id")
	List<PersistentParameter> parameters= new ArrayList<PersistentParameter>();

	@OneToMany(cascade = CascadeType.ALL,fetch=FetchType.EAGER)
	@MapsId("job_id")
	//@JoinColumn(name="job_id",referencedColumnName="job_id")
	List<PersistentPortResult> portResults= new ArrayList<PersistentPortResult>();

	@OneToMany(cascade = CascadeType.ALL,fetch=FetchType.EAGER)
	@MapsId("job_id")
	//@JoinColumn(name="job_id",referencedColumnName="job_id")
	List<PersistentOptionResult> optionResults= new ArrayList<PersistentOptionResult>();

	public PersistentJobContext(AbstractJobContext ctxt) {
		super(ctxt.getId(),ctxt.getName(),BoundXProcScript.from(ctxt.getScript(),ctxt.getInputs(),ctxt.getOutputs()),ctxt.getMapper());
		this.sId=ctxt.getId().toString();
		if (ctxt.getLogFile()==null)
			this.logFile="";
		else
			this.logFile=ctxt.getLogFile().toString();
		this.scriptUri=ctxt.getScript().getURI().toString();
		this.setResults(ctxt.getResults());
		this.sNiceName=ctxt.getName();
		this.load();
	}

	/**
	 * Constructs a new instance.
	 */
	public PersistentJobContext() {
		super(null,"",null,null);
	}

	private void load(){
		logger.debug("coping the objects to the model ");
		//if(this.getScript()==null){
			//XProcScript xcript=registry.getScript(URI.create(this.scriptUri)).load();
			//logger.debug(String.format("load script %s",xcript));
			//this.setScript(xcript);//getScriptService(URI.create(this.scriptUri)).getScript();
		//}

		for( XProcPortInfo portName:this.getScript().getXProcPipelineInfo().getInputPorts()){
			PersistentInputPort anon=new PersistentInputPort(this.getId(),portName.getName());
			for (Supplier<Source> src:this.getInputs().getInputs(portName.getName())){
				anon.addSource(new PersistentSource(src.get().getSystemId()));
			}
			this.inputPorts.add(anon);
		}
		// options 
		for(QName option:this.getInputs().getOptions().keySet()){
			this.options.add(new PersistentOption(this.getId(),option,this.getInputs().getOptions().get(option)));
		}
		//parameters 
		for( String portName:this.getScript().getXProcPipelineInfo().getParameterPorts()){
			for (QName paramName :this.getInputs().getParameters(portName).keySet()){
				this.parameters.add(new PersistentParameter(this.getId(),portName,paramName,this.getInputs().getParameters(portName).get(paramName)));
			}
		}

		this.sId=this.getId().toString();
		this.pMapper=new PersistentMapper(this.getMapper());
		this.sNiceName=this.getName();
		//results 
		//everything is inmutable but this
		this.updateResults();	
	}


	@PostLoad
	public void postLoad(){
		logger.debug("Post loading jobcontext");
		if(this.getScript()==null && registry!=null){
				XProcScript xcript=registry.getScript(URI.create(this.scriptUri)).load();
				logger.debug(String.format("load script %s",xcript));
				this.setScript(xcript);//getScriptService(URI.create(this.scriptUri)).getScript();
		}
		//we have all the model but we have to hidrate the actual objects
		XProcInput.Builder builder= new XProcInput.Builder();	
		for ( PersistentInputPort input:this.inputPorts){
			for (PersistentSource src:input.getSources()){
				builder.withInput(input.getName(),src);
			}
		}
		for (PersistentOption option:this.options){
			builder.withOption(option.getName(),option.getValue());
		}
		for(PersistentParameter param:this.parameters){
			builder.withParameter(param.getPort(),param.getName(),param.getValue());
		}
		this.setInput(builder.build());
		this.setId(JobIdFactory.newIdFromString(this.sId));

		this.setMapper(this.pMapper.getMapper());

		ResultSet.Builder rBuilder=new ResultSet.Builder();

		for(PersistentPortResult pRes: this.portResults){
			rBuilder.addResult(pRes.getPortName(),pRes.getJobResult());
		}
		for(PersistentOptionResult pRes: this.optionResults){
			rBuilder.addResult(pRes.getOptionName(),pRes.getJobResult());
		}
		this.setResults(rBuilder.build());
		this.setLogFile(URI.create(this.logFile));
		this.setName(this.sNiceName);	
		//so the context is configured once it leaves to the real world.
		if (ctxtFactory!=null)
			ctxtFactory.configure(this);
		
	}

	private void updateResults(){
		ResultSet rSet= this.getResults();
		if(this.portResults.size()==0)
			for(String port:rSet.getPorts()){
				for(JobResult res:rSet.getResults(port)){
					this.portResults.add(new PersistentPortResult(this.getId(),res,port));
				}
			}
		if(this.optionResults.size()==0)
			for(QName option:rSet.getOptions()){
				logger.debug(String.format(" Persisting job context with # %d result options",rSet.getResults(option).size()));
				for(JobResult res:rSet.getResults(option)){
					this.optionResults.add(new PersistentOptionResult(this.getId(),res,option));
				}

			}
	}





	/**
	 * Gets the script for this instance.
	 *
	 * @return The script.
	 */
	 URI getScriptUri() {
		return URI.create(this.scriptUri);
	}




	@Override
	public void writeResult(XProcResult result) {
		//build the result set
		super.writeResult(result);
		//and make sure that the new values get stored
		this.updateResults();
				
	}
	//Configuration for adding runtime inforamtion	
	@Transient
	static ScriptRegistry registry;
	public static void setScriptRegistry(ScriptRegistry sregistry){
		registry=sregistry;
	}
	@Transient
	static JobContextFactory ctxtFactory;
	public static void setJobContextFactory(JobContextFactory jobContextFactory){
		PersistentJobContext.ctxtFactory=jobContextFactory;
	}
		
}
