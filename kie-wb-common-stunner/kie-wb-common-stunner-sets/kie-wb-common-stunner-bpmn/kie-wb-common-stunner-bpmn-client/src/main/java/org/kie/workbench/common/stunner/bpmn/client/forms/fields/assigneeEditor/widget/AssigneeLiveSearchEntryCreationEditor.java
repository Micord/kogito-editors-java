/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.bpmn.client.forms.fields.assigneeEditor.widget;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.kie.workbench.common.stunner.bpmn.client.forms.fields.i18n.StunnerBPMNConstants;
import org.uberfire.ext.widgets.common.client.dropdown.InlineCreationEditor;
import org.uberfire.ext.widgets.common.client.dropdown.LiveSearchEntry;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;

@Dependent
@JsType(namespace = JsPackage.GLOBAL, name = "AssigneeLiveSearch_JSInterop")
public class AssigneeLiveSearchEntryCreationEditor implements InlineCreationEditor<String>,
                                                              AssigneeLiveSearchEntryCreationEditorView.Presenter {

    private TranslationService translationService;
    private AssigneeLiveSearchEntryCreationEditorView view;

    private ParameterizedCommand<LiveSearchEntry<String>> okCommand;
    private Command cancelCommand;

    private ParameterizedCommand<String> customEntryCommand;

    private static String roles = "1&2&3&4";

    @Inject
    public AssigneeLiveSearchEntryCreationEditor(AssigneeLiveSearchEntryCreationEditorView view, TranslationService translationService) {
        this.view = view;
        this.translationService = translationService;

        view.init(this);
    }

    public void setCustomEntryCommand(ParameterizedCommand<String> customEntryCommand) {
        this.customEntryCommand = customEntryCommand;
    }

    @Override
    public void init(ParameterizedCommand<LiveSearchEntry<String>> okCommand, Command cancelCommand) {
        this.okCommand = okCommand;
        this.cancelCommand = cancelCommand;

        roles = getUrl();
        customAcceptRoles(roles);
    }

    public static native void logArray(String rolesArray) /*-{
        console.log(rolesArray);
    }-*/;

    public static native String getUrl()  /*-{
        console.log(query);
        var roles = query.split("&roles=")[1];
        console.log(roles);
        return roles;
    }-*/;

    @Override
    public void clear() {
        view.clear();
    }

    @Override
    public void customAcceptRoles(String roles) {
        logArray(roles);
        this.customEntryCommand.execute(roles);
        this.okCommand.execute(new LiveSearchEntry<>(roles, roles));


    }

    private String[] delimiterRoles(String roles){
        return roles.split("\\W");
    }

    @Override
    public void onAccept() {
        String value = view.getValue();
        if (isValid(value)) {
            this.customEntryCommand.execute(value);
            this.okCommand.execute(new LiveSearchEntry<>(value, value));
        }
    }

    @Override
    public void onCancel() {
        view.clear();
        cancelCommand.execute();
    }

    private boolean isValid(String value) {
        view.clearErrors();

        if (value == null || value.isEmpty()) {
            view.showError(translationService.getTranslation(StunnerBPMNConstants.ASSIGNEE_CANNOT_BE_EMPTY));
            return false;
        }

        return true;
    }

    @Override
    public HTMLElement getElement() {
        return view.getElement();
    }

    @Override
    public String getFieldLabel() {
        return translationService.getTranslation(StunnerBPMNConstants.ASSIGNEE_LABEL);
    }
}
