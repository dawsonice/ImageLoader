/**
 * @author dawson dong
 */

package com.kisstools.imageloader.conf;

import java.io.InputStream;

public interface Loader {

	public InputStream load(String path);

}
