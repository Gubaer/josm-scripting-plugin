package org.openstreetmap.josm.plugins.scripting.js.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.PseudoCommand;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

abstract public class MultiCommand extends Command{

    protected OsmPrimitive[] primitives;

    protected static DataSet getDataSet(@NotNull OsmDataLayer layer) {
        Objects.requireNonNull(layer);
        return layer.data;
    }

    public MultiCommand(@NotNull OsmDataLayer layer) {
        super(getDataSet(layer));
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
