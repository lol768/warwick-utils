/*
 * Created on 11-Mar-2004
 *
 */
package uk.ac.warwick.util.content.textile2;

import java.net.URLEncoder;
import java.util.EnumSet;

import junit.framework.TestCase;
import uk.ac.warwick.util.content.MutableContent;
import uk.ac.warwick.util.content.texttransformers.RemoveLeadingNbspTransformer;
import uk.ac.warwick.util.core.HtmlUtils;

/**
 * @author Kieran Shaw
 * 
 */
public class TextileTest extends TestCase {

	public static void main(String[] args) throws Exception {
		TextileTest t = new TextileTest();
		System.out.println("Blah1");
		t.testRegex();
		System.out.println("Blah3");
	}

	public final void testRegex() throws Exception {
		System.out.println("Blah2");

		Textile2 tex = new Textile2();

		String longChinesePost =

		"&#20351;&#29992;&#25163;&#26426;&#21457;"
				+ "&#36865;&#30701;&#20449;�MISSHOME� &#21040;"
				+ "&#21495;&#30721;\"80041\",&#20043;&#21518;"
				+ "&#20250;&#25910;&#21040;&#20004;&#26465;&#20805;"
				+ "&#20540;&#25104;&#21151;&#30340;&#30701;&#20449;,"
				+ "&#27599;&#26465;&#30701;&#20449;&#23558;&#25910;"
				+ "&#21462;1.5&#38225;&#20316;&#20026;&#20170;&#21518;"
				+ "&#25320;&#25171;&#30005;&#35805;&#30340;&#39044;&#20184;"
				+ "&#36153;,&#21363;&#27599;&#27425;&#20805;&#20540;3&#38225;"
				+ "&#12290;(&#19968;&#26465;&#26159;&#21578;&#30693;&#21495;"
				+ "&#30721;&#65292;&#19968;&#26465;&#26159;&#21578;&#30693;"
				+ "&#23384;&#21495;&#30721;&#30340;&#26041;&#27861;&#65289;"
				+ "&#65292;&#28982;&#21518;&#29992;&#20805;&#36807;&#20540;"
				+ "&#30340;&#25163;&#26426;&#25320;&#25171;&#21495;&#30721;"
				+ "&#65306;0207 124 6666(&#27492;&#21495;&#30721;&#20219;"
				+ "&#20309;&#20844;&#21496;&#31614;&#32422;&#25163;&#26426;&#37117;"
				+ "&#21487;&#20197;&#29992;&#20813;&#36153;&#26102;&#38388;&#25320;"
				+ "&#25171;,&#19981;&#39069;&#22806;&#25910;&#36153;,&#21482;&#25187;"
				+ "&#20813;&#36153;&#26102;&#38388;),&#25320;&#36890;&#21518;&#20250;"
				+ "&#26377;&#25552;&#31034;&#38899;&#21578;&#30693;&#20320;&#30340;&#20313;"
				+ "&#39069;,&#28982;&#21518;&#30452;&#25509;&#25320;&#22269;&#38469;"
				+ "&#38271;&#36884;&#21306;&#21495;+&#34987;&#21483;&#21495;&#30721;"
				+ "&#23601;&#21487;&#20197;&#20102;,&#27604;&#22914;008610,*"
				+ " "
				+ "*******,"
				+ " &#36755;&#20837;&#34987;&#21483;&#21495;&#30721;&#21518;&#20250;"
				+ "&#26377;&#25552;&#31034;&#38899;&#21578;&#30693;&#21487;&#20197;"
				+ "&#36890;&#35805;&#30340;&#26102;&#38388;.&#29992;&#27492;&#26041;"
				+ "&#27861;&#25171;&#22238;&#20013;&#22269;&#25910;&#36153;&#26159;1P/"
				+ "&#20998;&#38047;,&#20063;&#23601;&#26159;&#27599;&#27425;&#20805;"
				+ "&#20540;&#21487;&#20197;&#36890;&#35805;300&#20998;&#38047;.&#27599;"
				+ "&#27425;&#25509;&#36890;&#30340;&#35805;&#65292;&#20250;&#25910;"
				+ "&#21462;10p&#30340;&#25509;&#36890;&#36153;&#12290;&#30456;&#27604;"
				+ "chinatown&#30340;&#19968;&#20123;&#30005;&#35805;&#21345;&#65292;10-30p,"
				+ "&#22909;&#22810;&#20102;&#65292;&#27599;&#27425;&#25171;&#30340;&#26102;"
				+ "&#38388;&#36234;&#20037;&#23601;&#36234;&#20415;&#23452;&#12290;&#31034;"
				+ "&#20363;:&#19968;&#27425;&#36890;&#35805;5&#20998;&#38047;,&#37027;&#20040;"
				+ "&#25910;&#36153;&#23601;&#26159;1P/&#20998;&#38047;5&#20998;&#38047;+10P=15P,*5&#20998;&#38047;+10P=15P,"
				+ "&#21363;3P/&#20998;&#38047;;&#19968;&#27425;&#36890;&#35805;30&#20998;&#38047;,"
				+ "&#37027;&#20040;&#25910;&#36153;&#23601;&#26159;1P/&#20998;&#38047;*30&#20998;"
				+ "&#38047;+10P=40P,&#21363;1.33P/&#20998;&#38047;;&#19968;&#27425;&#36890;&#35805;"
				+ "60&#20998;&#38047;,&#37027;&#20040;&#25910;&#36153;&#23601;&#26159;1P/&#20998;"
				+ "&#38047;*60&#20998;&#38047;+10P=70P,&#21363;1.17P/&#20998;&#38047;."
				+

				"&#20813;&#36153;&#25171;&#22238;&#20013;&#22269;"
				+ "02&#25163;&#26426;&#21644;3G&#25163;&#26426;&#65288;&#37096;&#20998;Orange&#25163;&#26426;&#65289;"
				+ "&#20351;&#29992;O2&#25163;&#26426;&#25110;3G&#25163;&#26426;&#30340;&#20813;&#36153;&#26102;&#38388;&#65292;&#25320;&#25171;&#65306; 0870-794-4157&#65288;&#21608;&#19968;&#21040;&#21608;&#20116;&#65289;&#65292; 0844-570-4157&#65288;&#21608;&#20845;&#65292;&#21608;&#26085;&#65289;&#65292;&#21487;&#20197;&#20813;&#36153;&#25320;&#25171;&#22238;&#20013;&#22269;&#65288;&#24231;&#26426;&#21644;&#25163;&#26426;&#37117;&#19968;&#26679;&#65289;&#25110;&#20854;&#20182;&#22269;&#23478;&#65292;&#36825;&#37324;&#21015;&#20986;&#37096;&#20998;&#65306;&#21253;&#25324;&#20013;&#22269;&#22823;&#38470;&#65292;&#39321;&#28207;&#12289;&#21488;&#28286;&#12289;&#32654;&#22269;&#12289;&#21152;&#25343;&#22823;&#12289;&#20420;&#32599;&#26031;&#12289;&#27874;&#22810;&#40654;&#21508;[&#25163;&#26426;&#65291;&#22266;&#23450;&#30005;&#35805;]&#65292;&#26085;&#26412;&#12289;&#38889;&#22269;&#12289;&#39532;&#26469;&#35199;&#20122;&#12289;&#26032;&#21152;&#22369;&#12289;&#28595;&#22823;&#21033;&#20122;&#12289;&#26032;&#35199;&#20848;&#12289;&#20197;&#33394;&#21015;&#12289;&#24052;&#35199;&#12289;&#21335;&#38750;&#12289;&#23612;&#26085;&#21033;&#20122;&#39318;&#37117;&#25289;&#21508;&#26031;&#12289;&#27888;&#22269;&#26364;&#35895;&#21644;&#27431;&#27954;&#20960;&#20046;&#25152;&#26377;&#22269;&#23478;&#65288;&#22266;&#23450;&#30005;&#35805;&#65289;&#12290;&#65289;&#12290;&#25320;&#25171;&#36825;&#20004;&#20010;&#21495;&#30721;&#20165;&#25187;&#38500;O2&#25163;&#26426;&#25110;3G&#30340;&#20813;&#36153;&#26102;&#38388;&#65292;&#19981;&#20250;&#20877;&#25910;&#21462;&#20219;&#20309;&#38271;&#36884;&#38468;&#21152;&#36153;&#29992;&#21450;&#20219;&#20309;&#25509;&#20837;&#36153;&#65292;&#24182;&#19988;&#36890;&#35805;&#36136;&#37327;&#36828;&#36828;&#22909;&#20110;saver&#21345;&#12290;&#24744;&#21487;&#20197;&#30465;&#21435;&#20080;&#21345;&#30340;&#38065;&#21644;&#26102;&#38388;&#12290;PAY AS YOU GO&#25163;&#26426;&#23601;&#26159;LOCAL RATE&#65292;&#21363;&#20320;&#29992;&#25163;&#26426;&#25171;&#33521;&#22269;&#30340;&#26379;&#21451;&#21644;&#25171;&#22238;&#22269;&#20215;&#26684;&#30456;&#21516;&#12290;&#22914;&#26524;&#25285;&#24515;&#34987;&#20081;&#25910;&#36153;&#65292;&#21487;&#36890;&#36807;O2&#32593;&#19978;&#24080;&#25143;&#26597;&#30475;&#33258;&#24049;&#30340;&#25910;&#36153;&#24773;&#20917;&#12290;"
				+ "&#22266;&#23450;&#30005;&#35805;&#65288;BT&#65292;TELEWEST&#31561;&#65289;&#25320;&#25171;&#25509;&#20837;&#21495;&#21518;&#20250;&#21548;&#21040;&#36153;&#29992;&#20026;4P/min&#30340;&#25552;&#31034;&#38899;&#65292;&#36825;&#26159;&#25351;&#22266;&#23450;&#30005;&#35805;&#65292;&#19982;O2&#25163;&#26426;&#25110;3G&#25163;&#26426;&#26080;&#20851;&#12290;&#20351;&#29992;O2&#25163;&#26426;&#25110;3G&#25163;&#26426;&#19981;&#20250;&#20877;&#25910;&#21462;&#20219;&#20309;&#36153;&#29992;&#65281;"
				+ "Orange&#30340;&#26576;&#20123;tariff&#21487;&#20197;&#25171;0870&#20813;&#36153;&#65292;&#25171;0844&#26159;&#19981;&#20813;&#36153;&#30340;&#65292;&#22823;&#23478;&#19981;&#35201;&#36731;&#20449;&#19968;&#20123;&#19981;&#36127;&#36131;&#30340;&#24191;&#21578;&#65292;&#26368;&#22909;&#20808;&#35810;&#38382;&#20320;&#30340;&#32593;&#32476;&#26381;&#21153;&#21830;&#65292;&#20294;&#26159;O2&#65292;3G&#26159;&#32943;&#23450;&#20813;&#36153;&#30340;&#65288;&#27880;&#24847;&#26159;&#20813;&#36153;&#26102;&#27573;&#20869;&#21734;&#65281;&#65289;"
				+ "&#20854;&#20182;&#32593;&#32476;&#25163;&#26426;&#65288;&#22914;Vodafone, T-mobile,3G,orange,virgin&#31561;&#65289;"
				+ "&#36825;&#26159;&#22312;&#23478;&#22352;&#30528;&#19981;&#29992;&#20986;&#38376;&#20080;&#21345;&#30340;&#26041;&#27861;&#65292;&#25512;&#33616;&#32473;&#27809;&#26377;O2&#25163;&#26426;&#30340;&#29992;&#25143;&#12290;"

				+ "&#20351;&#29992;&#25163;&#26426;&#21457;&#36865;&#30701;"
				+ "&#20449;�MISSHOME� &#21040;&#21495;&#30721;\"80041\",&#20043;"
				+ "&#21518;&#20250;&#25910;&#21040;&#20004;&#26465;&#20805;&#20540;"
				+ "&#25104;&#21151;&#30340;&#30701;&#20449;,&#27599;&#26465;&#30701;&#20449;"
				+ "&#23558;&#25910;&#21462;1.5&#38225;&#20316;&#20026;&#20170;&#21518;&#25320;"
				+ "&#25171;&#30005;&#35805;&#30340;&#39044;&#20184;&#36153;,&#21363;&#27599;"
				+ "&#27425;&#20805;&#20540;3&#38225;&#12290;(&#19968;&#26465;&#26159;&#21578;"
				+ "&#30693;&#21495;&#30721;&#65292;&#19968;&#26465;&#26159;&#21578;&#30693;"
				+ "&#23384;&#21495;&#30721;&#30340;&#26041;&#27861;&#65289;&#65292;&#28982;"
				+ "&#21518;&#29992;&#20805;&#36807;&#20540;&#30340;&#25163;&#26426;&#25320;"
				+ "&#25171;&#21495;&#30721;&#65306;0207 124 6666(&#27492;&#21495;&#30721;"
				+ "&#20219;&#20309;&#20844;&#21496;&#31614;&#32422;&#25163;&#26426;&#37117;&#21487;"
				+ "&#20197;&#29992;&#20813;&#36153;&#26102;&#38388;&#25320;&#25171;,&#19981;&#39069;"
				+ "&#22806;&#25910;&#36153;,&#21482;&#25187;&#20813;&#36153;&#26102;&#38388;),&#25320;"
				+ "&#36890;&#21518;&#20250;&#26377;&#25552;&#31034;&#38899;&#21578;&#30693;&#20320;&#30340;"
				+ "&#20313;&#39069;,&#28982;&#21518;&#30452;&#25509;&#25320;&#22269;&#38469;"
				+ "&#38271;&#36884;&#21306;&#21495;+&#34987;&#21483;&#21495;&#30721;&#23601;"
				+ "&#21487;&#20197;&#20102;,&#27604;&#22914;008610********,&#36755;&#20837;"
				+ "&#34987;&#21483;&#21495;&#30721;&#21518;&#20250;&#26377;&#25552;&#31034;"
				+ "&#38899;&#21578;&#30693;&#21487;&#20197;&#36890;&#35805;&#30340;&#26102;"
				+ "&#38388;.&#29992;&#27492;&#26041;&#27861;&#25171;&#22238;&#20013;&#22269;"
				+ "&#25910;&#36153;&#26159;1P/&#20998;&#38047;,&#20063;&#23601;&#26159;&#27599;"
				+ "&#27425;&#20805;&#20540;&#21487;&#20197;&#36890;&#35805;300&#20998;&#38047;."
				+ "&#27599;&#27425;&#25509;&#36890;&#30340;&#35805;&#65292;&#20250;&#25910;&#21462;"
				+ "10p&#30340;&#25509;&#36890;&#36153;&#12290;&#30456;&#27604;chinatown&#30340;&#19968;"
				+ "&#20123;&#30005;&#35805;&#21345;&#65292;10-30p,&#22909;&#22810;&#20102;&#65292;"
				+ "&#27599;&#27425;&#25171;&#30340;&#26102;&#38388;&#36234;&#20037;&#23601;&#36234;"
				+ "&#20415;&#23452;&#12290;&#31034;&#20363;:&#19968;&#27425;&#36890;&#35805;5&#20998;"
				+ "&#38047;,&#37027;&#20040;&#25910;&#36153;&#23601;&#26159;1P/&#20998;&#38047;"
				+ "*5&#20998;&#38047;+10P=15P,&#21363;3P/&#20998;&#38047;;&#19968;&#27425;&#36890;"
				+ "&#35805;30&#20998;&#38047;,&#37027;&#20040;&#25910;&#36153;&#23601;&#26159;1P/&#20998;"
				+ "&#38047;*30&#20998;&#38047;+10P=40P,&#21363;1.33P/&#20998;&#38047;;&#19968;&#27425;"
				+ "&#36890;&#35805;60&#20998;&#38047;,&#37027;&#20040;&#25910;&#36153;&#23601;&#26159;"
				+ "1P/&#20998;&#38047;*60&#20998;&#38047;+10P=70P,&#21363;1.17P/&#20998;&#38047;."

				+ "&#35831;&#27880;&#24847;&#65306;&#30001;&#20110;&#36825;&#31181;&#20805;&#20540;&#26041;&#27861;&#19981;&#20250;&#20135;&#29983;PIN&#30721;&#65292;&#25152;&#20197;&#21482;&#33021;&#29992;&#27880;&#20876;&#30340;&#25163;&#26426;&#65288;&#21363;&#21457;&#36865;&#30701;&#20449;&#30340;&#25163;&#26426;&#65289;&#25320;&#25171;&#22269;&#38469;&#38271;&#36884;&#12290;&#23545;&#20110;&#29992;Pay as you go &#30340;&#20154;&#65292;&#20063;&#21487;&#20197;&#29992;&#20197;&#19978;&#26041;&#27861;&#20805;&#20540;&#65292;&#19981;&#36807;&#35201;&#27880;&#24847;&#33258;&#24049;&#21345;&#37324;&#36824;&#26377;&#22810;&#23569;&#38065;&#65292;&#25320;&#25171;&#26102;&#30340;&#36153;&#29992;&#20026;&#26222;&#36890;&#30005;&#35805;&#36153;+1P/min&#30340;&#38271;&#36884;&#36153;&#12290;&#27880;&#24847;&#65306;&#65288;&#19968;&#27425;&#24615;&#21487;&#20805;&#20540;&#22810;&#27425;&#65292;&#37117;&#32047;&#35745;&#22312;&#20320;&#33258;&#24049;&#25163;&#26426;&#36153;&#37324;&#65292;&#27809;&#26377;&#19968;&#33324;SAVER&#21345;&#19977;&#20010;&#26376;&#30340;&#38480;&#26399;&#65289;"
				+ "&#19968;&#33324;BT&#24231;&#26426;&#25320;&#25171;0870 7944157 (8p/m),0844 5704157(4p/m)"
				+ "Access number 0870 794 4157 (weekdays)"
				+ "Access number 0844 570 4157 (weekend)"
				+ "&#29992;&#36825;&#20004;&#20010;&#21495;&#30721;&#25320;&#25171;&#30340;&#22269;&#23478;&#24231;&#26426;&#25110;&#32773;&#25163;&#26426;&#65292;O2&#25171;&#37117;&#31639;&#22312;&#20813;&#36153;&#26102;&#38388;&#37324;&#65292;&#24050;&#32463;&#39564;&#35777;&#36807;&#20102;&#65292;&#19981;&#20250;&#25910;&#38065;&#30340;&#65292;&#22823;&#23478;&#25918;&#24515;&#25171;&#21543;&#12290;&#30465;&#21435;&#20102;&#36153;&#38065;&#21644;&#20080;&#21345;&#36208;&#36335;&#30340;&#40635;&#28902;&#12290;"
				+ "&#29616;&#22312;&#26377;&#24456;&#22810;&#25171;&#22238;&#22269;&#30340;&#21495;&#30721;&#65292;&#25105;&#20174;&#21435;&#24180;&#24320;&#22987;&#19968;&#30452;&#29992;&#36825;&#20004;&#20010;&#65292;&#27809;&#20986;&#29616;&#36807;&#20160;&#20040;&#38382;&#39064;&#65292;&#20174;&#26469;&#27809;&#26377;&#20081;&#25910;&#36153;&#65292;&#36890;&#35805;&#36136;&#37327;&#19981;&#22909;&#20043;&#31867;&#30340;&#38382;&#39064;&#12290;&#27880;&#24847;&#20854;&#20182;&#21495;&#30721;&#26377;&#21487;&#33021;&#20081;&#25910;&#36153;&#65292;&#36890;&#35805;&#36136;&#37327;&#19981;&#22909;&#31561;&#31561;&#30340;&#38382;&#39064;&#65292;&#35831;&#22823;&#23478;&#23567;&#24515;&#20102;&#12290;&#36825;&#20004;&#20010;&#21495;&#30721;&#22823;&#23478;&#25918;&#24515;&#29992;";

		long timeTaken = System.currentTimeMillis();
		compare(longChinesePost, tex.apply(new MutableContent(null, longChinesePost)).getContent());
		System.out.println("took " + (System.currentTimeMillis() - timeTaken)
				+ " ms");

	}

