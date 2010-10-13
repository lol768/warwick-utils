<#assign uniqueId = random.nextInt(1000000)?c />
<#macro dimension value append=0><#compress>
	<#if value?string?ends_with("%")>
		${value}
	<#elseif value?string != 'auto'>
		${(value?number + append)?c}
	</#if>
</#compress></#macro>

<div align="${align}" id="video_${uniqueId}"></div>

<script type="text/javascript">
Event.onDOMReady(function(){
  object${uniqueId} = new FlashObject("${url}",'object${uniqueId}',"<@dimension value=width?default(640) />","<@dimension value=height?default(360) />");
  object${uniqueId}.addParam("wmode","transparent");
  object${uniqueId}.align = "${align}";
  object${uniqueId}.write("video_${uniqueId}");
});
</script>