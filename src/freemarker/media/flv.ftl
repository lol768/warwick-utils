<#assign uniqueId = random.nextInt(1000000)?c />
<#macro dimension value append=0><#compress>
	<#if value?string?ends_with("%")>
		${value}
	<#elseif value?string != 'auto'>
		${(value?number + append)?c}
	</#if>
</#compress></#macro>

<#if (title?default('') = '')>
<div align="${align}">
	<div id="video_${uniqueId}" class="media_tag_video"></div>
</div>

<script type="text/javascript">
Event.onDOMReady(function(){

  var insertFlash = function() {
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
  <#if newPlayer>
    object${uniqueId} = new FlashObject("${playerLocation}","obj${uniqueId}","<@dimension value=width?default(425) />","<@dimension value=height?default(350) append=24 />");
    object${uniqueId}.addVariable("file", url);
    <#if previewimage?default("") != ''>
      object${uniqueId}.addVariable("image", "${previewimage?default("")}");
    </#if>
    object${uniqueId}.addVariable("stretching", "${stretching?default("fill")}");
  <#else>
    object${uniqueId} = new FlashObject("${playerLocation}?autoStart=false&file="+ url +"<#if previewimage?default("") != ''>&image=${previewimage?default("")}</#if>&overstretch=true","obj${uniqueId}","<@dimension value=width?default(425) />","<@dimension value=height?default(350) append=24 />");
  </#if>
    object${uniqueId}.addParam("wmode","transparent");
    object${uniqueId}.addVariable("width","<@dimension value=width?default(425) />");
    object${uniqueId}.addVariable("height","<@dimension value=height?default(350) append=24 />");
    object${uniqueId}.addParam("allowfullscreen","true");
    object${uniqueId}.addVariable("showdownload","false");
    object${uniqueId}.align = "${align}";
    object${uniqueId}.write('video_${uniqueId}');
  };

	<#if mime_type?default('') == 'video/mp4' || url?ends_with('.mp4') || mime_type?default('') == 'video/x-m4v' || url?ends_with('.m4v') || mime_type?default('') == 'video/webm' || url?ends_with('.webm')>
	  
		/* Attempt HTML5 Video */ 
		var vidEl = new Element('video', {
			width: '<@dimension value=width?default(425) />',
			height: '<@dimension value=height?default(350) />',
			<#if previewimage?default("") != ''>poster: "${previewimage?default("")}",</#if>
			controls: 'controls',
			autoplay: 'autoplay'
		});
		var supportsVideo = !!vidEl.canPlayType;
		var supportsCodec = supportsVideo && (vidEl.canPlayType('${mime_type?default('video/mp4')}')<#if alternateRenditions?exists><#list alternateRenditions?keys as mime> || vidEl.canPlayType('${mime}')</#list></#if>);
		
		if (supportsCodec) {
		  <#if previewimage?default("") != ''>
		  var posterImage = new Element('img', {
		    src: '${previewimage?default("")}',
		    title: 'Click to play',
          	width: '<@dimension value=width?default(425) />',
          	height: '<@dimension value=height?default(350) />'
		  });
		  <#else>
		  var posterImage = new Element('div', {
		    style: 'background-color: #000000; width: <@dimension value=width?default(425) />px; height: <@dimension value=height?default(350) />px;',
		    title: 'Click to play'
		  });
		  </#if>
		  var container = $('video_${uniqueId}');
  		  container.insert(posterImage);
      	var startVideo = function(event){      	
	        vidEl.insert(new Element('source', {
	          src: '${url}',
	          type: '${mime_type?default('video/mp4')}',
	          width: '<@dimension value=width?default(425) />',
	          height: '<@dimension value=height?default(350) />'
	        }));
	        
	        <#if alternateRenditions?exists>
	        	<#list alternateRenditions?keys as mime>
	        		vidEl.insert(<#if mime == 'video/mp4' || mime == 'video/x-m4v'>{top:</#if>new Element('source', {
			          src: '${alternateRenditions[mime]}',
			          type: '${mime}',
			          width: '<@dimension value=width?default(425) />',
			          height: '<@dimension value=height?default(350) />'
			        })<#if mime == 'video/mp4' || mime == 'video/x-m4v'>}</#if>);		
	        	</#list>
	        </#if>
	        
	        // Place video in container's place, with container inside the video as fallback
	        posterImage.remove();
	        container.insert({after: vidEl});
	        vidEl.insert(container);
	        insertFlash();
      	};
      	
      	container.insert(new Element('div', { className: 'media_tag_play' }).observe('click', startVideo));
		  posterImage.observe('click', startVideo);		
		} else {
		  insertFlash();
		}
	<#else>
		<#-- FLV Video -->
		insertFlash();
	</#if>
});
</script>

