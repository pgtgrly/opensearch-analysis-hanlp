package org.opensearch.index.analysis;

import com.hankcs.cfg.Configuration;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.model.crf.CRFLexicalAnalyzer;
import com.hankcs.hanlp.model.perceptron.PerceptronLexicalAnalyzer;
import com.hankcs.hanlp.seg.Dijkstra.DijkstraSegment;
import com.hankcs.hanlp.seg.NShort.NShortSegment;
import com.hankcs.hanlp.seg.Other.DoubleArrayTrieSegment;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.lucene.TokenizerBuilder;
import com.hankcs.model.CRFNERecognizerInstance;
import com.hankcs.model.CRFPOSTaggerInstance;
import com.hankcs.model.CRFSegmenterInstance;
import com.hankcs.model.PerceptronCWSInstance;
import com.hankcs.model.PerceptronNERInstance;
import com.hankcs.model.PerceptronPOSInstance;
import org.apache.lucene.analysis.Tokenizer;
import org.opensearch.common.settings.Settings;
import org.opensearch.env.Environment;
import org.opensearch.index.IndexSettings;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Project: opensearch-analysis-hanlp
 * Description: Hanlp tokenizer factory
 * Author: Kenn
 * Create: 2018-12-14 15:10
 */
public class HanLPTokenizerFactory extends AbstractTokenizerFactory {
    /**
     * 分词类型
     */
    private final HanLPType hanLPType;
    /**
     * 分词配置
     */
    private final Configuration configuration;

    public HanLPTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings, HanLPType hanLPType) {
        super(indexSettings, settings, name);
        this.hanLPType = hanLPType;
        this.configuration = new Configuration(env, settings);
    }

    public static HanLPTokenizerFactory getHanLPTokenizerFactory(IndexSettings indexSettings, Environment env, String name,
                                                                 Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, env, name, settings, HanLPType.HANLP);
    }

    public static HanLPTokenizerFactory getHanLPStandardTokenizerFactory(IndexSettings indexSettings, Environment env, String name,
                                                                         Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, env, name, settings, HanLPType.STANDARD);
    }

    public static HanLPTokenizerFactory getHanLPIndexTokenizerFactory(IndexSettings indexSettings, Environment env, String name,
                                                                      Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, env, name, settings, HanLPType.INDEX);
    }

    public static HanLPTokenizerFactory getHanLPNLPTokenizerFactory(IndexSettings indexSettings, Environment env, String name,
                                                                    Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, env, name, settings, HanLPType.NLP);
    }

    public static HanLPTokenizerFactory getHanLPCRFTokenizerFactory(IndexSettings indexSettings, Environment env, String name,
                                                                    Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, env, name, settings, HanLPType.CRF);
    }

    public static HanLPTokenizerFactory getHanLPNShortTokenizerFactory(IndexSettings indexSettings, Environment env, String name,
                                                                       Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, env, name, settings, HanLPType.N_SHORT);
    }

    public static HanLPTokenizerFactory getHanLPDijkstraTokenizerFactory(IndexSettings indexSettings, Environment env, String name,
                                                                         Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, env, name, settings, HanLPType.DIJKSTRA);
    }

    public static HanLPTokenizerFactory getHanLPSpeedTokenizerFactory(IndexSettings indexSettings, Environment env, String name,
                                                                      Settings settings) {
        return new HanLPTokenizerFactory(indexSettings, env, name, settings, HanLPType.SPEED);
    }

    @Override
    public Tokenizer create() {
        switch (this.hanLPType) {
            case INDEX:
                configuration.enableIndexMode(true);
                return TokenizerBuilder.tokenizer(AccessController.doPrivileged((PrivilegedAction<Segment>) () ->
                                HanLP.newSegment().enableIndexMode(true)),
                        configuration);
            case NLP:
                return TokenizerBuilder.tokenizer(AccessController.doPrivileged((PrivilegedAction<Segment>) () ->
                                new PerceptronLexicalAnalyzer(
                                        PerceptronCWSInstance.getInstance().getLinearModel(),
                                        PerceptronPOSInstance.getInstance().getLinearModel(),
                                        PerceptronNERInstance.getInstance().getLinearModel())
                        ),
                        configuration);
            case CRF:
                if (CRFPOSTaggerInstance.getInstance().getTagger() == null) {
                    return TokenizerBuilder.tokenizer(
                            AccessController.doPrivileged((PrivilegedAction<Segment>) () ->
                                    new CRFLexicalAnalyzer(
                                            CRFSegmenterInstance.getInstance().getSegmenter()
                                    )),
                            configuration);
                } else if (CRFNERecognizerInstance.getInstance().getRecognizer() == null) {
                    return TokenizerBuilder.tokenizer(
                            AccessController.doPrivileged((PrivilegedAction<Segment>) () ->
                                    new CRFLexicalAnalyzer(
                                            CRFSegmenterInstance.getInstance().getSegmenter(),
                                            CRFPOSTaggerInstance.getInstance().getTagger()
                                    )),
                            configuration);
                } else {
                    return TokenizerBuilder.tokenizer(
                            AccessController.doPrivileged((PrivilegedAction<Segment>) () ->
                                    new CRFLexicalAnalyzer(
                                            CRFSegmenterInstance.getInstance().getSegmenter(),
                                            CRFPOSTaggerInstance.getInstance().getTagger(),
                                            CRFNERecognizerInstance.getInstance().getRecognizer()
                                    )),
                            configuration);
                }
            case N_SHORT:
                configuration.enableCustomDictionary(false).enablePlaceRecognize(true).enableOrganizationRecognize(true);
                return TokenizerBuilder.tokenizer(
                        AccessController.doPrivileged(
                                (PrivilegedAction<Segment>) () -> new NShortSegment()
                                        .enableCustomDictionary(false)
                                        .enablePlaceRecognize(true)
                                        .enableOrganizationRecognize(true)),
                        configuration);
            case DIJKSTRA:
                configuration.enableCustomDictionary(false).enablePlaceRecognize(true).enableOrganizationRecognize(true);
                return TokenizerBuilder.tokenizer(
                        AccessController.doPrivileged(
                                (PrivilegedAction<Segment>) () -> new DijkstraSegment()
                                        .enableCustomDictionary(false)
                                        .enablePlaceRecognize(true)
                                        .enableOrganizationRecognize(true)),
                        configuration);
            case SPEED:
                configuration.enableCustomDictionary(false);
                return TokenizerBuilder.tokenizer(
                        AccessController.doPrivileged(
                                (PrivilegedAction<Segment>) () -> new DoubleArrayTrieSegment().enableCustomDictionary(false)
                        ),
                        configuration);
            case HANLP:
            case STANDARD:
            default:
                return TokenizerBuilder.tokenizer(
                        AccessController.doPrivileged((PrivilegedAction<Segment>) HanLP::newSegment),
                        configuration);
        }
    }
}
