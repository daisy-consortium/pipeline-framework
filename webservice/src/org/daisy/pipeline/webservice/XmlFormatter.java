package org.daisy.pipeline.webservice;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.daisy.common.base.Filter;
import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.MessageAccessor;
import org.daisy.common.messaging.Message.Level;
import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcPortMetadata;
import org.daisy.pipeline.script.XProcScript;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

// TODO: Auto-generated Javadoc
/**
 * The Class XmlFormatter.
 */
public class XmlFormatter {

	/** The Constant NS_PIPELINE_DATA. */
	private static final String NS_PIPELINE_DATA = "http://www.daisy.org/ns/pipeline/data";

	/*
	 * example output: daisy-pipeline/webservice/docs/sampleXml/job.xml
	 */
	/**
	 * Job to xml.
	 * 
	 * @param job
	 *            the job
	 * @param serverAddress
	 *            the server address
	 * @return the document
	 */
	public static Document jobToXml(Job job, String serverAddress,int msgSeq) {
		Document doc = createDom("job");
		toXmlElm(job, doc, serverAddress,msgSeq);

		// for debugging only
		if (!Validator.validateXml(doc, Validator.jobSchema)) {
			System.out.println("INVALID XML:\n" + DOMToString(doc));
		}

		return doc;
	}

	/*
	 * example output: daisy-pipeline/webservice/docs/sampleXml/jobs.xml
	 */
	/**
	 * Jobs to xml.
	 * 
	 * @param jobs
	 *            the jobs
	 * @param serverAddress
	 *            the server address
	 * @return the document
	 */
	public static Document jobsToXml(Iterable<Job> jobs, String serverAddress) {
		Document doc = createDom("jobs");
		Element jobsElm = doc.getDocumentElement();

		Iterator<Job> it = jobs.iterator();
		while (it.hasNext()) {
			Job job = it.next();
			Element jobElm = toXmlElm(job, doc, serverAddress,0);
			jobsElm.appendChild(jobElm);
		}

		// for debugging only
		if (!Validator.validateXml(doc, Validator.jobsSchema)) {
			System.out.println("INVALID XML:\n" + DOMToString(doc));
		}

		return doc;
	}

	/*
	 * example output: daisy-pipeline/webservice/docs/sampleXml/script.xml
	 */
	/**
	 * Xproc script to xml.
	 * 
	 * @param script
	 *            the script
	 * @return the document
	 */
	public static Document xprocScriptToXml(XProcScript script) {
		Document doc = createDom("script");
		toXmlElm(script, doc);

		// for debugging only
		if (!Validator.validateXml(doc, Validator.scriptSchema)) {
			System.out.println("INVALID XML:\n" + DOMToString(doc));
		}

		return doc;
	}

	/*
	 * example output: daisy-pipeline/webservice/docs/sampleXml/scripts.xml
	 */
	/**
	 * Xproc scripts to xml.
	 * 
	 * @param scripts
	 *            the scripts
	 * @return the document
	 */
	public static Document xprocScriptsToXml(Iterable<XProcScript> scripts) {
		Document doc = createDom("scripts");
		Element scriptsElm = doc.getDocumentElement();

		Iterator<XProcScript> it = scripts.iterator();
		while (it.hasNext()) {
			XProcScript script = it.next();
			Element scriptElm = toXmlElm(script, doc);
			scriptsElm.appendChild(scriptElm);
		}

		// for debugging only
		if (!Validator.validateXml(doc, Validator.scriptsSchema)) {
			System.out.println("INVALID XML:\n" + DOMToString(doc));
		}

		return doc;
	}

	private static Element toXmlElm(XProcScript script, Document doc) {
		Element rootElm = null;

		if (doc.getDocumentElement().getNodeName().equals("script")) {
			rootElm = doc.getDocumentElement();
		} else {
			rootElm = doc.createElementNS(NS_PIPELINE_DATA, "script");
		}
		rootElm.setAttribute("href", script.getURI().toString());

		Element nicenameElm = doc.createElementNS(NS_PIPELINE_DATA, "nicename");
		nicenameElm.setTextContent(script.getName());
		rootElm.appendChild(nicenameElm);

		Element descriptionElm = doc.createElementNS(NS_PIPELINE_DATA,
				"description");
		descriptionElm.setTextContent(script.getDescription());
		rootElm.appendChild(descriptionElm);

		String homepage = script.getHomepage();
		if (homepage != null && homepage.trim().length() > 0) {
			Element homepageElm = doc.createElementNS(NS_PIPELINE_DATA,
					"homepage");
			homepageElm.setTextContent(homepage);
			rootElm.appendChild(homepageElm);
		}

		Iterator<XProcPortInfo> it_input = script.getXProcPipelineInfo()
				.getInputPorts().iterator();
		Iterator<XProcOptionInfo> it_options = script.getXProcPipelineInfo()
				.getOptions().iterator();

		while (it_input.hasNext()) {
			XProcPortInfo input = it_input.next();

			Element inputElm = doc.createElementNS(NS_PIPELINE_DATA, "input");
			inputElm.setAttribute("name", input.getName());

			if (input.isSequence() == true) {
				inputElm.setAttribute("sequenceAllowed", "true");
			} else {
				inputElm.setAttribute("sequenceAllowed", "false");
			}

			XProcPortMetadata meta = script.getPortMetadata(input.getName());
			inputElm.setAttribute("mediaType", meta.getMediaType());
			inputElm.setAttribute("desc", meta.getDescription());

			rootElm.appendChild(inputElm);
		}

		while (it_options.hasNext()) {
			XProcOptionInfo option = it_options.next();

			Element optionElm = doc.createElementNS(NS_PIPELINE_DATA, "option");
			optionElm.setAttribute("name", option.getName().toString());
			if (option.isRequired()) {
				optionElm.setAttribute("required", "true");
			} else {
				optionElm.setAttribute("required", "false");
			}

			XProcOptionMetadata meta = script.getOptionMetadata(option
					.getName());
			optionElm.setAttribute("type", meta.getType());
			optionElm.setAttribute("mediaType", meta.getMediaType());
			optionElm.setAttribute("desc", meta.getDescription());

			rootElm.appendChild(optionElm);
		}

		return rootElm;
	}

