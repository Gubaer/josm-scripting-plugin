package org.openstreetmap.josm.plugins.scripting.ui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.util.List;

import javax.script.ScriptEngineFactory;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.openstreetmap.josm.tools.ImageProvider;

/**
 * <p>Implements a list cell renderer for the list of scripting engines.</p>
 *
 */
public class ScriptEngineCellRenderer implements ListCellRenderer<ScriptEngineFactory> {

	private final JLabel lbl = new JLabel();
	
	protected String getDisplayName(ScriptEngineFactory factory){
		if (factory == null) return tr("Select an engine"); // used in the context of a combo box
		return tr("{1} (with engine {0})", factory.getEngineName(), factory.getLanguageName());
	}
	
	protected String getTooltipText(ScriptEngineFactory factory){
		if (factory == null) return "";
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<strong>").append(tr("Name:")).append("</strong> ").append(factory.getEngineName()).append("<br>");
		sb.append("<strong>").append(tr("Version:")).append("</strong> ").append(factory.getEngineVersion()).append("<br>");
		sb.append("<strong>").append(tr("Language:")).append("</strong> ").append(factory.getLanguageName()).append("<br>");
		sb.append("<strong>").append(tr("Language version:")).append("</strong> ").append(factory.getLanguageVersion()).append("<br>");
		sb.append("<strong>").append(tr("MIME-Types:")).append("</strong> ");
		List<String> types = factory.getMimeTypes();
		for(int i=0; i<types.size(); i++){
			if (i > 0 )sb.append(", ");
			sb.append(types.get(i));
		}
		sb.append("<br>");
		sb.append("</html>");
		
		return sb.toString();
	}
	
	protected void renderColors(boolean selected, boolean enabled){
		if (!selected || !enabled){
			lbl.setForeground(UIManager.getColor(enabled ? "List.foreground" : "Label.disabledForeground"));
			lbl.setBackground(UIManager.getColor("List.background"));
		} else {
			lbl.setForeground(UIManager.getColor("List.selectionForeground"));
			lbl.setBackground(UIManager.getColor("List.selectionBackground"));
		}
	}
	
	public ScriptEngineCellRenderer() {		
		lbl.setOpaque(true);
		lbl.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
		lbl.setIcon(ImageProvider.get("script-engine"));
	}

	@Override
	public Component getListCellRendererComponent(
			JList<? extends ScriptEngineFactory> list,
			ScriptEngineFactory factory, int index, boolean isSelected,
			boolean cellHasFocus) {
		boolean enabled = list.isEnabled();
		renderColors(isSelected, enabled);
		lbl.setText(getDisplayName(factory));
		lbl.setToolTipText(getTooltipText(factory));
		return lbl;
	}
}
