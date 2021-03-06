package org.daisy.pipeline.persistence.impl.job;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcResult;
import org.daisy.pipeline.job.AbstractJobContext;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobMonitorFactory;
import org.daisy.pipeline.job.JobResultSet;
import org.daisy.pipeline.job.URIMapper;
import org.daisy.pipeline.persistence.impl.webservice.PersistentClient;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.script.XProcScriptService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class persists job contexts.
 * The general idea is to write getters and setters (Access property)  when possible
 * to make proxification as transparent as possible.
 * Complex depenedencies of the context like ports, options, mapper
 * and results are wrapped in their own persistent objects so in this
 * case they're are persisted as fields. 
 * @author Javier Asensio Cubero capitan.cambio@gmail.com
 */
@Entity
@Table(name="job_contexts")
@Access(AccessType.FIELD)
public final class PersistentJobContext extends AbstractJobContext {
        public static final long serialVersionUID=1L;
        private static final Logger logger = LoggerFactory.getLogger(PersistentJobContext.class);


        //embedded mapper
        @Embedded
        private PersistentMapper pMapper;
        @ManyToOne
        private PersistentClient pClient;
        public static final String MODEL_CLIENT = "pClient";

        @OneToMany(cascade = CascadeType.ALL,fetch=FetchType.EAGER)
        @MapsId("job_id")
        private List<PersistentInputPort> inputPorts= new ArrayList<PersistentInputPort>();

        @OneToMany(cascade = CascadeType.ALL,fetch=FetchType.EAGER)
        //@JoinColumn(name="job_id",referencedColumnName="job_id")
        @MapsId("job_id")
        private List<PersistentOption> options= new ArrayList<PersistentOption>();

        @OneToMany(cascade = CascadeType.ALL,fetch=FetchType.EAGER)
        @MapsId("job_id")
        //@JoinColumn(name="job_id",referencedColumnName="job_id")
        private List<PersistentParameter> parameters= new ArrayList<PersistentParameter>();

        @OneToMany(cascade = CascadeType.ALL,fetch=FetchType.EAGER)
        @MapsId("job_id")
        //@JoinColumn(name="job_id",referencedColumnName="job_id")
        private List<PersistentPortResult> portResults= new ArrayList<PersistentPortResult>();

        @OneToMany(cascade = CascadeType.ALL,fetch=FetchType.EAGER)
        @MapsId("job_id")
        //@JoinColumn(name="job_id",referencedColumnName="job_id")
        private List<PersistentOptionResult> optionResults= new ArrayList<PersistentOptionResult>();

        PersistentJobContext(AbstractJobContext ctxt) {
                super(ctxt);
                // Map complex objects to their Persistent representation
                logger.debug("coping the objects to the model ");
                this.pMapper = new PersistentMapper(this.resultMapper);
                this.inputPorts = ContextHydrator.dehydrateInputPorts(this.getId(), this.getScript(), this.input);
                this.options = ContextHydrator.dehydrateOptions(this.getId(), this.input);
                this.parameters = ContextHydrator.dehydrateParameters(this.getId(), this.getScript(), this.input);
                this.pClient = (PersistentClient)this.getClient();
                //everything is inmutable but this
                this.updateResults();
        }

        @SuppressWarnings("unused") // used by jpa
        private PersistentJobContext() {
                super();
        }

        /**
         * Although we could delegate the actual hydration
         * to setters (i.e. getInput) the performance would be affected.
         * Therefore we prefer doing hydration on the PostLoad event.
         */
        @PostLoad
        @SuppressWarnings("unused")//jpa only
        private void postLoad(){
                logger.debug("Post loading jobcontext");
                //we have all the model but we have to hidrate the actual objects
                XProcInput.Builder builder=new XProcInput.Builder();
                ContextHydrator.hydrateInputPorts(builder,inputPorts);
                ContextHydrator.hydrateOptions(builder,options);
                ContextHydrator.hydrateParams(builder,parameters);
                this.input = builder.build();

                this.resultMapper = this.pMapper.getMapper();
                this.client = this.pClient;

                JobResultSet.Builder rBuilder=new JobResultSet.Builder();
                ContextHydrator.hydrateResultPorts(rBuilder,portResults);
                ContextHydrator.hydrateResultOptions(rBuilder,optionResults);
                this.results = rBuilder.build();
        }

        private void updateResults(){
                if(this.portResults.size()==0)
                        this.portResults=ContextHydrator.dehydratePortResults(this);
                if(this.optionResults.size()==0)
                        this.optionResults=ContextHydrator.dehydrateOptionResults(this);
        }

        @Column(name="job_id")
        @Id
        @Access(AccessType.PROPERTY)
        private String getStringId() {
                return this.getId().toString();
        }

        @SuppressWarnings("unused") //used by jpa
        private void setStringId(String sId) {
                this.id = JobIdFactory.newIdFromString(sId);
        }

        public static final String MODEL_BATCH_ID = "batch_id";
        @Column(name=MODEL_BATCH_ID)
        @Access(AccessType.PROPERTY)
        private String getStringBatchId() {
                return batchId != null ? batchId.toString() : null;
        }

        @SuppressWarnings("unused") //used by jpa
        private void setStringBatchId(String batchId) {
                if(batchId!=null){
                        this.batchId = JobIdFactory.newBatchIdFromString(batchId);
                }
        }

        @Column(name="log_file")
        @Access(AccessType.PROPERTY)
        private String getStringLogFile() {
                if(super.getLogFile()==null)
                        return "";
                return super.getLogFile().toString();
        }

        @SuppressWarnings("unused") //used by jpa
        private void setStringLogFile(String logFile) {
                this.logFile = URI.create(logFile);
        }

        @Column(name="script_id")
        @Access(AccessType.PROPERTY)
        private String getScriptId() {
                if(this.getScript()!=null){
                        return this.getScript().getDescriptor().getId();
                }else{
                        //throw new IllegalStateException("Script is null");
                        return "";
                }
        }

        @SuppressWarnings("unused") //used by jpa
        private void setScriptId(String id) {
                if(registry!=null){
                        XProcScriptService service=registry.getScript(id);
                        if (service!=null){
                                XProcScript xcript=service.load();
                                logger.debug(String.format("load script %s",xcript));
                                this.script = xcript;
                                return;
                        }
                }
                throw new IllegalStateException(
                                String.format("Illegal state for recovering XProcScript: registry %s"
                                        ,this.getScript(),registry));
        }

        @Column(name="nice_name")
        @Access(AccessType.PROPERTY)
        @Override
        public String getName() {
                return super.getName();
        }

        @SuppressWarnings("unused") // used by jpa
        private void setName(String Name) {
                this.niceName = Name;
        }

        @Override
        protected boolean collectResults(XProcResult result) {
                //build the result set
                boolean status = super.collectResults(result);
                //and make sure that the new values get stored
                this.updateResults();
                return status;
        }

        @Transient
        private static ScriptRegistry registry;
        static void setScriptRegistry(ScriptRegistry sregistry) {
                registry=sregistry;
        }

        void setMonitor(JobMonitorFactory monitorFactory) {
                this.monitor = monitorFactory.newJobMonitor(id);
        }

        // for unit tests

        XProcInput getInputs() {
                return input;
        }

        URIMapper getResultMapper() {
                return resultMapper;
        }
}
