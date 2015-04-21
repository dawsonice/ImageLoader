/**
 * @author dawson dong
 */

package com.kisstools.imageloader.loader;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.kisstools.utils.LogUtil;

public class NetLoader implements Loader {

	public static final String TAG = "NetLoader";

	@Override
	public InputStream load(String path) {
		InputStream inputStream = null;
		HttpURLConnection conn = null;
		try {
			URL url = new URL(path);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(10000);
			int responseCode = conn.getResponseCode();
			LogUtil.d(TAG, "response code " + responseCode);
			inputStream = conn.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return inputStream;
	}

}
