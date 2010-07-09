<#assign uniqueId = random.nextInt(1000000)?c />

<div align="${align}">
	<div id="video_${uniqueId}"></div>
</div>

<script type="text/javascript">
Event.onDOMReady(function(){
	<#if mime_type?default('') == 'video/mp4' || url?ends_with('.mp4')>
		/* Attempt HTML5 Video */ 
		var vidEl = new Element('video', {
			width: ${width?default(425)?number?c},
			height: ${height?default(350)?number?c},
			<#if previewimage?default("") != ''>poster: "${previewimage?default("")}",</#if>
			controls: 'controls',
			preload: 'meta'
		});
		var supportsVideo = !!vidEl.canPlayType;
		var supportsCodec = supportsVideo && vidEl.canPlayType('video/mp4');
		
		if (supportsVideo && supportsCodec) {
			vidEl.insert(new Element('source', {
				src: '${url}',
				type: '${mime_type?default('video/mp4')}'
			}));
		
			// Replace the div element with the video element (keeping the div as fallback)
			var div = $('video_${uniqueId}');
			div.insert({after: vidEl});
			vidEl.insert(div.remove());
		}
	</#if>

	<#if fallback?exists>
	  // check for H.264 version
	  <#if !fallbackVersion?exists>
	  	<#assign fallbackVersion = '9,0,115' />
	  </#if>
	  
	  if (FlashVersionDetector.isClientHasVersionString('${fallbackVersion}')) {
	  	url = '${url}'.toAbsoluteUrl();
	  } else {
	  	url = '${fallback}'.toAbsoluteUrl();
	  }
	<#else>
	  url = '${url}'.toAbsoluteUrl();
	</#if>
	<#if newPlayer && !url?starts_with('rtmp://')>
	  object${uniqueId} = new FlashObject("${playerLocation}","obj${uniqueId}","${width?default(425)?number?c}","${(height?default(350)?number + 20)?c}");
	  object${uniqueId}.addVariable("file", url);
	  <#if previewimage?default("") != ''>
	  	object${uniqueId}.addVariable("image", "${previewimage?default("")}");
	  </#if>
	  object${uniqueId}.addVariable("stretching", "${stretching?default("fill")}");
	<#else>
	  object${uniqueId} = new FlashObject("${playerLocation}?autoStart=false&file="+ url +"<#if previewimage?default("") != ''>&image=${previewimage?default("")}</#if>&overstretch=true","obj${uniqueId}","${width?default(425)?number?c}","${(height?default(350)?number + 20)?c}");
	</#if>
	  object${uniqueId}.addParam("wmode","transparent");
	  object${uniqueId}.addVariable("width",${width?default(425)?number?c});
	  object${uniqueId}.addVariable("height",${(height?default(350)?number + 20)?c});
	  object${uniqueId}.addParam("allowfullscreen","true");
	  object${uniqueId}.addVariable("showdownload","false");
	  object${uniqueId}.align = "${align}";
	  object${uniqueId}.write('video_${uniqueId}');
});
</script>

<#if download?exists && download = 'true'>
<a href="${url}?forceOpenSave=true">Download</a>
</#if>