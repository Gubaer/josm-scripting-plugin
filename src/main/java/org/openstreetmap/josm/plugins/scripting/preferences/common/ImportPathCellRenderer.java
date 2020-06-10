package org.openstreetmap.josm.plugins.scripting.preferences.common;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.logging.Logger;

import static org.openstreetmap.josm.plugins.scripting.preferences.common.IconUtil.saveImageGet;

public class ImportPathCellRenderer extends JLabel implements ListCellRenderer<File> {

    @SuppressWarnings("unused")
    static private final Logger logger =
        Logger.getLogger(ImportPathCellRenderer.class.getName());

    private Icon jarIcon;
    private Icon dirIcon;

    public ImportPathCellRenderer() {
        setOpaque(true);
        jarIcon = saveImageGet("jar");
        dirIcon = saveImageGet("directory");
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends File> list,
            File path,
    int index, boolean isSelected, boolean hasFocus) {
        setText(path.getAbsolutePath());
        if (isSelected) {
            setForeground(UIManager.getColor("List.selectionForeground"));
            setBackground(UIManager.getColor("List.selectionBackground"));
        } else {
            setForeground(UIManager.getColor("List.foreground"));
            setBackground(UIManager.getColor("List.background"));
        }
        if (path.isDirectory()) {
            setIcon(dirIcon);
        } else if (path.isFile() && path.getName().endsWith(".jar")) {
            setIcon(jarIcon);
        } else {
            setIcon(null);
        }
        return this;
    }
}
