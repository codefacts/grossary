package com.imslbd.grossary.template.page;

import com.imslbd.grossary.util.MyUtil;
import org.watertemplate.Template;

/**
 * Created by shahadat on 1/30/16.
 */
public class MyLoginTemplate extends Template {

    @Override
    protected String getFilePath() {
        return MyUtil.templatePath("/pages/my-login-form.html");
    }
}
