/*
* HelloWorld.js  -  displays the number of actually open layers 
*/

var JOptionPane = javax.swing.JOptionPane;
var Main = org.openstreetmap.josm.Main;

function getMapView() {
	if (Main.main == null) return null;
	if (Main.map == null) return null;
	return Main.map.mapView;
}


var mv = getMapView();
var numlayers = mv == null ? 0 : mv.getNumLayers();
JOptionPane.showMessageDialog(Main.parent, "[JavaScript] Hello World! You have " + numlayers + " layer(s).");
