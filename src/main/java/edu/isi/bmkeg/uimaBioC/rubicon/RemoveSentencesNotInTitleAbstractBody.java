package edu.isi.bmkeg.uimaBioC.rubicon;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.JCasUtil;

import bioc.BioCCollection;
import bioc.type.UimaBioCAnnotation;
import bioc.type.UimaBioCDocument;
import bioc.type.UimaBioCPassage;
import edu.isi.bmkeg.uimaBioC.UimaBioCUtils;
import edu.isi.bmkeg.uimaBioC.bin.rubicon.RUBICON_02_BioCToTsv;

public class RemoveSentencesNotInTitleAbstractBody extends JCasAnnotator_ImplBase {

	public final static String PARAM_KEEP_FLOATING_BOXES = ConfigurationParameterFactory
			.createConfigurationParameterName(
					RemoveSentencesNotInTitleAbstractBody.class, "keepFloatsStr");
	@ConfigurationParameter(mandatory = true, description = "Should we include floating boxes in the output.")
	String keepFloatsStr;
	Boolean keepFloats = false;

	Map<String,Map<String,Integer>> table = new HashMap<String, Map<String, Integer>>();
	
	private static Logger logger = Logger.getLogger(RemoveSentencesNotInTitleAbstractBody.class);
	
	public void initialize(UimaContext context)
			throws ResourceInitializationException {

		super.initialize(context);

		if(this.keepFloatsStr != null && this.keepFloatsStr.toLowerCase().equals("true") ) {
			keepFloats = true;
		} else {
			keepFloats = false;
		} 
		
	}

	public void process(JCas jCas) throws AnalysisEngineProcessException {

		UimaBioCDocument uiD = JCasUtil.selectSingle(jCas,
				UimaBioCDocument.class);
		if( uiD.getId().equals("skip") )
			return;

		
		List<UimaBioCAnnotation> floats = UimaBioCUtils.readFloats(jCas);
		List<Sentence> sentences = UimaBioCUtils.readAllReadableSentences(jCas);
		
		if( this.keepFloats ) 
			sentences.addAll(UimaBioCUtils.readAllFloatSentences(jCas));
		
		Set<Sentence> sentencesToKeep = new HashSet<Sentence>(sentences);
		
		List<Sentence> allSentences = JCasUtil.selectCovered(
					org.cleartk.token.type.Sentence.class, uiD
					);
		for(Sentence s : allSentences) {
			if( !sentencesToKeep.contains(s) ) {
				s.removeFromIndexes();
			}
		}		
		
	}
	
}