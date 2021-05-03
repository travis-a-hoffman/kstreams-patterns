package io.firkin.connectors;

/**
 * Provides some connector test functionality. Connector Error handling / behavior has been normalized
 * since Kafka 2.0.0. While the connect framework does implement most of this, note that it is up to
 * every connector to correctly implement all the features.
 *
 * See the following for more details on Connect Error Handling:
 *
 * <ul>
 *   <li>
 *     <a href="https://cwiki.apache.org/confluence/display/KAFKA/KIP-298%3A+Error+Handling+in+Connect">KIP-298</a>
 *   </li>
 *   <li>
 *     <a href="https://cwiki.apache.org/confluence/display/KAFKA/KIP-610%3A+Error+Reporting+in+Sink+Connectors">KIP-610</a>
 *   </li>
 * </ul>
 *
 * See the following for more details on KStreams Error Handling:
 *
 * <ul>
 *   <li>
 *     <a href="https://cwiki.apache.org/confluence/display/KAFKA/KIP-210+-+Provide+for+custom+error+handling++when+Kafka+Streams+fails+to+produce">KIP-210</a>
 *   </li>
 * </ul>
 */
public class ConnectorUtils {

}
