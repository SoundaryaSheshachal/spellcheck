/*
 * RSS Beas.
 */
package org.nameless.tools.spellcheck.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.hwpf.extractor.WordExtractor;

/**
 * A utility class for reading text from MS Word file.
 * @author bsodhi
 */
public class MSWordUtil {
    /**
     * 
     */
    public static final String CHARSET_UTF8 = "UTF-8";
    /**
     * 
     */
    public static final String MIME_MSWORD = "content/unknown";
    /**
     * 
     */
    private static Logger logger = Logger.getLogger(MSWordUtil.class.getName());
    
    /**
     * 
     * @param filename
     * @return
     */
    public static String[] extractParagraphs(String filename) {
        
        String[] paras = null;
        try {
            String mime = findMIMEType(filename);
            if ("application/msword".equalsIgnoreCase(mime)) {
                WordExtractor extractor = new WordExtractor(new FileInputStream(filename));
                paras = extractor.getParagraphText();
            } else {
                paras = new String[]{getFileContentAsUTF8Text(filename)};
            }
        } catch (IOException ex) {
            Logger.getLogger(MSWordUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return paras;
    }
    
    /**
     * 
     * @param filename
     * @return
     */
    public static ArrayList<String> extractWords(String filename) {
        
        ArrayList<String> words = new ArrayList<String>();
        String[] paras;
        try {
            String mime = findMIMEType(filename);
            if (MIME_MSWORD.equalsIgnoreCase(mime)) {
                WordExtractor extractor = new WordExtractor(new FileInputStream(filename));
                paras = extractor.getParagraphText();
            } else {
                paras = new String[]{getFileContentAsUTF8Text(filename)};
            }
            for (String para : paras) {
                words.add(para);
                /*
                String[] lines = para.split("\\n");
                for (String line : lines) {
                    String[] tabbed = line.split("\\t");
                    for (String tab : tabbed) {
                        String[] parts = tab.split(" ");
                        for (String word : parts) {
                            words.add(word);
                        }
                    }
                }
                */
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        logger.info(filename+" has "+words.size()+" para.");
        return words;
    }
    
    /**
     * 
     * @param filePath
     * @return
     * @throws java.io.IOException
     */
    private static String getFileContentAsUTF8Text(String filePath) throws IOException {

        File file = new File(filePath);
        byte[] data = new byte[(int)file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(data);
        fis.close();
        return new String(data, CHARSET_UTF8);
    }
    
    /**
     * 
     * @param file
     * @return
     * @throws java.net.MalformedURLException
     * @throws java.io.IOException
     */
    public static String findMIMEType(String file) throws MalformedURLException, IOException {
        URL u = new URL("file", null, file);
        String mime = u.openConnection().getContentType();
        logger.info("MIME type: " + mime);
        return mime;
    }

    /**
     * 
     * @param filename
     * @return
     */
    public static String extractText(String filename) {
        
        String text = null;
        try {
            if (MIME_MSWORD.equalsIgnoreCase(findMIMEType(filename))) {
                WordExtractor extractor = new WordExtractor(new FileInputStream(filename));
                text = extractor.getText();
            } else {
                text = getFileContentAsUTF8Text(filename);
            }
        } catch (IOException ex) {
            Logger.getLogger(MSWordUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return text;
    }
}
