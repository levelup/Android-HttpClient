package co.tophe.parser;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;

import co.tophe.ImmutableHttpRequest;

/**
 * <p>A {@link XferTransform} to turn a {@code String} into a {@link org.json.JSONArray}</p>
 *
 * <p>Use the {@link #INSTANCE}</p>
 *
 * @see BodyToJSONArray
 * @author Created by robUx4 on 20/08/2014.
 */
public final class XferTransformStringJSONArray implements XferTransform<String, JSONArray> {
	/**
	 * The instance you should use when you want to get a {@link org.json.JSONArray} from an {@link java.io.InputStream}.
	 *
	 * @see co.tophe.BaseHttpRequest.Builder#setContentParser(XferTransform) BaseHttpRequest.Builder.setContentParser()
	 */
	public static final XferTransformStringJSONArray INSTANCE = new XferTransformStringJSONArray();

	private XferTransformStringJSONArray() {
	}

	@Override
	public JSONArray transformData(String srcData, ImmutableHttpRequest request) throws IOException, ParserException {
		try {
			return new JSONArray(srcData);
		} catch (JSONException e) {
			throw new ParserException("Bad JSON data", e, srcData);
		} catch (NullPointerException e) {
			throw new ParserException("Invalid JSON data", e, srcData);
		}
	}
}
