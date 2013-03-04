/*
 * RSS Beas.
 */

package org.nameless.tools.spellcheck;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import org.nameless.tools.spellcheck.ui.PrefsHelper;

/**
 * Base class for the dictionary functionality. It provides the common logic for
 * loading the dictionaries from different sources and also for adding new
 * words to the custom dictionaries etc.
 * @author bsodhi
 */
public abstract class AbstractDictionary {
    /**
     * Encoding to be used for reading and writing the words.
     */
    public static final String CHAR_ENCODING = "UTF-8";
    
    /**
     * Indicates the main/system dictionary type.
     */
    public static final byte SYSTEM = 10;
    /**
     * Indicates the default custom dictionary type.
     */
    public static final byte CUSTOM_DEF = 11;
    /**
     * Indicates the non-default custom dictionary type.
     */
    public static final byte CUSTOM = 12;
    
    /**
     * Logger instance for this class.
     */
    protected Logger logger = Logger.getLogger(getClass().getName());
    /**
     * The dictionary of words.
     */
    protected ArrayList<String> dictionary;
    /**
     * Handle to the file if this dictionary is loaded from a plain disk file.
     */
    protected File dictionaryFile;
    /**
     * Type of the dictionary this instance holds.
     */
    protected byte type;

    /**
     * Adds a word to this dictionary. Addition is done to the in-memory 
     * dictionary and if loaded from a disk file the to that file as well.
     * The text is tokenized into individual words and converted to lowercase
     * before adding.
     * 
     * @param text
     * @throws java.io.IOException
     */
    public abstract void add(String text) throws IOException;

    /**
     * Checks if the supplied word exists in this dictionary.
     * @param word
     * @return true if word exists in this dictionary, else false.
     */
    public abstract boolean containsWord(String word);

    /**
     * Size of this dictionary.
     * @return Size of this dictionary.
     */
    public abstract int getSize();

    /**
     * Type of the dictionary this instance holds.
     * @return Dictionary type.
     */
    public byte getType() {
        return type;
    }

    /**
     * Type of the dictionary this instance holds.
     * @param type
     */
    public void setType(byte type) {
        this.type = type;
    }
    
    
    /**
     * Sorts the dictionary.
     */
    protected void sort() {
        Collections.sort(dictionary);
        logger.info("Dictionary size: " + dictionary.size() + " words.");        
    }
    
    /**
     * Initializes the system dictionary. The system dictionary files must be
     * present in a JAR/ZIP file created with the base directory named
     * "dictionaries". So the JAR/ZIP file will look like this:
     * jar:[url]!/dictionaries/*.*
     * @param jarRoot Root of the jar file under which dictionary files are
     * stored. E.g. 'dictionaries' in case of jar:[url]!/dictionaries/*.*
     * @return Dictionary words.
     * @throws java.io.IOException
     */
    public void loadFromClasspathJar(String jarRoot) throws IOException {
        
        dictionary = new ArrayList<String>();
        try {
            if (PrefsHelper.isDictIgnored(PrefsHelper.WordListTypes.All)) {
                logger.info("All system word lists are excluded!");
                return;
            }
            URL url = Thread.currentThread().
                    getContextClassLoader().getResource(jarRoot);
            JarURLConnection jarConnection = (JarURLConnection)url.openConnection();
            JarFile jarFile = jarConnection.getJarFile();
            Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry ent = (JarEntry) entries.nextElement();
                String name = ent.getName().toLowerCase();
                if (name.startsWith("meta-inf")) continue;
                if (name.contains("american") && PrefsHelper.isDictIgnored(PrefsHelper.WordListTypes.American)) {
                    logger.info("Skipping American words list: "+name);
                    continue;
                } else if (name.contains("british") && PrefsHelper.isDictIgnored(PrefsHelper.WordListTypes.British)) {
                    logger.info("Skipping British words list: "+name);
                    continue;
                } else if (name.contains("canadian") && PrefsHelper.isDictIgnored(PrefsHelper.WordListTypes.Canadian)) {
                    logger.info("Skipping Canadian words list: "+name);
                    continue;
                } else if (name.contains("english") && PrefsHelper.isDictIgnored(PrefsHelper.WordListTypes.English)) {
                    logger.info("Skipping English words list: "+name);
                    continue;
                }
                HashSet<String> words = 
                        processJarEntry(jarFile.getInputStream(ent));
                dictionary.addAll(words);
            }
            jarFile.close();
            sort();
        } catch (Exception ex) {
            IOException ioe = new IOException("Could not initialize dictionary.");
            ioe.initCause(ex);
            throw ioe;
        }
    }
    
    /**
     * Loads the words from the given dictioanry file into a collection.
     * The dictionary file should have only one word per line. The words are
     * converted to lower case before adding to the HashSet.
     * @param dict Dictionary file in the format having ow word per line.
     * @throws java.io.IOException
     */
    public void loadFromFile(File dict) throws IOException {
        this.dictionaryFile = dict;
        BufferedReader in = null;
        HashSet<String> words = new HashSet<String>();
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(dict),CHAR_ENCODING));
            String line = null;
            while ((line = in.readLine()) != null) {
                // Store all words in lowercase
                words.add(line.trim().toLowerCase());
            }
        } finally {
            try {
                in.close();
            } catch (Exception ex) {
            }
        }
        dictionary = new ArrayList<String>(words);
        sort();
    }
    
    /**
     * Reads the contents of the JAR/ZIP file entry (which is a dictionary file)
     * and adds the words found in it to the main dictionary data structure.
     * @param input ZIP/JAR file entry's input stream to read from.
     * @throws java.io.IOException
     */
    private HashSet<String> processJarEntry(InputStream input)
            throws IOException {
        
        HashSet<String> words = new HashSet<String>();
        InputStreamReader isr =
                new InputStreamReader(input,CHAR_ENCODING);
        BufferedReader reader = new BufferedReader(isr);
        String line;
        while ((line = reader.readLine()) != null) {
            words.add(line.trim().toLowerCase());
        }
        reader.close();
        return words;
    }    
}
