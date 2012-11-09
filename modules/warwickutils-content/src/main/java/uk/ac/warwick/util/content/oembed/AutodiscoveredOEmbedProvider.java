/**
 * Created by Michael Simons, michael-simons.eu and released under The BSD
 * License http://www.opensource.org/licenses/bsd-license.php Copyright (c)
 * 2010, Michael Simons All rights reserved. Redistribution and use in source
 * and binary forms, with or without modification, are permitted provided that
 * the following conditions are met: * Redistributions of source code must
 * retain the above copyright notice, this list of conditions and the following
 * disclaimer. * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. *
 * Neither the name of michael-simons.eu nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT
 * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.warwick.util.content.oembed;

import java.util.List;

import uk.ac.warwick.util.web.Uri;
import uk.ac.warwick.util.web.Uri.UriException;

import com.google.common.collect.ImmutableList;

/**
 * @author Michael J. Simons
 */
public class AutodiscoveredOEmbedProvider implements OEmbedProvider {
    
    /** The autodiscovered provider supports only one scheme... */
    private final ImmutableList<String> urlSchemes;

    private final Uri apiUrl;

    private final String name;

    private String format;

    public AutodiscoveredOEmbedProvider(final String originalUrl, final Uri apiUrl, final String format) {
        this.urlSchemes = ImmutableList.of(originalUrl);
        this.apiUrl = apiUrl;
        this.name = this.apiUrl.getAuthority();
        this.format = format;
    }

    public String getName() {
        return this.name;
    }

    public String getFormat() {
        return this.format;
    }

    public List<String> getUrlSchemes() {
        return this.urlSchemes;
    }

    public Uri toApiUrl(String url) throws UriException {
        return this.apiUrl;
    }
}