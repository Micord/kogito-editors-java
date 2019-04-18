/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.workbench.common.widgets.client.docks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;

@ApplicationScoped
@WorkbenchScreen(identifier = "org.docks.PlaceHolder")
public class DockPlaceHolder {

    @Inject
    private DockPlaceHolderView view;

    @WorkbenchPartTitle
    public String getTitle() {
        return "DockPlaceHolder"; // Never used.
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return view;
    }

    public void setView(final IsWidget widget) {
        view.clear();
        view.setWidget(widget);
    }
}
