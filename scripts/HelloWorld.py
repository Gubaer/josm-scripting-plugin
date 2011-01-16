#
# HelloWorld.py  - displays the number of actually open layers
# 
from javax.swing import JOptionPane
from org.openstreetmap.josm import Main

def getMapView():
	if Main.main == None:
		return None
	if Main.main.map == None:
		return None
	return Main.main.map.mapView


numlayers = 0
mv = getMapView()
if mv != None:
	numlayers = mv.getNumLayers()
	
JOptionPane.showMessageDialog(Main.parent, "[Python] Hello World! You have %s layer(s)." % numlayers)
