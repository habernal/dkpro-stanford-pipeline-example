package com.github.habernal;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.*;
import org.apache.uima.fit.component.CasDumpWriter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
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
    public static void main(String[] args)
            throws Exception
    {
        JCas jCas = JCasFactory.createJCas();
        jCas.setDocumentLanguage("en");
        jCas.setDocumentText(
                "Regional elections are taking place today in Catalonia, Spain, in which "
                        + "the biggest talking point is whether the autonomous region should"
                        + " become an independent country. The polls are being held early as the "
                        + "national government has not allowed an official referendum on "
                        + "independence. Opinion polls, according to Reuters, suggest the "
                        + "secessionists will win more than half of the 135 regional Parliament"
                        + " seats but less than half of the popular vote.");

        SimplePipeline.runPipeline(jCas,
                // tokenizer
                AnalysisEngineFactory.createEngineDescription(
                        StanfordSegmenter.class
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
                AnalysisEngineFactory.createEngineDescription(
                        CasDumpWriter.class
                )
        );

        // show entities
        for (NamedEntity ne : JCasUtil.select(jCas, NamedEntity.class)) {
            System.out.println("Found NEs: " + ne.getValue() + ", " + ne.getCoveredText());
        }
    }
}
