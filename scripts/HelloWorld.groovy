/*
 * HelloWorld.groovy - displays the number of actually open layers 
 */
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;

def numlayers = Main.main?.map?.mapView?.numLayers
if (numlayers == null) numlayers = 0

JOptionPane.showMessageDialog(Main.parent, "[Groovy] Hello World!\nYou have ${numlayers} layer(s).")
