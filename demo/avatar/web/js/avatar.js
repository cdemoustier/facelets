 
/* Object Extensions
 ***********************************************************/
Object.extend(String.prototype, {
	trim: function() {
		var s = this.replace( /^\s+/g, "" );
  		return s.replace( /\s+$/g, "" );
	}
});
Object.extend(Element, {
  replace: function(dest, src)  {
    var d = $(dest);
	var parent = d.parentNode;
	var temp = document.createElement('div');
	temp.innerHTML = this.serialize(src);
	parent.replaceChild(temp.firstChild,d);
  },
  serialize: function(e) {
	  var str = (e.xml) ? this.serializeIE(e) : this.serializeMozilla(e);
	  return str;
  },
  serializeIE: function(e) {
	  return e.xml;
  },
  serializeMozilla: function(e) {
	  return new XMLSerializer().serializeToString(e);
  },
  firstElement: function(e) {
	  var first = e.firstChild;
	  while (first && first.nodeType != 1) {
		  first = first.nextSibling;
	  }
	  return first;
  }
});
function $Error(e) {
	if (e.message) {
		return '['+e.lineNumber+'] ' + e.message;
	} else {
		return e.toString();
	}
};

/* Avatar Utility Class
 ***********************************************************/
var Avatar = {
	initialized: false,
	create: function() {
		return function() {
	  		if (Avatar.initialized) {
      			this.initialize.apply(this, arguments);
	  		} else {
				var args = arguments;
				var me = this;
	  			Event.observe(window,'load',function() {
					Avatar.initialized = true;
					me.initialize.apply(me, args);
				},false);
			}
		}
	},
	toArray: function(s,e) {
		if (typeof s == 'string') {
			return s.split((e)?e:' ').map(function(p) { return p.trim(); });
		}
		return s;
	}
};
function $Form(src) {
	if (src) {
		var form = $(src);
		while (form && form.tagName.toLowerCase() != 'form') {
			if (form.form) return form.form;
			form = form.parentNode;
		}
		return form;
	}
	return document.forms[0];
};

/* Turn any Element into a Avatar.Command
 ***********************************************************/
Avatar.Command = Avatar.create();
Avatar.Command.prototype = {
	initialize: function(action,options) {
		var form = (options && options.form) || $Form(action);
		var event = (options && options.event) || 'click';
		Event.observe(action,event,function(e) {
			new Avatar.Render(form,action,options);
			Event.stop(e);
			return false;
		},true);
	}
};

/* ViewState Hash over a given Form
 ***********************************************************/
Avatar.ViewState = Class.create();
Avatar.ViewState.Ignore = ['button','submit','reset','image'];
Avatar.ViewState.prototype = {
	initialize: function(form) {
		var e = Form.getElements($(form));
		var t,p;
		for (var i = 0; i < e.length; i++) {
			if (Avatar.ViewState.Ignore.indexOf(e[i].type) == -1) {
				t = e[i].tagName.toLowerCase();
				p = Form.Element.Serializers[t](e[i]);
				if (p && p[0].length != 0) {
					if (p[1].constructor != Array) p[1] = [p[1]];
					if (this[p[0]]) { this[p[0]] = this[p[0]].concat(p[1]); }
					else this[p[0]] = p[1];
				}
			}
		};
	},
	toQueryString: function() {
		var q = new Array();
		var i,j,p,v;
		for (property in this) {
			if (this[property]) {
				if (typeof this[property] == 'function') continue;
				p = encodeURIComponent(property);
				if (this[property].constructor == Array) {
					for (j = 0; j < this[property].length; j++) {
						v = this[property][j];
						if (v) {
							v = encodeURIComponent(v);
							q.push(p+'='+v);
						}
					}
				} else {
					v = encodeURIComponent(this[property]);
					q.push(p+'='+v);
				}
			}
		}
		return q.join('&');
	}
};

/* Handles Render Requests
 ***********************************************************/
Avatar.Render = Class.create();
Object.extend(Object.extend(Avatar.Render.prototype, Ajax.Request.prototype), {
  initialize: function(form, source, options) {
    this.transport = Ajax.getTransport();
    this.setOptions(options);
	
	// make sure we are posting
	this.options.method = 'post';
	
	// get form
	this.form = $Form(form);
	this.url = this.form.action;
	
	// create viewState
	var viewState = new Avatar.ViewState(this.form);
	
	// add passed parameters
	Object.extend(viewState, this.options.parameters || {});
	this.options.parameters = null;
	
	// add source
	var action = $(source);
	if (action && action.form) viewState[action.name] = action.value;
	else viewState[source] = source;
	
	// add render
	if (this.options.render) {
		viewState['avatar.RENDER'] = this.options.render;
	}
	
	// build url
	this.url += (this.url.match(/\?/) ? '&' : '?') + viewState.toQueryString();

	// setup event handling
    var onComplete = this.options.onComplete || Prototype.emptyFunction;
    this.options.onComplete = (function(transport, object) {
      this.renderView();
      onComplete(transport, object);
    }).bind(this);

	// send request
    this.request(this.url);
  },
  renderView: function() {
	var response = this.transport.responseXML.documentElement;
	
	// process each render-able area
	var renders = response.getElementsByTagName('render');
	var destId,src;
	for (var i = 0; i < renders.length; i++) {
		destId = renders[i].getAttribute('id');
		src = Element.firstElement(renders[i]);
		Element.replace(destId,src);
	}

    if (this.responseIsSuccess()) {
      if (this.onComplete)
        setTimeout(this.onComplete.bind(this), 10);
    }
  }
});

/* Take any Event and delegate it to an Observer
 ***********************************************************/
Avatar.Observer = Class.create();
Avatar.Observer.prototype = {
	initialize: function(form,trigger,options) {
		this.options = {};
		Object.extend(this.options, options || {});
		var source = this.options.source;
		Event.observe(window,'load',function() {
			if (form != null && trigger != null) {
				var fn = function(e) {
					alert('fired!');
					new Avatar.Render(form,(source || Event.element(e)),this.options);
					Event.stop(e);
					return false;
				};
				alert(trigger);
				var event = this.options.event || 'click';
				var triggers = Avatar.toArray(trigger);
				var events = Avatar.toArray(event);
				for (var i = 0; i < triggers.length; i++) {
					for (var j = 0; j < events.length; j++) {
						Event.observe($(triggers[i]),events[j],fn,true);
					}
				}
			}
		},true);
	}
};