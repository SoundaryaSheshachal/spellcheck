/*
 * SpellChecker.java
 *
 * Created on Nov 25, 2007, 8:44:34 PM
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nameless.tools.spellcheck;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.nameless.tools.spellcheck.ui.PrefsHelper;

/**
 * Main runnable task that does the spellings check for a chunk of text.
 * Typically it is expected to be run in a separate thread.
 *
 * @author bsodhi
 */
public class SpellCheckerTask implements Runnable {

    /**
     * Logger instance for this class.
     */
    private Logger logger = Logger.getLogger(getClass().getName());
    /**
     * Text to check spelling for.
     */
    private String text;
    /**
     * Listener for spelling check errors.
     */
    private final SpellingErrorListener listener;
    /**
     * This one holds the dictionaries that we check against.
     */
    private ArrayList<Dictionary> dictionaries;
    private HashSet<Integer> delimiters;
    /**
     * List of common suffixes added to words as shorthands. For example, s
     * (He's), re (They're), ll (She'll) etc.
     */
    private List<String> COMMON_SUFFIX = Arrays.asList("s", "re", "ll", "d", "t", "ve");

    /**
     * Creates the instance of this task by supplying it with the chunk of text
     * to spell check, and the spelling error listener.
     *
     * @param text Text to spell-check
     * @param listener Spelling error listener instance.
     * @param dictionaries
     * @param delims
     * @throws java.io.IOException
     */
    public SpellCheckerTask(String text, SpellingErrorListener listener,
            ArrayList<Dictionary> dictionaries, HashSet<Integer> delims) throws IOException {
        this.text = text;
        this.listener = listener;
        this.dictionaries = dictionaries;
        this.delimiters = delims;
    }

    /**
     * Checks is the given word exists in the dictionaries. It automatically
     * fires the spelling error events to the spelling error listener.
     *
     * @param word Word to search.
     */
    private boolean checkSpelling(String word) {

        boolean exists = false;
        if (word == null || word.trim().length() == 0) {
            exists = true;
        } else if (isFiltered(word)) {
            exists = true;
        } else {

            if (word.contains("-")) {
                if (!isInWordList(word)) {
                    boolean pe = true;
                    for (String part : word.split("-"))
                    {
                        pe = pe && checkSpelling(part.trim());
                    }
                    exists = pe;
                } else {
                    exists = true;
                }
            } else {
                exists = isInWordList(word);
            }
        }
        return exists;
    }

    /**
     * Checks if the given word is filtered. A word is filtered if it: 1) Is an
     * abbreviation (i.e. contains single alphabet characters separated by
     * dots). 2) Is in upper case and user setting says to ignore upper case
     * words 3) Is a number or a suffixed number e.g. 23rd, 45th, 1970s etc.
     *
     * @param word Word to check
     * @return true if the word is determined to be filtered, else false.
     */
    private boolean isFiltered(String word) {
        boolean filtered = false;
        try {
            Float.parseFloat(word);
            filtered = true;
        } catch (NumberFormatException nfe) {
        }
        if (isAbbreviation(word) || PrefsHelper.isUpperCaseWordsIgnored()
                && isUpperCase(word)) {
            filtered = true;
        }
        // Takes care of the suffixed numbers e.g. 23rd, 45th, 1970s etc.
        filtered = Pattern.matches("[0-9]+(st|nd|rd|th|s)?", word.toLowerCase());
        return filtered;
    }

    /**
     * If the given word is in upper case.
     *
     * @param word Word to check
     * @return true if the word is in upper case, else false.
     */
    private boolean isUpperCase(String word) {
        boolean ucase = true;
        char[] chars = word.toCharArray();
        for (char ch : chars) {
            if (Character.isLowerCase(ch)) {
                ucase = false;
                break;
            }
        }
        return ucase;
    }

    /**
     * If the given text is possibly an abbreviation. (i.e. contains single
     * alphabet characters seperated by dots).
     *
     * @param word Text to check.
     * @return
     */
    private boolean isAbbreviation(String word) {

        boolean abbr = true;
        String[] parts = word.split("\\.");
        for (String part : parts) {
            if (part.length() > 1 || !Pattern.matches("[a-zA-Z]", part)) {
                abbr = false;
                break;
            }
        }
        return abbr;
    }

    /**
     * Main spell-check work is done here. The text chunk supplied to this task
     * is tokenized into single words and each word is searched in the
     * dictionaries. Any white space around the words is trimmed and the
     * punctuations removed before searching the words in dictionary. Also,
     * hyphen seperated composite words will be split and searched seperately.
     */
    public void run() {

        /**
         * Replace all delimiters with single space so that words can be
         * tokenized with space as delimiter.
         */
        for (int x : delimiters) {
            text = text.replace((char) x, ' ');
        }

        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        boolean findCompoundWords = PrefsHelper.isFindCompoundWordsEnabled();
        ArrayList<String> ufl = new ArrayList<String>();
        for (; tokenizer.hasMoreTokens();) {
            String word = tokenizer.nextToken().trim();
            boolean endsWithPunc = word.matches(".*[,.!?;]");
            
            // Remove punctuation marks from both ends
            String prevWord = null;
            while (!word.equals(prevWord)) {
                prevWord = word;
                word = removePunctuation(word);
            }
            
            // Check spelling in word lists
            boolean found = checkSpelling(word);
            if (findCompoundWords) {
                if (!found) {
                    ufl.add(word);
                    if (endsWithPunc) pushErrorToListener(ufl);
                } else {
                    pushErrorToListener(ufl);
                }
            } else {
                if (!found) listener.addWord(word);
            }
        }
        pushErrorToListener(ufl);
    }

    /**
     * Removes any punctuation ('.', ',', '?', '!' etc.) from around the given
     * word.
     *
     * @param word
     * @return
     */
    private String removePunctuation(String word) {

        StringBuffer sb = new StringBuffer(word);
        if (word.length() == 0) {
            return word;
        }
        for (String cs : COMMON_SUFFIX) {
            if (word.endsWith("'" + cs) || word.endsWith("’" + cs)) {
                sb.delete(sb.length() - cs.length() - 1, sb.length());
                break;
            }
        }
        if (sb.length() > 0) {
            int first = Character.getType(sb.charAt(0));
            int last = Character.getType(sb.charAt(sb.length() - 1));
            if (last != Character.LOWERCASE_LETTER
                    && last != Character.UPPERCASE_LETTER) {
                sb.deleteCharAt(sb.length() - 1);
            }
            if (sb.length() > 0 && first != Character.LOWERCASE_LETTER
                    && first != Character.UPPERCASE_LETTER) {
                sb.deleteCharAt(0);
            }
        }
        return sb.toString();
    }

    private boolean isInWordList(String word) {
        boolean exists = false;
        for (Dictionary dict : dictionaries) {
            if (dict.containsWord(word)) {
                exists = true;
                break;
            }
        }
        return exists;
    }

    private void pushErrorToListener(ArrayList<String> ufl) {
        if (!ufl.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for(String x : ufl) sb.append(x).append(" ");
            listener.addWord(sb.toString());
            ufl.clear();
        }
    }
}