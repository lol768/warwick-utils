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

import java.util.HashMap;

/**
 * @author Michael J. Simons, 2011-11-09
 */
public class OEmbedBuilder {
	private final OEmbed oembed;
	
	public OEmbedBuilder() {
		this.oembed = new OEmbed();
	}

	public OEmbedBuilder withAutodiscovery(boolean autodiscovery) {
		oembed.setAutodiscovery(autodiscovery);
		return this;
	}

	public OEmbedBuilder withConsumer(String consumer) {
		oembed.setConsumer(consumer);
		return this;
	}
	
	/**
	 * Adds the given providers to the map if the format is supported
	 * @param provider
	 * @return
	 */
	public OEmbedBuilder withProviders(final OEmbedProvider... providers) {
		if(providers != null) {
			if(oembed.getProvider() == null)
				oembed.setProvider(new HashMap<String, OEmbedProvider>());
			for(OEmbedProvider provider : providers) {
				if(!this.oembed.getParser().containsKey(provider.getFormat().toLowerCase()))
					throw new RuntimeException(String.format("Invalid format %s", provider.getFormat()));
				oembed.getProvider().put(provider.getName(), provider);
			}
		}
		return this;
	}
	
	public OEmbedBuilder withHandlers(final OEmbedResponseHandler... handlers) {
		if(handlers != null) {
			if(oembed.getHandler() == null)
				oembed.setHandler(new HashMap<String, OEmbedResponseHandler>());
			for(OEmbedResponseHandler handler : handlers) {
				this.oembed.getHandler().put(handler.getFor(), handler);
			}
		}
		return this;
	}

	public OEmbed build() {
		return oembed;
	}
}