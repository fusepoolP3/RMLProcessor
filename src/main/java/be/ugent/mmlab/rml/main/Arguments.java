package be.ugent.mmlab.rml.main;

import org.wymiwyg.commons.util.arguments.ArgumentsWithHelp;
import org.wymiwyg.commons.util.arguments.CommandLine;

/**
 *
 * @author reto
 */
public interface Arguments extends ArgumentsWithHelp {
    

    @CommandLine(longName = "port", shortName = {"P"}, required = false,
            defaultValue = "8150",
            description = "The port on which the RML transformer service shall listen.")
    public int getPort();
    
    @CommandLine(longName = "mapping", shortName = {"M"}, required = false,
            description = "The mapping file or URI to be used for command line transformation.")
    public String getMapping();
    
    @CommandLine(longName = "output", shortName = {"O"}, required = false,
            description = "The file to write the output too.")
    public String getOutput();

    @CommandLine(longName = "enableCors", shortName = {"C"}, 
            description = "Enable a liberal CORS policy for the RML transformer service",
            isSwitch = true)
    public boolean enableCors();
    
    @CommandLine(longName = "startService", shortName = {"S"}, 
            description = "Starts a transformer service to allow RML transformation over HTTP.",
            isSwitch = true)
    public boolean startTransformerService();
    
}
