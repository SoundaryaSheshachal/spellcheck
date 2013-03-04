/*
 * SpellChecker.java
 *
 * Created on Nov 25, 2007, 8:24:03 PM
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nameless.tools.spellcheck;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import org.nameless.tools.spellcheck.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.nameless.tools.spellcheck.ui.PrefsHelper;

/**
 * This is the main spell checker class. It takes the input file and extracts
 * the text from it. Then it spawns multiple concurrent threads to execute
 * {@link SpellCheckerTask}s giving each of them a chunk from the whole text.
 * @author bsodhi
 */
public class SpellChecker {

    private Logger logger = Logger.getLogger(getClass().getName());
    private final BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(50);
    private int corePoolSize = 2;
    private int maxPoolSize = 2;
    private long keepAliveTime = 10L;
    private TimeUnit unit = TimeUnit.MILLISECONDS;
    private Dictionary sysDictionary = new Dictionary();
    private ArrayList<Dictionary> customDicts = new ArrayList<Dictionary>();
    public static String[] DELIMS_UNICODE = {
        "u000A", "u000B", "u000C", "u000D", "u2028", "u2029", "u0013", "u2003",/*Whitespace*/
        "u003A",/*Colon :*/
        "u2012", "u2013", "u2014", "u2015",/* Dash - */
        "u002C",/*Comma*/ 
        "u0009", /*Tab*/
        "u002E", /* Period .*/ 
        "u002F", /* Slash */
        "u005C" /*Backslash*/,
        "u0023" /*# sign*/,
        "u0022" /*Quotation mark*/
    };

//    private static Integer[] DELIMITERS = {
//            0x000A, 0x000B, 0x000C, 0x000D, 0x2028, 0x2029, 0x0013, 0x2003,/*Whitespace*/
//            0x003A,/*Colon :*/
//            0x2012, 0x2013, 0x2014, 0x2015,/* Dash - */
//            0x002C,/*Comma*/ (int)'\t', 0x002E /* Period .*/, (int)'/', (int)'\\'};
    
    /**
     * Creates the instance of this class and assigns it the input file to be
     * spell-checked. 
     * @throws java.io.IOException
     */
    public SpellChecker() throws IOException {
        // Load the main system sictionary
        sysDictionary.loadFromClasspathJar("dictionaries");
    }

    /**
     * Adds the given text to the dictionary. The given text is first
     * split using the regular expression: "\\n" (i.e. by newlines) and
     * each token thus found is added at the appropriare sorted order location
     * in the main dictionary in memory. Each token is trimmed of any white
     * space around it and is converted to lower case before inserting.
     * @param text Text to be added to dictionary.
     * @throws java.io.IOException
     */
    public void addToDefaultCustomDictionary(String text) throws IOException {
        for (Dictionary dict : customDicts) {
            if (Dictionary.CUSTOM_DEF == dict.getType()) {
                dict.add(text);
                break;
            }
        }
    }

    /**
     * This is the main method that a client will call to perform the spelling
     * check on a text file.
     * @param inputFile Input file to be spell-checked.
     * @param listener Spelling error listener.
     * @throws java.io.IOException
     */
    public void checkSpelling(String inputFile, 
            SpellingErrorListener listener) throws IOException {

        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, 
            maxPoolSize, keepAliveTime, unit, workQueue, 
            new ThreadPoolExecutor.CallerRunsPolicy());
        
        ArrayList<String> paras = MSWordUtil.extractWords(inputFile);
        ArrayList<Dictionary> dicts = new ArrayList<Dictionary>();
        dicts.add(sysDictionary);
        dicts.addAll(customDicts);
        for (String para : paras) {
            executor.execute(new SpellCheckerTask(para, listener, dicts, getDelimiters()));
        }
        
        executor.shutdown();
        try {
            // Wait for long enough time to let the process complete
            executor.awaitTermination(30, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        listener.writeErrorsToDocument();
        logger.info("Error count: "+listener.getCount());
    }

    /**
     * Adds a new custom dictionary.
     * @param dictFile
     * @throws java.io.IOException
     */
    public void addCustomDictionary(String dictFile, boolean isDefault) throws IOException {
        Dictionary d = new Dictionary();
        if (isDefault) {
            d.setType(Dictionary.CUSTOM_DEF);
        }
        d.loadFromFile(new File(dictFile));
        logger.info("Added dictionary ["+dictFile+"]. Size "+d.getSize()+" words.");
        customDicts.add(d);
    }
    
    /**
     * Clears all the custom dictionaries from the list.
     */
    public void clearCustomDictionaries() {
        customDicts.clear();
    }

    public HashSet<Integer> getDelimiters() throws NumberFormatException {
        
        HashSet<Integer> delimiters = new HashSet<Integer>();
        // Load word delims
        HashSet<String> delims = PrefsHelper.getWordDelimiters();
        if (delims.isEmpty()) {
            PrefsHelper.saveWordDelimiterPrefs(new HashSet<String>(Arrays.asList(DELIMS_UNICODE)));
        }
        for(String d : PrefsHelper.getWordDelimiters())
        {
            int hex = Integer.decode(d.replace("u", "0x"));
            delimiters.add(hex);
        }
        logger.info("Loaded "+delimiters.size()+" word delimiters.");
        return delimiters;
    }
}