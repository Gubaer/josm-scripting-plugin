/*
* HelloWorld.js  -  displays the number of actually open layers 
*/

var JOptionPane = javax.swing.JOptionPane;
var Main = org.openstreetmap.josm.Main;

var numlayers = Main.getLayerManager().getLayers().size();
JOptionPane.showMessageDialog(Main.parent, "[JavaScript] Hello World! You have " + numlayers + " layer(s).");
