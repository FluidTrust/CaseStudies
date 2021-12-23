package edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.application.internal;

import java.util.Optional;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.prolog4j.manager.IProverManager;

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
            serviceReference = context.getServiceReference(serviceClass);
            service = context.getService(serviceReference);
            return service;
        }

        public S get() {
            var localRef = service;
            if (localRef == null) {
                synchronized (this) {
                    localRef = service;
                    if (localRef == null) {
                        localRef = init();
                    }
                }
            }
            return localRef;
        }

        public void uninit() {
            synchronized(this) {
                if (serviceReference != null) {
                    context.ungetService(serviceReference);
                    serviceReference = null;
                    service = null;                                    
                }
            }
        }
    }

    private static Activator instance;
    private ServiceProvider<IProverManager> proverManager;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        setInstance(this);
        proverManager = new ServiceProvider<>(IProverManager.class, context);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        proverManager.uninit();
        proverManager = null;
        setInstance(null);
        super.stop(context);
    }

    private static void setInstance(Activator instance) {
        Activator.instance = instance;
    }

    public static Activator getInstance() {
        return Activator.instance;
    }

    public IProverManager getProverManager() {
        return Optional.ofNullable(proverManager)
            .map(ServiceProvider::get)
            .orElse(null);
    }
}
