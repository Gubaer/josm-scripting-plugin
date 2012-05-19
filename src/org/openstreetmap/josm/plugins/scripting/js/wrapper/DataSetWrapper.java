package org.openstreetmap.josm.plugins.scripting.js.wrapper;

import static org.openstreetmap.josm.plugins.scripting.js.wrapper.WrappingUtil.assertApi;
import static org.openstreetmap.josm.plugins.scripting.js.wrapper.WrappingUtil.toNativeArray;

import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveId;

/**
 * A wrapper for {@link DataSet}.
 */
public class DataSetWrapper extends NativeJavaObject {
	private static final long serialVersionUID = 6031164802735887302L;

	private DataSet ds() {
		return (DataSet)javaObject;
	}
	
	public DataSetWrapper(Scriptable scope, Object javaObject, Class<?> staticType) {
		super(scope, javaObject, staticType);
	}
	
	@Override
	public boolean has(String name, Scriptable start) {
		if(
				"get".equals(name)
			 || "add".equals(name)
			 || "remove".equals(name)
			 || "each".equals(name)
			 || "nodeBuilder".equals(name)
			 || "wayBuilder".equals(name)
			 || "selection".equals(name)
		) return true;
		return super.has(name, start);
	}

	public Object get(String name, Scriptable start) {
		if ("get".equals(name)) return fGet;
		if ("add".equals(name)) return fAdd;
		if ("remove".equals(name)) return fRemove;
		if ("each".equals(name)) return fEach;
		if ("nodeBuilder".equals(name)) return getNodeBuilder();
		if ("wayBuilder".equals(name)) return getWayBuilder();
		if ("selection".equals(name)) return new SelectionAccessor();
		return super.get(name, start);
	}
	
	private Object getNodeBuilder() {
		Scriptable scope = new NativeObject();
		scope.setParentScope(parent);
		scope.put("ds", scope, Context.javaToJS(ds(), parent));
		String script = 
			"var NodeBuilder = require('josm/builder').NodeBuilder;"
		  + "new NodeBuilder(ds);";
		Context ctx = Context.getCurrentContext();
		return ctx.evaluateString(scope,script, "fragment: create NodeBuilder for dataset", 0, null);
	}
	
	private Object getWayBuilder() {
		Scriptable scope = new NativeObject();
		scope.setParentScope(parent);
		scope.put("ds", scope, Context.javaToJS(ds(), parent));
		String script = 
			"var WayBuilder = require('josm/builder').WayBuilder;"
		  + "new WayBuilder(ds);";
		Context ctx = Context.getCurrentContext();
		return ctx.evaluateString(scope,script, "fragment: create WayBuilder for dataset", 0, null);
	}
		
	static private Function fGet = new BaseFunction() {		
		private static final long serialVersionUID = -1049214446967093815L;

		protected Object get(DataSet ds, PrimitiveId id) {
			OsmPrimitive primitive = ds.getPrimitiveById(id);
			return primitive == null ? Undefined.instance : primitive;
		}

		protected Object get(DataSet ds, OsmPrimitiveType type, long id) {
			OsmPrimitive primitive = ds.getPrimitiveById(id, type);
			return primitive == null ? Undefined.instance : primitive;
		}

		protected Object get(DataSet ds, String type, long id) {
			type = type.trim().toLowerCase();
			OsmPrimitiveType t = OsmPrimitiveType.fromApiTypeName(type);
			assertApi(t != null, "Unsupported primitive type ''{0}''", type);
			OsmPrimitive primitive = ds.getPrimitiveById(id, t);
			return primitive == null ? Undefined.instance : primitive;
		}		
				
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {			
			assertApi(thisObj instanceof DataSetWrapper, "DataSetWrapper expected as thisObj, got {0}", thisObj);
			DataSet ds = (DataSet)((Wrapper)thisObj).unwrap();
			if (args.length == 0) return Undefined.instance;
			Object ret = null;
			switch(args.length) {
			case 0: return Undefined.instance;
			case 1: 
				Object o = args[0];
				o = WrappingUtil.unwrap(o);
				assertApi(o != null && args[0] != Undefined.instance, "Argument 0 must not be null or undefined");
				assertApi(o instanceof PrimitiveId, "Argument 0: Expected a PrimitiveId, got {0}", o);
				ret = get(ds, (PrimitiveId)o);
				break;
			case 2:
				Object a0 = WrappingUtil.unwrap(args[0]);
				Object a1 = WrappingUtil.unwrap(args[1]);
				assertApi(a0 instanceof OsmPrimitiveType || a0 instanceof String, "Argument 0: Expected a string ''node'', ''way'' or ''relation'', or a OsmPrimitiveType, got {0}", a0);
				assertApi(a1 instanceof Number, "Argument 1: Expected a number, got {0}", a1);
				long id = ((Number)a1).longValue();
				assertApi(id != 0, "Argument 1: id != 0 expected");
				
				if (a0 instanceof OsmPrimitiveType) {
					ret = get(ds,(OsmPrimitiveType)a0, id);
				} else {
					ret = get(ds,(String)a0, id);
				}
				break;
			case 3:
				assertApi(false, "Unsupported number of arguments, expected 1 or 2, got {0}", args.length);
			}
			return ret == Undefined.instance ? Undefined.instance : Context.javaToJS(ret, scope);
		}	
	};
	
