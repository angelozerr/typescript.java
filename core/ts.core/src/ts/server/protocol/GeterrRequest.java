package ts.server.protocol;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import ts.server.geterr.ITypeScriptGeterrCollector;

/**
 * Geterr request; value of command field is "geterr". Wait for delay
 * milliseconds and then, if during the wait no change or reload messages have
 * arrived for the first file in the files list, get the syntactic errors for
 * the file, field requests, and then get the semantic errors for the file.
 * Repeat with a smaller delay for each subsequent file on the files list. Best
 * practice for an editor is to send a file list containing each file that is
 * currently visible, in most-recently-used order.
 */
public class GeterrRequest extends Request {

	private final static int EVENT_INIT = 0;
	private final static int EVENT_SYNTAX_DIAG = 4;
	private final static int EVENT_SEMANTIC_DIAG = 16;
	private final static int EVENT_FINAL = 20;

	private final ITypeScriptGeterrCollector collector;
	private final Map<String, Integer> files;
	private int delay;

	public GeterrRequest(String[] files, int delay, ITypeScriptGeterrCollector collector) {
		super(CommandNames.Geterr, new GeterrRequestArgs(files, delay), null);
		this.files = createFilesMap(files);
		this.delay = delay;
		this.collector = collector;
	}

	private Map<String, Integer> createFilesMap(String[] files) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (int i = 0; i < files.length; i++) {
			map.put(files[i], EVENT_INIT);
		}
		return map;
	}

	public Collection<String> getFiles() {
		return files.keySet();
	}

	public int getDelay() {
		return delay;
	}

	public boolean handleResponse(String event, String file, JsonArray diagnostics) {
		JsonObject diagnostic = null;
		String text = null;
		JsonObject start = null;
		JsonObject end = null;
		for (JsonValue value : diagnostics) {
			diagnostic = value.asObject();
			text = diagnostic.getString("text", null);
			start = diagnostic.get("start").asObject();
			end = diagnostic.get("end").asObject();
			collector.addDiagnostic(event, file, text, start.getInt("line", -1), start.getInt("offset", -1),
					end.getInt("line", -1), end.getInt("offset", -1));
		}
		Integer mask = files.get(file);
		mask = mask.intValue() | ("syntaxDiag".equals(event) ? EVENT_SYNTAX_DIAG : EVENT_SEMANTIC_DIAG);
		if (mask == EVENT_FINAL) {
			dispose(file);
			return true;
		} else {
			synchronized (files) {
				files.put(file, mask);
			}
			return false;
		}
	}

	public void dispose(String file) {
		synchronized (files) {
			files.remove(file);
		}
		synchronized (this) {
			this.notifyAll();
		}
	}

	@Override
	public void complete(JsonObject response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean isCompleted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected Object getResult() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public JsonObject call() throws Exception {
//		while (!files.isEmpty()) {
//			synchronized (this) {
//				// wait for 200ms otherwise if we don't set ms, if completion is
//				// executed several times
//				// quickly (do Ctrl+Space every time), the Thread could be
//				// blocked? Why?
//				this.wait(5);
//			}
//		}
//		return null;
//	}

}
