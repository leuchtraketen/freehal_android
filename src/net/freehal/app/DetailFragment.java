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
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

@SuppressLint("ValidFragment")
public class DetailFragment extends SherlockFragment {

	public static String ARG_ITEM_ID = "item_id";

	private String tab;
	private Activity activity;
	private History history;

	private static FreehalImpl onlineImpl;
	private static FreehalImpl offlineImpl;
	private static String log;
	private static String graph;

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
		Log.e("forTab", "id=" + id);
		id = SelectContent.validateId(id);
		Log.e("forTab", "(id=" + id + ")");
		if (!tabs.containsKey(id)) {
			Log.e("forTab", "not cached.");
			DetailFragment instance = new DetailFragment();
			instance.setTab(id);
			Bundle arguments = new Bundle();
			arguments.putString(DetailFragment.ARG_ITEM_ID, id);
			instance.setActivity(activity);
			instance.setArguments(arguments);
			tabs.put(id, instance);
		}
		Log.e("forTab", "(tabs.get(id).id=" + tabs.get(id).getTab() + ")");
		return tabs.get(id);
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

	public void gotInput(final EditText edit, final TextView text,
			final ScrollView scrollView, final FreehalImpl impl) {

		if (history == null)
			return;

		final String input = Util.toAscii(edit.getText().toString());

		if (input.length() > 0) {
			history.addInput(input);
			history.writeTo(text, scrollView);
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
					history.addOutput(output);
					// if (frag.isAdded() && frag.isVisible()) {
					history.writeTo(text, scrollView);
					SpeechHelper.getInstance().say(output, activity);
					showKeyboard(edit, scrollView, 1000);
				}

			};
			async.execute();
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

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

		final View rootView = inflater.inflate(R.layout.fragment_detail,
				container, false);
		final TextView text = (TextView) rootView.findViewById(R.id.detail);
		final EditText edit = (EditText) rootView
				.findViewById(R.id.edit_message);
		final ScrollView scrollView = (ScrollView) rootView
				.findViewById(R.id.scrollview);
		final Button sendButton = (Button) rootView
				.findViewById(R.id.button_send);

		history = createHistory(rootView, item);
		final FreehalImpl impl = chooseFreehalImpl(item);

		edit.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					gotInput(edit, text, scrollView, impl);
					return true;
				} else {
					scrollView.smoothScrollTo(0, edit.getBottom());
					return false;
				}
			}
		});
		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				gotInput(edit, text, scrollView, impl);
			}
		});

		history.writeTo(text, scrollView);
		showKeyboard(edit, scrollView, 1000);

		return rootView;
	}

	private void showKeyboard(final EditText edit, final ScrollView scrollView,
			final int timeToSleep) {
		if (edit.getOnFocusChangeListener() == null)
			edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus && activity != null) {
						InputMethodManager inputStatus = (InputMethodManager) activity
								.getSystemService(Context.INPUT_METHOD_SERVICE);
						inputStatus.showSoftInput(edit,
								InputMethodManager.SHOW_IMPLICIT);
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
			history.setAlternateText(R.string.comment_online);
		} else if (item.equals("offline")) {
			if (offlineImpl == null)
				offlineImpl = new FreehalImplOffline(this.getResources());
			impl = offlineImpl;
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
		((TextView) rootView.findViewById(R.id.about_detail)).setText(Html
				.fromHtml(getResources().getString(R.string.about_text)));
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
}
