package uk.ac.warwick.util.virusscan;

import com.google.common.io.ByteSource;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public interface VirusScanService {

    /**
     * @throws IOException if there was a problem opening the input
     */
    CompletableFuture<VirusScanResult> scan(ByteSource in) throws IOException;

    CompletableFuture<VirusScanServiceStatus> status();

}
