package net.freehal.app;

import java.util.HashMap;

import net.freehal.app.impl.FreehalImpl;
import net.freehal.app.impl.FreehalImplOffline;
import net.freehal.app.impl.FreehalImplOnline;
import net.freehal.app.select.SelectContent;
import net.freehal.app.util.ExecuteLater;
import net.freehal.app.util.SpeechHelper;
import net.freehal.app.util.Util;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

@SuppressLint("ValidFragment")
public class DetailFragment extends SherlockFragment {

	public static String ARG_ITEM_ID = "item_id";

	private String tab;
	private Activity activity;
	private History history;
	private HistoryAdapter historyAdapter;

	private static FreehalImpl onlineImpl;
	private static FreehalImpl offlineImpl;
	private static String log;
	private static String graph;
	private static Intent recievedIntent;

	private static HashMap<String, DetailFragment> tabs;
	static {
		tabs = new HashMap<String, DetailFragment>();
	}

	/**
	 * Singleton!
	 * 
	 * @param id
	 *            the id of the tab
	 * @param activity
	 *            the activity (OverviewActivity for tablets or DetailActivity
	 *            for phones)
	 * @return the singleton instance
	 */
	public static DetailFragment forTab(String id, Activity activity) {
		Log.e("forTab", "1: id=" + id);
		id = SelectContent.validateId(id);
		Log.e("forTab", "2: id=" + id);
		boolean isCached = tabs.containsKey(id);
		if (!isCached) {
			Log.e("forTab", "not cached.");
			DetailFragment instance = new DetailFragment();
			instance.setTab(id);
			Bundle arguments = new Bundle();
			arguments.putString(DetailFragment.ARG_ITEM_ID, id);
			instance.setActivity(activity);
			instance.setArguments(arguments);
			tabs.put(id, instance);
		}
		Log.e("forTab", "3: tabs.get(id).id=" + tabs.get(id).getTab());
		DetailFragment instance = tabs.get(id);
		if (!isCached) {
			tabs.remove(id);
		}
		return instance;
	}

	public String getTab() {
		return tab;
	}

	public void setTab(String tab) {
		this.tab = tab;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(null);
	}

	public String getOutput(final String input, final FreehalImpl impl) {
		impl.setInput(input);
		impl.compute();
		log = impl.getLog();
		graph = impl.getGraph();
		return impl.getOutput();
	}

	public void gotInput(final EditText edit, final FreehalImpl impl) {

		if (history == null)
			return;

		final String input = Util.toAscii(edit.getText().toString());

		if (input.length() > 0) {
			final int inputNo = history.addInput(input, "");
			edit.setText("");

			final String noOutput = getResources()
					.getString(R.string.no_output);
			AsyncTask<String, Void, String> async = new AsyncTask<String, Void, String>() {

				@Override
				protected String doInBackground(String... arg0) {
					System.out.println("input: " + input);
					String output = getOutput(input, impl);
					if (output == null)
						output = noOutput;
					System.out.println("output: " + input);
					return output;
				}

				@Override
				protected void onPostExecute(String output) {
					history.addOutput(output, inputNo + "");

					SpeechHelper.getInstance().say(output, activity);
					showKeyboard(edit, 1000);
				}

			};
			async.execute();
		}

	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putString("tab", tab);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (savedInstanceState != null && savedInstanceState.containsKey("tab"))
			tab = savedInstanceState.getString("tab");
		tab = SelectContent.validateId(tab);

		final View rootView;
		if (tab.equals("online") || tab.equals("offline")) {
			rootView = onCreateViewConversation(inflater, container,
					savedInstanceState, tab);
		} else if (tab.equals("log")) {
			rootView = onCreateViewLog(inflater, container, savedInstanceState,
					tab);
		} else if (tab.equals("graph")) {
			rootView = onCreateViewGraph(inflater, container,
					savedInstanceState, tab);
		} else if (tab.equals("settings")) {
			rootView = onCreateViewSettings(inflater, container,
					savedInstanceState, tab);
		} else if (tab.equals("about")) {
			rootView = onCreateViewAbout(inflater, container,
					savedInstanceState, tab);
		} else {
			Log.e("onCreateView", "unknown tab: " + tab);
			rootView = onCreateViewSettings(inflater, container,
					savedInstanceState, "offline");
		}

		return rootView;
	}