	static private Function fRemove = new BaseFunction() {		
		private static final long serialVersionUID = 2964460986578270740L;

		protected void remember(Object o, List<PrimitiveId> toRemove) {
			if (o == null || o == Undefined.instance) return;
			o = WrappingUtil.unwrap(o);
			if (o instanceof PrimitiveId) {
				toRemove.add((PrimitiveId)o);
			} else if (o instanceof OsmPrimitive) {
				toRemove.add(((OsmPrimitive)o).getPrimitiveId());
			} else if (o instanceof List<?>) {
				for (Object oo : (List<?>)o) {
					remember(oo, toRemove);
				}
			} else {
				assertApi(false, "Can''t remove an object of type ''{0}'' from a dataset.", o.getClass());
			}	 
		}
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {			
			List<PrimitiveId> toRemove = new ArrayList<PrimitiveId>();
			assertApi(thisObj instanceof DataSetWrapper, "DataSetWrapper expected as thisObj, got {0}", thisObj);
			if (args.length == 0) return Undefined.instance;
			for (int i=0; i< args.length; i++) {
				remember(args[i], toRemove);
			}
			DataSet ds = (DataSet)((Wrapper)thisObj).unwrap();
			try {
				ds.beginUpdate();
				for (PrimitiveId id: toRemove) {
					ds.removePrimitive(id);
				}
			} finally {
				ds.endUpdate();
			}			
			return Undefined.instance;
		}		
	};
	