	// public final void testLongUrl() throws Exception {
	//    
	// Textile tex = new Textile();
	//    
	// String longUrl =
	// "http://web1.epnet.com/externalframe.asp?tb=1&_ug=sid+D5C0FC81%2D81EE%2D4F7D%2D9061%2DC66D552104D5%40sessionmgr6+dbs+buh+cp+1+B2EB&_us=mh+1+hs+True+cst+0%3B1+or+Date+mdbs+buh+ss+SO+sm+KS+sl+0+ri+KAAACBVA00010732+dstb+KS+sel+False+frn+1+4EC7&_uso=hd+False+tg%5B0+%2D+st%5B0+%2DVrontis++and++Sharp+db%5B0+%2Dbuh+op%5B0+%2D+55B1&fi=buh_11013117_AN&lpdf=true&pdfs=2.6MB&bk=C&tn=1&tp=CP&es=cs%5Fclient%2Easp%3FT%3DP%26P%3DAN%26K%3D11013117%26rn%3D1%26db%3Dbuh%26is%3D1469347X%26sc%3DR%26S%3DR%26D%3Dbuh%26title%3DMarketing%2BReview%26year%3D2003%26bk%3DC&fn=1&rn=1&http://web1.epnet.com/externalframe.asp?tb=1&_ug=sid+D5C0FC81%2D81EE%2D4F7D%2D9061%2DC66D552104D5%40sessionmgr6+dbs+buh+cp+1+B2EB&_us=mh+1+hs+True+cst+0%3B1+or+Date+mdbs+buh+ss+SO+sm+KS+sl+0+ri+KAAACBVA00010732+dstb+KS+sel+False+frn+1+4EC7&_uso=hd+False+tg%5B0+%2D+st%5B0+%2DVrontis++and++Sharp+db%5B0+%2Dbuh+op%5B0+%2D+55B1&fi=buh_11013117_AN&lpdf=true&pdfs=2.6MB&bk=C&tn=1&tp=CP&es=cs%5Fclient%2Easp%3FT%3DP%26P%3DAN%26K%3D11013117%26rn%3D1%26db%3Dbuh%26is%3D1469347X%26sc%3DR%26S%3DR%26D%3Dbuh%26title%3DMarketing%2BReview%26year%3D2003%26bk%3DC&fn=1&rn=1&";
	//        
	// long timeTaken = System.currentTimeMillis();
	// tex.process(longUrl);
	// System.out.println("took " + (System.currentTimeMillis() - timeTaken) + "
	// ms");
	//    
	// }

