package org.openstreetmap.josm.plugins.scripting.js.api;

import static org.openstreetmap.josm.tools.I18n.trn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import org.openstreetmap.josm.command.AddPrimitivesCommand;
import org.openstreetmap.josm.command.PseudoCommand;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.DefaultNameFormatter;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.scripting.util.Assert;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * <p>A command to add a collection of primitives to a layer.</p>
 *
 * <p>This is an alternative for {@link AddPrimitivesCommand} which accepts
 * primitives instead of primitives data and adds the primitives in one
 * batch (between ds.beginUpdat() and ds.endUpdate()), and provides a
 * list of child command for the command dialog.</p>
 */
public class AddMultiCommand extends MultiCommand {

    /** remembers the added objects */
    private OsmPrimitive[] added;


    /**
     * Creates a command for adding a collection of objects to a layer.
     *
     * <ul>
     *   <li>null values are skipped</li>
     *   <li>adds duplicate objects only once</li>
     *   <li>orders the objects to add according to their type, nodes first, then ways, then relations</li>
     * </ul>
     *
     * @param layer the layer where the objects are added to
     * @param toAdd the collection of objects to add
     */
    public AddMultiCommand(OsmDataLayer layer, Collection<OsmPrimitive> toAdd){
        super(layer);
        Assert.assertArgNotNull(toAdd);
        List<OsmPrimitive> normalized = normalize(toAdd);
        added = new OsmPrimitive[normalized.size()];
        normalized.toArray(added);
    }

    @Override
    public boolean executeCommand() {
        DataSet ds = getLayer().data;
        int i=0;
        try {
            ds.beginUpdate();
            for (i=0; i< added.length; i++ ){
                ds.addPrimitive(added[i]);
                added[i].setModified(true);
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
            for (int i=added.length-1; i>=0; i--) {
                ds.removePrimitive(added[i]);
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
        return trn("Added {0} primitive", "Added {0} primitives", added.length, added.length);
    }

    @Override
    public Collection<PseudoCommand> getChildren() {
        List<PseudoCommand> children = new ArrayList<>();
        for (final OsmPrimitive p: added) {
            PseudoCommand cmd = new PseudoCommand() {
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
            };
            children.add(cmd);
        }
        return children;
    }
}
