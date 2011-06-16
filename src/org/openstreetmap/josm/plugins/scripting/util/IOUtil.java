package org.openstreetmap.josm.plugins.scripting.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

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
	
	static public void close(InputStream is){
		if (is != null) {
			try {
				is.close();
			} catch(IOException e){
				// ignore
			}
		}
	}
	
	static public void close(Writer writer){
		if (writer != null)
			try {
				writer.close();
			} catch (IOException e) {
			}
	}
}