	public final void testDashes() throws Exception {

		String source = "one - dash. two -- dashes. 6-4=2";

		String expected = "<p>one &#8211; dash. two&#8212;dashes. 6-4=2</p>";

		compare(source, expected);
	}

	public final void testDimensionSign() throws Exception {

		String source = "3x4=12 3 x 4=12";

		String expected = "<p>3&#215;4=12 3&#215;4=12</p>";

		compare(source, expected);
	}

	public final void testBackslashes() throws Exception {

		String source = "\\ check *one* backslash does not muck up";

		String expected = "<p>\\ check <strong>one</strong> backslash does not muck up</p>";

		compare(source, expected);

	}

	public final void testNoTextileBlockAtStart() throws Exception {

		String source = "\\\\\n*no textile*\n\\\\\n*yes*";

		String expected = "<p>*no textile*</p><p><strong>yes</strong></p>";

		compare(source, expected);

	}

	public final void testNoTextileBlockAtStart2() throws Exception {

		String source = "\\\\\\  \n*no textile*\n\\\\\\  \n*yes*";

		String expected = "<pre><code>*no textile*</code></pre><p><strong>yes</strong></p>";

		compare(source, expected);

	}

	public final void testNoTextileBlockAtStart3() throws Exception {

		String source = "\\\\\\\\\n*no textile*\n\\\\\\\\\n*yes*";

		String expected = "<p>*no textile*</p><p><strong>yes</strong></p>";

		compare(source, expected);

	}

	public final void testEmptyString() throws Exception {

		String source = "";

		String expected = "";

		compare(source, expected);

	}

	public final void testNoTextileBlockAtEnd() throws Exception {

		String source = "*yes*\n\\\\\n*no*\n\\\\";

		String expected = "<p><strong>yes</strong><p>*no*</p></p>";

		compare(source, expected);

	}

	public final void testJustTextileBlock() throws Exception {

		String source = "\\\\\n*no*\n\\\\";

		String expected = "<p>*no*</p>";

		compare(source, expected);

	}

	public final void testNoTextile() throws Exception {

		String source = "*yes1* \n\\\\\n *no2* \n\\\\\n *yes3* \n\\\\\n *no4* \n\\\\\n *yes5* \n\\\\\n *no6* \n\\\\\n ~yes7~ ";

		String expected = "<p><strong>yes1</strong> <p> *no2* </p><p><strong>yes3</strong> <p> *no4* </p></p><p><strong>yes5</strong> <p> *no6* </p></p><p><sub>yes7</sub></p></p>";

		compare(source, expected);

	}

	public final void testNoTextileAndCodeBlock() throws Exception {

		String source = "*yes1*\n\\\\\\\n*no2* no textile\n\\\\\\\n*yes*";

		String expected = "<p><strong>yes1</strong><pre><code>*no2* no textile</code></pre></p><p><strong>yes</strong></p>";

		compare(source, expected);

	}

	public final void testNoTextileAndNoLineBreaks() throws Exception {

		String source = "*yes1*\n\\\\\\\\\n*no2* no \n\ntextile\n\\\\\\\\\n*yes*";

		String expected = "<p><strong>yes1</strong><p>*no2* no textile</p></p><p><strong>yes</strong></p>";

		compare(source, expected);

	}

	public final void testEscapeASingleChar() throws Exception {

		String source = " *yes1* \\*no2* _yes3_ \\_no4\\_ blah\\*\\";

		String expected = "<p><strong>yes1</strong> &#0042;no2* <em>yes3</em> &#0095;no4&#0095; blah&#0042;\\</p>";

		compare(source, expected);

	}

	public final void testAllTextile() throws Exception {

		String source = "*yes1*\n\n\n~yes2~";

		String expected = "<p><strong>yes1</strong></p><p><sub>yes2</sub></p>";

		compare(source, expected);

	}

	public final void testNewLinesBeginningWithHyphen() throws Exception {

		String source = " - Monday\n- Tuesday\n- Wednesday";

		String expected = "<p>- Monday<br />- Tuesday<br />- Wednesday</p>";

		compare(source, expected);

	}

	public final void testNewLinesBeginningWithDoubleHyphen() throws Exception {

		String source = " -- Monday\n-- Tuesday\n-- Wednesday";

		String expected = "<p>&#8212;Monday<br />&#8212;Tuesday<br />&#8212;Wednesday</p>";

		compare(source, expected);

	}

	public final void testNewLines() throws Exception {

		String source = " *yes1*\nnew line\nnew line";

		String expected = "<p><strong>yes1</strong><br />new line<br />new line</p>";

		compare(source, expected);

	}

	public final void testHatWithinUrl() throws Exception {

		String source = "\"http://www2.warwick.ac.uk/^curef\":http://www2.warwick.ac.uk/^curef";

		String expected = "<p><a href=\"http://www2.warwick.ac.uk/^curef\">http://www2.warwick.ac.uk/^curef</a></p>";

		compare(source, expected);
	}

	public final void testTildeWithinUrl() throws Exception {

		String source = "\"http://www2.warwick.ac.uk/~curef\":http://www2.warwick.ac.uk/~curef blah";

		String expected = "<p><a href=\"http://www2.warwick.ac.uk/~curef\">http://www2.warwick.ac.uk/~curef</a> blah</p>";

		compare(source, expected);
	}

	public final void testTildeWithinLinks() throws Exception {

		String source = "blah ~some subscript http://warwick.ac.uk/blah/ please~";

		String expected = "<p>blah <sub>some subscript <a href=\"http://warwick.ac.uk/blah/\">http://warwick.ac.uk/blah/</a> please</sub></p>";

		compare(source, expected);
	}

	public final void testEllipses() throws Exception {

		String source = "...test .....test...\n...test";

		String expected = "<p>...test &#8230;..test&#8230;<br />...test</p>";

		compare(source, expected);

	}

	public final void testPoundSignsConverted() throws Exception {
		String source = "\u00a3test \u00a3\u00a3test\u00a3\n\u00a3test";

		String expected = "<p>&pound;test &pound;&pound;test&pound;<br />&pound;test</p>";

		compare(source, expected);

	}

	public final void testImgTag() throws Exception {

		String source = "!/test.gif!";

		String expected = "<p><img src=\"/test.gif\" alt=\"\" /></p>";

		compare(source, expected);

	}

	public final void testImgTagWithParameters() throws Exception {

		String source = "!/test.gif?a=b!";

		String expected = "<p><img src=\"/test.gif?a=b\" alt=\"\" /></p>";

		compare(source, expected);

	}

	public final void testNoImgTagWithinWord() throws Exception {

		String source = "test!test!";

		String expected = "<p>test!test!</p>";

		compare(source, expected);

	}

	public final void testNoImgTagAfterPunctuation() throws Exception {

		String source = "test!z.z!.!.!.!";

		String expected = "<p>test!z.z!.!.!.!</p>";

		compare(source, expected);

	}

	public final void testNoImgTagWithoutDot() throws Exception {

		String source = "test !?!?!?!?!";

		String expected = "<p>test !?!?!?!?!</p>";

		compare(source, expected);

	}

	public final void testLists() throws Exception {

		String source = "* one\n" + "* two\n" + "* three\n" + "\n"
				+ "More stuff";

		String expected = "<ul><li>one</li><li>two</li><li>three</li></ul><p>More stuff</p>";

		compare(source, expected);

	}

	/*
	 * These two methods include the <p> tag within the last list element
	 * because it is indented.
	 * 
	 * This is INTENTIONAL!!
	 * 
	 */

	public final void testUnorderedListsWithExtraLineBreaks() throws Exception {

		String source = "* one\n\n" + "* two\n\n" + "* three\n\n the end";

		String expected = "<ul><li>one</li></ul><ul><li>two</li></ul><ul><li>three<p>the end</p></li></ul>";

		compare(source, expected);

	}

