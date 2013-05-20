#
# A simple Python plugin which logs layer events to the console
#
from org.openstreetmap.josm.plugins.scripting.python import JosmPythonPlugin
from org.openstreetmap.josm.gui import MapView
#from org.openstreetmap.josm.gui.MapView import LayerChangeListener

def log(msg):
    """logs a messages to the console"""
    print "LayerEventPlugin: ", msg

class LayerEventListener(MapView.LayerChangeListener):
    def activeLayerChange(self, oldLayer, newLayer):
        name = newLayer.getName() if newLayer else "<None>"
        log("Changed active layer: new active layer is '" + name + "'")
    
    def layerAdded(self, newLayer):
        log("Added a new layer '" + newLayer.getName() + "'")
    
    def layerRemoved(self, oldLayer):
        log("Removed layer '" + (oldLayer.name if oldLayer else "None"))
        
class LayerEventPlugin(JosmPythonPlugin):
    def onLoad(self):        
        log("Starting ...")
        MapView.addLayerChangeListener(LayerEventListener())
        
    def onMapFrameChanged(self, oldFrame, newFrame):
        log("Changed map frame:")
        log("   old frame is: " + (oldFrame.toString() if oldFrame else "None"))
        log("   new frame is: "+ (newFrame.toString() if newFrame else "None")) 
