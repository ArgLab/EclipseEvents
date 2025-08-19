package edu.runtimeanalysis;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.runtimeanalysis.core.UsageLogger;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "edu.runtimeanalysis";

    // The shared instance
    private static Activator plugin;
    
    // Usage logger instance
    private UsageLogger usageLogger;
    
    /**
     * The constructor
     */
    public Activator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        
        // Initialize the usage logger
        usageLogger = new UsageLogger();
        usageLogger.logAction("PLUGIN_STARTED", null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (usageLogger != null) {
            usageLogger.logAction("PLUGIN_STOPPED", null);
            usageLogger.close();
        }
        
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }
    
    /**
     * Returns the usage logger
     * @return the usage logger instance
     */
    public UsageLogger getUsageLogger() {
        return usageLogger;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}