<#assign uniqueId = random.nextInt(1000000)?c />

<div align="${align}" id="video_${uniqueId}"></div>

<script type="text/javascript">
Event.onDOMReady(function(){
  object${uniqueId} = new FlashObject("${url}",'object${uniqueId}',"<#if width?default('')?string?contains('%')>${width}<#else>${width?default(640)?number?c}</#if>","<#if height?default('')?string?contains('%')>${height}<#else>${height?default(360)?number?c}</#if>");
  object${uniqueId}.addParam("wmode","transparent");
  object${uniqueId}.align = "${align}";
  object${uniqueId}.write("video_${uniqueId}");
});
</script>