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
        name = newLayer.getName() if newLayer != None else "<none>"
        log("active layer change: new active layer is '" + name + "'")
    
    def layerAdded(self, newLayer):
        log("added a new layer '" + newLayer.getName() + "'")
    
    def layerRemoved(self, oldLayer):
        if oldLayer == None:
            log("removed layer 'None'")
        else:
            log("removed layer '" + oldLayer.getName() + "'")
    

class LayerEventPlugin(JosmPythonPlugin):
    def onLoad(self):        
        log("starting ...")
        #
        # register our layer event listener
        #
        MapView.addLayerChangeListener(LayerEventListener())
        
    def onMapFrameChanged(self, oldFrame, newFrame):
        log("   old frame is: " + oldFrame)
        log("   new frame is: " +  newFrame) 
        
        
        
        
    