	public final void testOrderedListsWithExtraLineBreaks() throws Exception {

		String source = "# one\n\n" + "# two\n\n" + "# three\n\n the end";

		String expected = "<ol><li>one</li></ol><ol><li>two</li></ol><ol><li>three<p>the end</p></li></ol>";

		compare(source, expected);

	}

	public final void testSingleElementListWithExtraLineBreaks()
			throws Exception {
		String source = "# one\n\n" + "the end";

		String expected = "<ol><li>one</li></ol><p>the end</p>";

		compare(source, expected);
	}

	public final void testHeaders() throws Exception {

		String source = "h1. Stuff\n\n" + "Some stuff\n\n"
				+ "h2. Some more stuff\n\n" + "Some more stuff";

		String expected = "<h1>Stuff</h1><p>Some stuff</p>"
				+ "<h2>Some more stuff</h2>" + "<p>Some more stuff</p>";

		compare(source, expected);

	}

	public final void testStrong() throws Exception {

		String source = "*strong* *more strong*.";

		String expected = "<p><strong>strong</strong> <strong>more strong</strong>.</p>";

		compare(source, expected);

	}

	public final void testCopyrightConversion() throws Exception {

		String source = "(C) 1999 a (C)    (C) (c) \n\n(C) after a word(C)";

		String expected = "<p>&#169; 1999 a &#169;    &#169; (c)</p><p>&#169; after a word&#169;</p>";

		compare(source, expected);

	}
	
	public final void testCopyrightConversion2() throws Exception {

		String source = "This is a copyright symbol: (C)";

		String expected = "<p>This is a copyright symbol: &#169;</p>";

		compare(source, expected);

	}

	public final void testEscapeCopyrightConversion() throws Exception {

		String source = "space \\(C) new line\n\\(C)";

		String expected = "<p>space (&#0067;) new line<br />(&#0067;)</p>";

		compare(source, expected);

	}

	public final void testRegisteredConversion() throws Exception {

		String source = "a (R)    (R) (r) \n\n(R) after a word(R)";

		String expected = "<p>a &#174;    &#174; (r)</p><p>&#174; after a word&#174;</p>";

		compare(source, expected);

	}

	public final void testEscapedRegisteredConversion() throws Exception {

		String source = "space \\(R) new line\n\\(R)";

		String expected = "<p>space (&#0082;) new line<br />(&#0082;)</p>";

		compare(source, expected);

	}

	public final void testTrademarkConversion() throws Exception {

		String source = "a (TM)    (TM) (tm) \n\n(TM) after a word(TM)";

		String expected = "<p>a &#8482;    &#8482; (tm)</p><p>&#8482; after a word&#8482;</p>";

		compare(source, expected);

	}

	public final void testEscapedTrademarkConversion() throws Exception {

		String source = "space \\(TM) new line\n\\(TM) after a word\\(TM)";

		String expected = "<p>space (&#0084;&#0077;) new line<br />(&#0084;&#0077;) after a word(&#0084;&#0077;)</p>";

		compare(source, expected);

	}

	public final void testNoWrapWithParagraphs() throws Exception {

		String source = "*A title*";

		String expected = "<p><strong>A title</strong></p>";

		compare(source, expected);

	}

	public final void testDeletedText() throws Exception {

		String source = "-deleted-";

		String expected = "<p><del>deleted</del></p>";

		compare(source, expected);

	}

	public final void testEmStrongCombinedText() throws Exception {

		String source = "*_emstrong_*";

		String expected = "<p><strong><em>emstrong</em></strong></p>";

		compare(source, expected);

	}

	public final void testStarsInStringText() throws Exception {

		String source = "test*strong*test";

		String expected = "<p>test*strong*test</p>";

		compare(source, expected);

	}

	public final void testDashesInStringText() throws Exception {

		String source = "test-dash-test";

		String expected = "<p>test-dash-test</p>";

		compare(source, expected);

	}

	public final void testDashesInUrlsText() throws Exception {

		String source = "some stuff - \"linktest\":http://www-2.warwick";

		String expected = "<p>some stuff &#8211; <a href=\"http://www-2.warwick\">linktest</a></p>";

		compare(source, expected);

	}

	public final void testNoSpacesBetweenTagsText() throws Exception {

		String source = " * not strong *";

		String expected = "<ul><li>not strong *</li></ul>";

		compare(source, expected);

	}

	public final void testNoExtraSpacesText() throws Exception {

		String source = "_ems_\n\n_ems_";

		String expected = "<p><em>ems</em></p><p><em>ems</em></p>";

		compare(source, expected);

	}

	public final void testUrlLinkWithSpaces() throws Exception {
		String source = " http://localhost/some/path ";
		String expected = "<p><a href=\"http://localhost/some/path\">http://localhost/some/path</a></p>";
		compare(source, expected);

	}

	public final void testUrlLinkWithSpacesMixedCase() throws Exception {
		String source = " http://LocaLhost/some/path ";
		String expected = "<p><a href=\"http://LocaLhost/some/path\">http://LocaLhost/some/path</a></p>";
		compare(source, expected);

	}

	public final void testUrlLinkFollowedByPunctuation() throws Exception {
		String source = " http://localhost/some/path. http://localhost/some/path.";
		String expected = "<p><a href=\"http://localhost/some/path\">http://localhost/some/path</a>. <a href=\"http://localhost/some/path\">http://localhost/some/path</a>.</p>";
		compare(source, expected);

	}

	public final void testUrlSurroundedByMarkup() throws Exception {
		String source = "Check out ~this \"cool link\":http://www.ibm.com~";
		String expected = "<p>Check out <sub>this <a href=\"http://www.ibm.com\">cool link</a></sub></p>";
		compare(source, expected);
	}

	public final void testUrlSurroundedByMarkup3() throws Exception {
		String source = "Check out ^this \"cool link\":http://www.ibm.com^";
		String expected = "<p>Check out <sup>this <a href=\"http://www.ibm.com\">cool link</a></sup></p>";
		compare(source, expected);
	}

	public final void testLinkSurroundedByMarkup() throws Exception {
		String source = "Check out *this cool link: http://www.ibm.com*";
		String expected = "<p>Check out <strong>this cool link: <a href=\"http://www.ibm.com\">http://www.ibm.com</a></strong></p>";
		compare(source, expected);
	}

	public final void testUrlSurroundedBySub() throws Exception {
		String source = " ~Check out this cool link: http://www.ibm.com~ ";
		String expected = "<p><sub>Check out this cool link: <a href=\"http://www.ibm.com\">http://www.ibm.com</a></sub></p>";
		compare(source, expected);
	}

	public final void testUrlSurroundedBySup() throws Exception {
		String source = "Check out ^this cool link: http://ibm.com^";
		String expected = "<p>Check out <sup>this cool link: <a href=\"http://ibm.com\">http://ibm.com</a></sup></p>";
		compare(source, expected);
	}

	public final void testUrlSurroundedByDel() throws Exception {
		String source = "-Check out this cool link: http://www.ibm.com-";
		String expected = "<p><del>Check out this cool link: <a href=\"http://www.ibm.com\">http://www.ibm.com</a></del></p>";
		compare(source, expected);
	}

	// temporarily commenting out <ins> until find some use/style for it
	public final void xtestUrlSurroundedByIns() throws Exception {
		String source = "ha +Check out this cool link: http://www.ibm.com+ ha";
		String expected = "<p><ins>Check out this cool link: <a href=\"http://www.ibm.com\">http://www.ibm.com</a></ins></p>";
		compare(source, expected);
	}

	public final void testUrlSurroundedByCode() throws Exception {
		String source = "@Check out this cool link: http://www.ibm.com@";
		String expected = "<p><code>Check out this cool link: http://www.ibm.com</code></p>";
		compare(source, expected);
	}

	public final void testUrlSurroundedByEm() throws Exception {
		String source = "_Check out this cool link: http://www.ibm.com_";
		String expected = "<p><em>Check out this cool link: <a href=\"http://www.ibm.com\">http://www.ibm.com</a></em></p>";
		compare(source, expected);
	}

	public final void testUrlSurroundedByCite() throws Exception {
		String source = "??Check out this cool link: http://www.ibm.com??";
		String expected = "<p><cite>Check out this cool link: <a href=\"http://www.ibm.com\">http://www.ibm.com</a></cite></p>";
		compare(source, expected);
	}

	public final void testUrlLinkFollowedByManyDots() throws Exception {
		String source = "http://localhost/some/path.... http://localhost/some/path?! http://localhost/some/path!!! ";
		String expected = "<p><a href=\"http://localhost/some/path\">http://localhost/some/path</a>.... <a href=\"http://localhost/some/path\">http://localhost/some/path</a>?! <a href=\"http://localhost/some/path\">http://localhost/some/path</a>!!!</p>";
		compare(source, expected);

	}

	/*
	 * public final void testUrlInBrackets() throws Exception { String source =
	 * "(http://localhost/some/path) (http://localhost/some/path) "; String
	 * expected = "<p>(<a
	 * href=\"http://localhost/some/path\">http://localhost/some/path</a>) (<a
	 * href=\"http://localhost/some/path\">http://localhost/some/path</a>)</p>";
	 * compare(source, expected); }
	 */
	public final void testUrlLinkWithNewLines() throws Exception {
		String source = "\nhttp://localhost/some/path\n";
		String expected = "<p><a href=\"http://localhost/some/path\">http://localhost/some/path</a></p>";
		compare(source, expected);

	}

	public final void testSeveralUrlLinks() throws Exception {
		String source = " http://localhost/some/path http://localhost/some/path ";
		String expected = "<p><a href=\"http://localhost/some/path\">http://localhost/some/path</a> <a href=\"http://localhost/some/path\">http://localhost/some/path</a></p>";
		compare(source, expected);

	}

	public final void testSeveralUrlLinksWithNewLines() throws Exception {
		String source = "\nhttp://localhost/some/path\nhttp://localhost/some/path";
		String expected = "<p><a href=\"http://localhost/some/path\">http://localhost/some/path</a><br /><a href=\"http://localhost/some/path\">http://localhost/some/path</a></p>";
		compare(source, expected);

	}

	public final void testLink() throws Exception {

		String source = "\"A link\":http://localhost some other text";

		String expected = "<p><a href=\"http://localhost\">A link</a> some other text</p>";

		compare(source, expected);

	}

