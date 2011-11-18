/*
 * RSS Beas.
 */

package org.nameless.tools.spellcheck.ui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * User prefernces are manipulated via this class.
 * @author bsodhi
 */
public class PrefsHelper {
    public static final String CAPSWORDSIGNORED = "upper.case.words.ignored";
    public static final String DICTIONARIES = "dictionaries";
    public static final String LASTDIRECTORY = "last.accessed.directory";
    public static final String WORDDELIMITERS = "word.delimiters";
    
    /**
     * User preferences instance for this application.
     */
    private static Preferences userPrefs = 
        Preferences.userNodeForPackage(PrefsHelper.class);
    /**
     * Returns all the custom dictionaries set by the current user.
     * @return A HashMap containing the dictionaries. Key is the ID for the
     * dictionary and value is the absolute path of the dictionary file.
     */
    public static HashMap<String, String> getDictionaries() {
        
        HashMap<String, String> dicts = new HashMap<String, String>();
        try {
            Preferences prefsNode = userPrefs.node(DICTIONARIES);
            String[] keys = prefsNode.keys();
            
            for (String key : keys) {
                dicts.put(key, prefsNode.get(key, ""));
            }
            
        } catch (BackingStoreException ex) {
            Logger.getLogger(PrefsHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dicts;
    }
    
    public static HashSet<String> getWordDelimiters() {

        HashSet<String> delims = new HashSet<String>();
        try {
            Preferences prefsNode = userPrefs.node(WORDDELIMITERS);
            String[] keys = prefsNode.keys();
            delims.addAll(Arrays.asList(keys));
            
        } catch (BackingStoreException ex) {
            Logger.getLogger(PrefsHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return delims;
    }

    /**
     * Gets the default custom dictionary set by the current user.
     * @return Path of the default custom dictionary if set by the current user.
     * If not set then empty string will be returned.
     */
    public static String getDefaultDictionary() {
        
        Preferences prefsNode = userPrefs.node(DICTIONARIES);
        return prefsNode.get("default", "");
    }

    /**
     * If the upper case words are to be ignored during the spell checking.
     * @return
     */
    public static boolean isUpperCaseWordsIgnored() {
        return userPrefs.getBoolean(CAPSWORDSIGNORED, false);
    }
    /**
     * Sets the user preference for ignoring the upper case words during the
     * spelling check.
     * @param ignore
     */
    public static void setUpperCaseWordsIgnored(boolean ignore) {
        try {
            userPrefs.putBoolean( CAPSWORDSIGNORED, ignore);
            userPrefs.flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(PrefsHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Saves the custom dictionary preferences as selected by the current
     * user.
     * @param dictPrefs
     */
    public static void saveDictionaryPrefs(HashMap dictPrefs) {
        try {
            Preferences prefs = userPrefs.node(DICTIONARIES);
            prefs.removeNode();
            prefs = userPrefs.node(DICTIONARIES);
            Iterator it = dictPrefs.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                prefs.put(key, dictPrefs.get(key).toString());
            }
            prefs.flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(PrefsHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void saveWordDelimiterPrefs(HashSet<String> wordDelims) {
        try {
            Preferences prefs = userPrefs.node(WORDDELIMITERS);
            prefs.removeNode();
            prefs = userPrefs.node(WORDDELIMITERS);
            for (String item : wordDelims) {
                prefs.put(item, item);
            }
            prefs.flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(PrefsHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Gets the last accessed directory by the user via a file chooser dialog
     * open through this application so that next time the file chooser
     * opens in the last location.
     * @return
     */
    public static String getLastAccessedDirectory() {
        return userPrefs.get(LASTDIRECTORY, null);
    }

    /**
     * Sets the last accessed directory by the user via a file chooser dialog
     * open through this application so that next time the file chooser
     * opens in the last location.
     * @param path
     */
    public static void setLastAccessedDirectory(String path) {
        try {
            userPrefs.put( LASTDIRECTORY, path);
            userPrefs.flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(PrefsHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
