package ts.eclipse.ide.server.nodejs.internal.ui.console;

import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ts.client.installtypes.BeginInstallTypesEventBody;
import ts.client.installtypes.EndInstallTypesEventBody;
import ts.client.installtypes.IInstallTypesListener;
import ts.eclipse.ide.ui.TypeScriptUIPlugin;
import ts.eclipse.ide.ui.console.ITypeScriptConsole;
import ts.eclipse.ide.ui.console.LineType;

public class InstallTypesConsoleListener implements IInstallTypesListener {

	public static final IInstallTypesListener INSTANCE = new InstallTypesConsoleListener();

	@Override
	public void onBegin(BeginInstallTypesEventBody body) {
		getConsole().doAppendLine(LineType.DATA, "Begin installing @types...");
	}

	@Override
	public void onEnd(EndInstallTypesEventBody body) {
		getConsole().doAppendLine(LineType.DATA, "End installing @types.");
	}

	public void logTelemetry(String telemetryEventName, JsonObject payload) {
		Set<Entry<String, JsonElement>> properties = payload.entrySet();
		ITypeScriptConsole console = getConsole();
		for (Entry<String, JsonElement> entry : properties) {
			console.doAppendLine(LineType.DATA, entry.getKey() + ": " + entry.getValue());
		}
	}

	private ITypeScriptConsole getConsole() {
		if (TypeScriptUIPlugin.getDefault() != null) {
			return TypeScriptUIPlugin.getDefault().getInstallTypesConsole();
		}
		return null;
	}

}
