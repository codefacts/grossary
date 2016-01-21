package com.imslbd.grossary.controller;

import com.imslbd.grossary.template.page.PageTmptBuilder;
import io.crm.web.template.TemplateUtil;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

import static io.crm.web.template.TemplateUtil.EMPTY_TEMPLATE;

/**
 * Created by shahadat on 1/20/16.
 */
public class SiteController {
    private final Vertx vertx;

    public SiteController(Vertx vertx, Router router) {
        this.vertx = vertx;
        index(router);
    }

    void index(Router router) {
        router.get("/site").handler(ctx -> {
            ctx.response().end(new PageTmptBuilder("Site Admin")
                .body(EMPTY_TEMPLATE).build().render());
        });
    }
}
