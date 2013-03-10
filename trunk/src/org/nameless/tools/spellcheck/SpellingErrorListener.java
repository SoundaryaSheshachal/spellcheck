/*
 * RSS Beas.
 */

package org.nameless.tools.spellcheck;

/**
 *
 * @author bsodhi
 */
public interface SpellingErrorListener {

    /**
     * Adds the given word into the list of spelling errors and the document.
     * A word will be added only once even if there are multiple occurrances of
     * this word in the checked document.
     * 
     * @param text Word to be added.
     */
    void addWord(String text);

    /**
     * Returns the count of misspelt words found.
     * @return Number of spelling errors.
     */
    int getCount();

    /**
     * Clears all the errors.
     */
    void clearErrors();

    /**
     * Writes all the errors to a document.
     */
    void writeErrorsToDocument();
}
