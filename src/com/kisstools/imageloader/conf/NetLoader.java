/**
 * @author dawson dong
 */

package com.kisstools.imageloader.conf;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class NetLoader implements Loader {

	@Override
	public InputStream load(String path) {
		InputStream inputStream = null;
		URLConnection conn = null;
		try {
			URL url = new URL(path);
			conn = url.openConnection();
			inputStream = conn.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return inputStream;
	}

}
