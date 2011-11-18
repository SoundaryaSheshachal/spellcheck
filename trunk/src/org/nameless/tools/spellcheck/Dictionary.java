/*
 * RSS Beas.
 */

package org.nameless.tools.spellcheck;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Implementation of a words dictionary.
 * @author bsodhi
 */
public class Dictionary extends AbstractDictionary {

    public Dictionary() {
        super();
    }

    public Dictionary(ArrayList<String> dictionary) {
        super();
        this.dictionary = dictionary;
        sort();
    }

    public int getSize() {
        return dictionary.size();
    }
    
    public boolean containsWord(String word) {
        return Collections.binarySearch(dictionary, word.toLowerCase()) >= 0;
    }

    /**
     * Adds a word to this dictionary. Addition is done to the in-memory 
     * dictionary and if loaded from a disk file the to that file as well.
     * The text is tokenized into individual words and converted to lowercase
     * before adding.
     * 
     * @param text
     * @throws java.io.IOException
     */
    public synchronized void add(String text) throws IOException {
        String[] tokens = text.split("\\n");
        for (String word : tokens) {
            String origWord = word;
            word = word.trim().toLowerCase();
            int index = Collections.binarySearch(dictionary, word);
            if (index < 0) {
                logger.info("Adding " + word + " to dictionary.");
                dictionary.add(Math.abs(index) - 1, word);
                FileOutputStream fos = new FileOutputStream(dictionaryFile, true);
                fos.write(origWord.getBytes(CHAR_ENCODING));
                fos.write("\n".getBytes(CHAR_ENCODING));
                fos.close();
            }
        }
    }
}
