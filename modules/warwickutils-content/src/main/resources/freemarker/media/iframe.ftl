<#assign uniqueId = random.nextInt(1000000)?c />
<#macro dimension value append=0><#compress>
	<#if value?string?ends_with("%")>
		${value}
	<#elseif value?string != 'auto'>
		${(value?number + append)?c}
	</#if>
</#compress></#macro>

<div align="${align}" id="video_${uniqueId}">
	<iframe width="<@dimension value=width?default(640) />" height="<@dimension value=height?default(360) />" src="${url}" frameborder="0" allowfullscreen></iframe>
</div>