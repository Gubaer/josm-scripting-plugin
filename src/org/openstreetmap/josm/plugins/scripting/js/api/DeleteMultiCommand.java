package org.openstreetmap.josm.plugins.scripting.js.api;

import static org.openstreetmap.josm.tools.I18n.trn;

import java.util.Collection;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

public class DeleteMultiCommand extends MultiCommand {

    boolean[] oldstate;

    /**
     * Creates a command for deleting a collection of objects in a data layer.
     *
     * <ul>
     *   <li>null values are skipped</li>
     *   <li>removes duplicate objects only once</li>
     *   <li>orders the objects to remove according to their type, relations
     *   first, then ways, then nodes</li>
     * </ul>
     *
     * @param layer the layer where the objects are added to
     * @param toAdd the collection of objects to add
     */
    public DeleteMultiCommand(OsmDataLayer layer,
            @NotNull Collection<OsmPrimitive> toDelete) {
        super(layer);
        Objects.requireNonNull(toDelete);
        toDelete = normalize(toDelete);
        primitives  = new OsmPrimitive[toDelete.size()];
        toDelete.toArray(primitives);
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified,
            Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
        // empty - we have our own undo implementation
    }

    @Override
    public boolean executeCommand() {
        DataSet ds = getAffectedDataSet();
        try {
            ds.beginUpdate();
            oldstate = new boolean[primitives.length];
            for (int i=0; i<primitives.length; i++) {
                oldstate[i] = primitives[i].isDeleted();
                primitives[i].setDeleted(true);
            }
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
            for (int i=0; i<primitives.length; i++) {
                primitives[i].setDeleted(oldstate[i]);
            }
        } finally {
            ds.endUpdate();
        }
    }

    @Override
    public String getDescriptionText() {
        return trn("Deleted {0} primitive",
                "Deleted {0} primitives", primitives.length);
    }
}
