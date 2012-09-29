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
package net.freehal.app.impl;

import net.freehal.app.R;
import net.freehal.app.util.Util;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

public class FreehalUser {

	private static FreehalUser user;

	private Account[] accounts;
	private String emailAddr;
	private String userName;

	public static void init(final Context context) {
		if (user == null)
			user = new FreehalUser(context);
	}

	public static FreehalUser get() {
		return user;
	}

	private FreehalUser(final Context context) {
		if (accounts == null) {
			AccountManager am = AccountManager.get(context);
			accounts = am.getAccounts();
		}
	}

	public String getEmailAddr(final String or) {
		return Util.getPreferences().getString("userEmail", findOutEmailAddr(or));
	}

	public String findOutEmailAddr(final String or) {
		if (emailAddr == null) {
			String userGoogle = null;
			String userOther = null;

			for (Account account : accounts) {
				if (account.name.contains("gmail") || account.name.contains("googlemail")) {
					userGoogle = account.name;
				} else {
					userOther = account.name;
				}
			}

			emailAddr = userGoogle != null ? userGoogle : userOther;
		}
		return emailAddr != null ? emailAddr : or;
	}

	public String getUserName(final String or) {
		return Util.getPreferences().getString("userName", findOutUserName(or));
	}

	public String findOutUserName(final String or) {
		if (userName == null) {
			userName = this.findOutEmailAddr(or).split("[@]")[0];
		}
		return userName != null ? userName : or;
	}

	public String getFreehalName() {
		return Util.getPreferences().getString("freehalName", findOutFreehalName());
	}

	public String findOutFreehalName() {
		return Util.getString(R.string.person_freehal);
	}
}