	public final void testLinkInBrackets() throws Exception {

		String source = "(\"my url\":http://www.warwick.ac.uk/blah/)";

		String expected = "<p>(<a href=\"http://www.warwick.ac.uk/blah/\">my url</a>)</p>";

		compare(source, expected);

	}

	public final void testLinkFollowedByPunctuation() throws Exception {

		String source = "\"A link\":http://localhost. some other text";

		String expected = "<p><a href=\"http://localhost\">A link</a>. some other text</p>";

		compare(source, expected);

	}

	public final void testManyLinks() throws Exception {

		String source = "\"A link\":http://localhost\n\"A link\":http://localhost";

		String expected = "<p><a href=\"http://localhost\">A link</a><br /><a href=\"http://localhost\">A link</a></p>";

		compare(source, expected);

	}

	public final void testComplicatedLink() throws Exception {

		String source = "\"A link\":http://www.ezarchive.com/discotheque/AlbumSpace/4CI638KLU9/_zid-1743184/_open-/andrew_bird_-_14_-_the_happy_birthday_song.mp3;file=/andrew_bird_-_14_-_the_happy_birthday_song.mp3";

		String expected = "<p><a href=\"http://www.ezarchive.com/discotheque/AlbumSpace/4CI638KLU9/_zid-1743184/_open-/andrew_bird_-_14_-_the_happy_birthday_song.mp3;file=/andrew_bird_-_14_-_the_happy_birthday_song.mp3\">A link</a></p>";

		compare(source, expected);

	}

	public final void testSuperscript() throws Exception {

		String source = "Test^super^Test";

		String expected = "<p>Test<sup>super</sup>Test</p>";

		compare(source, expected);

	}

	public final void testSubscript() throws Exception {

		String source = " Test~sub~Test";

		String expected = "<p>Test<sub>sub</sub>Test</p>";

		compare(source, expected);

	}

	public final void testEscapeSuperscript() throws Exception {

		String source = "Test^this .\\^And ^more\\^testing ^.^";

		String expected = "<p>Test<sup>this .&#0094;And ^more&#0094;testing ^.</sup></p>";

		compare(source, expected);

	}

	public final void testEscapeSubscript() throws Exception {

		String source = " \\Test ~lots\\~of \\~escaped \\~tilde~andextra~";

		String expected = "<p>\\Test <sub>lots&#0126;of &#0126;escaped &#0126;tilde</sub>andextra~</p>";

		compare(source, expected);

	}

	public final void testDoNotStrikeOut() throws Exception {

		String source = "4-10+10-9";

		String expected = "<p>4-10+10-9</p>";

		compare(source, expected);
	}

	public final void testSmilie() throws Exception {

		String source = " :-) ";

		String expected = "<p>:-)</p>";

		compare(source, expected);
	}

	public final void testStrikeOut() throws Exception {

		String source = "4 -10+10- 9";

		String expected = "<p>4 <del>10+10</del> 9</p>";

		compare(source, expected);
	}

	// lol denide!
	/*
	 * public final void testNoStrikeOutWith2Dashes() throws Exception {
	 * 
	 * String source = "4 -- smiling face -- 9";
	 * 
	 * String expected = "<p>4 &mdash;10+10&mdash; 9</p>";
	 * 
	 * compare(source, expected); }
	 * 
	 * public final void testNoStrikeOutWithManyDashes() throws Exception {
	 * 
	 * String source = "4 --------10+10------- 9";
	 * 
	 * String expected = "<p>4
	 * &mdash;&mdash;&mdash;&mdash;10+10&mdash;&mdash;&mdash;&ndash; 9</p>";
	 * 
	 * compare(source, expected); }
	 */
	public final void testStripHtml() throws Exception {
		String source = "<div>Test *strong* Test</div>";

		String expected = "<p>Test <strong>strong</strong> Test</p>";

		compare(source, expected, true, false, false, null, null, false);
	}

	public final void testStripHtmlAfter() throws Exception {
		String source = "<div>Test *strong* Test</div>";

		String expected = "Test strong Test";

		compare(source, expected, false, true, false, null, null, false);
	}

	public final void testStripSpecificHtml() throws Exception {
		String source = "<strong>Test</strong> <sTyle>style</style> <script>test</script>";

		String expected = "<p><strong>Test</strong> style test</p>";

		compare(source, expected, false, false, false, "style,script ", null, false);
	}

	public final void testKeepEscapedHtml() throws Exception {
		String source = "&lt;strong&gt;Test&lt;/strong&gt;";

		String expected = "<p>&lt;strong&gt;Test&lt;/strong&gt;</p>";

		compare(source, expected, false, false, false, null, null, false);
	}

	public final void testOnlyAllowCertainTags() throws Exception {
		String source = "<strong>test</strong>\n\ntest";

		String expected = "<p>test</p><p>test</p>";

		compare(source, expected, false, false, false, null, "p,br", false);
	}

	public final void testNegativeLookAhead() throws Exception {

		String source = "<strong><br><p><font>Stuff</font><page><P >test</p>";
		String converted = source.replaceAll(
				"(?i)<(?!(p|br|/p|/br)(\\s.*?>|>)).*?>", "");
		String expected = "<br><p>Stuff<P >test</p>";
		assertEquals(expected, converted);

	}

	public final void testLatex() throws Exception {

		String latex = "f(x) = \\int_{-\\infty}^xe^{-t^2}dt";
		String source = "[latex]" + latex + "[/latex]";
		String expected = "<img class=\"latex\" src=\"/cgi-bin/mimetex.cgi?"
				+ URLEncoder.encode(latex.replaceAll(" ", "~"), "utf-8") + "\" alt=\""
				+ HtmlUtils.htmlEscape(latex) + "\">";
		compare(source, expected);

	}

	public final void testEscapeLatex() throws Exception {

		String latex = "f(x)=\\int_{-\\infty}^xe^{-t^2}dt";
		String source = "\\\\\n[latex]" + latex + "[/latex]\n\\\\";
		String expected = "<p>[latex]" + latex + "[/latex]</p>";
		compare(source, expected);

	}

	public final void testHtmlEntitiesInHtml() throws Exception {

		String source = "*t - t*";
		String expected = "<p><strong>t &#8211; t</strong></p>";
		compare(source, expected);

	}

	public final void testJavascriptLinks() throws Exception {

		String source = "\"Click\":javascript:alert('test'&#41;";
		String expected = "<p><a href=\"\">Click</a></p>";
		compare(source, expected);

	}

	public final void testGoodLinks() throws Exception {

		String source = "\"Click\":http://javascript.com/;";
		String expected = "<p><a href=\"http://javascript.com/;\">Click</a></p>";
		compare(source, expected);

		source = "\"Click\":https://javascript.com/;";
		expected = "<p><a href=\"https://javascript.com/;\">Click</a></p>";
		compare(source, expected);

		source = "\"Click\":/javascript.com/;";
		expected = "<p><a href=\"/javascript.com/;\">Click</a></p>";
		compare(source, expected);

		source = "\"Click\":javascript.com/;";
		expected = "<p><a href=\"javascript.com/;\">Click</a></p>";
		compare(source, expected);

	}

