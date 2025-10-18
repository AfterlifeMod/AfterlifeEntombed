package com.dracolich777.afterlibs;

import com.dracolich777.afterlibs.network.NetworkHandler;
import com.dracolich777.afterlibs.util.VerboseLogger;

/**
 * AfterLibs utility class (embedded version)
 */
public class AfterLibs {
    
    public static final String MOD_ID = "afterlibs_api";
    
    /**
     * Initialize the AfterLibs components
     */
    public static void commonSetup() {
        // Initialize the network handler
        NetworkHandler.register();
        
        VerboseLogger.info(AfterLibs.class, "AfterLibs common setup complete");
        VerboseLogger.logSeparator(AfterLibs.class, "AfterLibs Framework Loaded");
    }
}
