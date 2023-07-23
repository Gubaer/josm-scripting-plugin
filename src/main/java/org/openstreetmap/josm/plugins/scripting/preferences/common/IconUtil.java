package org.openstreetmap.josm.plugins.scripting.preferences.common;

import org.openstreetmap.josm.tools.ImageProvider;

import javax.swing.*;

import java.util.logging.Logger;

import static org.openstreetmap.josm.tools.I18n.tr;

public class IconUtil {

    @SuppressWarnings("unused")
    static private final Logger logger = Logger.getLogger(IconUtil.class.getName());

    static public Icon saveImageGet(String name) {
        ImageIcon icon = ImageProvider.getIfAvailable(name);
        if (icon == null) {
            logger.warning(tr("Failed to load icon ''{0}''", name));
        }
        return icon;
    }

    static public Icon saveImageGet(String dir, String name) {
        ImageIcon icon = ImageProvider.getIfAvailable(dir, name);
        if (icon == null) {
            logger.warning(tr("Failed to load icon ''{0}/{1}''", dir, name));
        }
        return icon;
    }
}
