
from org.openstreetmap.josm.plugins.scripting.python import JosmPythonPlugin

class TestPlugin1(JosmPythonPlugin):
    def onLoad(self):
        print "onLoad: TestPlugin1 starting ..."
        
    def onMapFrameChanged(self, oldFrame, newFrame):
        print "onMapFrameChanged: TestPlugin1 starting ..."
        
        
    