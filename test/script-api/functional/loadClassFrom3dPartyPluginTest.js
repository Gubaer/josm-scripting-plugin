
var cls = josm.loadClassFrom3dPartyPlugin(
   "contourmerge",
   "org.openstreetmap.josm.plugins.contourmerge.ContourMergePlugin"
);
var enabled = cls.isEnabled();

try {
    var cls = josm.loadClassFrom3dPartyPlugin(
       "no-such-plugin",
       "org.openstreetmap.josm.plugins.contourmerge.ContourMergePlugin"
    );
    throw new Error("expected an exception");
} catch(e) {
    if (!e.toString().includes("PluginNotFoundException")) {
        throw e;
    }
}

try {
    var cls = josm.loadClassFrom3dPartyPlugin(
       "contourmerge",
       "org.openstreetmap.josm.plugins.contourmerge.NoSuchClass"
    );
    throw new Error("expected an exception");
} catch(e) {
    if (!e.toString().includes("ClassNotFoundException")) {
        throw e;
    }
}
