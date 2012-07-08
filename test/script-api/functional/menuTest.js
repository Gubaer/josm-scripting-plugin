/*
 * Functional test for manipulating menu items
 * 
 * Load in the JOSM scripting console and run.
 * 
 * See manual steps at the end
 */

var util = require("josm/util");
var ut = require("josm/unittest");
var menu = require("josm/ui/menu");

var action = new menu.JSAction({
	name: "My Action",
	iconName: "script-engine",
	onInitEnabled: function() {
		this.enabled = false;
	}
});

// --------------- tests for property 'name' -----------------
// we can assign anything to a name 
action.name = "Another name";
util.assert(action.name == "Another name", "1 - Unexpected name");
action.name = null;
util.assert(action.name == undefined, "2 - Unexpected name");
action.name = undefined;
util.assert(action.name == undefined, "3 - Unexpected name");
action.name = {};
util.assert(util.isSomething(action.name), "4 - Unexpected name");
action.name = "Another name";

// --------------- tests for property 'tooltip' -----------------
//we can assign anything to a tooltip 
action.tooltip = "Another tooltip";
util.assert(action.tooltip == "Another tooltip", "1 - Unexpected tooltip");
action.tooltip = null;
util.assert(action.tooltip == undefined, "2 -Unexpected tooltip");
action.tooltip = undefined;
util.assert(action.tooltip == undefined, "3 - Unexpected tooltip");
action.tooltip = {};
util.assert(util.isSomething(action.tooltip), "4 - Unexpected tooltip");
action.tooltip = "Another tooltip";


//--------------- tests for property 'onExecute' -----------------
function onExecute() {
	util.println("Action is executing ...");
};
action.onExecute = onExecute;
action.onExecute = null;
util.assert(!util.isDef(action.onExecute), "should be defined");
action.onExecute = onExecute;
action.onExecute = undefined;
util.assert(!util.isDef(action.onExecute), "should be defined");
ut.expectAssertionError(function() {
	// string is not supported
	action.onExecute = "println 'hello world!";
});
action.onExecute = onExecute;

// --------------- tests for property 'onInitEnabled' -----------------

function onInitEnabled() {
	util.println("Action ''{0}'': Initializing enabled state ...", this.name);
};
action.onInitEnabled = onInitEnabled;
action.onInitEnabled = null;
util.assert(!util.isDef(action.onInitEnabled), "should be defined");
action.onInitEnabled = onInitEnabled;
action.onInitEnabled = undefined;
util.assert(!util.isDef(action.onInitEnabled), "should be defined");
ut.expectAssertionError(function() {
	// string is not supported
	action.onInitEnabled = "println 'hello world!";
});
action.onInitEnabled = onInitEnabled;

//--------------- tests for property 'onUpdateEnabled' -----------------
function onUpdateEnabled(selection) {
	if (selection) {
		util.println("Action ''{0}'': Update enabled state - selection has changed", this.name);
		this.enabled = !selection.isEmpty();
	} else {
		util.println("Action ''{0}'': Update enabled state - layers have changed", this.name);
		this.enabled = true;
	}
};
action.onUpdateEnabled = onUpdateEnabled;
action.onUpdateEnabled = null;
util.assert(!util.isDef(action.onUpdateEnabled), "should be defined");
action.onUpdateEnabled = onUpdateEnabled;
action.onUpdateEnabled = undefined;
util.assert(!util.isDef(action.onUpdateEnabled), "should be defined");
ut.expectAssertionError(function() {
	// string is not supported
	action.onUpdateEnabled = "println 'hello world!";
});
action.onUpdateEnabled = onUpdateEnabled;


//--------------- tests for adding the action to a menu  -----------------
// add the action to the menu
action.addToMenu(josm.menu.get("edit"));

//--------------- tests for adding the action to the toolbar --------------
//action.addToToolbar({at: "start"});
//action.addToToolbar({at: "end"});
//action.addToToolbar({at: 5});
//action.addToToolbar({after: "open"});
//action.addToToolbar({before: "upload"});
action.addToToolbar();


// continue manually:
//
// 1.   create a layer  -> should trigger onUpdateEnabled
// 2.   add objects and change the selection  -> should trigger onUpdateEnabled
// 3.   select the menu item "Edit->My Action" -> should trigger onExecute 

