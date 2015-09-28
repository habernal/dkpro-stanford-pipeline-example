package com.github.habernal;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.*;
import de.tudarmstadt.ukp.dkpro.core.tokit.ParagraphSplitter;
import org.apache.uima.fit.component.CasDumpWriter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

/**
 * Example of Stanford pipeline in DKPro Core
 *
 * @author Ivan Habernal
 */
public class StanfordPipelineExample
{

    private static final String TEMP_PATH = "/tmp/xmi-temp";

    /**
     * Reads all XMI files in the directory and dumps the annotations to std out
     *
     * @throws Exception
     */
    public static void readDataAndDump()
            throws Exception
    {
        SimplePipeline.runPipeline(
                // read all XMI files (there's only one)
                CollectionReaderFactory.createReaderDescription(
                        XmiReader.class,
                        XmiReader.PARAM_SOURCE_LOCATION,
                        TEMP_PATH,
                        XmiReader.PARAM_PATTERNS,
                        XmiReader.INCLUDE_PREFIX + "*.xmi"
                ),
                // dump annotations to std out
                AnalysisEngineFactory.createEngineDescription(
                        CasDumpWriter.class
                )
        );
    }

    /**
     * Creates one document on the spot, runs the pipeline, and stores to the filesystem
     *
     * @throws Exception
     */
    public static void createDataAndWriteToXMIs()
            throws Exception
    {
        // create JCas on the spot
        JCas jCas = JCasFactory.createJCas();
        jCas.setDocumentLanguage("en");
        jCas.setDocumentText(
                "Regional elections are taking place today in Catalonia, Spain, in which "
                        + "the biggest talking point is whether the autonomous region should"
                        + " become an independent country. The polls are being held early as the "
                        + "national government has not allowed an official referendum on "
                        + "independence. Opinion polls, according to Reuters, suggest the "
                        + "secessionists will win more than half of the 135 regional Parliament"
                        + " seats but less than half of the popular vote."
                        + "\n"
                        + "The pro-independence parties include the Junts pel Sí (Together for "
                        + "Yes) coalition and the left-wing Popular Unity Candidacy (CUP). If "
                        + "they win a clear majority, the Junts pel Sí hopes to hold further "
                        + "elections in eighteen months after developing future state institutions"
                        + " such as a separate tax office. However, they might have to find a "
                        + "compromise candidate for regional president because CUP does not support"
                        + " the incumbent, Artur Mas. The CUP is also campaigning for Catalonia "
                        + "to leave the European Union."
                        + "\n"
                        + "In contrast, the People's Party, led by Spanish Prime Minister Mariano "
                        + "Rajoy, has called for Catalonia to remain within Spain. Rajoy took part "
                        + "in a campaign video on Friday in which he says \"united we will win\".");

        // create some metadata
        DocumentMetaData metaData = DocumentMetaData.create(jCas);
        metaData.setDocumentTitle("Independence debate as Catalonia holds regional elections");
        metaData.setDocumentUri(
                "https://en.wikinews.org/wiki/Independence_debate_as_Catalonia_holds_regional_elections?dpl_id=2465939");
        metaData.setDocumentId("2465939");

        SimplePipeline.runPipeline(jCas,
                // tokenizer
                AnalysisEngineFactory.createEngineDescription(
                        StanfordSegmenter.class
                ),
                // paragraphs (single line break)
                AnalysisEngineFactory.createEngineDescription(
                        ParagraphSplitter.class,
                        ParagraphSplitter.PARAM_SPLIT_PATTERN,
                        ParagraphSplitter.SINGLE_LINE_BREAKS_PATTERN
                ),
                // lemma
                AnalysisEngineFactory.createEngineDescription(
                        StanfordLemmatizer.class
                ),
                // POS
                AnalysisEngineFactory.createEngineDescription(
                        StanfordPosTagger.class
                ),
                // dependency parsing
                AnalysisEngineFactory.createEngineDescription(
                        StanfordParser.class
                ),
                // NER
                AnalysisEngineFactory.createEngineDescription(
                        StanfordNamedEntityRecognizer.class
                ),
                // coreference
                AnalysisEngineFactory.createEngineDescription(
                        StanfordCoreferenceResolver.class
                ),
                // write to XMI
                AnalysisEngineFactory.createEngineDescription(
                        XmiWriter.class,
                        XmiWriter.PARAM_TARGET_LOCATION, TEMP_PATH
                )
        );

        // show paragraphs
        for (Paragraph p : JCasUtil.select(jCas, Paragraph.class)) {
            System.out.println("Paragraph: " + p.getCoveredText());
        }

        // show entities
        for (NamedEntity ne : JCasUtil.select(jCas, NamedEntity.class)) {
            System.out.println("Found NEs: " + ne.getValue() + ", " + ne.getCoveredText());
        }
    }

    public static void main(String[] args)
            throws Exception
    {
        createDataAndWriteToXMIs();
        readDataAndDump();
    }
}
