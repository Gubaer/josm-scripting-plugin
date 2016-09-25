package org.openstreetmap.josm.plugins.scripting.js.api;

import static org.openstreetmap.josm.tools.I18n.trn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.scripting.util.Assert;

public class DeleteMultiCommand extends Command {

    OsmPrimitive[] deleted;
    boolean[] oldstate;

    protected List<OsmPrimitive> normalize(Collection<OsmPrimitive> toAdd) {
        List<OsmPrimitive> ret = new ArrayList<>(toAdd);
        ret.remove(null);
        Collections.sort(ret,(OsmPrimitive o1, OsmPrimitive o2) -> {
                // relations -> ways -> nodes
            int c = - o1.getType().compareTo(o2.getType());
            if (c != 0) return c;
            long i = o1.getId(); long j = o2.getId();
            if (i == j) return 0;
            return i < j ? -1 : 1;
        });
        OsmPrimitive last = null;
        for(Iterator<OsmPrimitive> it = ret.iterator(); it.hasNext(); ) {
            OsmPrimitive cur = it.next();
            if (last == null) {last=cur; continue;}
            if (last.equals(cur)) it.remove();
            last =cur;
        }
        return ret;
    }

    protected static OsmDataLayer checkLayer(OsmDataLayer layer) {
        Assert.assertArgNotNull(layer);
        return layer;
    }

    /**
     * Creates a command for deleting a collection of objects in a data layer.
     *
     * <ul>
     *   <li>null values are skipped</li>
     *   <li>removes duplicate objects only once</li>
     *   <li>orders the objects to remove according to their type, relations first, then ways, then nodes</li>
     * </ul>
     *
     * @param layer the layer where the objects are added to
     * @param toAdd the collection of objects to add
     */
    public DeleteMultiCommand(OsmDataLayer layer, Collection<OsmPrimitive> toDelete) {
        super(checkLayer(layer));
        Assert.assertArgNotNull(toDelete);
        toDelete = normalize(toDelete);
        deleted  = new OsmPrimitive[toDelete.size()];
        toDelete.toArray(deleted);
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified,
            Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
        // empty - we have our own undo implementation
    }

    @Override
    public boolean executeCommand() {
        DataSet ds = getLayer().data;
        try {
            ds.beginUpdate();
            oldstate = new boolean[deleted.length];
            for (int i=0; i<deleted.length; i++) {
                oldstate[i] = deleted[i].isDeleted();
                deleted[i].setDeleted(true);
            }
        } finally {
            ds.endUpdate();
        }
        return true;
    }

    @Override
    public void undoCommand() {
        DataSet ds = getLayer().data;
        try {
            ds.beginUpdate();
            for (int i=0; i<deleted.length; i++) {
                deleted[i].setDeleted(oldstate[i]);
            }
        } finally {
            ds.endUpdate();
        }
    }

    @Override
    public String getDescriptionText() {
        return trn("Deleted {0} primitive", "Deleted {0} primitives", deleted.length);
    }
}
