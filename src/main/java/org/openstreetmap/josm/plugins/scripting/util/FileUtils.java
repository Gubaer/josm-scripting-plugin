package org.openstreetmap.josm.plugins.scripting.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileUtils {

    /**
     * Builds a reader which reads from a UTF-8 encoded text file
     *
     * @param file the text file
     * @return the reader
     * @throws IOException
     */
    public static Reader buildTextFileReader(File file) throws IOException {
        return new InputStreamReader(
                new FileInputStream(file),
                StandardCharsets.UTF_8
        );
    }
}
