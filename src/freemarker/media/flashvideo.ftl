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
  object${uniqueId}.align = "${align}";
  object${uniqueId}.write("video_${uniqueId}");
});
</script>
<#else>
<a href="${url}?forceOpenSave=true" id="playVideo_${uniqueId}">${title}</a>

<head>
	<script type="text/javascript">
	Event.onDOMReady(function() {
		$('playVideo_${uniqueId}').observe('click', function(evt) {
			var insertFlash = function(id) {
			  object${uniqueId} = new FlashObject("${url}",'object${uniqueId}',"<@dimension value=width?default(640) />","<@dimension value=height?default(360) />");
			  object${uniqueId}.addParam("wmode","transparent");
			  object${uniqueId}.align = "${align}";
			  object${uniqueId}.write("video_${uniqueId}");
			};
			
			var container = new Element('div', { id: 'video_${uniqueId}', style: 'position:fixed;z-index:5050;top:50%;left:50%;padding:15px;background:white;-webkit-border-radius:15px;-moz-border-radius:15px;border-radius:15px;-moz-box-shadow: 0px 5px 25px rgba(0,0,0,0.5);-webkit-box-shadow: 0px 5px 25px rgba(0,0,0,0.5);box-shadow: 0px 5px 25px rgba(0,0,0,0.5);border:1px solid #CCC;visibility:hidden;' });
			var overlay = new Element('div', { id: 'overlay_${uniqueId}', style: 'position:fixed;top:0;left:0;width:100%;height:100%;z-index:5000;background-color:#000;-moz-opacity: 0.0;opacity:0.0;filter:alpha(opacity=0);-webkit-transition: opacity 0.3s;-moz-transition: opacity 0.3s;transition: opacity 0.3s;' });
			
			var closeFn = function(evt) {			
				container.remove();
	
				overlay.setAttribute('style',overlay.getAttribute('style') + '-moz-opacity: 0.0;opacity:.0;filter:alpha(opacity=0);');
				setTimeout(function() { overlay.remove(); }, 1000);
	
				evt.stop();
			};
			overlay.observe('click', closeFn);
	
			document.body.insert(overlay);
			document.body.insert(container);
			
			insertFlash('video_${uniqueId}');
			
			<#if description?default('') != ''>
				container.insert(new Element('p').update('${description?js_string}'));
			</#if>
				
			<#if closeButtonImgUrl?default('') != ''>
				container.insert({ top:
					new Element('img', { src: '${closeButtonImgUrl}', width: 30, height: 30, style: 'cursor:pointer;position:absolute;top:-12px;right:-12px;z-index:5100;' })
					.observe('click', closeFn)
				});
			</#if>

			container.style.marginLeft = '-' + Math.round(container.getWidth() / 2) + 'px';
			container.style.marginTop = '-' + Math.round(container.getHeight() / 2) + 'px';
			container.style.visibility = 'visible';
	
			// Look for the ESC key press and hide the window
			var escObserver = function(evt) {
				if (evt.keyCode == Event.KEY_ESC) {
					closeFn(evt);
					document.stopObserving('keydown',escObserver);
				}
			}.bind(this);
			document.observe('keydown', escObserver);
	
			overlay.setAttribute('style',overlay.getAttribute('style') + '-moz-opacity: 0.6;opacity:.6;filter:alpha(opacity=60);');
	
			evt.stop();
		});
	});
	</script>
</head>
</#if>