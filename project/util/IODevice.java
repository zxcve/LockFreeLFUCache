package project.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class IODevice {
	public static final int fileSize = 27;
	private static final String file = "http://people.cs.vt.edu/applewil/CS5510/dict.bin";

	private static URL getURL() {
		URL fileURL = null;
		try {
			fileURL = new URL(file);
		} catch (MalformedURLException e) {
			System.err.println(e.getMessage());
		}
		return fileURL;
	}

	public static char read(int addr) {
		char read = 0;
		try {
			InputStream inputStream = getURL().openStream();
			inputStream.skip(addr);
			read = (char)inputStream.read();
			inputStream.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		return read;
	}

}
