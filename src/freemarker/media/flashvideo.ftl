<#assign uniqueId = random.nextInt(1000000)?c />

<div align="${align}" id="video_${uniqueId}"></div>

<script type="text/javascript">
  object${uniqueId} = new FlashObject("${url}",'',"${width?default(425)?number?c}","${height?default(350)?number?c}");
  object${uniqueId}.addParam("wmode","transparent");
  object${uniqueId}.align = "${align}";
  object${uniqueId}.write("video_${uniqueId}");
</script>