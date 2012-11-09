<#assign uniqueId = random.nextInt(1000000)?c />

<div align="${align}" id="audio_${uniqueId}"></div>

<#if altplayer?default("") = "true">
<script type="text/javascript">
Event.onDOMReady(function(){
  var makeFlash = function() {
    object${uniqueId} = new FlashObject("${alternatePlayerLocation}",'object${uniqueId}',"${width?default(290)?number}","${height?default(24)?number}");
    object${uniqueId}.addParam("wmode","transparent");
    object${uniqueId}.addParam("menu","false");
    object${uniqueId}.addParam("play","true");
    object${uniqueId}.addParam("FlashVars","playerID=1&amp;bg=0xf8f8f8&amp;leftbg=0xeeeeee&amp;lefticon=0x666666"
            + "&amp;rightbg=0xcccccc&amp;rightbghover=0x999999&amp;righticon=666666"
            + "&amp;righticonhover=0xFFFFFF&amp;text=0x666666&amp;slider=0x666666"
            + "&amp;track=0xFFFFFF&amp;border=0x666666&amp;loader=0x9FFFB8" 
            + "&amp;soundFile=${url}");
    object${uniqueId}.align = "${align}";
    object${uniqueId}.write("audio_${uniqueId}");
  };

<#if useNativeElements>
  if (NativeAudio.canPlayType('${mimeType}')) {
    var p = new LongPlayer(new NativeAudio('${url}'), "audio_${uniqueId}");
    p.setWidth = ${width?default(290)?number};
  } else {
    makeFlash();
  }
<#else>
  makeFlash();
</#if>

});
</script>
<#else>
<script type="text/javascript">
Event.onDOMReady(function(){
  var makeFlash = function() {
    object${uniqueId} = new FlashObject("${playerLocation}?theFile=${url}",'object${uniqueId}',"${width?default(16)?number}","${height?default(16)?number}");
    object${uniqueId}.addParam("wmode","transparent");
    object${uniqueId}.addParam("bgcolor","#FFFFFF");
    object${uniqueId}.align = "${align}";
    object${uniqueId}.write("audio_${uniqueId}");
  };
  
<#if useNativeElements>
  if (NativeAudio.canPlayType('${mimeType}')) {
    new ButtonPlayer(new NativeAudio('${url}'), "audio_${uniqueId}");
  } else {
    makeFlash();
  }
<#else>
  makeFlash();
</#if>

});
</script>
</#if>

<#if download?exists && download = 'true'>
<a href="${url}?forceOpenSave=true">Download</a>
</#if>