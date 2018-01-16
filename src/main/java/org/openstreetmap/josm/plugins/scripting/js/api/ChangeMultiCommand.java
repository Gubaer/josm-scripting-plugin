package org.openstreetmap.josm.plugins.scripting.js.api;

import static org.openstreetmap.josm.tools.I18n.trn;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.PrimitiveData;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

public class ChangeMultiCommand extends MultiCommand {

    private Change change;
    private PrimitiveData[] oldState;

    /**
     * Creates a command for adding a collection of objects to a layer.
     *
     * <ul>
     *   <li>null values are skipped</li>
     *   <li>changes duplicate objects only once</li>
     * </ul>
     *
     * @param layer the layer where the objects are added to. Must not be null.
     * @param toChange the collection of objects to add. Must not be null.
     * @param change the change to apply. Must not be null
     * @throws IllegalArgumentException thrown if one of the parameters is null
     */
    public ChangeMultiCommand(OsmDataLayer layer,
            @NotNull Collection<OsmPrimitive> toChange, @NotNull Change change){
        super(layer);
        Objects.requireNonNull(toChange);
        Objects.requireNonNull(change);
        List<OsmPrimitive> normalized = normalize(toChange);
        primitives = new OsmPrimitive[normalized.size()];
        normalized.toArray(primitives);
        this.change = change;
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified,
            Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
        // empty - we have our own undo implementation
    }

    @Override
    public String getDescriptionText() {
        return trn("Changed {0} primitive", "Changed {0} primitives",
                primitives.length, primitives.length);
    }

    @Override
    public boolean executeCommand() {
        DataSet ds = getAffectedDataSet();
        try {
            oldState = new PrimitiveData[primitives.length];
            ds.beginUpdate();
            for (int i=0; i< primitives.length; i++) {
                OsmPrimitive p = primitives[i];
                oldState[i] = p.save();
                change.apply(p);
                p.setModified(true);
            }
            return true;
        } finally {
            ds.endUpdate();
        }
    }

    @Override
    public void undoCommand() {
        DataSet ds = getAffectedDataSet();
        try {
            ds.beginUpdate();
            for (int i=primitives.length-1; i>=0; i--){
                OsmPrimitive p = primitives[i];
                p.load(oldState[i]);
            }
        } finally {
            ds.endUpdate();
        }
    }
}