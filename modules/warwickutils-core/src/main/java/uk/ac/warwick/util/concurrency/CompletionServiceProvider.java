package uk.ac.warwick.util.concurrency;

public interface CompletionServiceProvider {
    
    <T> CountingCompletionService<T> newCompletionService();

}
