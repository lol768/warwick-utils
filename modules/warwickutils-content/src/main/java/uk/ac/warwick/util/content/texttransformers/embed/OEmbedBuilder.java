package uk.ac.warwick.util.content.texttransformers.embed;

public class OEmbedBuilder {
    private final OEmbed oembed = new OEmbed();

    public OEmbedBuilder withAutodiscovery(boolean autodiscovery) {
        oembed.setAutodiscovery(autodiscovery);
        return this;
    }

    /**
     * Adds the given providers to the map if the format is supported
     * 
     * @param provider
     * @return
     */
    public OEmbedBuilder withProviders(final OEmbedProvider... providers) {
        if (providers != null) {                
            for (OEmbedProvider provider: providers) {
                if (!this.oembed.getParser().containsKey(provider.getFormat()))
                    throw new IllegalArgumentException(String.format("Invalid format %s", provider.getFormat()));
                
                oembed.getProvider().put(provider.getName(), provider);
            }
        }
        return this;
    }

    public OEmbed build() {
        return oembed;
    }
}