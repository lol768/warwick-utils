package uk.ac.warwick.util.virusscan;

import java.util.Optional;

public interface VirusScanResult {

    enum Status { error, virus, clean };

    Status getStatus();

    Optional<String> getVirus();

    Optional<String> getError();

}
