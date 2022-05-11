package org.openstreetmap.josm.plugins.scripting.js.api;

import static org.openstreetmap.josm.tools.I18n.trn;

import java.util.*;

import javax.validation.constraints.NotNull;

import org.openstreetmap.josm.command.AddPrimitivesCommand;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

/**
 * <p>A command to add a collection of primitives to a layer.</p>
 *
 * <p>This is an alternative for {@link AddPrimitivesCommand} which accepts
 * primitives instead of primitives data and adds the primitives in one
 * batch (between ds.beginUpdat() and ds.endUpdate()), and provides a
 * list of child command for the command dialog.</p>
 */
public class AddMultiCommand extends MultiCommand {

    /**
     * Creates a command for adding a collection of objects to a layer.
     *
     * <ul>
     *   <li>null values are skipped</li>
     *   <li>adds duplicate objects only once</li>
     *   <li>orders the objects to add according to their type, nodes first,
     *   then ways, then relations</li>
     * </ul>
     *
     * @param layer the layer where the objects are added to
     * @param toAdd the collection of objects to add
     */
    public AddMultiCommand(OsmDataLayer layer,
            @NotNull Collection<OsmPrimitive> toAdd){
        super(layer);
        Objects.requireNonNull(toAdd);
        List<OsmPrimitive> normalized = normalize(toAdd);
        primitives = new OsmPrimitive[normalized.size()];
        normalized.toArray(primitives);
        // make sure nodes will be added before ways, nodes and ways before relations
        Arrays.sort(primitives,
                Comparator.comparing(p -> p.getPrimitiveId().getType())
        );
    }

    @Override
    public boolean executeCommand() {
        final DataSet ds = getAffectedDataSet();
        try {
            ds.beginUpdate();
            Arrays.stream(primitives)
                .filter(p -> ds.getPrimitiveById(p) == null)
                .forEach(p -> {
                    ds.addPrimitive(p);
                    p.setModified(true);
                });
        } finally {
            ds.endUpdate();
        }
        return true;
    }

    @Override
    public void undoCommand() {
        DataSet ds = getAffectedDataSet();
        try {
            ds.beginUpdate();
            for (int i=primitives.length-1; i>=0; i--) {
                ds.removePrimitive(primitives[i]);
            }
        } finally {
            ds.endUpdate();
        }
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified,
         Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
        // empty - we have our own undo implementation
    }

    @Override
    public String getDescriptionText() {
        return trn("Added {0} primitive", "Added {0} primitives",
                primitives.length, primitives.length);
    }
}
