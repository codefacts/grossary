package com.imslbd.grossary.template;

import com.imslbd.grossary.MyUris;
import com.imslbd.grossary.util.MyUtil;
import io.crm.web.ST;
import io.vertx.core.json.JsonObject;
import org.watertemplate.Template;

import java.util.Arrays;
import java.util.List;

/**
 * Created by someone on 27/10/2015.
 */
public class SidebarTmpt extends Template {
    private final List<JsonObject> links;

    {
        links = Arrays.asList(
                aLink(MyUris.DASHBOARD)
        );
    }

    public SidebarTmpt(final String currentPath) {
        links.forEach(link -> {
            if (currentPath.equals(link.getString(ST.uri))) {
                link.put(ST.active, "list-group-item-success");
            }
        });

        addCollection("links", links, (link, m) -> {
            m.add(ST.uri, link.getString(ST.uri));
            m.add(ST.label, link.getString(ST.label));
            m.add(ST.active, link.getString(ST.active));
        });
    }

    @Override
    protected String getFilePath() {
        return MyUtil.templatePath("sidebar.html");
    }

    private JsonObject aLink(MyUris dashboard) {
        return new JsonObject()
                .put(ST.uri, dashboard.value)
                .put(ST.label, dashboard.label);
    }
}