	public final void testFixHtml() throws Exception {

		String source = "<div>Well formed html</div>";
		String expected = "<div>Well formed html</div>";
		compare(source, expected, false, false, true, null, null, false);

		source = "<div>Badly formed html<div>";
		expected = "<div>Badly formed html  <div></div></div>";
		compare(source, expected, false, false, true, null, null, false);

		source = "<object classid=\"clsid:d27cdb6e-ae6d-11cf-96b8-444553540000\" codebase=\"http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=7,0,0,0\" width=\"290\" height=\"24\" id=\"\" align=\"middle\"><param name=\"allowScriptAccess\" value=\"sameDomain\" /><param name=\"quality\" value=\"high\" /><param name=\"ShowControls\" value=\"true\" /><param name=\"VideoBorderColor\" value=\"ffffff\" /><param name=\"WindowLess\" value=\"false\" /><param name=\"movie\" value=\"http://blogs.warwick.ac.uk/media/audio-player.swf\" /><param name=\"wmode\" value=\"transparent\" /><param name=\"FlashVars\" value=\"playerID=1&amp;bg=0xf8f8f8&amp;leftbg=0xeeeeee&amp;lefticon=0x666666&amp;rightbg=0xcccccc&amp;rightbghover=0x999999&amp;righticon=0x666666&amp;righticonhover=0xFFFFFF&amp;text=0xff3333&amp;slider=0xff3333&amp;track=0xFFFFFF&amp;border=0x666666&amp;loader=0x9FFFB8&amp;soundFile=http%3A%2F%2Fwww.ezarchive.com%2Fdiscotheque%2FAlbumSpace%2F34P292BC12%2F_zid-1687232%2F_open-%2FSince%252BI%252BLeft%252BYou.mp3%3Bfile%3D%2FSince%252BI%252BLeft%252BYou.mp3\" /><param name=\"menu\" value=\"false\" /><param name=\"VideoBorderWidth\" value=\"0\" /><param name=\"AutoStart\" value=\"false\" /><param name=\"quality\" value=\"high\" /><embed allowScriptAccess=\"sameDomain\" type=\"application/x-shockwave-flash\" pluginspage=\"http://www.macromedia.com/go/getflashplayer\" src=\"http://blogs.warwick.ac.uk/media/audio-player.swf\" width=\"290\" height=\"24\" id=\"\" name=\"\" align=\"middle\" ShowControls=\"true\" VideoBorderColor=\"ffffff\" WindowLess=\"false\" movie=\"http://blogs.warwick.ac.uk/media/audio-player.swf\" wmode=\"transparent\" FlashVars=\"playerID=1&amp;bg=0xf8f8f8&amp;leftbg=0xeeeeee&amp;lefticon=0x666666&amp;rightbg=0xcccccc&amp;rightbghover=0x999999&amp;righticon=0x666666&amp;righticonhover=0xFFFFFF&amp;text=0xff3333&amp;slider=0xff3333&amp;track=0xFFFFFF&amp;border=0x666666&amp;loader=0x9FFFB8&amp;soundFile=http%3A%2F%2Fwww.ezarchive.com%2Fdiscotheque%2FAlbumSpace%2F34P292BC12%2F_zid-1687232%2F_open-%2FSince%252BI%252BLeft%252BYou.mp3%3Bfile%3D%2FSince%252BI%252BLeft%252BYou.mp3\" menu=\"false\" VideoBorderWidth=\"0\" AutoStart=\"false\" quality=\"high\"></embed></object></div></div><i>";
        expected = "<object classid=\"clsid:d27cdb6e-ae6d-11cf-96b8-444553540000\" codebase=\"http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=7,0,0,0\" width=\"290\" height=\"24\" align=\"middle\">  <param name=\"allowScriptAccess\" value=\"sameDomain\" />  <param name=\"quality\" value=\"high\" />  <param name=\"ShowControls\" value=\"true\" />  <param name=\"VideoBorderColor\" value=\"ffffff\" />  <param name=\"WindowLess\" value=\"false\" />  <param name=\"movie\" value=\"http://blogs.warwick.ac.uk/media/audio-player.swf\" />  <param name=\"wmode\" value=\"transparent\" />  <param name=\"FlashVars\" value=\"playerID=1&amp;bg=0xf8f8f8&amp;leftbg=0xeeeeee&amp;lefticon=0x666666&amp;rightbg=0xcccccc&amp;rightbghover=0x999999&amp;righticon=0x666666&amp;righticonhover=0xFFFFFF&amp;text=0xff3333&amp;slider=0xff3333&amp;track=0xFFFFFF&amp;border=0x666666&amp;loader=0x9FFFB8&amp;soundFile=http%3A%2F%2Fwww.ezarchive.com%2Fdiscotheque%2FAlbumSpace%2F34P292BC12%2F_zid-1687232%2F_open-%2FSince%252BI%252BLeft%252BYou.mp3%3Bfile%3D%2FSince%252BI%252BLeft%252BYou.mp3\" />  <param name=\"menu\" value=\"false\" />  <param name=\"VideoBorderWidth\" value=\"0\" />  <param name=\"AutoStart\" value=\"false\" />  <param name=\"quality\" value=\"high\" />  <embed allowscriptaccess=\"sameDomain\" type=\"application/x-shockwave-flash\" pluginspage=\"http://www.macromedia.com/go/getflashplayer\" src=\"http://blogs.warwick.ac.uk/media/audio-player.swf\" width=\"290\" height=\"24\" align=\"middle\" showcontrols=\"true\" videobordercolor=\"ffffff\" windowless=\"false\" movie=\"http://blogs.warwick.ac.uk/media/audio-player.swf\" wmode=\"transparent\" flashvars=\"playerID=1&amp;bg=0xf8f8f8&amp;leftbg=0xeeeeee&amp;lefticon=0x666666&amp;rightbg=0xcccccc&amp;rightbghover=0x999999&amp;righticon=0x666666&amp;righticonhover=0xFFFFFF&amp;text=0xff3333&amp;slider=0xff3333&amp;track=0xFFFFFF&amp;border=0x666666&amp;loader=0x9FFFB8&amp;soundFile=http%3A%2F%2Fwww.ezarchive.com%2Fdiscotheque%2FAlbumSpace%2F34P292BC12%2F_zid-1687232%2F_open-%2FSince%252BI%252BLeft%252BYou.mp3%3Bfile%3D%2FSince%252BI%252BLeft%252BYou.mp3\" menu=\"false\" videoborderwidth=\"0\" autostart=\"false\" quality=\"high\"></embed></object><i></i>";
		compare(source, expected, false, false, true, null, null, false);

		source = "This is excellent news :) I'm also hoping that WGA will let me register soon...";
		expected = "<p>This is excellent news :) I’m also hoping that <span class=\"caps\">WGA</span> will let me register soon…</p>";
		compare(source, expected, false, false, true, null, null, false);
	}

	/* Textile 2 Tests */

	public final void testTables() {

		String source = "Here is a table\n\ntable{border:1px solid black}.\n|\\2. this is|{background:red;width:200px}. a|^<>{height:200px}. row|\n|this|<>{padding:10px}. is|^. another|(bob#bob). row|";
		String expected = "<p>Here is a table</p><table style=\"border:1px solid black;\"><tr><td colspan=\"2\">this is</td><td style=\"background:red;width:200px;\">a</td><td style=\"vertical-align:top;height:200px;text-align:justify;\">row</td></tr><tr><td>this</td><td style=\"padding:10px;text-align:justify;\">is</td><td style=\"vertical-align:top;\">another</td><td class=\"bob\" id=\"bob\">row</td></tr></table>";
		compare(source, expected);

	}

	public final void testTablesWithHeaderRow() {

		String source = "Here is a table\n\n|_. this|_. is|_. a|_. header|\n|\\2. this is|{background:red;width:200px}. a|^<>{height:200px}. row|\n|this|<>{padding:10px}. is|^. another|(bob#bob). row|";
		String expected = "<p>Here is a table</p><table><tr><th>this</th><th>is</th><th>a</th><th>header</th></tr><tr><td colspan=\"2\">this is</td><td style=\"background:red;width:200px;\">a</td><td style=\"vertical-align:top;height:200px;text-align:justify;\">row</td></tr><tr><td>this</td><td style=\"padding:10px;text-align:justify;\">is</td><td style=\"vertical-align:top;\">another</td><td class=\"bob\" id=\"bob\">row</td></tr></table>";
		compare(source, expected);

	}

	public final void testStyles() {

		String source = "Let's get some %{color:red}red% in here";
		String expected = "<p>Let&#8217;s get some <span style=\"color:red;\">red</span> in here</p>";
		compare(source, expected);

	}

	public final void testId() {

		String source = "Let's get some %(#red)red% in here";
		String expected = "<p>Let&#8217;s get some <span id=\"red\">red</span> in here</p>";
		compare(source, expected);

	}

	public final void testClass() {

		String source = "Let's get some %(red)red% in here";
		String expected = "<p>Let&#8217;s get some <span class=\"red\">red</span> in here</p>";
		compare(source, expected);

	}

	public final void testLanguage() {

		String source = "Let's get some %[fr]french% in here";
		String expected = "<p>Let&#8217;s get some <span lang=\"fr\">french</span> in here</p>";
		compare(source, expected);

	}

	public final void testAlignLeft() {

		String source = "Here's an !<http://blogs.warwick.ac.uk/image.jpg! image on the left.";
		String expected = "<p>Here&#8217;s an <div style=\"float:left\"><img src=\"http://blogs.warwick.ac.uk/image.jpg\" alt=\"\" /></div> image on the left.</p>";
		compare(source, expected);

	}

	public final void testAlignRight() {

		String source = "Here's an !>http://blogs.warwick.ac.uk/image.jpg! image on the right.";
		String expected = "<p>Here&#8217;s an <div style=\"float:right\"><img src=\"http://blogs.warwick.ac.uk/image.jpg\" alt=\"\" /></div> image on the right.</p>";
		compare(source, expected);

	}

	public final void testAlignCenter() {

		String source = "Here's an !=http://blogs.warwick.ac.uk/image.jpg! image in the center.";
		;
		String expected = "<p>Here&#8217;s an <div style=\"float:center\"><img src=\"http://blogs.warwick.ac.uk/image.jpg\" alt=\"\" /></div> image in the center.</p>";
		compare(source, expected);

	}

	public final void testJustify() {

		String source = "Here's some normal text\n\np<>. Here's some justified text\n\nMore normal text";
		String expected = "<p>Here&#8217;s some normal text</p><p style=\"text-align:justify;\">Here&#8217;s some justified text</p><p>More normal text</p>";
		compare(source, expected);

	}

	public final void testCitation() {

		String source = "Here's some text ??holy crap a citation?? holy crap!";
		String expected = "<p>Here&#8217;s some text <cite>holy crap a citation</cite> holy crap!</p>";
		compare(source, expected);

	}

	public final void testEscapingInsidePreCode() {

		String source = "<pre>\n<code>\n	$text = str_replace(\"<p>%::%</p>\",\"\",$text);\n	$text = str_replace(\"%::%</p>\",\"\",$text);\n	$text = str_replace(\"%::%\",\"\",$text);\n\n</code>\n</pre>";
		String expected = "<pre><code>    $text = str_replace(\"&lt;p&gt;%::%&lt;/p&gt;\",\"\",$text);    $text = str_replace(\"%::%&lt;/p&gt;\",\"\",$text);    $text = str_replace(\"%::%\",\"\",$text);</code><br /></pre>";
		compare(source, expected);

	}

	public final void testComplexEscaping() {
		String source = "This isn't code.\n\n" +
				"The code that made that happen:\n" +
				"\\\\\n" +
				"[media width='640' height='480']http://www.youtube.com/watch?v=06D_EFNGmOQ[/media]\n" +
				"\\\\\n\n" +
				"And some latex:\n\n" +
				"[latex]f(x)=\\int_{-\\infty}^xe^{-t^2}dt[/latex]\n\n" +
				"And the code that made that happen:\n" +
				"\\\\\n" +
				"[latex]f(x)=\\int_{-\\infty}^xe^{-t^2}dt[/latex]\n" +
				"\\\\\n" +
				"So you see, my friends:";
		String expected = "<p>This isn&#8217;t code.</p><p>The code that made that happen:<p>[media width='640' height='480']http://www.youtube.com/watch?v=06D_EFNGmOQ[/media]</p></p><p>And some latex:</p><img class=\"latex\" src=\"/cgi-bin/mimetex.cgi?f%28x%29%3D%5Cint_%7B-%5Cinfty%7D%5Exe%5E%7B-t%5E2%7Ddt\" alt=\"f(x)=\\int_{-\\infty}^xe^{-t^2}dt\"><p>And the code that made that happen:<p>[latex]f(x)=\\int_{-\\infty}^xe^{-t^2}dt[/latex]</p></p><p>So you see, my friends:</p>";
		compare(source, expected);
	}

