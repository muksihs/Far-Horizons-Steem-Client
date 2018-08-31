package steem;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.fusesource.restygwt.client.JsonEncoderDecoder;
import org.fusesource.restygwt.client.JsonEncoderDecoder.DecodingException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

import elemental2.dom.DomGlobal;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import steem.model.accountinfo.AccountInfo;
import steem.model.discussion.Discussions;
import steem.model.discussion.Discussions.Discussion;
import steem.model.discussion.Discussions.JsonMetadata;

@JsType(namespace = "steem", name = "api", isNative=true)
public class SteemApi {
	public static native void getTrendingTags(String afterTag, int limit,
			SteemCallbackArray<TrendingTagsResult> callback);
	
	public static native void getAccounts(String[] username, SteemCallbackArray<AccountInfo> callback);
	
	@JsMethod(name="getContentReplies")
	private static native void _getContentReplies(String username, String permlink, SteemCallback<JavaScriptObject> discussion);
	@JsOverlay
	public static void getContentReplies(String username, String permlink, SteemCallback<Discussions> cb) {
		
		SteemCallback<JavaScriptObject> _cb=new SteemCallback<JavaScriptObject>() {
			@Override
			public void onResult(JavaScriptObject error, JavaScriptObject result) {
				if (error!=null || result==null) {
					cb.onResult(error, null);
					return;
				}
				String json = JsonUtils.stringify(result);
				json = "{\"discussions\":"+json+"}";
				Discussions discussions;
				try {
					discussions = Util.discussionsCodec.decode(json);
				} catch (DecodingException e) {
					DomGlobal.console.log("=== "+e.getMessage());
					GWT.log(e.getMessage(), e);
					cb.onResult(error, null);
					return;
				}
				cb.onResult(error, discussions);
			}
		};
		
		_getContentReplies(username, permlink, _cb);
	}
	
	@JsMethod(name="getContent")
	private static native void _getContent(String username, String permlink, SteemCallback<JavaScriptObject> cb);
	@JsOverlay
	public static void getContent(String username, String permlink, SteemCallback<Discussion> cb) {
		SteemCallback<JavaScriptObject> parseCb=new SteemCallback<JavaScriptObject>() {
			@Override
			public void onResult(JavaScriptObject error, JavaScriptObject result) {
				if (error!=null) {
					cb.onResult(error, null);
					return;
				}
				if (result==null) {
					DomGlobal.console.log("getDiscussionsByBlog: NULL RESPONSE.");
					return;
				}
				try {
					String stringify = JsonUtils.stringify(result);
					Discussion d = Util.discussionCodec.decode(stringify);
					if (d==null) {
						DomGlobal.console.log("Decoding FAIL: d == null!");
						cb.onResult(error, null);
						return;
					}
					cb.onResult(error, d);
				} catch (DecodingException e) {
					DomGlobal.console.log("DecodingException: "+e.getMessage());
				}				
			}
		};
		_getContent(username, permlink, parseCb);
	}
	
	private static native void getDiscussionsByBlog(JavaScriptObject query, SteemCallback<JavaScriptObject> cb);
	@JsOverlay
	public static void getDiscussionsByBlog(String username, int count, SteemCallback<Discussions> cb) {
		if (username==null) {
			username="";
		}
		username=username.replace("\"", "\\\"");
		JSONObject query = new JSONObject();
		query.put("tag", new JSONString(username));
		query.put("limit", new JSONNumber(count));
		SteemCallback<JavaScriptObject> parseCb=new SteemCallback<JavaScriptObject>() {
			@Override
			public void onResult(JavaScriptObject error, JavaScriptObject result) {
				if (error!=null) {
					cb.onResult(error, null);
					return;
				}
				if (result==null) {
					DomGlobal.console.log("getDiscussionsByBlog: NULL RESPONSE.");
					return;
				}
				try {
					String stringify = "{\"discussions\":"+JsonUtils.stringify(result)+"}";
					Discussions d = Util.discussionsCodec.decode(stringify);
					if (d==null) {
						DomGlobal.console.log("Decoding FAIL: d == null!");
						cb.onResult(error, null);
						return;
					}
					cb.onResult(error, d);
				} catch (DecodingException e) {
					DomGlobal.console.log("DecodingException: "+e.getMessage());
				}
			}
		};
		getDiscussionsByBlog(query.getJavaScriptObject(), parseCb);
	}
	public static class Util {
		public static interface DiscussionsCodec extends JsonEncoderDecoder<Discussions>{}
		public static DiscussionsCodec discussionsCodec = GWT.create(DiscussionsCodec.class);
		public static interface DiscussionCodec extends JsonEncoderDecoder<Discussion>{}
		public static DiscussionCodec discussionCodec = GWT.create(DiscussionCodec.class);
		public static interface JsonMetadataCodec extends JsonEncoderDecoder<JsonMetadata>{}
		public static JsonMetadataCodec jsonMetadataCodec = GWT.create(JsonMetadataCodec.class);
	}
}