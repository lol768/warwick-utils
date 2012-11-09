<#assign uniqueId = random.nextInt(1000000) />
<#macro dimension value append=0><#compress>
	<#if value?string?ends_with("%")>
		${value}
	<#elseif value?string != 'auto'>
		${(value?number + append)?c}
	</#if>
</#compress></#macro>

<#assign embedcode>
<object 
classid='CLSID:22d6f312-b0f6-11d0-94ab-0080c74c7e95'
codebase='http://activex.microsoft.com/activex/controls/mplayer/en/nsmp2inf.cab#Version=5,1,52,701'
standby='Loading Microsoft Windows Media Player components...' type='application/x-oleobject'
width='<@dimension value=width?default(400) />' 
height='<@dimension value=height?default(300) append=46 />'
>
<param name='fileName' value='###URL###'>
<param name='animationatStart' value='true'>
<param name='transparentatStart' value='true'>
<param name='autoStart' value='true'>
<param name='showStatusBar' value='false'>
<param name='showControls' value='true'>
<param name='loop' value='false'>
<param name='scale' value='tofit'>
<embed
 type="application/x-mplayer2"
 src="###URL###"
 showcontrols="TRUE"
 scale="TOFIT"
 showstatusbar="0"
 autostart="TRUE"
 width='<@dimension value=width?default(400) />' 
 height='<@dimension value=height?default(300) append=46 />'
></embed>
</object>
</#assign>

<div align="${align}"><div id="video_${uniqueId?c}" style="border: 1px solid #999; width: <@dimension value=width?default(400) />px; height: <@dimension value=height?default(300) append=46 />px; background: url(${previewimage}) center center no-repeat;"
 onclick="loadVideo_${uniqueId?c}(this)"></div></div>
<script type="text/javascript">
window.loadVideo_${uniqueId?c} = function (element) {
  // i bet it doesn't love this.
  <#if absoluteUrl?default('')?length gt 0>
  	url = '${url}';
  	absoluteUrl = '${absoluteUrl}';
  <#else>
  	url = '${url}';
  	absoluteUrl = url.toAbsoluteUrl();
  </#if>

  result = "${embedcode?js_string}";
  
  result = result.replace(/###URL###/g,absoluteUrl);
  
  element.innerHTML = result;
}
</script>

<#if download?exists && download = 'true'>
<a href="${url}?forceOpenSave=true">Download</a>
</#if>