	public final void testComplexEscapingWithWindowsLineBreaks() {
		String source = "This isn't code.\r\n\r\nThe code that made that happen:\r\n\\\\\r\n[media width='640' height='480']http://www.youtube.com/watch?v=06D_EFNGmOQ[/media]\r\n\\\\\r\n\r\nAnd some latex:\r\n\r\n[latex]f(x)=\\int_{-\\infty}^xe^{-t^2}dt[/latex]\r\n\r\nAnd the code that made that happen:\r\n\\\\\r\n[latex]f(x)=\\int_{-\\infty}^xe^{-t^2}dt[/latex]\r\n\\\\\r\nSo you see, my friends:";
		String expected = "<p>This isn&#8217;t code.</p><p>The code that made that happen:<p>[media width='640' height='480']http://www.youtube.com/watch?v=06D_EFNGmOQ[/media]</p></p><p>And some latex:</p><img class=\"latex\" src=\"/cgi-bin/mimetex.cgi?f%28x%29%3D%5Cint_%7B-%5Cinfty%7D%5Exe%5E%7B-t%5E2%7Ddt\" alt=\"f(x)=\\int_{-\\infty}^xe^{-t^2}dt\"><p>And the code that made that happen:<p>[latex]f(x)=\\int_{-\\infty}^xe^{-t^2}dt[/latex]</p></p><p>So you see, my friends:</p>";
		compare(source, expected);
	}

	public final void testSingleCharacters() {
		String source = "Holy *a* frigging *cow* monster!";
		String expected = "<p>Holy <strong>a</strong> frigging <strong>cow</strong> monster!</p>";
		compare(source, expected);
	}

	public final void testNonGreedyMatching() {
		String source = "Holy *aa* frigging *cow* monster!";
		String expected = "<p>Holy <strong>aa</strong> frigging <strong>cow</strong> monster!</p>";
		compare(source, expected);
	}

	public final void testRelativeImageUrl() {
		String source = "!/here/isanimage.gif!";
		String expected = "<p><img src=\"/here/isanimage.gif\" alt=\"\" /></p>";
		compare(source, expected);
	}
	
	public final void testScriptTags() {
		String source = "<SCRIPT type='text/javascript'>var simon = \"victory\";</scrIPT>";
		compare(source,source);
	}

	public final void testCreativeCommons() {
		String source = "<!--Creative Commons License--><div style='margin-left: auto; margin-right: auto; text-align:center;'><a rel=\"license\" href=\"http://creativecommons.org/licenses/by-nc/2.5/\"><img alt=\"Creative Commons License\" border=\"0\" src=\"http://creativecommons.org/images/public/somerights20.png\"/></a></div><br />This work is licensed under a <a rel=\"license\" href=\"http://creativecommons.org/licenses/by-nc/2.5/\">Creative Commons Attribution NonCommercial 2.5 License</a>.<!--/Creative Commons License--><!-- <rdf:RDF xmlns=\"http://web.resource.org/cc/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"><Work rdf:about=\"\"><license rdf:resource=\"http://creativecommons.org/licenses/by-nc/2.5/\" /><dc:title>M. Bison</dc:title><dc:date>2006</dc:date><dc:description>Mat Mannion's weblog</dc:description><dc:creator><Agent><dc:title>Mathew Mannion</dc:title></Agent></dc:creator><dc:rights><Agent><dc:title>Mathew Mannion</dc:title></Agent></dc:rights><dc:source rdf:resource=\"http://blogs.warwick.ac.uk/mmannion/\" /></Work><License rdf:about=\"http://creativecommons.org/licenses/by-nc/2.5/\"><permits rdf:resource=\"http://web.resource.org/cc/Reproduction\"/><permits rdf:resource=\"http://web.resource.org/cc/Distribution\"/><requires rdf:resource=\"http://web.resource.org/cc/Notice\"/><requires rdf:resource=\"http://web.resource.org/cc/Attribution\"/><permits rdf:resource=\"http://web.resource.org/cc/DerivativeWorks\"/><prohibits rdf:resource=\"http://web.resource.org/cc/CommercialUse\"/></License></rdf:RDF> -->";
		// String expected = "<div style='margin-left: auto; margin-right: auto;
		// text-align:center;'><a rel=\"license\"
		// href=\"http://creativecommons.org/licenses/by-nc/2.5/\"><img
		// alt=\"Creative Commons License\" border=\"0\"
		// src=\"http://creativecommons.org/images/public/somerights20.png\"/></a></div><br
		// />This work is licensed under a <a rel=\"license\"
		// href=\"http://creativecommons.org/licenses/by-nc/2.5/\">Creative
		// Commons Attribution NonCommercial 2.5 License</a>.";
		compare(source, source);
	}

	public final void testEmailAddress() {
		String source = "M.J.Mannion@warwick.ac.uk M.J.Mannion@warwick.ac.uk";
		String expected = "<p><a href=\"mailto:M.J.Mannion@warwick.ac.uk\">M.J.Mannion@warwick.ac.uk</a> <a href=\"mailto:M.J.Mannion@warwick.ac.uk\">M.J.Mannion@warwick.ac.uk</a></p>";
		compare(source, expected);
	}

	public final void testAutomaticCodeConversion() {
		/*
		 * The original Textile2 behaviour is to convert any line it sees that
		 * starts with whitespace into code as if it was in @@ tags. We don't
		 * want this, so we're looking to see that it DOESN'T do that!
		 */
		String source = "Here is some text\n\n   Here is some text with leading whitespace\n\nHere is more text";
		String expected = "<p>Here is some text</p><p>Here is some text with leading whitespace</p><p>Here is more text</p>";
		compare(source, expected);
	}
	
	public final void testDestroyEmbedTags() {
		String source="<embed src=\"http://www.netwinner.com/flash/banners/468x60.swf\" \nquality=\"high\" bgcolor=\"#ffffff\" width=\"468\" height=\"60\" name=\"myNetwinnerBanner468x60\" type=\"application/x-shockwave-flash\" pluginspage=\"http://www.adobe.com/shockwave/download/download.cgi?P1_Prod_Version=ShockwaveFlash\"> \n\n";
		String expected="";
		//strip all html before convert
		compare(source,expected, true, false, false, null, null, false);
	}
	
	public final void testDontDestroyGoodThings() {
		String source="I think that 9 < 5, but I also believe that 1 > 2 because I'm a stupid.";
		String expected="<p>I think that 9 &lt; 5, but I also believe that 1 &gt; 2 because I&#8217;m a stupid.</p>";
		//strip all html before convert
		compare(source,expected, true, false, false, null, null, false);
	}
	
	public final void testHongfengSidebar() {
		String source="<notextile>\n<!-- Site meter -->\n<script type=\"text/javascript\" src=\"http://s28.sitemeter.com/js/counter.js?site=s28warwickjava\">\n</script>\n<noscript>\n<a href=\"http://s28.sitemeter.com/stats.asp?site=s28warwickjava\" target=\"_top\">\n<img src=\"http://s28.sitemeter.com/meter.asp?site=s28warwickjava\" alt=\"Site Meter\" border=\"0\"/></a>\n</noscript>\n<!-- Copyright (c)2006 Site Meter -->\n</notextile>";
		String expected="<!-- Site meter --><script type=\"text/javascript\" src=\"http://s28.sitemeter.com/js/counter.js?site=s28warwickjava\"></script><noscript><a href=\"http://s28.sitemeter.com/stats.asp?site=s28warwickjava\" target=\"_top\"><img src=\"http://s28.sitemeter.com/meter.asp?site=s28warwickjava\" alt=\"Site Meter\" border=\"0\"/></a></noscript><!-- Copyright (c)2006 Site Meter -->";
		compare(source,expected);
		
	}
	
	public final void testHongfengSidebar2() {
		String source = "<!--Html comment-->text<!--html comment-->text<!------html comment  -------->";
		compare(source,source);
	}
	
	public final void testNastyLink() {
		String source="http://www.hello.com/here_is_my_best_effort_to-break-textile.mp3";
		String expected="<p><a href=\"http://www.hello.com/here_is_my_best_effort_to-break-textile.mp3\">http://www.hello.com/here_is_my_best_effort_to-break-textile.mp3</a></p>";
		compare(source,expected);
	}
	
	public final void testNastyHtmlLink() {
		String source="<p><a href=\"http://www.hello.com/here_is_my_best_effort_to-break-textile.mp3\">http://www.hello.com/here_is_my_best_effort_to-break-textile.mp3</a></p>";
		compare(source,source);
	}
	
	public final void testStrangeText() {
		String source="\\-x-Mimisiku-X-ukisimiM-x-\\";
		String expected="<p>\\<del>x-Mimisiku-X-ukisimiM-x</del>\\</p>";
		compare(source,expected);
	}
	
	public final void testURLConverter() {
		String source="<font color=\"#660066\"> Quote a link: http://forums.warwick.ac.uk</font>";
		String expected="<font color=\"#660066\"> Quote a link: <a href=\"http://forums.warwick.ac.uk\">http://forums.warwick.ac.uk</a></font>";
		compare(source,expected);
	}
	
	public final void testWikipediaLink() {
		String source= "You can find it here http://en.wikipedia.org/wiki/Strict_liability_(criminal)";
		String expected="<p>You can find it here <a href=\"http://en.wikipedia.org/wiki/Strict_liability_(criminal)\">http://en.wikipedia.org/wiki/Strict_liability_(criminal)</a></p>";
		compare(source,expected);
	}
	
	//FIXME
//	public final void testWikipediaLinkExplicit() {
//		String source="You can find it \"here\":http://en.wikipedia.org/wiki/Strict_liability_(criminal)";
//		String expected = "<p>You can find it <a href=\"http://en.wikipedia.org/wiki/Strict_liability_(criminal)\">here</a></p>";
//		compare(source,expected);
//	}
	
	public final void testGlyphConversionAtStartOfLine() {
		String source="(C) Mat Mannion 2006";
		String expected="<p>&#169; Mat Mannion 2006</p>";
		compare(source,expected);
	}
	
	//FIXME [TXT-10]
//	public final void testDefineListsUsingNumbers() {
//		String source = "1. Item 1\n2. Item 2\n3. Item 3";
//		String expected = "<ol>\n<li>Item 1</li>\n<li>Item 2</li>\n<li>Item 3</li>\n</ol>";
//		compare(source,expected);
//	}