	static private Function fAdd = new BaseFunction() {		
		private static final long serialVersionUID = -5546306635329482920L;

		protected void remember(Object o, List<OsmPrimitive> toAdd) {
			if (o == null || o == Undefined.instance)
				return;
			o = WrappingUtil.unwrap(o);
			if (o instanceof OsmPrimitive) {
				toAdd.add((OsmPrimitive) o);
			} else if (o instanceof List<?>) {
				for (Object oo : (List<?>) o) {
					remember(oo, toAdd);
				}
			} else if (o instanceof OsmPrimitive) {
				toAdd.add((OsmPrimitive) o);
			} else {
				assertApi(false, "Can''t add an object of type ''{0}'' to the dataset.", o.getClass());
			}
		}
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			List<OsmPrimitive> toAdd = new ArrayList<OsmPrimitive>();
			assertApi(thisObj instanceof DataSetWrapper, "DataSetWrapper as this expected, got {0}", thisObj);
			if (args.length == 0) return Undefined.instance;
			for (int i=0; i< args.length; i++){		
				remember(args[i], toAdd);
			}
			DataSet ds = (DataSet)((Wrapper)thisObj).unwrap();
			try {
				ds.beginUpdate();
				for (OsmPrimitive primitive: toAdd) {
					ds.addPrimitive(primitive);
				}
			} finally {
				ds.endUpdate();
			}			
			return Undefined.instance;
		}
	};
		
	static private abstract class DataSetIteratingFunction extends BaseFunction {
		private static final long serialVersionUID = 677196798091194462L;
		protected DataSet ds = null;
		protected Function delegate = null;
		
		protected Object invoke(Context ctx, Scriptable scope,Object primitive) {
			Context cx = Context.getCurrentContext();
			return delegate.call(cx, scope, null, new Object[]{primitive});
		}
		
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			assertApi(thisObj instanceof DataSetWrapper, "DataSetWrapper as this expected, got {0}", thisObj);
			ds = (DataSet)((Wrapper)thisObj).unwrap();
			assertApi(args.length == 1, "Expected a Function as argument, got {0} arguments", args.length);
			assertApi(args[0] != null && args[0] != Undefined.instance, "Argument 0: Expected a Function, got {0}", args[0]);
			assertApi(args[0] instanceof Function, "Argument 0: Expected a Function, got {0}", args[0]);
			this.delegate = (Function)args[0];
			return Undefined.instance;
		}
	};
	
	static private Function fEach = new DataSetIteratingFunction() {
		private static final long serialVersionUID = 2702021710026212929L;

		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			super.call(cx,scope,thisObj,args);
			try {
				ds.getReadLock().lock();
				for (OsmPrimitive primitive : ds.allPrimitives()) {
					invoke(cx,scope, Context.javaToJS(primitive, scope));
				}
			} finally {
				ds.getReadLock().unlock();
			}
			return Undefined.instance;
		}
	};

	class SelectionAccessor extends ScriptableObject {		
		
		public SelectionAccessor() {
			put("add", this, fAddSelection);
			put("remove", this, fRemoveSelection);
			put("set", this, fSetSelection);	
			put("clear", this, fClearSelection);
			put("toggle", this, fToggleSelection);
			put("get", this, fGetSelection);
			put("isSelected", this, fIsSelected);
		}
		
		@Override
		public boolean has(String name, Scriptable start) {
			if ("nodes".equals(name)) return true;
			if ("ways".equals(name)) return true;
			if ("relations".equals(name)) return true;
			if ("isEmpty".equals(name)) return true;
			return super.has(name, start);
		}

		@Override
		public Object get(String name, Scriptable start) {
			if ("nodes".equals(name)) return getNodes();
			if ("ways".equals(name)) return getWays();
			if ("relations".equals(name)) return getNodes();
			if ("isEmpty".equals(name)) return getIsEmpty();
			return super.get(name, start);
		}
		
		private Object getNodes() {
			return toNativeArray(ds().getSelectedNodes(), DataSetWrapper.this.parent);
		}
		
		private Object getWays() {
			return toNativeArray(ds().getSelectedWays(), DataSetWrapper.this.parent);
		}

		private Object getRelations() {
			return toNativeArray(ds().getSelectedRelations(), DataSetWrapper.this.parent);
		}
		
		private Object getIsEmpty() {
			return ds().selectionEmpty();
		}
		
		public DataSet getDataSet() {
			return ds();
		}

		@Override
		public String getClassName() {
			return getClass().getName();
		}
	};
	
	static private  void remember(Object o, List<PrimitiveId> toAdd) {
		if (o == null || o == Undefined.instance)
			return;
		o = WrappingUtil.unwrap(o);
		if (o instanceof PrimitiveId) {
			toAdd.add((PrimitiveId) o);
		} else if (o instanceof List<?>) {
			for (Object oo : (List<?>) o) {
				remember(oo, toAdd);
			}
		} else {
			assertApi(false, "Can''t process an object of type ''{0}''.", o.getClass());
		}
	}
	
	static private Function fAddSelection = new BaseFunction() {		
		private static final long serialVersionUID = -5546306635329482920L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			List<PrimitiveId> toAdd = new ArrayList<PrimitiveId>();
			assertApi(thisObj instanceof SelectionAccessor, "SelectionAccessor as this expected, got {0}", thisObj);
			SelectionAccessor selection = (SelectionAccessor)thisObj;
			DataSet ds = selection.getDataSet();
			if (args.length == 0) return Undefined.instance;
			for (int i=0; i< args.length; i++){		
				remember(args[i], toAdd);
			}
			ds.addSelected(toAdd);
			return Undefined.instance;
		}
	};
	
	static private Function fRemoveSelection = new BaseFunction() {		
		private static final long serialVersionUID = -5546306635329482920L;
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			List<PrimitiveId> toRemove = new ArrayList<PrimitiveId>();
			assertApi(thisObj instanceof SelectionAccessor, "SelectionAccessor as this expected, got {0}", thisObj);
			SelectionAccessor selection = (SelectionAccessor)thisObj;
			DataSet ds = selection.getDataSet();
			if (args.length == 0) return Undefined.instance;
			for (int i=0; i< args.length; i++){		
				remember(args[i], toRemove);
			}
			ds.clearSelection(toRemove);
			return Undefined.instance;
		}
	};
	
	static private Function fSetSelection = new BaseFunction() {		
		private static final long serialVersionUID = 1L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			List<PrimitiveId> toSet = new ArrayList<PrimitiveId>();
			assertApi(thisObj instanceof SelectionAccessor, "SelectionAccessor as this expected, got {0}", thisObj);
			SelectionAccessor selection = (SelectionAccessor)thisObj;
			DataSet ds = selection.getDataSet();
			if (args.length == 0) return Undefined.instance;
			for (int i=0; i< args.length; i++){		
				remember(args[i], toSet);
			}
			ds.setSelected(toSet);
			return Undefined.instance;
		}
	};
	
	static private Function fClearSelection = new BaseFunction() {		
		private static final long serialVersionUID = 1L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			assertApi(thisObj instanceof SelectionAccessor, "SelectionAccessor as this expected, got {0}", thisObj);
			SelectionAccessor selection = (SelectionAccessor)thisObj;
			DataSet ds = selection.getDataSet();
			assertApi(args.length == 0, "Expected 0 arguments, got {0}", args.length);
			ds.clearSelection();
			return Undefined.instance;
		}
	};
	
	static private Function fGetSelection = new BaseFunction() {		
		private static final long serialVersionUID = 1L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			assertApi(thisObj instanceof SelectionAccessor, "SelectionAccessor as this expected, got {0}", thisObj);
			SelectionAccessor selection = (SelectionAccessor)thisObj;
			DataSet ds = selection.getDataSet();
			assertApi(args.length == 0, "Expected 0 arguments, got {0}", args.length);
			return toNativeArray(ds.getSelected(), scope);
		}
	};
	
	static private Function fToggleSelection = new BaseFunction() {		
		private static final long serialVersionUID = 1L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			List<PrimitiveId> toToggle = new ArrayList<PrimitiveId>();
			assertApi(thisObj instanceof SelectionAccessor, "SelectionAccessor as this expected, got {0}", thisObj);
			SelectionAccessor selection = (SelectionAccessor)thisObj;
			DataSet ds = selection.getDataSet();
			if (args.length == 0) return Undefined.instance;
			for (int i=0; i< args.length; i++){		
				remember(args[i], toToggle);
			}
			ds.toggleSelected(toToggle);
			return Undefined.instance;
		}
	};
	
	static private Function fIsSelected = new BaseFunction() {		
		private static final long serialVersionUID = 1L;

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			assertApi(thisObj instanceof SelectionAccessor, "SelectionAccessor as this expected, got {0}", thisObj);
			SelectionAccessor selection = (SelectionAccessor)thisObj;
			DataSet ds = selection.getDataSet();
			assertApi(args.length == 1, "Expected exactly one OSM primitive, got {0} arguments", args.length);
			Object o = args[0];
			if (o == null || o == Undefined.instance) return false;
			o = WrappingUtil.unwrap(o);
			assertApi(o instanceof OsmPrimitive, "Expected an OsmPrimitive, got {0}", o);
			return ds.isSelected((OsmPrimitive)o);
		}
	};
}
