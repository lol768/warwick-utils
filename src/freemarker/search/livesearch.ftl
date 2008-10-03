<#assign uniqueId = random.nextInt(1000000)?c />

<form action="http://search.warwick.ac.uk/${index?default("sitebuilder")}/" method="get"> ${label}<br>

<input id="liveSearchText${uniqueId}" name="q" size="15" class="searchText" autocomplete="off"> <img alt="Spinner" id="search_spinner${uniqueId}" src="/static_war/images/tab-spinner.gif" style="display: none;" border="0"> <br>

<input value="Search" type="submit">
<input name="urlPrefix" value="${urlPrefix}" type="hidden">
<input name="indexSection" value="${index}" type="hidden"> <script type="text/javascript">
new Form.Element.Observer('liveSearchText${uniqueId}', 1, function(element, value)
{new Ajax.Updater('search_results${uniqueId}', '/ajax/lvsch/micro.html?urlPrefix=${urlPrefix}&indexSection=${index}',
{asynchronous:true,
evalScripts:true,
onComplete:function(request){Element.hide('search_spinner${uniqueId}')},
onLoading:function(request){Element.show('search_spinner${uniqueId}')},
method:'post',
parameters:'q=' + value });});

</script>
</form>

<style type="text/css">
	div#search_results${uniqueId} ul li {background-image:none; padding-left:0}
	div#search_results${uniqueId} ul {padding-left:0}
	#rhsContent .content ul {padding-left:0}
</style>

<div id="search_results${uniqueId}"></div>
<br>