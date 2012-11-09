/**
 * Created by Michael Simons, michael-simons.eu
 * and released under The BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 *
 * Copyright (c) 2011, Michael Simons
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

import java.util.ArrayList;

/**
 * @author Michael J. Simons, 2011-11-09
 */
public class OEmbedProviderBuilder {
	private final DefaultOEmbedProvider oembedProvider = new DefaultOEmbedProvider();
	
	public OEmbedProviderBuilder withName(final String name) {
		this.oembedProvider.setName(name);
		return this;
	}
	
	public OEmbedProviderBuilder withFormat(final String format) {
		this.oembedProvider.setFormat(format);
		return this;
	}
	
	public OEmbedProviderBuilder withEndpoint(final String endpoint) {
		this.oembedProvider.setEndpoint(endpoint);
		return this;
	}
	
	public OEmbedProviderBuilder withUrlSchemes(final String... urlSchemes) {
		if(urlSchemes != null) {
			if(this.oembedProvider.getUrlSchemes() == null)
				this.oembedProvider.setUrlSchemes(new ArrayList<String>());
			for(String urlScheme : urlSchemes)
				this.oembedProvider.getUrlSchemes().add(urlScheme);
		}
		return this;
	}
	
	public OEmbedProviderBuilder withMaxHeight(final Integer maxHeight) {
		this.oembedProvider.setMaxHeight(maxHeight);
		return this;
	}
	
	public OEmbedProviderBuilder withMaxWidth(final Integer maxWidth) {
		this.oembedProvider.setMaxWidth(maxWidth);
		return this;
	}

	public OEmbedProvider build() {
		return oembedProvider;
	}
}