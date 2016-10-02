package org.openstreetmap.josm.plugins.scripting.js.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.PseudoCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.scripting.util.Assert;

abstract public class MultiCommand extends Command{

    protected OsmPrimitive[] primitives;

    protected static OsmDataLayer checkLayer(OsmDataLayer layer) {
        Assert.assertArgNotNull(layer);
        return layer;
    }

    public MultiCommand(OsmDataLayer layer) {
        super(checkLayer(layer));
    }

    protected List<OsmPrimitive> normalize(Collection<OsmPrimitive> toAdd) {
        return toAdd.stream()
            .filter(p -> p != null)
            .distinct()
            .collect(Collectors.toList());
    }

    @Override
    public Collection<PseudoCommand> getChildren() {
        return Arrays.stream(primitives)
            .map(CommandForPrimitive::new)
            .collect(Collectors.toList());
    }

}
