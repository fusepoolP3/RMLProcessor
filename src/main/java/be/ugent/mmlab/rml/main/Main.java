package be.ugent.mmlab.rml.main;

import be.ugent.mmlab.rml.core.RMLEngine;
import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.model.RMLMapping;
import eu.fusepool.p3.transformer.HttpRequestEntity;
import eu.fusepool.p3.transformer.SyncTransformer;
import eu.fusepool.p3.transformer.commons.Entity;
import eu.fusepool.p3.transformer.commons.util.InputStreamEntity;
import eu.fusepool.p3.transformer.server.TransformerServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLStructureException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLSyntaxException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.R2RMLDataError;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;
import org.wymiwyg.commons.util.arguments.ArgumentHandler;

/**
 *
 * @author mielvandersande, andimou
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        Arguments arguments = ArgumentHandler.readArguments(Arguments.class, args);
        if (arguments != null) {
            if (arguments.startTransformerService()) {
                if (arguments.getMapping() != null) {
                    System.err.println("When starting as service no mapping must be specified as argument");
                    return;
                }
                if (arguments.getMapping() != null) {
                    System.err.println("When starting as service no output must be specified as argument");
                    return;
                }
                startService(arguments);
            } else {
                if ((arguments.getMapping() == null) 
                        || (arguments.getOutput() == null)) { 
                    System.err.println("Missing required argument, use -H to show usage");
                } else {
                    runRML(arguments.getMapping(), arguments.getOutput());
                }
            }
        }
    }

    private static void startService(Arguments arguments) throws Exception {
        TransformerServer server = new TransformerServer(arguments.getPort(), arguments.enableCors());
        server.start(new SyncTransformer() {

                    @Override
                    public Entity transform(HttpRequestEntity hre) throws IOException {
                        final File mappingFile = File.createTempFile("rml", "data");
                        try (FileOutputStream dataFaos = new FileOutputStream(mappingFile)) {
                            hre.writeData(dataFaos);
                        }
                        final File outFile = File.createTempFile("out", "data");
                        runRML(mappingFile.getAbsolutePath(), outFile.getAbsolutePath());
                        return new InputStreamEntity() {

                            @Override
                            public MimeType getType() {
                                try {
                                    return new MimeType(SupportedFormat.N_TRIPLE);
                                } catch (MimeTypeParseException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }

                            @Override
                            public InputStream getData() throws IOException {
                                return new FileInputStream(outFile);
                            }
                        };
                    }

                    @Override
                    public boolean isLongRunning() {
                        return true;
                    }

                    @Override
                    public Set<MimeType> getSupportedInputFormats() {
                        try {
                            Set<MimeType> results = new HashSet<>();
                            results.add(new MimeType(SupportedFormat.N_TRIPLE));
                            results.add(new MimeType(SupportedFormat.TURTLE));
                            return results;
                        } catch (MimeTypeParseException ex) {
                            throw new RuntimeException(ex);
                        }
                    }

                    @Override
                    public Set<MimeType> getSupportedOutputFormats() {
                        try {
                            return Collections.singleton(new MimeType(SupportedFormat.N_TRIPLE));
                        } catch (MimeTypeParseException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });
        server.join();
    }

    static void runRML(String mappingLocation, String outputFile) {
        try {
            String graphName = "";
            RMLMapping mapping = RMLMappingFactory.extractRMLMapping(mappingLocation);
            RMLEngine engine = new RMLEngine();
            System.out.println("mapping document " + mappingLocation);
            engine.runRMLMapping(mapping, graphName, outputFile, true);
        } catch (IOException | InvalidR2RMLStructureException | InvalidR2RMLSyntaxException | R2RMLDataError | RepositoryException | RDFParseException | SQLException ex) {
            System.out.println(ex.getMessage());
        }

    }
}
