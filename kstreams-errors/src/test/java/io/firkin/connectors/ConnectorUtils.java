/*
 * Copyright © 2021 Travis Hoffman (travis@firkin.io)
 * Copyright © 2021 Firkin IO (https://firkin.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
