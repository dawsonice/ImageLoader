/**
 * @author dawson dong
 */

package com.kisstools.imageloader.loader;

import java.io.InputStream;

public interface Loader {

	public InputStream load(String path);

}
