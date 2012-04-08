/*
* HelloWorld.js  -  displays the number of actually open layers 
*/
var Swing = new JavaImporter(
	Packages.javax.swing.JOptionPane
);

var josm = new JavaImporter(
	Packages.org.openstreetmap.josm.Main
);

function getMapView() {
	if (josm.Main.main == null) return null;
	if (josm.Main.map == null) return null;
	return josm.Main.map.mapView;
}


var mv = getMapView();
var numlayers = mv == null ? 0 : mv.getNumLayers();
Swing.JOptionPane.showMessageDialog(josm.Main.parent, "[JavaScript] Hello World! You have " + numlayers + " layer(s).");
