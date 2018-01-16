/*
 * HelloWorld.groovy - displays the number of actually open layers
 */
import javax.swing.JOptionPane
import org.openstreetmap.josm.Main
import org.openstreetmap.josm.gui.MainApplication

def numlayers = MainApplication.getLayerManager().getLayers().size()
JOptionPane.showMessageDialog(Main.parent, "[Groovy] Hello World!\nYou have ${numlayers} layer(s).")
