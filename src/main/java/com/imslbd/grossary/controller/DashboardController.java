package com.imslbd.grossary.controller;

import com.imslbd.grossary.MyUris;
import com.imslbd.grossary.template.page.PageTmptBuilder;
import com.imslbd.grossary.template.page.SiteTmpt;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

/**
 * Created by someone on 06/12/2015.
 */
public class DashboardController {
    private final Vertx vertx;

    public DashboardController(Vertx vertx, Router router) {
        this.vertx = vertx;
        index(router);
    }

    private void index(Router router) {
        router.get(MyUris.DASHBOARD.value).handler(ctx -> {
            ctx.response().end(
                new PageTmptBuilder("Grocery")
                    .body(new SiteTmpt())
                    .build().render()
            );
        });
    }

    public static void main(String... args) {
        System.out.println(
            new PageTmptBuilder("Call Center")
                .body(new SiteTmpt())
                .build().render()
        );
    }
}
