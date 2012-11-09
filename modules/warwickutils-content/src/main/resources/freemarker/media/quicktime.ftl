<#assign uniqueId = random.nextInt(1000000) />
<#macro dimension value append=0><#compress>
	<#if value?string?ends_with("%")>
		${value}
	<#elseif value?string != 'auto'>
		${(value?number + append)?c}
	</#if>
</#compress></#macro>

<#assign embedcode>
	<object classid='clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B' codebase='http://www.apple.com/qtactivex/qtplugin.cab#version=6,0,2,0' 
	width='<@dimension value=width?default(400) />' 
	height='<@dimension value=height?default(300) append=10 />'>
	<param name='src' value='${url}'>
	<param name='controller' value='TRUE'>
	<param name='target' value='myself'>
	<param name='type' value='video/quicktime'>
	<param name='scale' value='ASPECT'>
	<param name='autoplay' value='TRUE'>
	<embed
	 src="${url}"
	 autoplay="TRUE"
	 scale="ASPECT"
	 type="video/quicktime" 
	 controller="TRUE"
	 width='<@dimension value=width?default(400) />' 
	 height='<@dimension value=height?default(300) append=10 />'
	></embed>
	</object>
</#assign>
<div align="${align}"><div style="border: 1px solid #999; width: <@dimension value=width?default(400) />px; height: <@dimension value=height?default(300) append=10 />px; background: url(${previewimage}) center center no-repeat;" 
 onclick="loadVideo_${uniqueId?c}(this)"></div></div>
<script type="text/javascript">
window.loadVideo_${uniqueId?c} = function (element) {
  result = "${embedcode?js_string}";
  element.innerHTML = result;
}
</script>

<#if download?exists && download = 'true'>
<a href="${url}?forceOpenSave=true">Download</a>
</#if>