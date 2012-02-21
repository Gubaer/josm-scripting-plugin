'''
RWN2Garmin.py  - Numbered networks to Garmin GPX file converter
This code is released under the GNU General
Public License v2 or later.

The GPL v3 is accessible here:
http://www.gnu.org/licenses/gpl.html

The GPL v2 is accessible here:
http://www.gnu.org/licenses/old-licenses/gpl-2.0.html

It comes with no warranty whatsoever.

This code illustrates how to use Jython to:
* work with selected items or how to process all the primitives of a certain kind (node, way, relation)

'''
from javax.swing import JOptionPane, JDialog
from java.awt.event import ActionListener, ActionEvent
from org.openstreetmap.josm import Main
import org.openstreetmap.josm.command as Command
import org.openstreetmap.josm.data.osm.Node as Node
import org.openstreetmap.josm.data.osm.Way as Way
import org.openstreetmap.josm.data.osm.TagCollection as TagCollection
import org.openstreetmap.josm.data.osm.DataSet as DataSet
import time

def getMapView():
    if Main.main and Main.main.map:
        return Main.main.map.mapView
    else:
        return None

mv = getMapView()
f = open('C:/export.gpx', 'w')
f.write('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n')
f.write('<gpx xmlns="http://www.topografix.com/GPX/1/1" creator="OSM Route Manager" version="1.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd">\n')
f.write('<!-- All data by OpenStreetMap, licensed under cc-by-sa-2.0 (http://creativecommons.org/licenses/by-sa/2.0/). -->\n')
if mv and mv.editLayer and mv.editLayer.data:
    #selectedNodes = mv.editLayer.data.getSelectedNodes()
    #selectedWays = mv.editLayer.data.getSelectedWays()
    selectedRelations = mv.editLayer.data.getSelectedRelations()

    if not(selectedRelations):
        JOptionPane.showMessageDialog(Main.parent, "Please select a collection relation")
    else:
        # nodetype = Node().getType()
        print
        for collection in selectedRelations:
            print 'COLLECTION:', collection
            for member in collection.getMembers():
                print 'MEMBER:',member
                if member.isNode():
                    node = member.getNode()
                    coords = node.getCoor()
                    lon = coords.getX()
                    lat = coords.getY()
                    rwn_ref = node.get('rwn_ref')
                    f.write('\t<wpt lat="' + str(lat) + '" lon="' + str(lon) + '">\n')
                    if rwn_ref:
                        f.write('\t\t<name>' + rwn_ref + '</name>\n')
                    f.write('\t</wpt>\n')
            for member in collection.getMembers():
                if member.isRelation():
                    routerelation = member.getRelation()
                    f.write('\t<trk>\n')
                    networkname =  routerelation.get('network:name')
                    if not(networkname):
                        networkname =  ''
                    else:
                        networkname += ' '
                    note = routerelation.get('note')
                    if not(note): note =  ''
                    f.write('\t\t<name>' + networkname + note + '</name>\n')
                    f.write('\t\t<src>OpenStreetMap.org</src>\n')
                    f.write('\t\t<type>foot</type>\n')
                    for routerelmember in routerelation.getMembers():
                        if routerelmember.isWay():
                            f.write('\t\t<trkseg>\n')
                            way=routerelmember.getWay()
                            for waynode in way.getNodes():
                                 coords = waynode.getCoor()
                                 lon = coords.getX()
                                 lat = coords.getY()
                                 f.write('\t\t\t<trkpt lat="' + str(lat) + '" lon="' + str(lon) + '"> </trkpt>\n')

                            f.write('\t\t</trkseg>\n')
                    f.write('\t</trk>\n')
f.write('</gpx>\n')
f.close()