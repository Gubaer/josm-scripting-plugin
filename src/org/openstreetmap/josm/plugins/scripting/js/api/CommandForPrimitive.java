package org.openstreetmap.josm.plugins.scripting.js.api;

import java.util.Collection;
import java.util.Collections;

import javax.swing.Icon;

import org.openstreetmap.josm.command.PseudoCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.DefaultNameFormatter;
import org.openstreetmap.josm.tools.ImageProvider;

class CommandForPrimitive extends PseudoCommand {

    private final OsmPrimitive p;
    public CommandForPrimitive(final OsmPrimitive p) {
        this.p = p;
    }
    @Override
    public Collection<? extends OsmPrimitive> getParticipatingPrimitives() {
        return Collections.singleton(p);
    }

    @Override
    public String getDescriptionText() {
        return p.getDisplayName(DefaultNameFormatter.getInstance());
    }

    @Override
    public Icon getDescriptionIcon() {
        return ImageProvider.get(p.getType());
    }
}
