/**
 * @author dawson dong
 */

package com.kisstools.imageloader.conf;

import java.io.InputStream;

import android.content.ContentResolver;
import android.net.Uri;

import com.kisstools.KissTools;

public class ContentLoader implements Loader {

	@Override
	public InputStream load(String path) {
		Uri uri = Uri.parse(path);
		ContentResolver resolver = KissTools.getApplicationContext()
				.getContentResolver();
		InputStream inputStream = null;
		try {
			resolver.openInputStream(uri);
		} catch (Exception e) {
		}
		return inputStream;
	}

}
