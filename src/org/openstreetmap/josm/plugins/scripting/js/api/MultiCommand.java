package org.openstreetmap.josm.plugins.scripting.js.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.scripting.util.Assert;

abstract public class MultiCommand extends Command{

    protected static OsmDataLayer checkLayer(OsmDataLayer layer) {
        Assert.assertArgNotNull(layer);
        return layer;
    }

    public MultiCommand(OsmDataLayer layer) {
        super(checkLayer(layer));
    }

    protected List<OsmPrimitive> normalize(Collection<OsmPrimitive> toAdd) {
        List<OsmPrimitive> ret = new ArrayList<>(toAdd);
        ret.remove(null);
        Collections.sort(ret,
                (OsmPrimitive o1, OsmPrimitive o2) -> {
                int c = o1.getType().compareTo(o2.getType());
                if (c != 0) return c;
                long i = o1.getId(); long j = o2.getId();
                if (i == j) return 0;
                return i < j ? -1 : +1;
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

}
