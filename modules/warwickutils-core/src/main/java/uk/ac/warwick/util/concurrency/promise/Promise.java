package uk.ac.warwick.util.concurrency.promise;

/**
 * A Promise is effectively a synchronous version of a Future. What
 * you're effectively saying to the code that you pass the Promise to is that
 * you agree to the contract that by the time the other code wants to use it,
 * the Promise will have been fulfilled.
 * <p>
 * An example of this is where you have a command to create a forum topic. This
 * might, internally, use a command to create the initial forum post, which
 * would usually require a topic to already exist. By passing the Promise of a
 * topic instead, you guarantee that by the time the post creation command gets
 * around to creating the post, the Promise of a topic will have been fulfilled.
 * <p>
 * In effect, any producer is itself a promise of the thing that it's producing.
 * The create post command is, effectively, a promise of a Post.
 */
public interface Promise<T> {
    
    T fulfilPromise() throws UnfulfilledPromiseException;

}
