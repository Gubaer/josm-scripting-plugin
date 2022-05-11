package org.openstreetmap.josm.plugins.scripting.ui;

import org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor;
import org.openstreetmap.josm.tools.ImageProvider;

import javax.swing.*;
import java.awt.*;

import static org.openstreetmap.josm.plugins.scripting.model.ScriptEngineDescriptor.ScriptEngineType.GRAALVM;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Implements a list cell renderer for the list of scripting engines.
 */
public class ScriptEngineCellRenderer
    implements ListCellRenderer<ScriptEngineDescriptor> {

    static public final String DISPLAYED_GRAAL_ENGINE_NAME = "GraalVM";

//    static public String defaultEngineName(String name) {
//        return Optional.ofNullable(name)
//            .map(String::trim)
//            .filter(String::isEmpty)
//            .orElse(tr("unknown engine"));
//    }

    static public String defaultEngineName(ScriptEngineDescriptor desc) {
        if (desc.getEngineType() == GRAALVM) {
            return DISPLAYED_GRAAL_ENGINE_NAME;
        }
        return desc.getEngineName()
            .map(String::trim)
            .filter(s -> ! s.isEmpty())
            .orElse(tr("unknown"));
    }

    private final JLabel lbl = new JLabel();

    private String getDisplayName(ScriptEngineDescriptor descriptor){
        if (descriptor == null) return tr("Select an engine");
        final String engineName = defaultEngineName(descriptor);
        final String languageName = descriptor.getLanguageName()
                .orElse(tr("unknown"));
        // used in the context of a combo box
        return tr("{1} (with engine {0})", engineName, languageName);
    }

    private void addNameValuePairToToolTip(StringBuilder sb, String name,
                                             String value) {
        sb.append("<strong>").append(name).append("</strong> ")
          .append(value).append("<br>");
    }

    private String getTooltipText(ScriptEngineDescriptor descriptor) {
        if (descriptor == null) return "";
        final StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        if (descriptor.getEngineName().isPresent()) {
            addNameValuePairToToolTip(sb, tr("Name:"),
                defaultEngineName(descriptor));
        }
        if (descriptor.getEngineVersion().isPresent()) {
            addNameValuePairToToolTip(sb, tr("Version:"),
                descriptor.getEngineVersion().get());
        }
        if (descriptor.getLanguageName().isPresent()) {
            addNameValuePairToToolTip(sb, tr("Language:"),
                descriptor.getLanguageName().get());
        }
        if (descriptor.getLanguageVersion().isPresent()) {
            addNameValuePairToToolTip(sb, tr("Language version:"),
                descriptor.getLanguageVersion().get());
        }
        sb.append("<strong>").append(tr("MIME-Types:")).append("</strong> ");
        sb.append(String.join(", ", descriptor.getContentMimeTypes()));
        sb.append("<br>");
        sb.append("</html>");
        return sb.toString();
    }

    private void renderColors(boolean selected, boolean enabled){
        if (!selected || !enabled){
            lbl.setForeground(UIManager.getColor(enabled
                ? "List.foreground"
                : "Label.disabledForeground"));
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
            JList<? extends ScriptEngineDescriptor> list,
            ScriptEngineDescriptor descriptor, int index, boolean isSelected,
            boolean cellHasFocus) {
        boolean enabled = list.isEnabled();
        renderColors(isSelected, enabled);
        lbl.setText(getDisplayName(descriptor));
        lbl.setToolTipText(getTooltipText(descriptor));
        return lbl;
    }
}
