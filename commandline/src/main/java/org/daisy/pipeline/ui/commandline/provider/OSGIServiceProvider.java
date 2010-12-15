package org.daisy.pipeline.ui.commandline.provider;

import org.daisy.pipeline.modules.ModuleRegistry;
import org.daisy.pipeline.xproc.XProcessorFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class OSGIServiceProvider implements ServiceProvider {

	BundleContext mCtxt;
	ServiceTracker mModuleRegistryTracker;
	public OSGIServiceProvider(BundleContext ctxt) {

		mCtxt = ctxt;
	}
	@Override
	public ModuleRegistry getModuleRegistry() {

		ModuleRegistry reg=null;

		if(mModuleRegistryTracker==null){
			mModuleRegistryTracker= new ServiceTracker(mCtxt, ModuleRegistry.class.getName(), null);
			mModuleRegistryTracker.open();
		}
		
		
		try {
			reg = (ModuleRegistry) mModuleRegistryTracker.waitForService(5000);
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted");
		}
		if(reg==null){
			throw new RuntimeException("No service found");
		}
		mModuleRegistryTracker.close();
		mModuleRegistryTracker=null;
		return reg;
	}
	@Override
	public XProcessorFactory getXProcessorFactory() {
		XProcessorFactory fact=null;
		ServiceTracker tracker;
	
		tracker= new ServiceTracker(mCtxt, XProcessorFactory.class.getName(), null);
		tracker.open();
		
		try {
			fact = (XProcessorFactory) tracker.waitForService(5000);
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted");
		}
		if(fact==null){
			throw new RuntimeException("No service found");
		}
		tracker.close();
		return fact;
	} 

}