	/**
	 * To xml elm.
	 * 
	 * @param job
	 *            the job
	 * @param doc
	 *            the doc
	 * @param serverAddress
	 *            the server address
	 * @return the element
	 */
	private static Element toXmlElm(Job job, Document doc, String serverAddress,int msgSeq) {
		Element rootElm = null;

		if (doc.getDocumentElement().getNodeName().equals("job")) {
			rootElm = doc.getDocumentElement();
		} else {
			rootElm = doc.createElementNS(NS_PIPELINE_DATA, "job");
		}

		Job.Status status = job.getStatus();

		rootElm.setAttribute("id", job.getId().toString());
		rootElm.setAttribute("status", status.toString());

		Element scriptElm = doc.createElementNS(NS_PIPELINE_DATA, "script");
		scriptElm.setAttribute("href", job.getScript().getURI().toString());
		rootElm.appendChild(scriptElm);

		Element messagesElm = doc.createElementNS(NS_PIPELINE_DATA, "messages");
		//TODO wrap this in a static context
		HashSet<Level> levels= new HashSet<Level>();
		levels.add(Level.WARNING);
		levels.add(Level.INFO);
		levels.add(Level.ERROR);
		Filter<List<Message>> seqFilt= new MessageAccessor.SequenceFilter(msgSeq);
		Filter<List<Message>> levelFilt= new MessageAccessor.LevelFilter(levels);
		//end of wrapping things
		
		
		// TODO remove this
		//TESTING force a dummy message
		Element testelm = doc.createElementNS(NS_PIPELINE_DATA, "message");
		testelm.setAttribute("level", "INFO");
		testelm.setAttribute("sequence", "45");
		testelm.setTextContent("Hello. I am a message.");
		messagesElm.appendChild(testelm);
		rootElm.appendChild(messagesElm);
		// end TESTING
		
		try {
			List<Message> msgs= job.getMonitor().getMessageAccessor().filtered(new Filter[]{seqFilt,levelFilt});
			for (Message msg :msgs) {
				Element singleMsgElm = doc.createElementNS(NS_PIPELINE_DATA,
						"message");
				singleMsgElm.setAttributeNS(NS_PIPELINE_DATA, "level",msg.getLevel().toString());
				singleMsgElm.setAttributeNS(NS_PIPELINE_DATA, "sequence",msg.getSequence()+"");
				singleMsgElm.setTextContent(msg.getMsg());
				messagesElm.appendChild(singleMsgElm);
			}
			rootElm.appendChild(messagesElm);
		} catch (Exception e) {
			System.out.println(e.getCause());
		}
		
		if (status == Job.Status.DONE) {
			Element resultElm = doc.createElementNS(NS_PIPELINE_DATA, "result");
			resultElm.setAttribute("href", serverAddress + "/jobs/"
					+ job.getId().toString() + "/result.zip");
			rootElm.appendChild(resultElm);
			
			// reference the log file
			Element logElm = doc.createElementNS(NS_PIPELINE_DATA, "log");
			logElm.setAttribute("href", serverAddress + "/jobs/" + job.getId()
					+ "/log");
			rootElm.appendChild(logElm);
		}

		return rootElm;
	}

	/**
	 * Creates the dom.
	 * 
	 * @param documentElementName
	 *            the document element name
	 * @return the document
	 */
	public static Document createDom(String documentElementName) {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory
					.newDocumentBuilder();
			DOMImplementation domImpl = documentBuilder.getDOMImplementation();
			Document document = domImpl.createDocument(NS_PIPELINE_DATA,
					documentElementName, null);

			return document;

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}

	}

	/*
	 * from:
	 * http://www.journaldev.com/71/utility-java-class-to-format-xml-document
	 * -to-xml-string-and-xml-to-document
	 */
	/**
	 * DOM to string.
	 * 
	 * @param doc
	 *            the doc
	 * @return the string
	 */
	public static String DOMToString(Document doc) {
		String xmlString = "";
		if (doc != null) {
			try {
				TransformerFactory transfac = TransformerFactory.newInstance();
				Transformer trans = transfac.newTransformer();
				trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				trans.setOutputProperty(OutputKeys.INDENT, "yes");
				StringWriter sw = new StringWriter();
				StreamResult result = new StreamResult(sw);
				DOMSource source = new DOMSource(doc);
				trans.transform(source, result);
				xmlString = sw.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return xmlString;
	}

	/**
	 * Node to string.
	 * 
	 * @param node
	 *            the node
	 * @return the string
	 */
	public static String nodeToString(Node node) {
		Document doc = node.getOwnerDocument();
		DOMImplementationLS domImplLS = (DOMImplementationLS) doc
				.getImplementation();
		LSSerializer serializer = domImplLS.createLSSerializer();
		serializer.getDomConfig().setParameter("xml-declaration", false);
		String string = serializer.writeToString(node);
		return string.trim();
	}

}
