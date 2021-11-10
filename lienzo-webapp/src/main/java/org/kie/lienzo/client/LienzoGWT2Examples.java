package org.kie.lienzo.client;

import com.google.gwt.core.client.EntryPoint;
import org.timepedia.exporter.client.ExporterUtil;

public class LienzoGWT2Examples extends BaseLienzoExamples implements EntryPoint {

    @Override
    public void onModuleLoad() {
        doLoad();
        ExporterUtil.exportAll();
    }
}
