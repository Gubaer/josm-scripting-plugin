#
# HelloWorld.py  - displays the number of actually open layers
# 
from javax.swing import JOptionPane
from org.openstreetmap.josm import Main

numlayers = Main.getLayerManager().getLayers().size()	
JOptionPane.showMessageDialog(Main.parent, "[Python] Hello World! You have %s layer(s)." % numlayers)
