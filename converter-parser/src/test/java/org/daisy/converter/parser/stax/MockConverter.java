package org.daisy.converter.parser.stax;

import java.net.URI;
import java.util.HashMap;

import org.daisy.pipeline.modules.converter.Converter;
import org.daisy.pipeline.modules.converter.Converter.MutableConverter;
import org.daisy.pipeline.modules.converter.ConverterArgument;
import org.daisy.pipeline.modules.converter.ConverterFactory;
import org.daisy.pipeline.modules.converter.ConverterRunnable;
import org.daisy.pipeline.modules.converter.ConverterRunnableTest;
import org.daisy.pipeline.modules.converter.MutableConverterArgument;

public class MockConverter implements Converter,MutableConverter{
	private URI mUri;
	/** The name. */
	private String mName;
	
	/** The version. */
	private String mVersion;
	
	/** The description. */
	private String mDescription;
	
	/** The arguments. */
	private HashMap<String, ConverterArgument> mArguments = new HashMap<String, ConverterArgument>();
	

	
	
	/**
	 * Instantiates a new oSGI converter using the given OSGIConverterFactory services
	 *
	 * @param factory the factory
	 */
	public MockConverter() {
			
	}
	
	/* (non-Javadoc)
	 * @see org.daisy.pipeline.modules.converter.Converter.MutableConverter#addArgument(org.daisy.pipeline.modules.converter.Converter.ConverterArgument)
	 */
	public void addArgument(ConverterArgument argument){
		mArguments.put(argument.getName(), argument);
	}
	
	/* (non-Javadoc)
	 * @see org.daisy.pipeline.modules.converter.Converter#getArgument(java.lang.String)
	 */
	@Override
	public ConverterArgument getArgument(String name) {
		return mArguments.get(name);
	}

	/* (non-Javadoc)
	 * @see org.daisy.pipeline.modules.converter.Converter#getArguments()
	 */
	@Override
	public Iterable<ConverterArgument> getArguments() {
		
		return mArguments.values();
	}


	/* (non-Javadoc)
	 * @see org.daisy.pipeline.modules.converter.Converter#getName()
	 */
	public String getName() {
		return mName;
	}

	/* (non-Javadoc)
	 * @see org.daisy.pipeline.modules.converter.Converter.MutableConverter#setVersion(java.lang.String)
	 */
	public void setVersion(String version) {
		mVersion = version;
	}

	/* (non-Javadoc)
	 * @see org.daisy.pipeline.modules.converter.Converter#getVersion()
	 */
	public String getVersion() {
		return mVersion;
	}

	/* (non-Javadoc)
	 * @see org.daisy.pipeline.modules.converter.Converter.MutableConverter#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		mDescription = description;
	}

	/* (non-Javadoc)
	 * @see org.daisy.pipeline.modules.converter.Converter#getDescription()
	 */
	public String getDescription() {
		return mDescription;
	}
	
	/* (non-Javadoc)
	 * @see org.daisy.pipeline.modules.converter.Converter.MutableConverter#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		mName=name;
	}
	
	/**
	 * Gets the registry.
	 *
	 * @return the registry
	 */
	
	
	/**
	 * The Class OSGIConverterArgument.
	 */
	public static class MockConverterArgument extends  MutableConverterArgument{

		/* (non-Javadoc)
		 * @see org.daisy.pipeline.modules.converter.Converter.MutableConverterArgument#setName(java.lang.String)
		 */
		@Override
		public void setName(String name) {
			this.mName=name;
		}

		/* (non-Javadoc)
		 * @see org.daisy.pipeline.modules.converter.Converter.MutableConverterArgument#setBind(java.lang.String)
		 */
		@Override
		public void setBind(String bind) {
			this.mBind=bind;
			
		}

		/* (non-Javadoc)
		 * @see org.daisy.pipeline.modules.converter.Converter.MutableConverterArgument#setDesc(java.lang.String)
		 */
		@Override
		public void setDesc(String desc) {
			this.mDesc=desc;
			
		}

		

		/* (non-Javadoc)
		 * @see org.daisy.pipeline.modules.converter.Converter.MutableConverterArgument#setOptional(boolean)
		 */
		@Override
		public void setOptional(boolean optional) {
			mOptional=optional;
			
		}

	

		@Override
		public void setBindType(BindType bindType) {
			this.mBindType=bindType;			
		}

		@Override
		public void setDirection(Direction direction) {
			this.mDirection=direction;
			
		}

		@Override
		public void setMediaType(String mediaType) {
			this.mMediaType=mediaType;			
		}

		@Override
		public void setOutputType(OutputType outputType) {
			this.mOutputType=outputType;
			
		}

		@Override
		public void setSequence(boolean sequence) {
			this.mSequence=sequence;
		}
		
	}

	/* (non-Javadoc)
	 * @see org.daisy.pipeline.modules.converter.Converter#getFactory()
	 */
	@Override
	public ConverterFactory getFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.daisy.pipeline.modules.converter.Converter#getRunnable()
	 */
	@Override
	public ConverterRunnable getRunnable() {
		return  null;//new ConverterRunnable(this) ;
	}


	@Override
	public URI getURI() {
		// TODO Auto-generated method stub
		return this.mUri;
	}

	@Override
	public void setURI(URI uri) {
		this.mUri=uri;
	}
	
	
 public static class MockFactory implements ConverterFactory{

	@Override
	public MutableConverter newConverter() {
		return new MockConverter();
	}

	@Override
	public MutableConverterArgument newArgument() {
		return new MockConverterArgument();
	}
	 
 } 


}