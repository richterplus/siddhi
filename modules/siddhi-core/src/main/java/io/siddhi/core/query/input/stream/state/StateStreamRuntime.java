/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.siddhi.core.query.input.stream.state;

import io.siddhi.core.config.SiddhiQueryContext;
import io.siddhi.core.event.MetaComplexEvent;
import io.siddhi.core.event.state.MetaStateEvent;
import io.siddhi.core.event.state.StateEvent;
import io.siddhi.core.query.input.ProcessStreamReceiver;
import io.siddhi.core.query.input.stream.StreamRuntime;
import io.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import io.siddhi.core.query.input.stream.state.receiver.SequenceMultiProcessStreamReceiver;
import io.siddhi.core.query.input.stream.state.receiver.SequenceSingleProcessStreamReceiver;
import io.siddhi.core.query.input.stream.state.runtime.InnerStateRuntime;
import io.siddhi.core.query.processor.ProcessingMode;
import io.siddhi.core.query.processor.Processor;

import java.util.List;

/**
 * Stream Runtime implementation to represent {@link StateEvent}.
 */
public class StateStreamRuntime implements StreamRuntime {

    private MetaStateEvent metaStateEvent;
    private InnerStateRuntime innerStateRuntime;
    private SiddhiQueryContext siddhiQueryContext;

    public StateStreamRuntime(SiddhiQueryContext siddhiQueryContext, MetaStateEvent metaStateEvent) {
        this.siddhiQueryContext = siddhiQueryContext;
        this.metaStateEvent = metaStateEvent;
    }

    public List<SingleStreamRuntime> getSingleStreamRuntimes() {
        return innerStateRuntime.getSingleStreamRuntimeList();
    }

    @Override
    public StreamRuntime clone(String key) {
        StateStreamRuntime stateStreamRuntime = new StateStreamRuntime(siddhiQueryContext, metaStateEvent);
        stateStreamRuntime.innerStateRuntime = this.innerStateRuntime.clone(key);
        for (SingleStreamRuntime singleStreamRuntime : stateStreamRuntime.getSingleStreamRuntimes()) {
            ProcessStreamReceiver processStreamReceiver = singleStreamRuntime.getProcessStreamReceiver();
            if (processStreamReceiver instanceof SequenceMultiProcessStreamReceiver) {
                ((SequenceMultiProcessStreamReceiver) processStreamReceiver).setStateStreamRuntime(stateStreamRuntime);
            } else if (processStreamReceiver instanceof SequenceSingleProcessStreamReceiver) {
                ((SequenceSingleProcessStreamReceiver) processStreamReceiver).setStateStreamRuntime(stateStreamRuntime);
            }
        }
        ((StreamPreStateProcessor) stateStreamRuntime.innerStateRuntime.getFirstProcessor()).setThisLastProcessor(
                (StreamPostStateProcessor)
                        stateStreamRuntime.innerStateRuntime.getLastProcessor());
        return stateStreamRuntime;
    }

    @Override
    public void setCommonProcessor(Processor commonProcessor) {
        innerStateRuntime.setQuerySelector(commonProcessor);
        innerStateRuntime.init();
    }

    @Override
    public MetaComplexEvent getMetaComplexEvent() {
        return metaStateEvent;
    }

    @Override
    public ProcessingMode getProcessingMode() {
        return ProcessingMode.BATCH;
    }

    public InnerStateRuntime getInnerStateRuntime() {
        return innerStateRuntime;
    }

    public void setInnerStateRuntime(InnerStateRuntime innerStateRuntime) {
        this.innerStateRuntime = innerStateRuntime;
    }

    public void resetAndUpdate() {
        innerStateRuntime.reset();
        innerStateRuntime.update();
    }
}
