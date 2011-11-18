/*
 * RSS Beas.
 */

package org.nameless.tools.spellcheck;

import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Holds the spelling errors. It also acts as a sort of listener for the errors
 * and keeps on addding them to the {@link Document} instance it is created
 * with.
 * @author bsodhi
 */
public class SpellingErrorAdapter implements SpellingErrorListener {

    /**
     * Document to which to add the spelling errors.
     */
    private Document document;
    /**
     * Spelling errors.
     */
    private TreeSet<String> errors = new TreeSet<String>();
    
    /**
     * Initializes this instance with the document to which to add the
     * spelling errors.
     * 
     * @param document
     */
    public SpellingErrorAdapter(Document document) {
        this.document = document;
    }

    /**
     * Adds the given word into the list of spelling errors and the document.
     * A word will be added only once even if there are multiple occurrances of
     * this word in the checked document.
     * 
     * @param text Word to be added.
     */
    public void addWord(String text) {
        if (errors.add(text)) {
            /*
            try {
                document.insertString(document.getLength(), errors.last() +"\n", null);
            } catch (BadLocationException ex) {
                Logger.getLogger(SpellingErrorAdapter.class.getName()).log(Level.SEVERE, null, ex);
            }*/
        }
        
    }

    public void writeErrorsToDocument() {
        StringBuilder sb = new StringBuilder();
        for (String error : errors) {
            sb.append(error).append("\n");
        }
        try {
            document.insertString(document.getLength(), sb.toString(), null);
        } catch (BadLocationException ex) {
            Logger.getLogger(SpellingErrorAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void clearErrors() {
        errors.clear();
    }

    /**
     * Returns the count of misspelt words found.
     * @return Number of spelling errors.
     */
    public int getCount() {
        return errors.size();
    }

}
