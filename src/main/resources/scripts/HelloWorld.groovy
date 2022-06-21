/*
 * HelloWorld.groovy - displays the number of open layers
 */

import javax.swing.JOptionPane
import org.openstreetmap.josm.gui.MainApplication

def numLayers = MainApplication.getLayerManager().getLayers().size()
JOptionPane.showMessageDialog(
    MainApplication.getMainFrame(),
    "[Groovy] Hello World! You have $numLayers layer(s)."
)
