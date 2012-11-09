<#assign uniqueId = random.nextInt(1000000)?c />
<#macro dimension value append=0><#compress>
	<#if value?string?ends_with("%")>
		${value}
	<#elseif value?string != 'auto'>
		${(value?number + append)?c}
	</#if>
</#compress></#macro>

<div align="${align}" id="video_${uniqueId}" style="width: <@dimension value=width?default(550) />px"></div>

<script type="text/javascript">
Event.onDOMReady(function(){
  object${uniqueId} = new FlashObject("//prezi.com/bin/preziloader.swf",'object${uniqueId}',"<@dimension value=width?default(550) />","<@dimension value=height?default(400) />");
  object${uniqueId}.addParam("wmode","transparent");
  object${uniqueId}.addParam("allowfullscreen","true");
  object${uniqueId}.addParam("allowscriptaccess","always");
  object${uniqueId}.addParam("bgcolor","#ffffff");
  
  object${uniqueId}.addVariable("prezi_id","${id}");
  object${uniqueId}.addVariable("lock_to_path","<#if locktopath?default('false') == 'true'>1<#else>0</#if>");
  object${uniqueId}.addVariable("color","ffffff");
  object${uniqueId}.addVariable("autoplay","no");
  object${uniqueId}.addVariable("autohide_ctrls","0");
  
  object${uniqueId}.align = "${align}";
  object${uniqueId}.write("video_${uniqueId}");
});
</script>