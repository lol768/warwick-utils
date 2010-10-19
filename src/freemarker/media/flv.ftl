<#assign uniqueId = random.nextInt(1000000)?c />
<#macro dimension value append=0><#compress>
	<#if value?string?ends_with("%")>
		${value}
	<#elseif value?string != 'auto'>
		${(value?number + append)?c}
	</#if>
</#compress></#macro>

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
  <#if newPlayer && !url?starts_with('rtmp://')>
    object${uniqueId} = new FlashObject("${playerLocation}","obj${uniqueId}","<@dimension value=width?default(425) />","<@dimension value=height?default(350) append=20 />");
    object${uniqueId}.addVariable("file", url);
    <#if previewimage?default("") != ''>
      object${uniqueId}.addVariable("image", "${previewimage?default("")}");
    </#if>
    object${uniqueId}.addVariable("stretching", "${stretching?default("fill")}");
  <#else>
    object${uniqueId} = new FlashObject("${playerLocation}?autoStart=false&file="+ url +"<#if previewimage?default("") != ''>&image=${previewimage?default("")}</#if>&overstretch=true","obj${uniqueId}","<@dimension value=width?default(425) />","<@dimension value=height?default(350) append=20 />");
  </#if>
    object${uniqueId}.addParam("wmode","transparent");
    object${uniqueId}.addVariable("width","<@dimension value=width?default(425) />");
    object${uniqueId}.addVariable("height","<@dimension value=height?default(350) append=20 />");
    object${uniqueId}.addParam("allowfullscreen","true");
    object${uniqueId}.addVariable("showdownload","false");
    object${uniqueId}.align = "${align}";
    object${uniqueId}.write('video_${uniqueId}');
  };

	<#if mime_type?default('') == 'video/mp4' || url?ends_with('.mp4') || mime_type?default('') == 'video/x-m4v' || url?ends_with('.m4v')>
	  
		/* Attempt HTML5 Video */ 
		var vidEl = new Element('video', {
			width: '<@dimension value=width?default(425) />',
			height: '<@dimension value=height?default(350) />',
			<#if previewimage?default("") != ''>poster: "${previewimage?default("")}",</#if>
			controls: 'controls',
			preload: 'meta',
			autoplay: 'autoplay'
		});
		var supportsVideo = !!vidEl.canPlayType;
		var supportsCodec = supportsVideo && vidEl.canPlayType('video/mp4');
		
		if (supportsCodec) {
		  var posterImage = new Element('img', {
		    src: '${previewimage?default("")}',
		    title: 'Click to play'
		  });
		  var container = $('video_${uniqueId}');
  		  container.insert(posterImage);
      	var startVideo = function(event){
        vidEl.insert(new Element('source', {
          src: '${url}',
          type: '${mime_type?default('video/mp4')}',
          width: '<@dimension value=width?default(425) />',
          height: '<@dimension value=height?default(350) />'
        }));
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