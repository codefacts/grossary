package com.imslbd.grossary.template.page;

import com.imslbd.grossary.util.MyUtil;
import io.crm.util.Util;
import org.watertemplate.Template;

/**
 * Created by shahadat on 2/13/16.
 */
public class CallCenterSupervisor extends Template {
    @Override
    protected String getFilePath() {
        return MyUtil.templatePath("/pages/call-center-supervisor.html");
    }
}
