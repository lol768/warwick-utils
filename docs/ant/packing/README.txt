To pack CSS/JS, you need to run a copy with the filters specified from a source to a target directory.

Eg:

<!-- Define the JSMin filter we use to pack JS files -->
<typedef name="jsmin" classname="uk.ac.warwick.utils.ant.JSMinFilter">
	<classpath>
		<pathelement location="${war.classes}" />
		<fileset dir="${packtag.lib.dir}">
			<include name="*.jar" />
		</fileset>
		<fileset dir="${ant.lib.dir}">
			<include name="*.jar" />
		</fileset>
	</classpath>
</typedef>

<!-- Define the IBloom filter we use to pack CSS files -->
<typedef name="ibloom" classname="uk.ac.warwick.sbr.utils.IBloomFilter">
	<classpath>
		<pathelement location="${war.classes}" />
		<fileset dir="${packtag.lib.dir}">
			<include name="*.jar" />
		</fileset>
		<fileset dir="${ant.lib.dir}">
			<include name="*.jar" />
		</fileset>
	</classpath>
</typedef>

<!-- Concatenate all JS files into a single packed file -->
<concat destfile="${docs.static.target}/scripts/all-scripts.js" fixlastline="no">
	<filelist dir="${docs.static.target}/scripts/">
		<file name="libs/prototype.js" />
		<file name="libs/scriptaculous.js" />
		<file name="libs/effects.js" />
		<file name="libs/dragdrop.js" />
		<file name="libs/lightbox/js/lightbox.js" />
	</filelist>

	<!-- Compact js file -->
	<filterchain>
		<tokenfilter>
			<filetokenizer />
			<jsmin />
		</tokenfilter>
	</filterchain>
</concat>

<copy todir="${docs.static.target}/css/packed">
	<fileset dir="${docs.static.target}/css">
		<include name="*.css" />
	</fileset>
	
	<filterchain>
		<tokenfilter>
			<filetokenizer />
			<ibloom />
		</tokenfilter>
	</filterchain>
</copy>