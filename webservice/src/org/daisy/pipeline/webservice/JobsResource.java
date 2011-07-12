package org.daisy.pipeline.webservice;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.daisy.pipeline.DaisyPipelineContext;
import org.daisy.pipeline.jobmanager.JobID;
import org.daisy.pipeline.modules.converter.Converter.ConverterArgument;
import org.daisy.pipeline.modules.converter.ConverterDescriptor;
import org.daisy.pipeline.modules.converter.ConverterRunnable;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


public class JobsResource extends ServerResource {

	@Get("xml")
	public Representation getResource() {
		DaisyPipelineContext context = ((WebApplication)this.getApplication()).getDaisyPipelineContext();
		String serverAddress = ((WebApplication)this.getApplication()).getServerAddress();
		DomRepresentation dom = new DomRepresentation(
				MediaType.APPLICATION_XML, XmlFormatter.jobsToXml(context.getJobManager().getJobList(), serverAddress));
		setStatus(Status.SUCCESS_OK);
		return dom;
	}

	
	
	// the client is submitting a string of xml data
	// so, I would think this annotation should read @Post("xml")
	
	// already tried using DomRepresentation as the function param, and an XML DOM as the http request body, but it doesn't seem to work well.
	
	// another issue, would like to be more specific about the media type: 
	// client says "contentType: application/xml", server says @Post("xml") ==> OPTIONS 405 (unsupported method)
	// client says no content type, server says @Post("xml") ==> POST 415 (media type error)
	// client says "contentType: application/xml", server says @Post ==> OPTIONS 405 (unsupported method)
	// client says no content type, server says @Post ==> POST 200 (works)
	
	@Post
	public Representation createResource(Representation representation) {	
		try {
			String s = representation.getText();
			
			try {
			      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			      DocumentBuilder builder = factory.newDocumentBuilder();
			      InputSource is = new InputSource(new StringReader(s));
			      Document doc = builder.parse(is);
			      
			      boolean isValid = validateJobRequest(doc);
			      if (!isValid) {
			    	  setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			    	  return null;
			      }
			      
			      ConverterRunnable converterRunnable = createConverterRunnable(doc);
			      
			      if (converterRunnable != null) {
			    	  DaisyPipelineContext context = ((WebApplication)this.getApplication()).getDaisyPipelineContext();
			    	  JobID newId = context.getJobManager().addJob(converterRunnable);
			    	  
			    	  // return the URI of the new job
			    	  Representation newJobUriRepresentation = new EmptyRepresentation();
			    	  newJobUriRepresentation.setLocationRef(((WebApplication)this.getApplication()).getServerAddress() + "/jobs/" + newId.getID());
			    	  
			    	  setStatus(Status.SUCCESS_CREATED);
			    	  return newJobUriRepresentation;
			      }
			      else {
			    	  setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			    	  return null;
			      } 
			    }
			    catch( Exception ex ) {
			      ex.printStackTrace();
			      setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		    	  return null;
			    }
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return null;
		}
	}
	
	private boolean validateJobRequest(Document doc) {
		// TODO job request validation
		return true;
	}



	private ConverterRunnable createConverterRunnable(Document doc) {
		
		Element converterElm = (Element)doc.getElementsByTagName("useConverter").item(0);
		String converterUri = converterElm.getAttribute("href");
		
		DaisyPipelineContext context = ((WebApplication)this.getApplication()).getDaisyPipelineContext();
	    ConverterDescriptor converterDescriptor;
		try {
			converterDescriptor = context.getConverterRegistry().getDescriptor(new URI(converterUri));
			
			if (converterDescriptor != null) {
				ConverterRunnable converterRunnable = converterDescriptor.getConverter().getRunnable();
		    	
		    	NodeList inputNodes = doc.getElementsByTagName("input");
		    	for(int i = 0; i<inputNodes.getLength(); i++) {
		    		Element inputElm = (Element)inputNodes.item(i);
		    		String name = inputElm.getAttribute("name");
		    		
		    		
		    		ConverterArgument arg = converterDescriptor.getConverter().getArgument(name);
		    		ConverterRunnable.ValuedConverterArgument valueArg = null;
		    		
		    		if (arg.getType() == ConverterArgument.Type.INPUT) {
		    			// TODO support inline XML input
		    			// TODO support a sequence of input documents
			    		// NodeList docwrapperNodes = inputElm.getElementsByTagName("docwrapper");
			    		// this would have already been checked during validation (not yet implemented)
			    		// if (docwrapperNodes.getLength() == 0) return null;
			    		
		    			// get the XML of the input document, serialized as a string
			    		// String docstring = XmlFormatter.nodeToString(docwrapperNodes.item(0));
		    			
		    			// here we just expect a URI.  this is temporary for the beta.
		    			String val = inputElm.getTextContent();
			    		valueArg = converterRunnable.new ValuedConverterArgument(val, arg);
			    		
		    		}
		    		else if (arg.getType() == ConverterArgument.Type.OPTION || arg.getType() == ConverterArgument.Type.PARAMETER) {
		    			String val = inputElm.getTextContent();
		    			valueArg = converterRunnable.new ValuedConverterArgument(val, arg);
		    		}
		    		// we don't care about output arguments in the webservice
		    		else if (arg.getType() == ConverterArgument.Type.OUTPUT) {
		    			valueArg = converterRunnable.new ValuedConverterArgument("Nothing", arg);
		    		}
		    		
		    		if (valueArg != null) {
		    			converterRunnable.setValue(valueArg);
		    		}
		    		else {
		    			return null;
		    		}
		    	}
		    	return converterRunnable;
				
			}
			else {
				return null;
			}

		
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	    
	    			
	}
	
	
	
	
}