	//FIXME [TXT-29]
//	public final void testMixedLists() {
//		String source="* one\n* two\n# three\n* four\n* five\n# six\n# seven\n# eight\n* nine";
//		String expected="<ul><li>one</li><li>two</li><ol><li>three</li><ul><li>four</li><li>five</li><ol><li>six</li><li>seven</li><li>eight</li><ul><li>nine</li></ul></li></ol></li></ul></li></ol></li></ul>";
//		compare(source,expected);
//	}
	
	public final void testListsWithEllipsis() {
		String source="* This is bulleted...";
		String expected="<ul><li>This is bulleted&#8230;</li></ul>";
		compare(source,expected);
	}
	
	public final void testBlockQuoteWithEllipsis() {
		String source="bq. This is bulleted...";
		String expected="<blockquote><p>This is bulleted&#8230;</p></blockquote>";
		compare(source,expected);
	}
	
	public final void testSmartQuoteConversion() {
		String source="A line with a smartquote's there";
		String expected="<p>A line with a smartquote&#8217;s there</p>";
		compare(source,expected);
	}
	
	public final void testMixedIndentedLists() {
		String source="* one\n* two\n*# three\n* four\n* five\n*# six\n*# seven\n*# eight\n*#* nine";
		String expected="<ul><li>one</li><li>two<ol><li>three</li></ol></li><li>four</li><li>five<ol><li>six</li><li>seven</li><li>eight<ul><li>nine</li></ul></li></ol></li></ul>";
		compare(source,expected);
	}
	
	public final void testEntityConversionInTags() {
		String source="*Some stuff - some more*";
		String expected="<p><strong>Some stuff &#8211; some more</strong></p>";
		compare(source,expected);
	}
	
	//FIXME [TXT-38]
//	public final void testNoBrsInHtml() {
//		String source="<table>\n<tr>\n<td>\nTest\n</td>\n</tr>\n</table>";
//		String expected="<table>\n<tr>\n<td>\nTest\n</td>\n</tr>\n</table>";
//		compare(source,expected);
//	}
	
	//FIXME [TXT-52]
//	public final void testListsWithBlankLines() {
//		String source="*  Red\n\n* Green\n\n* Blue";
//		String expected="<ul><li> Red<br /></li><li>Green<br /></li><li>Blue<br /></li></ul>";
//		compare(source,expected);
//	}
	
	public final void testNoFollowLinks() {
		String source="Blah blah <a href=\"http://blogs.warwick.ac.uk/\">Check out my kick-ass website!</a>";
		
		//without link changing
		String expected="<p>Blah blah <a href=\"http://blogs.warwick.ac.uk/\">Check out my kick-ass website!</a></p>";
		
		compare(source, expected, false, false, false, null, null, false);
		
		//with link changing
		expected="<p>Blah blah <a rel=\"nofollow\" href=\"http://blogs.warwick.ac.uk/\">Check out my kick-ass website!</a></p>";
		
		compare(source, expected, false, false, false, null, null, true);
	}
	
	public final void testRemoveLeadingNbsp() {
		String source="<p>Here is a normal paragraph</p><p>&nbsp;And with a leading nbsp</p><h1>&nbsp;And a heading&nbsp;with a leading nbsp</h1>";
		String expected="<p>Here is a normal paragraph</p><p>And with a leading nbsp</p><h1>And a heading&nbsp;with a leading nbsp</h1>";
		assertEquals(expected,new RemoveLeadingNbspTransformer().apply(new MutableContent(null, source)).getContent());
	}
	
	public final void testMaliciousImgTagCodeTextile() {
	    String source="!http://www.google.com/noimage.jpg\"onerror=\"window.location='http://www.nastysite.com';!";
	    // should not be converted at all
	    String expected="<p>!http://www.google.com/noimage.jpg&#8221;onerror=&#8221;window.location=&#8217;http://www.nastysite.com&#8217;;!</p>";
	    compare(source, expected);
	}
	
	public final void testMaliciousImgTagCodeHtml() {
        String source="<img src=\"http://www.google.com/noimage.jpg\" onerror=\"window.location='http://www.nastysite.com';\" />";
        String expected="<p><img src=\"http://www.google.com/noimage.jpg\" border=\"0\" /></p>";
        compare(source, expected, false, false, true, null, null, false);
    }
	
	public final void testGlyphCharactersInAutoURLs() {
		String source = "Here is URL one: http://www.warwick.ac.uk/~cuscav/link1.jpg and link two: http://www.warwick.ac.uk/~cuscav/link2.jpg and hopefully we won't have strikethroughs, but we will have auto-links!";
		String expected = "<p>Here is <span class=\"caps\">URL</span> one: <a href=\"http://www.warwick.ac.uk/%7Ecuscav/link1.jpg\">http://www.warwick.ac.uk/%7Ecuscav/link1.jpg</a> and link two: <a href=\"http://www.warwick.ac.uk/%7Ecuscav/link2.jpg\">http://www.warwick.ac.uk/%7Ecuscav/link2.jpg</a> and hopefully we won&#8217;t have strikethroughs, but we will have auto-links!</p>";
		compare(source,expected);
	}
	
	public final void testCodeInPreCode() {
		String source = "<pre><code><b>This is some bold text</b></code></pre>";
		String expected = "<pre><code>&lt;b&gt;This is some bold text&lt;/b&gt;</code></pre>";
		compare(source,expected);
	}
	
	public final void testNoGlyphConversionInPreCode() {
		String source = "\\\\\\\nstatic {\n\tcharToEntity = new HashMap<String, String>();\n\tcharToEntity.put(\" \", \"&nbsp;\");\n\tcharToEntity.put(\"¡\", \"&iexcl;\");\n\tcharToEntity.put(\"¢\", \"&cent;\");\n\tcharToEntity.put(\"£\", \"&pound;\");}\n\\\\\\";
		String expected = "<pre><code>static {    charToEntity = new HashMap&lt;String, String&gt;();    charToEntity.put(\" \", \"&amp;nbsp;\");    charToEntity.put(\"¡\", \"&amp;iexcl;\");    charToEntity.put(\"¢\", \"&amp;cent;\");    charToEntity.put(\"£\", \"&amp;pound;\");}</code></pre>";
		compare(source,expected);
	}
	
	public final void testUseDefaultMP3Player() {
		String source = "[media]blah.mp3[/media]";
		
		TextileString text = new TextileString(source);
		String output = text.getHtml();
		
		assertTrue(output.contains("wimpy.swf"));
		assertFalse(output.contains("mp3player.swf"));
	}
	
	public final void testForceAlternativeMP3Player() {
		String source = "[media]blah.mp3[/media]";
		
		TextileString text = new TextileString(source);
		text.setOptions(EnumSet.of(TransformationOptions.alwaysUseAlternativeMp3Player));
		
		String output = text.getHtml();
		
		assertFalse(output.contains("wimpy.swf"));
		assertTrue(output.contains("mp3player.swf"));
	}
	
	// UTL-39 can't fix
//	public void testLinkWithUnderscores() {
//		for (String link : new String[] {
//				"http://www2.warwick.ac.uk/site/carer-administered_midazolam_-__final_issued_17apr09sb.pdf",
//				"http://www2.warwick.ac.uk/_file_.pdf",
//				"http://www2.warwick.ac.uk/site/carer-administered_midazolam_-__final_issued_17apr09sb_.pdf"
//			}) {
//			TextileString text = new TextileString(String.format("%s", link));
//			String output = text.getHtml();
//			String expected = String.format("<p><a href=\"%s\">%s</a></p>",link,link);
//			assertFalse("Shouldn't convert these underscores to em in " + link, output.contains("<em"));
//			assertEquals("Failed linkify for " + link, expected, output);
//		}
//	}
	
	public void testNoSpaceOnAutoLinking() {
		String source = "Hey check this out:\n\nhttp://www.warwick.ac.uk \n\nAwesome, eh!";
		// om nom nom extra space is gone!
		String expected = "<p>Hey check this out:</p><p><a href=\"http://www.warwick.ac.uk\">http://www.warwick.ac.uk</a></p><p>Awesome, eh!</p>";
		
		compare(source, expected);
	}
	
	public void testNoSpaceOnAutoLinkingWithDot() {
		String source = "Hey check this out:\n\nhttp://www.warwick.ac.uk.\n\nAwesome, eh!";
		// om nom nom extra space is gone!
		String expected = "<p>Hey check this out:</p><p><a href=\"http://www.warwick.ac.uk\">http://www.warwick.ac.uk</a>.</p><p>Awesome, eh!</p>";
		
		compare(source, expected);
	}

	private void compare(String source, String expected,
			boolean stripAllHtmlBeforeConvert,
			boolean stripAllHtmlAfterConvert, boolean fixHtml,
			String disallowTags, String onlyAllowTags, boolean addNoFollow) {
		TextileString text = new TextileString(source);
		
		text.setDisallowTags(disallowTags);
		text.setOnlyAllowTags(onlyAllowTags);
		text.setStripAllHtmlAfterConvert(stripAllHtmlAfterConvert);
		text.setStripAllHtmlBeforeConvert(stripAllHtmlBeforeConvert);
		text.setCorrectHtml(fixHtml);
		text.setAddNoFollow(addNoFollow);

		String htmlText = text.getHtml();
		
		if (htmlText != null) {
			htmlText = htmlText.replaceAll("\n", "");
			htmlText = htmlText.replaceAll("\t", "");
		}

		assertEquals("Converted text should match", expected, htmlText);
	}

	private void compare(String source, String expected) {
		compare(source, expected, false, false, false, null, null, false);
	}

	@Override
	protected void setUp() throws Exception {
	    System.setProperty(
                TextileTextTransformer.TEXTILE_SERVICE_LOCATION_PROPERTY_KEY,
                "http://localhost:2000/textile");
        
        System.setProperty("textile.media.mp3WimpyPlayerLocation", "wimpy.swf");
        System.setProperty("textile.media.mp3AlternatePlayerLocation", "mp3player.swf");
        System.setProperty("textile.media.quicktimePreviewImage", "qt.png");
        System.setProperty("textile.media.windowsMediaPreviewImage", "wmp.jpg");
        System.setProperty("textile.media.flvPlayerLocation", "flyplayer.swf");
        System.setProperty("textile.latex.location", "/cgi-bin/mimetex.cgi");
	}

}