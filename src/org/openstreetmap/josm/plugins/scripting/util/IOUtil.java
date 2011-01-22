package org.openstreetmap.josm.plugins.scripting.util;

import java.io.IOException;
import java.io.Reader;

public class IOUtil {

	static public void close(Reader reader){
		if (reader != null) {
			try {
				reader.close();
			} catch(IOException e){
				// ignore
			}
		}
	}
}
