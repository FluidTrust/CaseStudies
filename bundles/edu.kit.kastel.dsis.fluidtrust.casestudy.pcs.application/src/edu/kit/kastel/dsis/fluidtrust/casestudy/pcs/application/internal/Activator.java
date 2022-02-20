package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.internal;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


public class Activator extends Plugin {

    protected static class ServiceProvider<S> {
        protected final Class<S> serviceClass;
        protected final BundleContext context;
        protected ServiceReference<S> serviceReference;
        protected volatile S service;

        public ServiceProvider(Class<S> serviceClass, BundleContext context) {
            this.serviceClass = serviceClass;
            this.context = context;
        }

        protected S init() {
            this.serviceReference = this.context.getServiceReference(this.serviceClass);
            this.service = this.context.getService(this.serviceReference);
            return this.service;
        }

        public S get() {
            var localRef = this.service;
            if (localRef == null) {
                synchronized (this) {
                    localRef = this.service;
                    if (localRef == null) {
                        localRef = init();
                    }
                }
            }
            return localRef;
        }

        public void uninit() {
            synchronized(this) {
                if (this.serviceReference != null) {
                    this.context.ungetService(this.serviceReference);
                    this.serviceReference = null;
                    this.service = null;
                }
            }
        }
    }

    private static Activator instance;


    private static void setInstance(Activator instance) {
        Activator.instance = instance;
    }

    public static Activator getInstance() {
        return Activator.instance;
    }
}
