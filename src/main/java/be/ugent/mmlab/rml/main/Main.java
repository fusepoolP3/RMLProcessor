package be.ugent.mmlab.rml.main;

import be.ugent.mmlab.rml.core.RMLEngine;
import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.model.RMLMapping;
import eu.fusepool.p3.transformer.HttpRequestEntity;
import eu.fusepool.p3.transformer.SyncTransformer;
import eu.fusepool.p3.transformer.Transformer;
import eu.fusepool.p3.transformer.TransformerFactory;
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
import javax.servlet.http.HttpServletRequest;
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
                startService(arguments);
            } else {
                runRML(arguments.getMapping(), arguments.getOutput());
            }
        }
    }

    private static void startService(Arguments arguments) throws Exception {
        TransformerServer server = new TransformerServer(arguments.getPort(), arguments.enableCors());
        server.start(new TransformerFactory() {

            @Override
            public Transformer getTransformer(HttpServletRequest hsr) {
                final String mapping = hsr.getParameter("mapping");
                return new SyncTransformer() {

                    @Override
                    public Entity transform(HttpRequestEntity hre) throws IOException {
                        final File dataFile = File.createTempFile("rml", "data");
                        final FileOutputStream dataFaos = new FileOutputStream(dataFile);
                        hre.writeData(dataFaos);
                        dataFaos.close();
                        runRML(mapping, dataFile.getAbsolutePath());
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
                                return new FileInputStream(dataFile);
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
                            //TODO detect acttually supported format from engine dined in mapping
                            Set<MimeType> results = new HashSet<>();
                            results.add(new MimeType("application/xml"));
                            results.add(new MimeType("text/xml"));
                            results.add(new MimeType("application/json"));
                            results.add(new MimeType("text/tab-separated-values"));
                            results.add(new MimeType("text/csv"));
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
                };
            }
        });
        server.join();
    }

    static void runRML(String mappingFile, String outputFile) {
        try {
            String graphName = "";
            RMLMapping mapping = RMLMappingFactory.extractRMLMapping(mappingFile);
            RMLEngine engine = new RMLEngine();
            System.out.println("mapping document " + mappingFile);
            engine.runRMLMapping(mapping, graphName, outputFile, true);
        } catch (IOException | InvalidR2RMLStructureException | InvalidR2RMLSyntaxException | R2RMLDataError | RepositoryException | RDFParseException | SQLException ex) {
            System.out.println(ex.getMessage());
        }

    }
}
