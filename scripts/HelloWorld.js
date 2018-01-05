/*
* HelloWorld.js  -  displays the number of actually open layers
*/

var JOptionPane = javax.swing.JOptionPane;
var Main = org.openstreetmap.josm.Main;
var MainApplication = org.openstreetmap.josm.gui.MainApplication;

var numlayers = MainApplication.getLayerManager().getLayers().size();
JOptionPane.showMessageDialog(Main.parent, "[JavaScript] Hello World! You have " + numlayers + " layer(s).");
