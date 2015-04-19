/**
 * @author dawson dong
 */

package com.kisstools.imageloader.conf;

import java.io.FileInputStream;
import java.io.InputStream;

import android.net.Uri;

import com.kisstools.utils.UrlUtil;

public class FileLoader implements Loader {

	@Override
	public InputStream load(String path) {
		Uri uri = UrlUtil.parse(path);
		String absPath = uri.getPath();
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(absPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return inputStream;
	}

}
