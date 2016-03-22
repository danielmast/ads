package framework;

import java.io.Serializable;

/**
 * The authenticator value of the ADS. Is sent from the writer to the reader, enabling the reader to prove correctness
 * of query responses from the server.
 */
public abstract class Authenticator implements Serializable {
}
