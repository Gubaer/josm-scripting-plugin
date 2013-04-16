/*
 * js functions used in the documentation site for the scritping
 * plugin
 */
 (function($){
	$(document).ready(function() {
		$("a[data-js-object]").click(function() {
			//alert($(this).data("js-object"));
			var target = $(this).data("js-object");
			var l = target.split(":");
			var kind = l[0];
			var object = l[1].replace(/\//g, "_");

			var link;
			switch(kind) {
				case "module": link = "modules/" + object; break;
				case "namespace": link = "namespaces/" + object; break;
				case "class": link = "classes/" + object; break;
				case "mixin": link = "mixins/" + object; break;
			}
			link = "../apidoc/" + link + ".html";
			document.location.href = link;
		});

		$("a[data-josm-class]").click(function() {
			var cls = $(this).data("josm-class");
			cls = cls.replace(/\./g, "/");
			var link = "http://josm.openstreetmap.de/doc/" + cls + ".html";
			document.location.href = link;
		});
	});
 }(jQuery));