	public View onCreateViewConversation(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState, final String item) {

		final View rootView = inflater.inflate(
				R.layout.fragment_conversation_detail, container, false);
		final EditText edit = (EditText) rootView
				.findViewById(R.id.edit_message);
		final Button sendButton = (Button) rootView
				.findViewById(R.id.button_send);

		history = createHistory(rootView, item);
		final FreehalImpl impl = chooseFreehalImpl(item);

		edit.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					gotInput(edit, impl);
					return true;
				} else {
					return false;
				}
			}
		});
		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				gotInput(edit, impl);
			}
		});

		showKeyboard(edit, 1000);

		ListView list = (ListView) rootView.findViewById(R.id.listView);

		historyAdapter = new HistoryAdapter(rootView.getContext(),
				R.layout.row, history);
		historyAdapter.setListView(list);
		list.setAdapter(historyAdapter);

		registerForContextMenu(list);

		if (DetailFragment.hasRecievedIntent()) {
			Intent intent = DetailFragment.getRecievedIntent();
			String action = intent.getAction();
			String type = intent.getType();

			if (Intent.ACTION_SEND.equals(action) && type != null) {
				if ("text/plain".equals(type)) {
					edit.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
					gotInput(edit, impl);
				}
			}
		}

		return rootView;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = this.getActivity().getMenuInflater();
		inflater.inflate(R.menu.statement, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (history == null)
			return true;

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		final int position = info.position;
		final String text;
		if (history.getRef(position).length() > 0)
			text = history.getText(Integer.parseInt(history.getRef(position)),
					position);
		else
			text = history.getText(position);
		EditText edit = (EditText) getView().findViewById(R.id.edit_message);

		switch (item.getItemId()) {
		case R.id.menu_statement_ask_again:
			edit.setText(text);
			final FreehalImpl impl = chooseFreehalImpl(tab);
			gotInput(edit, impl);
			return true;
		case R.id.menu_statement_edit:
			edit.setText(text);
			return true;
		case R.id.menu_statement_share_conversation:
			share(Html.fromHtml(history.toString()).toString(),
					this.getResources().getString(
							R.string.subject_share_conversation));
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void share(final String text, final String subject) {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, text);
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		sendIntent.setType("text/plain");
		startActivity(sendIntent);
	}

	private void showKeyboard(final EditText edit, final int timeToSleep) {
		edit.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				ExecuteLater asyncScroll = new ExecuteLater(500, 5) {
					@Override
					public void run() {
						if (history != null)
							history.refresh();
					}
				};
				asyncScroll.execute();
				return false;
			}
		});
		edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && activity != null) {
					InputMethodManager inputStatus = (InputMethodManager) activity
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					inputStatus.showSoftInput(edit,
							InputMethodManager.SHOW_IMPLICIT);

					ExecuteLater asyncScroll = new ExecuteLater(300, 3) {
						@Override
						public void run() {
							if (history != null)
								history.refresh();
						}
					};
					asyncScroll.execute();
				}
			}
		});

		ExecuteLater asyncFocus = new ExecuteLater(timeToSleep) {
			@Override
			public void run() {
				edit.requestFocus();
			}
		};
		asyncFocus.execute();
	}

	private History createHistory(View rootView, String item) {
		History hist = new History(rootView.getContext(), item);
		if (item.equals("online")) {
			hist.setAlternateText(R.string.comment_online);
		} else if (item.equals("offline")) {
			hist.setAlternateText(R.string.comment_offline);
		}
		return hist;
	}

	private FreehalImpl chooseFreehalImpl(String item) {

		final FreehalImpl impl;
		if (item.equals("online")) {
			if (onlineImpl == null)
				onlineImpl = new FreehalImplOnline(this.getResources());
			impl = onlineImpl;
			if (history != null)
				history.setAlternateText(R.string.comment_online);
		} else if (item.equals("offline")) {
			if (offlineImpl == null)
				offlineImpl = new FreehalImplOffline(this.getResources());
			impl = offlineImpl;
			if (history != null)
				history.setAlternateText(R.string.comment_offline);
		} else {
			impl = null;
		}
		return impl;
	}

	public View onCreateViewSettings(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState, final String item) {

		View rootView = inflater.inflate(R.layout.fragment_settings_detail,
				container, false);
		((TextView) rootView.findViewById(R.id.settings_detail))
				.setText("test");
		return rootView;
	}

	public View onCreateViewAbout(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState, final String item) {

		View rootView = inflater.inflate(R.layout.fragment_about_detail,
				container, false);

		final String htmlAbout = getResources().getString(R.string.about_text);
		final String appVersionName = Util.getVersion(this.getActivity()
				.getApplicationContext()).versionName;
		final String onlineVersionName = chooseFreehalImpl("online")
				.getVersionName();
		final String offlineVersionName = chooseFreehalImpl("offline")
				.getVersionName();

		((TextView) rootView.findViewById(R.id.about_detail)).setText(Html
				.fromHtml(String.format(htmlAbout, appVersionName,
						onlineVersionName, offlineVersionName)));

		return rootView;
	}

	public View onCreateViewLog(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState, final String item) {

		View rootView = inflater.inflate(R.layout.fragment_log_detail,
				container, false);
		// ((TextView)
		// rootView.findViewById(R.id.log_heading)).setText(R.string.tab_log);

		TextView view = (TextView) rootView.findViewById(R.id.log_detail);
		if (log == null || log.length() == 0)
			view.setText(Html.fromHtml(getResources()
					.getString(R.string.no_log)));
		else
			view.setText(log);
		view.setTypeface(Typeface.MONOSPACE);

		return rootView;
	}

	public View onCreateViewGraph(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState, final String item) {

		View rootView = inflater.inflate(R.layout.fragment_graph_detail,
				container, false);

		WebView view = (WebView) rootView.findViewById(R.id.graph_detail);
		if (graph == null || graph.length() < 1000)
			view.loadData(getResources().getString(R.string.no_graph),
					"text/html", null);
		else
			view.loadData(graph, "text/html", null);
		view.getSettings().setSupportZoom(true);
		view.getSettings().setBuiltInZoomControls(true);

		return rootView;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public static void setRecievedIntent(Intent intent) {
		recievedIntent = intent;
	}

	public static Intent getRecievedIntent() {
		Intent intent = recievedIntent;
		recievedIntent = null;
		return intent;
	}

	public static boolean hasRecievedIntent() {
		return recievedIntent == null ? false : true;
	}
}
