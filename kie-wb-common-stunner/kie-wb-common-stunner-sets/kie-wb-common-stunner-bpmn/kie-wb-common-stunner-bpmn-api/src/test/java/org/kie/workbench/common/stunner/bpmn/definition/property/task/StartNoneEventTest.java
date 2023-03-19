/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.bpmn.definition.property.task;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Before;
import org.junit.Test;
import org.kie.workbench.common.stunner.bpmn.definition.StartNoneEvent;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.BPMNGeneralSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.Name;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StartNoneEventTest {

    private Validator validator;

    private static final String NAME_VALID = "Start Task";

    @Before
    public void init() {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        this.validator = vf.getValidator();
    }

    @Test
    public void testNameValid() {
        Name name = new Name(NAME_VALID);
        Set<ConstraintViolation<Name>> violations = this.validator.validate(name);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testNameEmpty() {
        Name name = new Name("");
        Set<ConstraintViolation<Name>> violations = this.validator.validate(name);
        assertEquals(1,
                     violations.size());
    }

    @Test
    public void testBPMNGeneralSetNameValid() {
        BPMNGeneralSet bpmnGeneralSet = new BPMNGeneralSet();
        bpmnGeneralSet.setName(new Name(NAME_VALID));
        Set<ConstraintViolation<BPMNGeneralSet>> violations = this.validator.validate(bpmnGeneralSet);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testBPMNGeneralSetNameEmpty() {
        BPMNGeneralSet bpmnGeneralSet = new BPMNGeneralSet();
        bpmnGeneralSet.setName(new Name(""));
        Set<ConstraintViolation<BPMNGeneralSet>> violations = this.validator.validate(bpmnGeneralSet);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testStartNoneEventNameValid() {
        StartNoneEvent startNoneEvent = new StartNoneEvent();
        startNoneEvent.getGeneral().setName(new Name(NAME_VALID));
        Set<ConstraintViolation<StartNoneEvent>> violations = this.validator.validate(startNoneEvent);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testStartNoneEventNameEmpty() {
        StartNoneEvent startNoneEvent = new StartNoneEvent();
        startNoneEvent.getGeneral().setName(new Name(""));
        Set<ConstraintViolation<StartNoneEvent>> violations = this.validator.validate(startNoneEvent);
        assertTrue(violations.isEmpty());
    }
}
