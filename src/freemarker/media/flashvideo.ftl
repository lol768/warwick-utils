<#assign uniqueId = random.nextInt(1000000)?c />
<#macro dimension value append=0><#compress>
	<#if value?string?ends_with("%")>
		${value}
	<#elseif value?string != 'auto'>
		${(value?number + append)?c}
	</#if>
</#compress></#macro>
<#if (title?default('') = '')>

<div align="${align}" id="video_${uniqueId}"></div>

<script type="text/javascript">
Event.onDOMReady(function(){
  object${uniqueId} = new FlashObject("${url}",'object${uniqueId}',"<@dimension value=width?default(640) />","<@dimension value=height?default(360) />");
  object${uniqueId}.addParam("wmode","transparent");
  object${uniqueId}.addParam("allowfullscreen","true");
  object${uniqueId}.align = "${align}";
  object${uniqueId}.write("video_${uniqueId}");
});
</script>
<#else>
<a href="${url}?forceOpenSave=true" id="playVideo_${uniqueId}">${title}</a>

<head>
	<script type="text/javascript">
	Event.onDOMReady(function() {
		addEvent(document.getElementById('playVideo_${uniqueId}'), 'click', function(evt) {
			var insertFlash = function(id) {
			  object${uniqueId} = new FlashObject("${url}",'object${uniqueId}',"<@dimension value=width?default(640) />","<@dimension value=height?default(360) />");
			  object${uniqueId}.addParam("wmode","transparent");
			  object${uniqueId}.addParam("allowfullscreen","true");
			  object${uniqueId}.align = "${align}";
			  object${uniqueId}.write("video_${uniqueId}");
			};
			
			var container = document.createElement('div');
			container.setAttribute('id', 'video_${uniqueId}');
			container.setAttribute('style', 'position:fixed;z-index:5050;top:50%;left:50%;padding:15px;background:white;-webkit-border-radius:15px;-moz-border-radius:15px;border-radius:15px;-moz-box-shadow: 0px 5px 25px rgba(0,0,0,0.5);-webkit-box-shadow: 0px 5px 25px rgba(0,0,0,0.5);box-shadow: 0px 5px 25px rgba(0,0,0,0.5);border:1px solid #CCC;visibility:hidden;');
			
			var overlay = document.createElement('div');
			overlay.setAttribute('id', 'overlay_${uniqueId}');
			overlay.setAttribute('style', 'position:fixed;top:0;left:0;width:100%;height:100%;z-index:5000;background-color:#000;-moz-opacity: 0.0;opacity:0.0;filter:alpha(opacity=0);-webkit-transition: opacity 0.3s;-moz-transition: opacity 0.3s;transition: opacity 0.3s;');
			
			var closeFn = function(evt) {
				container.parentNode.removeChild(container);
	
				overlay.setAttribute('style',overlay.getAttribute('style') + '-moz-opacity: 0.0;opacity:.0;filter:alpha(opacity=0);');
				setTimeout(function() { overlay.parentNode.removeChild(overlay); }, 1000);
	
				if (evt.preventDefault) {
					evt.preventDefault();
				} else {
					evt.stop();
				}
				
				return false;
			};
			addEvent(overlay, 'click', closeFn);
	
			document.body.appendChild(overlay);
			document.body.appendChild(container);
			
			insertFlash('video_${uniqueId}');
			
			<#if description?default('') != ''>
				var p = document.createElement('p');
				p.innerHTML = '${description?js_string}';
			
				container.appendChild(p);
			</#if>
				
			<#if closeButtonImgUrl?default('') != ''>
				var img = document.createElement('img');
				img.setAttribute('src', '${closeButtonImgUrl}');
				img.setAttribute('width', 30);
				img.setAttribute('height', 30);
				img.setAttribute('style', 'cursor:pointer;position:absolute;top:-12px;right:-12px;z-index:5100;');
				
				container.insertBefore(img, container.firstChild);
				
				addEvent(img, 'click', closeFn);
			</#if>

			container.style.marginLeft = '-' + Math.round(container.getWidth() / 2) + 'px';
			container.style.marginTop = '-' + Math.round(container.getHeight() / 2) + 'px';
			container.style.visibility = 'visible';
	
			// Look for the ESC key press and hide the window
			var escObserver = function(evt) {
				if (evt.keyCode == 27 /* KEY_ESC */) {
					closeFn(evt);
					
					if (document.removeEventListener) {
				        document.removeEventListener('keydown', escObserver, false);
				    } else {
				        document.detachEvent("onkeydown", escObserver);
				    }
				}
			}.bind(this);
			addEvent(document, 'keydown', escObserver);
	
			overlay.setAttribute('style',overlay.getAttribute('style') + '-moz-opacity: 0.9;opacity:.9;filter:alpha(opacity=90);');
	
			if (evt.preventDefault) {
				evt.preventDefault();
			} else {
				evt.stop();
			}
			
			return false;
		});
	});
	</script>
</head>
</#if>