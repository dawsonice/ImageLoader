/**
 * @author dawson dong
 */

package com.kisstools.imageloader.conf;

import android.text.TextUtils;

import com.kisstools.utils.SecurityUtil;

public class FileNamer implements Namer {

	@Override
	public String create(String text) {
		if (TextUtils.isEmpty(text)) {
			return null;
		}
		return SecurityUtil.getMD5(text) + ".png";
	}

}