<#if download?exists && download = 'true'>
<a href="${url}?forceOpenSave=true">Download</a>
</#if>

<#else>
<a href="${url}?forceOpenSave=true" id="playVideo_${uniqueId}">${title}</a>

<head>
	<script type="text/javascript">
	Event.onDOMReady(function() {
		$('playVideo_${uniqueId}').observe('click', function(evt) {
			var insertFlash = function(id) {
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
			  <#if newPlayer>
			    object${uniqueId} = new FlashObject("${playerLocation}","obj${uniqueId}","<@dimension value=width?default(425) />","<@dimension value=height?default(350) append=24 />");
			    object${uniqueId}.addVariable("file", url);
			    <#if previewimage?default("") != ''>
			      object${uniqueId}.addVariable("image", "${previewimage?default("")}");
			    </#if>
			    object${uniqueId}.addVariable("stretching", "${stretching?default("fill")}");
			  <#else>
			    object${uniqueId} = new FlashObject("${playerLocation}?autoStart=false&file="+ url +"<#if previewimage?default("") != ''>&image=${previewimage?default("")}</#if>&overstretch=true","obj${uniqueId}","<@dimension value=width?default(425) />","<@dimension value=height?default(350) append=24 />");
			  </#if>
			    object${uniqueId}.addVariable("autostart", "true");
			    object${uniqueId}.addParam("wmode","transparent");
			    object${uniqueId}.addVariable("width","<@dimension value=width?default(425) />");
			    object${uniqueId}.addVariable("height","<@dimension value=height?default(350) append=24 />");
			    object${uniqueId}.addParam("allowfullscreen","true");
			    object${uniqueId}.addVariable("showdownload","false");
			    object${uniqueId}.align = "${align}";
			    object${uniqueId}.write(id);
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
			
			<#if mime_type?default('') == 'video/mp4' || url?ends_with('.mp4') || mime_type?default('') == 'video/x-m4v' || url?ends_with('.m4v') || mime_type?default('') == 'video/webm' || url?ends_with('.webm')>
				/* Attempt HTML5 Video */ 
				var vidEl = new Element('video', {
					width: '<@dimension value=width?default(425) />',
					height: '<@dimension value=height?default(350) />',
					<#if previewimage?default("") != ''>poster: "${previewimage?default("")}",</#if>
					controls: 'controls',
					autoplay: 'autoplay'
				});
				
				vidEl.insert(new Element('source', {
		          	src: '${url}',
		          	type: '${mime_type?default('video/mp4')}',
		          	width: '<@dimension value=width?default(425) />',
		          	height: '<@dimension value=height?default(350) />'
		        }));
	        
		        <#if alternateRenditions?exists>
		        	<#list alternateRenditions?keys as mime>
		        		vidEl.insert(<#if mime == 'video/mp4' || mime == 'video/x-m4v'>{top:</#if>new Element('source', {
				          src: '${alternateRenditions[mime]}',
				          type: '${mime}',
				          width: '<@dimension value=width?default(425) />',
				          height: '<@dimension value=height?default(350) />'
				        })<#if mime == 'video/mp4' || mime == 'video/x-m4v'>}</#if>);		
		        	</#list>
		        </#if>
		        
				var supportsVideo = !!vidEl.canPlayType;
				var supportsCodec = supportsVideo && (vidEl.canPlayType('${mime_type?default('video/mp4')}')<#if alternateRenditions?exists><#list alternateRenditions?keys as mime> || vidEl.canPlayType('${mime}')</#list></#if>);
								
				if (supportsCodec) {
					container.update(vidEl.insert(new Element('div', { id: 'videoFallback_${uniqueId}' })));
					insertFlash('videoFallback_${uniqueId}');
				} else {
					insertFlash('video_${uniqueId}');
				}
			<#else>
				<#-- FLV Video -->
				insertFlash('video_${uniqueId}');
			</#if>
			
			<#if description?default('') != ''>
				container.insert(new Element('p').update('${description?js_string}'));
			</#if>
			
			<#if download?exists && download = 'true'>
				container.insert(new Element('p').update(new Element('a', { href: '${url}?forceOpenSave=true' }).update('Download')));
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
	
			overlay.setAttribute('style',overlay.getAttribute('style') + '-moz-opacity: 0.9;opacity:.9;filter:alpha(opacity=90);');
	
			evt.stop();
		});
	});
	</script>
</head>
</#if>