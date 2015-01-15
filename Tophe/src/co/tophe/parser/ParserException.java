package co.tophe.parser;

import android.support.annotation.Nullable;

/**
 * Runtime Exception that occurs when data parsing with {@link co.tophe.HttpResponse} fails.
 */
public class ParserException extends Exception {
	private static final long serialVersionUID = 3213822444086259097L;
	private final String sourceData;

	public ParserException(String detailMessage, Exception cause, String sourceData) {
		super(getDetailMessage(detailMessage, sourceData), cause);
		this.sourceData = sourceData;
	}

	/**
	 * Get the source data that created the parsing error
	 * @return {@code null} if the data was not available
	 */
	@Nullable
	public String getSourceData() {
		return sourceData;
	}

	private static String getDetailMessage(String detailMessage, String sourceData) {
		StringBuilder sb = new StringBuilder();
		if (null!=detailMessage)
			sb.append(detailMessage);
		if (null!=sourceData) {
			sb.append(" data'");
			sb.append(sourceData);
			sb.append('\'');
		}
		return sb.toString();
	}
}