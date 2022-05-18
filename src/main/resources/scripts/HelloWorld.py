#
# HelloWorld.py  - displays the number of actually open layers
#
from javax.swing import JOptionPane
from org.openstreetmap.josm.gui import MainApplication

num_layers = MainApplication.getLayerManager().getLayers().size()
JOptionPane.showMessageDialog(
    MainApplication.getMainFrame(),
    "[Python] Hello World! You have %s layer(s)." % num_layers)
