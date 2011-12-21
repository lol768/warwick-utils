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
		<div id="video_${uniqueId}" class="media_tag_video">
			<video id="html5video_${uniqueId}" width="<@dimension value=width?default(425) />" height="<@dimension value=height?default(350) append=24 />" <#if previewimage?default("") != ''>poster="${previewimage?default("")}"</#if> controls="controls" preload="metadata">
				<source src="${url}" type="${mime_type?default('video/mp4')}" width="<@dimension value=width?default(425) />" height="<@dimension value=height?default(350) />" />
				<#if alternateRenditions?exists>
					<#list alternateRenditions?keys as mime>
						<source src="${alternateRenditions[mime]}" type="${mime}" width="<@dimension value=width?default(425) />" height="<@dimension value=height?default(350) />"
					</#list>
				</#if>
			</video>
		</div>
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
				var vidEl = document.getElementById('html5video_${uniqueId}');
				var supportsVideo = !!vidEl.canPlayType;
			
				var mimeType = '${mime_type?default('video/mp4')}';
				/* For MP4, check correctly with the codecs UTL-112 */
				if (mimeType == 'video/mp4'){
					mimeType = 'video/mp4; codecs="avc1.42E01E, mp4a.40.2"';
				}
			
				var supportsCodec = supportsVideo && (vidEl.canPlayType(mimeType)<#if alternateRenditions?exists><#list alternateRenditions?keys as mime> || vidEl.canPlayType('${mime}')</#list></#if>);
			
				if (supportsCodec && supportsCodec != 'maybe') {
			  
			  		var fallbackContainer = document.getElementById('video_${uniqueId}');
			  		var div = document.createElement('div');
		  	  		div.setAttribute('class', 'media_tag_play');
		  	  		fallbackContainer.appendChild(div);  	 
	          		fallbackContainer.parentNode.insertBefore(vidEl, fallbackContainer.nextSibling);
	          		vidEl.appendChild(fallbackContainer);
	          		insertFlash();
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
				addEvent(document.getElementById('playVideo_${uniqueId}'), 'click', function(evt) {
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
			
					<#if mime_type?default('') == 'video/mp4' || url?ends_with('.mp4') || mime_type?default('') == 'video/x-m4v' || url?ends_with('.m4v') || mime_type?default('') == 'video/webm' || url?ends_with('.webm')>
						/* Attempt HTML5 Video */ 
						var vidEl = document.createElement('video');
						vidEl.setAttribute('width', '<@dimension value=width?default(425) />');
						vidEl.setAttribute('height', '<@dimension value=height?default(350) />');
						<#if previewimage?default("") != ''>
							vidEl.setAttribute('poster', "${previewimage?default("")}");
						</#if>
						vidEl.setAttribute('controls', 'controls');
						vidEl.setAttribute('autoplay', 'autoplay');
				
						var source = document.createElement('source');
	      				source.setAttribute('src', '${url}');
			      		source.setAttribute('type', '${mime_type?default('video/mp4')}');
			      		source.setAttribute('width', '<@dimension value=width?default(425) />');
			      		source.setAttribute('height', '<@dimension value=height?default(350) />');
	      		
		        		vidEl.appendChild(source);
	        
				        <#if alternateRenditions?exists>
				        	<#list alternateRenditions?keys as mime>{
				        		var altSource = document.createElement('source');
					      		altSource.setAttribute('src', '${alternateRenditions[mime]}');
					      		altSource.setAttribute('type', '${mime}');
					      		altSource.setAttribute('width', '<@dimension value=width?default(425) />');
					      		altSource.setAttribute('height', '<@dimension value=height?default(350) />');
				        	
				        		vidEl.<#if mime == 'video/mp4' || mime == 'video/x-m4v'>insertBefore<#else>appendChild</#if>(altSource<#if mime == 'video/mp4' || mime == 'video/x-m4v'>, vidEl.firstChild</#if>);
				        	}</#list>
				        </#if>
		        
						var supportsVideo = !!vidEl.canPlayType;
				
						var mimeType = '${mime_type?default('video/mp4')}';
						/* For MP4, check correctly with the codecs UTL-112 */
						if (mimeType == 'video/mp4'){
							mimeType = 'video/mp4; codecs="avc1.42E01E, mp4a.40.2"';
						}
				
						var supportsCodec = supportsVideo && (vidEl.canPlayType(mimeType)<#if alternateRenditions?exists><#list alternateRenditions?keys as mime> || vidEl.canPlayType('${mime}')</#list></#if>);
							
						if (supportsCodec && supportsCodec != 'maybe') {
							container.appendChild(vidEl);
							var fallbackContainer = document.createElement('div');
							fallbackContainer.setAttribute('id', 'videoFallback_${uniqueId}');
							vidEl.appendChild(fallbackContainer);	
							insertFlash('videoFallback_${uniqueId}');
						} else {
							insertFlash('video_${uniqueId}');
						}
					<#else>
						<#-- FLV Video -->
						insertFlash('video_${uniqueId}');
					</#if>
			
					<#if description?default('') != ''>
						var p = document.createElement('p');
						p.innerHTML = '${description?js_string}';
						container.appendChild(p);
					</#if>
			
					<#if download?exists && download = 'true'>
						var p = document.createElement('p');
						p.innerHTML = "<a href='${url}?forceOpenSave=true'>Download</a>";
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

					var marginLeft, marginTop;
					// id6 - use jQuery
					if(typeof jQuery != "undefined"){
						marginLeft = Math.round(jQuery(container).width() / 2);
						marginTop = Math.round(jQuery(container).height() / 2);
					} 
					// id5 - use Prototype
					else {
						marginLeft = Math.round(container.getWidth() / 2);
						marginTop = Math.round(container.getHeight() / 2);
					}

					container.style.marginLeft = '-' + marginLeft + 'px';
					container.style.marginTop = '-' + marginTop + 'px';
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