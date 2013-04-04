
#
# A simple Python plugin which does basically nothing. It only prints
# a message when one of the callback methods is invoked by JOSM.
#
from org.openstreetmap.josm.plugins.scripting.python import JosmPythonPlugin

class HelloWorldPlugin(JosmPythonPlugin):
    def onLoad(self):        
        print "onLoad: starting ..."
        
    def onMapFrameChanged(self, oldFrame, newFrame):
        print "onMapFrameChanged:"
        print "   old frame is: ", oldFrame
        print "   new frame is: ", newFrame 
        
        
        
    