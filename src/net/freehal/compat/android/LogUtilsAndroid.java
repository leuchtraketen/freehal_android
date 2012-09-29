/*******************************************************************************
 * Copyright (c) 2006 - 2012 Tobias Schulz and Contributors.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/gpl.html>.
 ******************************************************************************/
package net.freehal.compat.android;

import net.freehal.compat.sunjava.LogUtilsStandard.AbstractLogStream;
import net.freehal.compat.sunjava.LogUtilsStandard.LogStream;
import net.freehal.compat.sunjava.LogUtilsStandard.StackTraceUtils;
import net.freehal.core.util.RegexUtils;
import android.util.Log;

public class LogUtilsAndroid {

	public static class AndroidLogStream extends AbstractLogStream {

		public static LogStream create() {
			return new AndroidLogStream();
		}

		@Override
		public void add(String type, String msg, StackTraceElement ste) {
			StringBuilder tag = new StringBuilder();
			tag.append(StackTraceUtils.lastPackage(ste)).append(".").append(StackTraceUtils.className(ste))
					.append(":").append(ste.getLineNumber());

			// remove all carriage returns
			if (msg.contains("\\r")) {
				msg = RegexUtils.replace(msg, "\\\\r", "");
			}

			int priority = type.equals("error") ? Log.ERROR : type.equals("warn") ? Log.WARN : type
					.equals("info") ? Log.INFO : type.equals("debug") ? Log.DEBUG : Log.INFO;

			Log.println(priority, tag.toString(), msg);
		}

		@Override
		public void flush() {}
	}
}