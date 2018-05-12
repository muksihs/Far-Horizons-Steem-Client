package steem;

import com.google.gwt.core.client.JavaScriptObject;

import jsinterop.annotations.JsFunction;

@JsFunction
public interface SteemCallback<T> {
	void onResult(JavaScriptObject error, T result);
}