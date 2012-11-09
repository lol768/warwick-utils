/**
 * Created by Michael Simons, michael-simons.eu
 * and released under The BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 *
 * Copyright (c) 2010, Michael Simons
 * All rights reserved.
 *
 * Redistribution  and  use  in  source   and  binary  forms,  with  or   without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source   code must retain   the above copyright   notice,
 *   this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary  form must reproduce  the above copyright  notice,
 *   this list of conditions  and the following  disclaimer in the  documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name  of  michael-simons.eu   nor the names  of its contributors
 *   may be used  to endorse   or promote  products derived  from  this  software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS  PROVIDED BY THE  COPYRIGHT HOLDERS AND  CONTRIBUTORS "AS IS"
 * AND ANY  EXPRESS OR  IMPLIED WARRANTIES,  INCLUDING, BUT  NOT LIMITED  TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL  THE COPYRIGHT HOLDER OR CONTRIBUTORS  BE LIABLE
 * FOR ANY  DIRECT, INDIRECT,  INCIDENTAL, SPECIAL,  EXEMPLARY, OR  CONSEQUENTIAL
 * DAMAGES (INCLUDING,  BUT NOT  LIMITED TO,  PROCUREMENT OF  SUBSTITUTE GOODS OR
 * SERVICES; LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT  LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE  USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.warwick.util.content.oembed;

import java.util.List;
import java.util.regex.Pattern;

import uk.ac.warwick.util.web.Uri;
import uk.ac.warwick.util.web.Uri.UriException;
import uk.ac.warwick.util.web.UriBuilder;

/**
 * @author Michael J. Simons
 */
public class DefaultOEmbedProvider implements OEmbedProvider {
    
    private String name;

    private String endpoint;

    private String format;

    private Integer maxWidth;

    private Integer maxHeight;

    private List<String> urlSchemes;
		
	public Uri toApiUrl(final String url) throws UriException {
		final UriBuilder uri;

		if(this.getEndpoint().toLowerCase().contains("%{format}"))
			uri = new UriBuilder(Uri.parse(getEndpoint().replaceAll(Pattern.quote("%{format}"), getFormat())));
		else {
			uri = new UriBuilder(Uri.parse(getEndpoint()));
			uri.addQueryParameter("format", getFormat());
		}
		
		uri.addQueryParameter("url", url);
		if (this.getMaxWidth() != null) {
			uri.addQueryParameter("maxwidth", this.getMaxWidth().toString());
		}
		
		if (this.getMaxHeight() != null) {
			uri.addQueryParameter("maxheight", this.getMaxHeight().toString());
		}		
		
		return uri.toUri();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getEndpoint() {
		return endpoint;
	}
	
	public void setEndpoint(String apiEndpoint) {
		this.endpoint = apiEndpoint;
	}
	
	public List<String> getUrlSchemes() {
		return urlSchemes;
	}
	
	public void setUrlSchemes(List<String> urlSchemes) {
		this.urlSchemes = urlSchemes;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public Integer getMaxWidth() {
		return maxWidth;
	}

	public void setMaxWidth(Integer maxWidth) {
		this.maxWidth = maxWidth;
	}

	public Integer getMaxHeight() {
		return maxHeight;
	}

	public void setMaxHeight(Integer maxHeight) {
		this.maxHeight = maxHeight;
	}
}