<#assign uniqueId = random.nextInt(1000000)?c />

<script type="text/javascript">
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
  object${uniqueId} = new FlashObject("${playerLocation}?autoStart=false&file="+ url +"<#if previewimage?default("") != ''>&image=${previewimage?default("")}</#if>&overstretch=true",'',"${width?default(425)?number?c}","${(height?default(350)?number + 20)?c}");
  object${uniqueId}.addParam("wmode","transparent");
  object${uniqueId}.addVariable("width",${width?default(425)?number?c});
  object${uniqueId}.addVariable("height",${(height?default(350)?number + 20)?c});
  object${uniqueId}.addParam("allowfullscreen","true");
  object${uniqueId}.addVariable("showdownload","false");
  object${uniqueId}.align = "${align}";
  object${uniqueId}.write();
</script>

<#if download?exists && download = 'true'>
<a href="${url}?forceOpenSave=true">Download</a>
</#if>