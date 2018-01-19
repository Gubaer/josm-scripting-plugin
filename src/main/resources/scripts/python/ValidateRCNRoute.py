'''
- Validation of a rcn route relation

This code is released under the GNU General
Public License v2 or later.

The GPL v3 is accessible here:
http://www.gnu.org/licenses/gpl.html

The GPL v2 is accessible here:
http://www.gnu.org/licenses/old-licenses/gpl-2.0.html

It comes with no warranty whatsoever.

This code illustrates how to use Jython to:
* loop over all members of a route relation
* find out whether the member is a node, a way or a relation
* add/change properties of a relation
* remove properties of a relation
* add members to a relation
* remove members from a relation
* sort all members backwards

* How to set an element selected

'''
from javax.swing import JOptionPane
from org.openstreetmap.josm import Main
import org.openstreetmap.josm.command as Command
import org.openstreetmap.josm.data.osm.Node as Node
import org.openstreetmap.josm.data.osm.Way as Way
import org.openstreetmap.josm.data.osm.Relation as Relation
import org.openstreetmap.josm.data.osm.TagCollection as TagCollection
import org.openstreetmap.josm.data.osm.DataSet as DataSet
import org.openstreetmap.josm.data.osm.RelationMember as RelationMember
import re

commandsList = []
reNumberDashNumber = re.compile(r'\d+-\d+')
def getMapView():
    if Main.main and Main.main.map:
        return Main.main.map.mapView
    else:
        return None

mv = getMapView()
if mv and mv.editLayer and mv.editLayer.data:
    dummy_relation = Relation()
    selectedRelations = mv.editLayer.data.getSelectedRelations()

    if not(selectedRelations):
        JOptionPane.showMessageDialog(Main.parent, "Please select a route relation")
    else:
        print
        for route in selectedRelations:
            newRelation = Relation(route)
            relationChanged = False
            name = route.get('name')
            if name:
                if reNumberDashNumber.match(name):
                    print 'removing name when it is of the form ##-##'
                    newRelation.remove('name')
                    relationChanged = True
            else:
                name = ''
            ref = route.get('ref')
            if ref:
                if reNumberDashNumber.match(ref):
                    print 'removing ref when it is of the form ##-##'
                    newRelation.remove('ref')
                    relationChanged = True
            else:
                ref = ''
            if relationChanged:
                commandsList.append(Command.ChangeCommand(route, newRelation))
                
                Main.main.undoRedo.add(Command.SequenceCommand("Removing name and/or ref " + name + '/' + ref, commandsList))
                commandsList = []

            rcn_refs = []; route_relation_names = []; memberslist = []
            endnodes = []; prev_endnodes = []
            continuous_forward = True; continuous_backward = True
            prev_role = None; prev_endnodes_before_forward = None; last_endnodes_before_backward = None
            for member in route.getMembers():
                if member.isWay():
                    role = member.getRole()
                    memberslist.append(member)
                    way = member.getWay()
                    #JOptionPane.showMessageDialog(Main.parent, 'way is selected')
                    endnodes = [way.getNode(0), way.getNode(way.nodesCount-1)]
                    notfoundyet = True
                    for endnode in endnodes:
                        # inventorizing of rcn_ref on end nodes
                        rcn_ref = endnode.get('rcn_ref')
                        if rcn_ref:
                            rcn_refs.append(int(rcn_ref))
                            for referrer in endnode.getReferrers():
                                if referrer.getType() is dummy_relation.getType():
                                    if referrer.get('type')=='network' and referrer.get('network')=='rcn':
                                        relname=referrer.get('name')
                                        if relname:
                                            route_relation_names.append(relname)
                                        
                                    elif referrer.get('type')=='collection':
                                        route_relation_names.append('Node not assigned to network yet')
                        # checking for continuity on ways
                        if notfoundyet:
                            if role:
                                if prev_role:
                                    if role=='forward' and prev_role=='forward' and endnode in prev_endnodes:
                                        notfoundyet = False
                                    elif role=='forward' and prev_role=='backward' and endnode in last_endnodes_before_backward:
                                        notfoundyet = False
                                    elif role=='backward' and prev_role=='forward' and endnode in prev_endnodes:
                                        notfoundyet = False
                                    elif role=='backward' and prev_role=='backward' and endnode in prev_endnodes:
                                        notfoundyet = False
                                else:
                                    if role=='forward' and endnode in prev_endnodes:
                                        notfoundyet = False
                                    elif role=='backward' and endnode in prev_endnodes:
                                        notfoundyet = False
                            else:
                                if prev_role:
                                    if prev_role=='forward' and endnode in prev_endnodes:
                                        notfoundyet = False
                                    elif prev_role=='backward' and endnode in last_endnodes_before_backward:
                                        notfoundyet = False
                                else:
                                    if endnode in prev_endnodes:
                                        notfoundyet = False
                    # Analysis of continuity of ways
                    if prev_endnodes and notfoundyet:
                        if role:
                            if role == 'forward':
                                continuous_forward = False
                            elif role == 'backward':
                                continuous_backward = False
                        else:
                            continuous_forward = False
                            continuous_backward = False
                    if role=='forward':
                        if not(prev_endnodes_before_forward):
                            prev_endnodes_before_forward  = prev_endnodes
                    elif prev_role=='forward' and role=='backward':
                        if not(last_endnodes_before_backward):
                            last_endnodes_before_backward = prev_endnodes
                    elif not(role) and prev_role=='backward':
                        prev_endnodes_before_forward = None
                    prev_role = role
                    prev_endnodes = endnodes
            # Drawing conclusions about continuity of ways
            if continuous_forward:
                print 'route is continous in the forward direction'
            else:
                print 'route is NOT CONTINUOUS in the forward direction'
            if continuous_backward:
                print 'route is continous in the backward direction'
            else:
                print 'route is NOT CONTINUOUS in the backward direction'

            # Drawing conclusions about rcn_refs
            print rcn_refs
            if len(rcn_refs) > 1:
                newRelation = Relation(route)
                relationChanged = False

                if rcn_refs[0] > rcn_refs[1]:
                    rcn_refs.sort()
                    print 'Flipping members order'
                    for member in reversed(memberslist):
                        newRelation.addMember( newRelation.getMembersCount(), member)
                        newRelation.removeMember (0)
                    commandsList.append(Command.ChangeCommand(route, newRelation))
                    Main.main.undoRedo.add(Command.SequenceCommand("Flipping order of members", commandsList))
                    commandsList = []
                note = route.get('note')
                newNote = str(rcn_refs[0]).zfill(2) + '-' + str(rcn_refs[1]).zfill(2)
                if not(note) or note != newNote:
                    if not(note): note = 'nothing'
                    newRelation.put('note', newNote)
                    relationChanged = True
                    commandsList.append(Command.ChangeCommand(route, newRelation))
                
                    Main.main.undoRedo.add(Command.SequenceCommand("Changing note from " + note + ' to ' + newNote, commandsList))
                    commandsList = []

                if len(route_relation_names) > 1 and route_relation_names[0] != route_relation_names[1]:
                    print
                    print 'This is probably a CONNECTION to another network'
                    print route_relation_names
            else:
                print 'less than 2 end nodes with rcn_ref found'