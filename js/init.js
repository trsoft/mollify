var mollifyDefaults = {
	"template-url": "templates/",
	"service-path": "backend/",
	"limited-http-methods" : false,
	"list-view-columns": {
		"name": { width: 250 },
		"size": {},
		"file-modified": { width: 150 }
	},
	"list-view-columns-search": {
		"name": { width: 250 },
		"path": { width: 150 },
		"size": {}
	}
};

!function($) {

	"use strict"; // jshint ;_;
	
	var mollify = {
		App : {},
		view : {},
		ui : {},
		events : {},
		service : {},
		filesystem : {},
		plugins : {},
		features : {},
		dom : {},
		templates : {}
	};
	
	mollify._time = new Date().getTime();
	mollify._hiddenInd = 0;
	mollify.settings = false;
	mollify.session = false;
	
	/* APP */

	mollify.App.init = function(s, p) {
		mollify.App.pageUrl = window.location.href;
		mollify.App.pageUrl = mollify.App.pageUrl.substring(0, mollify.App.pageUrl.lastIndexOf('/')+1);
		
		mollify.plugins.register(new mollify.plugin.Core());
		if (p) {
			for (var i=0, j=p.length; i < j; i++)
				mollify.plugins.register(p[i]);
		}
		
		mollify.settings = $.extend({}, mollifyDefaults, s);
		mollify.service.init(mollify.settings["limited-http-methods"]);
		
		mollify.events.addEventHandler(function(e) {
			if (e.type == 'session/start') {
				mollify.session = e.payload;
				mollify.session.id = mollify.session.session_id;
				mollify.session.admin = (mollify.session.default_permission == 'A');		
				
				mollify.filesystem.init(mollify.session.folders);
				mollify.App._start();
			} else if (e.type == 'session/end') {
				mollify.session = false;
				mollify.filesystem.init([]);
				mollify.App._start();
			}
		});

		mollify.ui.initialize();
		mollify.plugins.initialize();

		mollify.service.get("session/info/3", function(s) {
			mollify.App.setSession(s);
		}, function(c, e) {
			$("#mollify").html("Failed to initialize Mollify");
		});
	};
	
	mollify.App.setSession = function(s) {
		mollify.events.dispatch("session/start", s);
	};
	
	mollify.App._start = function() {
		var $c = $("#mollify");
		if (!mollify.session || !mollify.session.authenticated) {
			new mollify.view.LoginView().init($c);
		} else {
			new mollify.view.MainView().init($c);
		}
	};
	
	mollify.getItemDownloadInfo = function(item) {
		if (!item) return false;

		if (item.is_file)
			return {
				name: item.name,
				url: mollify.filesystem.getDownloadUrl(item)
			};
		else
			if (mollify.plugins.exists("plugin-archiver")) return {
				name: item.name + ".zip",	//TODO get extension from plugin
				url: mollify.plugins.get("plugin-archiver").getDownloadCompressedUrl(item)
			};

		return false;
	}
	
	/* EVENTS */
	var et = mollify.events;
	et._handlers = [];
		
	et.addEventHandler = function(h) {
		et._handlers.push(h);
	};
	
	et.dispatch = function(type, payload) {
		var e = { type: type, payload: payload };
		$.each(et._handlers, function(i, h) {
			h(e);
		});
	};
	
	/* SERVICE */
	var st = mollify.service;
	
	st.init = function(limitedHttpMethods) {
		st._limitedHttpMethods = !!limitedHttpMethods;
	};
	
	st.url = function(u, full) {
		if (u.startsWith('http')) return u;
		var url = mollify.settings["service-path"]+"r.php/"+u;
		if (!full) return url;
		return mollify.App.pageUrl + url;
	};
	
	st.get = function(url, s, err) {
		st._do("GET", url, null, s, err);
	};

	st.post = function(url, data, s, err) {
		st._do("POST", url, data, s, err);
	};
	
	st.put = function(url, data, s, err) {
		st._do("PUT", url, data, s, err);
	};
	
	st.del = function(url, data, s, err) {
		st._do("DELETE", url, data, s, err);
	};
			
	st._do = function(type, url, data, s, err) {
		var t = type;
		var diffMethod = (st._limitedHttpMethods && (t == 'PUT' || t == 'DELETE'));
		if (diffMethod) t = 'POST';
		
		$.ajax({
			type: t,
			url: st.url(url),
			processData: false,
			data: data ? JSON.stringify(data) : null,
			contentType: 'application/json',
			dataType: 'json',
			success: function(r, st, xhr) {
				if (!r) {
					if (err) err(0, "todo");
					return;
				}
				if (s) s(r.result);
			},
			error: function(xhr, st, error) {
				var code = 999;	//unknown
				var error = {};
				var data = false;
				var defaultErrorHandler = true;
				if (xhr.responseText && xhr.responseText.startsWith('{')) error = JSON.parse(xhr.responseText);
				if (error && window.def(error.code)) code = error.code;
				
				if (err) defaultErrorHandler = !!err(code, error);
				if (defaultErrorHandler) mollify.ui.dialogs.showError(code, error);
			},
			beforeSend: function (xhr) {
				if (mollify.session && mollify.session.id)
					xhr.setRequestHeader("mollify-session-id", mollify.session.id);
				if (st._limitedHttpMethods || diffMethod)
					xhr.setRequestHeader("mollify-http-method", type);
			}
		});
	};
	
	/* FILESYSTEM */
	
	var mfs = mollify.filesystem;
	
	mfs.init = function(f) {
		mollify.filesystem.roots = [];
		mollify.filesystem.rootsById = {};
		
		if (f && mollify.session.authenticated) {
			mollify.filesystem.roots = f;
			for (var i=0,j=f.length; i<j; i++)
				mollify.filesystem.rootsById[f[i].id] = f[i];
		}
	};
	
	mfs.getDownloadUrl = function(item) {
		if (!item.is_file) return false;
		return mollify.service.url("filesystem/"+item.id, true);
	};
	
	mfs.itemDetails = function(item, data, cb, err) {
		mollify.service.post("filesystem/"+item.id+"/details/", { data : data }, cb, err);
	};
	
	mfs.folderInfo = function(f, hierarchy, data, cb, err) {
		mollify.service.post("filesystem/"+f.id+"/info/" + (hierarchy ? "?h=1" : ""), { data : data }, cb, err);
	};
	
	mfs.folders = function(parent, cb, err) {
		if (parent == null) {
			cb(mfs.roots);
			return;
		}
		mollify.service.get("filesystem/"+parent.id+"/folders/", cb, err);
	};
	
	mfs.copy = function(i, to, cb, err) {
		if (!i) return;
		
		if (window.isArray(i) && i.length > 1) {
			if (!to) {
				mollify.ui.dialogs.folderSelector({
					title: mollify.ui.texts.get('copyMultipleFileDialogTitle'),
					message: mollify.ui.texts.get('copyMultipleFileMessage', [i.length]),
					actionTitle: mollify.ui.texts.get('copyFileDialogAction'),
					handler: {
						onSelect: function(f) { ft._copyMany(i, f, cb, err); },
						canSelect: function(f) { return ft.canCopyTo(i, f); }
					}
				});
			} else
				mfs._copyMany(i, to, cb, err);

			return;	
		}
		
		if (window.isArray(i)) i = i[0];
		
		if (!to) {
			mollify.ui.dialogs.folderSelector({
				title: mollify.ui.texts.get('copyFileDialogTitle'),
				message: mollify.ui.texts.get('copyFileMessage', [i.name]),
				actionTitle: mollify.ui.texts.get('copyFileDialogAction'),
				handler: {
					onSelect: function(f) { ft._copy(i, f, cb, err); },
					canSelect: function(f) { return ft.canCopyTo(i, f); }
				}
			});
		} else
			mfs._copy(i, to, cb, err);
	};
	
	mfs.copyHere = function(item, name, cb, err) {
		if (!item) return;
		
		if (!name) {
			mollify.ui.dialogs.input({
				title: mollify.ui.texts.get('copyHereDialogTitle'),
				message: mollify.ui.texts.get('copyHereDialogMessage'),
				defaultValue: item.name,
				yesTitle: mollify.ui.texts.get('copyFileDialogAction'),
				noTitle: mollify.ui.texts.get('dialogCancel'),
				handler: {
					isAcceptable: function(n) { return !!n && n.length > 0 && n != item.name; },
					onInput: function(n) { mfs._copyHere(item, n, cb, err); }
				}
			});
		} else {
			mfs._copyHere(item, name, cb, err);
		}
	};
	
	mfs.canCopyTo = function(item, to) {
		if (window.isArray(item)) {
			for(var i=0,j=item.length;i<j;i++)
				if (!mfs.canCopyTo(item[i], to)) return false;
			return true;
		}
		
		// cannot copy into file
		if (to.is_file) return false;

		// cannot copy into itself
		if (item.id == to.id) return false;
		
		// cannot copy into same location
		if (item.parent_id == to.id) return false;
		return true;
	};
	
	mfs.canMoveTo = function(item, to) {
		if (window.isArray(item)) {
			for(var i=0,j=item.length;i<j;i++)
				if (!mfs.canMoveTo(item[i], to)) return false;
			return true;
		}
		
		// cannot move into file
		if (to.is_file) return false;

		// cannot move folder into its own subfolder
		if (!to.is_file && item.root_id == to.root_id && to.path.startsWith(item.path)) return false;

		// cannot move into itself
		if (item.id == to.id) return false;
		
		// cannot move into same location
		if (item.parent_id == to.id) return false;
		return true;
	};

	mfs._copyHere = function(i, name, cb, err) {
		mollify.service.post("filesystem/"+i.id+"/copy/", {name:name}, function(r) {
			mollify.events.dispatch('filesystem/copy', { items: [ i ], name: name });
			if (cb) cb(r);
		}, err);
	};
		
	mfs._copy = function(i, to, cb, err) {
		mollify.service.post("filesystem/"+i.id+"/copy/", {folder:to.id}, function(r) {
			mollify.events.dispatch('filesystem/copy', { items: [ i ], to: to });
			if (cb) cb(r);
		}, err);
	};
	
	mfs._copyMany = function(i, to, cb, err) {
		mollify.service.post("filesystem/items/", {action: 'copy', items: i, to: to}, function(r) {
			mollify.events.dispatch('filesystem/copy', { items: i, to: to });
			if (cb) cb(r);
		}, err);
	};
	
	mfs.move = function(i, to, cb, err) {
		if (!i) return;
		
		if (window.isArray(i) && i.length > 1) {
			if (!to) {
				mollify.ui.dialogs.folderSelector({
					title: mollify.ui.texts.get('moveMultipleFileDialogTitle'),
					message: mollify.ui.texts.get('moveMultipleFileMessage', [i.length]),
					actionTitle: mollify.ui.texts.get('moveFileDialogAction'),
					handler: {
						onSelect: function(f) { mfs._moveMany(i, f, cb, err); },
						canSelect: function(f) { return mfs.canMoveTo(i, f); }
					}
				});
			} else
				mfs._moveMany(i, to, cb, err);

			return;	
		}
		
		if (window.isArray(i)) i = i[0];
		
		if (!to) {
			mollify.ui.dialogs.folderSelector({
				title: mollify.ui.texts.get('moveFileDialogTitle'),
				message: mollify.ui.texts.get('moveFileMessage', [i.name]),
				actionTitle: mollify.ui.texts.get('moveFileDialogAction'),
				handler: {
					onSelect: function(f) { mfs._move(i, f, cb, err); },
					canSelect: function(f) { return mfs.canMoveTo(i, f); }
				}
			});
		} else
			mfs._move(i, to, cb, err);
	};
	
	mfs._move = function(i, to, cb, err) {
		mollify.service.post("filesystem/"+i.id+"/move/", {id:to.id}, function(r) {
			mollify.events.dispatch('filesystem/move', { items: [ i ], to: to });
			if (cb) cb(r);
		}, err);
	};

	mfs._moveMany = function(i, to, cb, err) {
		mollify.service.post("filesystem/items/", {action: 'move', items: i, to: to}, function(r) {
			mollify.events.dispatch('filesystem/move', { items: i, to: to });
			if (cb) cb(r);
		}, err);
	};
	
	mfs.rename = function(item, name, cb, err) {
		if (!name || name.length === 0) {
			mollify.ui.dialogs.input({
				title: mollify.ui.texts.get(item.is_file ? 'renameDialogTitleFile' : 'renameDialogTitleFolder'),
				message: mollify.ui.texts.get('renameDialogNewName'),
				defaultValue: item.name,
				yesTitle: mollify.ui.texts.get('renameDialogRenameButton'),
				noTitle: mollify.ui.texts.get('dialogCancel'),
				handler: {
					isAcceptable: function(n) { return !!n && n.length > 0 && n != item.name; },
					onInput: function(n) { mfs._rename(item, n, cb, err); }
				}
			});
		} else {
			mfs._rename(item, name, cb, err);
		}
	};
	
	mfs._rename = function(item, name, cb, err) {
		mollify.service.put("filesystem/"+item.id+"/name/", {name: name}, function(r) {
			mollify.events.dispatch('filesystem/rename', { items: [item], name: name });
			if (cb) cb(r);
		}, err);
	};
	
	mfs._handleDenied = function(action, i, data, msgTitle, acceptCb) {
		var allAcceptable = true;
		var handlers = [];
		for(var k in data.items) {
			var plugin = mollify.plugins.get(k);
			if (!plugin || !plugin.actionValidationHandler) return false;
			
			var handler = plugin.actionValidationHandler();
			handlers.push(handler);
			var acceptable = handler.isAcceptable(action, i, data.items[k], data);
			if (!acceptable) allAcceptable = false;
		}
		
		if (allAcceptable) {
			var acceptMessages = [];
			var acceptKeys = [];
			for(var ind=0,j=handlers.length; ind<j; ind++) {
				var hm = handlers[ind].getAcceptMessages(action, i, data.items[k], data);
				for(var ak in hm) {
					acceptKeys.push(ak);
					acceptMessages.push(hm[ak]);
				}
			}
			// retry with accept keys
			mollify.ui.dialogs.confirmActionAccept(msgTitle, acceptMessages, function() {
				acceptCb(acceptKeys);
			});
		} else {
			var deniedMessages = [];
			for(var ind=0,j=handlers.length; ind<j; ind++)
				deniedMessages = deniedMessages.concat(handlers[ind].getDeniedMessages(action, i, data.items[k], data));
			mollify.ui.dialogs.showActionDeniedMessage(msgTitle, deniedMessages);
		}
		return true;
	}
	
	mfs.del = function(i, cb, err) {
		if (!i) return;
		
		if (window.isArray(i) && i.length > 1) {
			mfs._delMany(i, cb, err);
			return;	
		}
		
		if (window.isArray(i)) i = i[0];
		mfs._del(i, cb, function(c, e) {
			// request denied
			if (c == 109 && e.data && e.data.items) {
				return !mfs._handleDenied("delete", i, e.data, "TODO Could not delete item", function(acceptKeys) { mfs._del(i, cb, err, acceptKeys); });
			}
			if (err) return err(c, e);
		});
	};
	
	mfs._del = function(item, cb, err, acceptKeys) {
		mollify.service.del("filesystem/"+item.id, acceptKeys ? { acceptKeys : acceptKeys } : null, function(r) {
			mollify.events.dispatch('filesystem/delete', { items: [item] });
			if (cb) cb(r);
		}, err);
	};
	
	mfs._delMany = function(i, cb, err) {
		mollify.service.post("filesystem/items/", {action: 'delete', items: i}, function(r) {
			mollify.events.dispatch('filesystem/delete', { items: i });
			if (cb) cb(r);
		}, err);
	};
	
	mfs.createFolder = function(folder, name, cb, err) {
		mollify.service.post("filesystem/"+folder.id+"/folders/", {name: name}, function(r) {
			mollify.events.dispatch('filesystem/createfolder', { items: [folder], name: name });
			if (cb) cb(r);
		}, err);
	};

	/* PLUGINS */
	
	var pl = mollify.plugins;
	pl._list = {};
	
	pl.register = function(p) {
		var id = p.id;
		if (!id) return;
		
		pl._list[id] = p;
	};
	
	pl.initialize = function() {
		for (var id in pl._list) {
			var p = pl._list[id];
			if (p.initialize) p.initialize();
		}
	};
	
	pl.get = function(id) {
		if (!window.def(id)) return pl._list;
		return pl._list[id];
	};
	
	pl.exists = function(id) {
		return !!pl._list[id];
	};
	
	pl.url = function(id, p) {
		var url = mollify.settings["service-path"]+"plugin/"+id;
		if (!p) return url;
		return url +"/client/"+p;
	};
	
	pl.getItemContextRequestData = function(item) {
		var requestData = {};
		for (var id in pl._list) {
			var plugin = pl._list[id];
			if (!plugin.itemContextRequestData) continue;
			var data = plugin.itemContextRequestData(item);
			if (!data) continue;
			requestData[id] = data;
		}
		return requestData;
	};
	
	pl.getItemContextPlugins = function(item, ctx) {
		var data = {};
		if (!ctx) return data;
		var d = ctx.details;
		if (!d || !d.plugins) return data;
		for (var id in pl._list) {
			var plugin = pl._list[id];
			if (!plugin.itemContextHandler) continue;
			var pluginData = plugin.itemContextHandler(item, ctx, d.plugins[id]);
			if (pluginData) data[id] = pluginData;
		}
		return data;
	};
	
	pl.getItemCollectionPlugins = function(items, ctx) {
		var data = {};
		if (!items || !window.isArray(items) || items.length < 1) return data;
		
		for (var id in pl._list) {
			var plugin = pl._list[id];
			if (!plugin.itemCollectionHandler) continue;
			var pluginData = plugin.itemCollectionHandler(items, ctx);
			if (pluginData) data[id] = pluginData;
		}
		return data;
	};
	
	pl.getMainViewPlugins = function() {
		var plugins = [];
		for (var id in pl._list) {
			var plugin = pl._list[id];
			if (!plugin.mainViewHandler) continue;
			plugins.push(plugin);
		}
		return plugins;
	};

	pl.getFileViewPlugins = function() {
		var plugins = [];
		for (var id in pl._list) {
			var plugin = pl._list[id];
			if (!plugin.fileViewHandler) continue;
			plugins.push(plugin);
		}
		return plugins;
	};
		
	/* FEATURES */
	
	var ft = mollify.features;
	ft.hasFeature = function(id) {
		return mollify.session.features && mollify.session.features[id];
	};
	
	/* TEMPLATES */
	var mt = mollify.templates;
	mt._loaded = [];
	
	mt.url = function(name) {
		var base = mollify.settings["template-url"] || 'templates/';
		return mollify.helpers.noncachedUrl(base + name);
	};
	
	mt.load = function(name, url, cb) {
		if (mt._loaded.indexOf(name) >= 0) {
			if (cb) cb();
			return;
		}
		
		$.get(url ? url : mt.url(name), function(h) {
			mt._loaded.push(name);
			$("body").append(h);
			if (cb) cb();
		});
	};
	
	/* DOM */
	var md = mollify.dom;
	md._hiddenLoaded = [];
		
	md.importScript = function(url) {
		$.getScript(url);
	};
		
	md.importCss = function(url) {
		var link = $("<link>");
		link.attr({
			type: 'text/css',
			rel: 'stylesheet',
			href: mollify.helpers.noncachedUrl(url)
		});
		$("head").append(link);
	};

	md.loadContent = function(contentId, url, cb) {
		if (md._hiddenLoaded.indexOf(contentId) >= 0) {
			if (cb) cb();
			return;
		}
		var id = 'mollify-tmp-'+(mollify._hiddenInd++);
		$('<div id="'+id+'" style="display:none"/>').appendTo($("body")).load(mollify.helpers.urlWithParam(url, "_="+mollify.time), function() {
			md._hiddenLoaded.push(contentId);
			if (cb) cb();
		});
	};
					
	md.loadContentInto = function($target, url, handler, process) {
		$target.load(mollify.helpers.urlWithParam(url, "_="+mollify.time), function() {
			if (process) mollify.ui.process($target, process, handler);
			if (typeof handler === 'function') handler();
			else if (handler.onLoad) handler.onLoad($target);
		});
	};
		
	md.template = function(id, data, opt) {
		return $("#"+id).tmpl(data, opt);
	};

	/* HELPERS */
	
	mollify.helpers = {
		getPluginActions : function(plugins) {
			var list = [];
			
			if (plugins) {
				for (var id in plugins) {
					var p = plugins[id];
					if (p.actions) {
						list.push({title:"-",type:'separator'});
						$.merge(list, p.actions);
					}
				}
			}
			var downloadActions = [];
			var firstDownload = -1;
			for (var i=0,j=list.length; i<j; i++) {
				var a = list[i];
				if (a.group == 'download') {
					if (firstDownload < 0) firstDownload = i;
					downloadActions.push(a);
				}
			}
			if (downloadActions.length > 1) {
				for (var i=1,j=downloadActions.length; i<j; i++) list.remove(downloadActions[i]); 
				list[firstDownload] = {
					type: "submenu",
					items: downloadActions,
					primary: downloadActions[0]
				};
			}
			return list;
		},
	
		getPrimaryActions : function(actions) {
			if (!actions) return [];
			var result = [];
			var downloadActions = [];
			for (var i=0,j=actions.length; i<j; i++) {
				var a = actions[i];
				if (a.type == 'primary') result.push(a);
			}
			return result;
		},

		getSecondaryActions : function(actions) {
			if (!actions) return [];
			var result = [];
			for (var i=0,j=actions.length; i<j; i++) {
				var a = actions[i];
				if (a.id == 'download' || a.type == 'primary') continue;				
				result.push(a);
			}
			return mollify.helpers.cleanupActions(result);
		},
		
		cleanupActions : function(actions) {
			if (!actions) return [];				
			var last = -1;
			for (var i=actions.length-1,j=0; i>=j; i--) {
				var a = actions[i];
				if (a.type != 'separator' && a.title != '-') {
					last = i;
					break;
				}
			}
			if (last < 0) return [];
			
			var first = -1;
			for (var i2=0; i2<=last; i2++) {
				var a2 = actions[i2];
				if (a2.type != 'separator' && a2.title != '-') {
					first = i2;
					break;
				}
			}
			actions = actions.splice(first, (last-first)+1);
			var prevSeparator = false;
			for (var i3=actions.length-1,j2=0; i3>=j2; i3--) {
				var a3 = actions[i3];
				var separator = (a3.type == 'separator' || a3.title == '-');
				if (separator && prevSeparator) actions.splice(i3, 1);
				prevSeparator = separator;
			}
			
			return actions;
		},
		
		urlWithParam : function(url, param) {
			return url + (window.strpos(url, "?") ? "&" : "?") + param;
		},
		
		noncachedUrl : function(url) {
			return mollify.helpers.urlWithParam(url, "_="+mollify._time);
		},
	
		formatDateTime : function(time, fmt) {
			return time.format(fmt);
		},
		
		parseInternalTime : function(time) {
			var ts = new Date();
			ts.setYear(time.substring(0,4));
			ts.setMonth(time.substring(4,6) - 1);
			ts.setDate(time.substring(6,8));
			ts.setHours(time.substring(8,10));
			ts.setMinutes(time.substring(10,12));
			ts.setSeconds(time.substring(12,14));
			return ts;
		},
	
		formatInternalTime : function(time) {
			if (!time) return null;
			return time.format('yymmddHHMMss', time);
		}
	};

	window.mollify = mollify;

	/* Common */
	
	window.isArray = function(o) {
		return Object.prototype.toString.call(o) === '[object Array]';
	}
	
	if(typeof String.prototype.trim !== 'function') {
		String.prototype.trim = function() {
			return this.replace(/^\s+|\s+$/g, ''); 
		}
	}
	
	if(typeof String.prototype.startsWith !== 'function') {
		String.prototype.startsWith = function(s) {
			if (!s || s.length === 0) return false;
			return this.substring(0, s.length) == s; 
		}
	}
	
	window.def = function(o) {
		return (typeof(o) != 'undefined');
	}
	
	if (!Array.prototype.indexOf) { 
		Array.prototype.indexOf = function(obj, start) {
			for (var i = (start || 0), j = this.length; i < j; i++) {
				if (this[i] === obj) { return i; }
			}
			return -1;
		}
	}
	
	if (!Array.prototype.remove) { 
		Array.prototype.remove = function(from, to) {
			if (typeof(to) == 'undefined' && typeof(from) == 'object')
				from = this.indexOf(from);
			var rest = this.slice((to || from) + 1 || this.length);
			this.length = from < 0 ? this.length + from : from;
			return this.push.apply(this, rest);
		};
	}
	
	window.strpos = function(haystack, needle, offset) {
		// Finds position of first occurrence of a string within another  
		// 
		// version: 1109.2015
		// discuss at: http://phpjs.org/functions/strpos
		// +   original by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
		// +   improved by: Onno Marsman    
		// +   bugfixed by: Daniel Esteban
		// +   improved by: Brett Zamir (http://brett-zamir.me)
		var i = (haystack + '').indexOf(needle, (offset || 0));
		return i === -1 ? false : i;
	}
	
	/**
	*
	*  Base64 encode / decode
	*  http://www.webtoolkit.info/
	*
	**/
	 
	window.Base64 = {
	 
		// private property
		_keyStr : "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",
	 
		// public method for encoding
		encode : function (input) {
			var output = "";
			var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
			var i = 0;
	 
			input = window.Base64._utf8_encode(input);
	 
			while (i < input.length) {
	 
				chr1 = input.charCodeAt(i++);
				chr2 = input.charCodeAt(i++);
				chr3 = input.charCodeAt(i++);
	 
				enc1 = chr1 >> 2;
				enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
				enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
				enc4 = chr3 & 63;
	 
				if (isNaN(chr2)) {
					enc3 = enc4 = 64;
				} else if (isNaN(chr3)) {
					enc4 = 64;
				}
	 
				output = output +
				this._keyStr.charAt(enc1) + this._keyStr.charAt(enc2) +
				this._keyStr.charAt(enc3) + this._keyStr.charAt(enc4);
	 
			}
	 
			return output;
		},
	 
		// public method for decoding
		decode : function (input) {
			var output = "";
			var chr1, chr2, chr3;
			var enc1, enc2, enc3, enc4;
			var i = 0;
	 
			input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");
	 
			while (i < input.length) {
	 
				enc1 = this._keyStr.indexOf(input.charAt(i++));
				enc2 = this._keyStr.indexOf(input.charAt(i++));
				enc3 = this._keyStr.indexOf(input.charAt(i++));
				enc4 = this._keyStr.indexOf(input.charAt(i++));
	 
				chr1 = (enc1 << 2) | (enc2 >> 4);
				chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
				chr3 = ((enc3 & 3) << 6) | enc4;
	 
				output = output + String.fromCharCode(chr1);
	 
				if (enc3 != 64) {
					output = output + String.fromCharCode(chr2);
				}
				if (enc4 != 64) {
					output = output + String.fromCharCode(chr3);
				}
	 
			}
	 
			output = window.Base64._utf8_decode(output);
	 
			return output;
	 
		},
	 
		// private method for UTF-8 encoding
		_utf8_encode : function (string) {
			string = string.replace(/\r\n/g,"\n");
			var utftext = "";
	 
			for (var n = 0; n < string.length; n++) {
	 
				var c = string.charCodeAt(n);
	 
				if (c < 128) {
					utftext += String.fromCharCode(c);
				}
				else if((c > 127) && (c < 2048)) {
					utftext += String.fromCharCode((c >> 6) | 192);
					utftext += String.fromCharCode((c & 63) | 128);
				}
				else {
					utftext += String.fromCharCode((c >> 12) | 224);
					utftext += String.fromCharCode(((c >> 6) & 63) | 128);
					utftext += String.fromCharCode((c & 63) | 128);
				}
	 
			}
	 
			return utftext;
		},
	 
		// private method for UTF-8 decoding
		_utf8_decode : function (utftext) {
			var string = "";
			var i = 0;
			var c = 0, c1 = 0, c2 = 0;
	 
			while ( i < utftext.length ) {
	 
				c = utftext.charCodeAt(i);
	 
				if (c < 128) {
					string += String.fromCharCode(c);
					i++;
				}
				else if((c > 191) && (c < 224)) {
					c2 = utftext.charCodeAt(i+1);
					string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
					i += 2;
				}
				else {
					c2 = utftext.charCodeAt(i+1);
					var c3 = utftext.charCodeAt(i+2);
					string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
					i += 3;
				}
	 
			}
	 
			return string;
		}
	}
}(window.jQuery);