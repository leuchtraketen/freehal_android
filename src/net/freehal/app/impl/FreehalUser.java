package net.freehal.app.impl;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

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
		if (emailAddr == null) {
			String userGoogle = null;
			String userOther = null;

			for (Account account : accounts) {
				if (account.name.contains("gmail")
						|| account.name.contains("googlemail")) {
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
		if (userName == null) {
			userName = this.getEmailAddr(or).split("[@]")[0];
		}
		return userName != null ? userName : or;
	}
}
