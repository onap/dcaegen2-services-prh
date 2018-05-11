/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.dcaegen2.services.prh.tasks;

import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/13/18
 */


public abstract class Task {

    private Task taskProcess;

    public void setNext(Task task) {
        this.taskProcess = task;
    }

    public void receiveRequest(Object body) throws PrhTaskException {

        Object response = execute(body);
        if (taskProcess != null) {
            taskProcess.receiveRequest(response);
        }
    }

    abstract Object execute(Object object) throws PrhTaskException;

    abstract void initConfigs();
}
