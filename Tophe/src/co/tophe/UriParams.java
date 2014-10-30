package co.tophe;

import java.util.ArrayList;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Pair;

public class UriParams implements HttpUriParameters {
	private final ArrayList<Pair<String,String>> mParams;

	public UriParams(int capacity) {
		this.mParams = new ArrayList<Pair<String,String>>(capacity);
	}

	public UriParams() {
		this.mParams = new ArrayList<Pair<String,String>>();
	}

	public UriParams(Uri fromUri) {
		this.mParams = new ArrayList<Pair<String,String>>();
		String query = fromUri.getEncodedQuery();
		if (query != null) {
			int start = 0;
			do {
				int next = query.indexOf('&', start);
				int end = (next == -1) ? query.length() : next;

				int separator = query.indexOf('=', start);
				if (separator > end || separator == -1) {
					separator = end;
				}

				final String value;
				if (separator == end) {
					value = "";
				} else {
					String encodedValue = query.substring(separator + 1, end);
					value = Uri.decode(encodedValue);
				}

				String name = query.substring(start, separator);
				add(Uri.decode(name), value);

				// Move start to end of name.
				start = end + 1;
			} while (start < query.length());
		}
	}

	@Override
	public void add(@NonNull String name, String value) {
		mParams.add(new Pair<String,String>(name, value));
	}

	@Override
	public void add(@NonNull String name, boolean b) {
		add(name, String.valueOf(b));
	}

	@Override
	public void add(@NonNull String name, int i) {
		add(name, String.valueOf(i));
	}

	@Override
	public void add(@NonNull String name, long l) {
		add(name, String.valueOf(l));
	}

	@Override
	public void addUriParameters(Uri.Builder uriBuilder) {
		for (Pair<String,String> param : mParams) {
			uriBuilder.appendQueryParameter(param.first, param.second);
		}
	}
}
