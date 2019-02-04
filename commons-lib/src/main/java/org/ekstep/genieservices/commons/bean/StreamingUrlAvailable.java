package org.ekstep.genieservices.commons.bean;

/**
 * This class holds the identifier of the content and streamingUrl.
 */

public class StreamingUrlAvailable {

    private String identifier;
    private String streamingUrl;

    public StreamingUrlAvailable(String identifier, String streamingUrl) {
        this.identifier = identifier;
        this.streamingUrl = streamingUrl;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getStreamingUrl() {
        return streamingUrl;
    }

}
