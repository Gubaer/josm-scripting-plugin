/*
* HelloWorld.js  -  displays the number of actually open layers
*/

var JOptionPane = javax.swing.JOptionPane;
var MainApplication = org.openstreetmap.josm.gui.MainApplication;

var numLayers = MainApplication.getLayerManager().getLayers().size();
JOptionPane.showMessageDialog(
    MainApplication.getMainFrame(),
    "[JavaScript] Hello World! You have " + numLayers + " layer(s)."
);
