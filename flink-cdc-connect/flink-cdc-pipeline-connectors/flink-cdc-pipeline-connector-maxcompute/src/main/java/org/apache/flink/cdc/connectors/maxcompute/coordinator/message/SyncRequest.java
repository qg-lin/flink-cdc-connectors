/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.cdc.connectors.maxcompute.coordinator.message;

import org.apache.flink.runtime.operators.coordination.CoordinationRequest;

/**
 * A request signaling the {@link
 * org.apache.flink.cdc.connectors.maxcompute.coordinator.SessionManageCoordinator} to await
 * requests from all operators. Upon receiving this request from every operator, the coordinator
 * proceeds to send a response.
 */
public class SyncRequest implements CoordinationRequest {
    private static final long serialVersionUID = 1L;

    private final int operatorIndex;

    public SyncRequest(int operatorIndex) {
        this.operatorIndex = operatorIndex;
    }

    public int getOperatorIndex() {
        return operatorIndex;
    }
}
