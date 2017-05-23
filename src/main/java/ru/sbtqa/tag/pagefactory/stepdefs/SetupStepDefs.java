package ru.sbtqa.tag.pagefactory.stepdefs;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.WebElement;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbtqa.tag.allure.TagAllureReporter;
import ru.sbtqa.tag.allurehelper.ParamsHelper;
import ru.sbtqa.tag.pagefactory.Page;
import ru.sbtqa.tag.pagefactory.PageFactory;
import ru.sbtqa.tag.pagefactory.annotations.ElementTitle;
import ru.sbtqa.tag.pagefactory.drivers.TagWebDriver;
import ru.sbtqa.tag.pagefactory.exceptions.FactoryRuntimeException;
import ru.sbtqa.tag.pagefactory.support.Environment;
import ru.sbtqa.tag.pagefactory.support.OnFailureScheduler;
import ru.sbtqa.tag.qautils.properties.Props;
import ru.sbtqa.tag.qautils.reflect.ClassUtilsExt;
import ru.sbtqa.tag.qautils.reflect.FieldUtilsExt;
import ru.sbtqa.tag.videorecorder.VideoRecorder;
import ru.yandex.qatools.htmlelements.element.HtmlElement;

public class SetupStepDefs {

    private static final Logger LOG = LoggerFactory.getLogger(SetupStepDefs.class);

    @Before()
    public void setUp() {

        //Apply failure callback
        TagAllureReporter.applyFailureCallback(OnFailureScheduler.class);

        //try to connect logger property file if exists
        String path = "src/test/resources/config/log4j.properties";
        if (new File(path).exists()) {
            PropertyConfigurator.configure(path);
            LOG.info("Log4j proprties were picked up on the path " + path);
        } else {
            LOG.warn("There is no log4j.properties on the path " + path);
        }

        try {
            String tasksToKill = Props.get("tasks.to.kill");
            if (!"".equals(tasksToKill)) {
                for (String task : tasksToKill.split(",")) {
                    if (SystemUtils.IS_OS_WINDOWS) {
                        Runtime.getRuntime().exec("taskkill /IM " + task.trim() + " /F");
                    } else {
                        Runtime.getRuntime().exec("killall " + task.trim());
                    }
                }
            }
        } catch (IOException e) {
            LOG.debug("Failed to kill one of task to kill", e);
        }

        PageFactory.getDriver();
        PageFactory.getInstance();    
    }

    @After
    public void tearDown() {
        if (PageFactory.isVideoRecorderEnabled() && VideoRecorder.getInstance().isVideoStarted()) {
            String videoPath = VideoRecorder.getInstance().stopRecording();
            if (videoPath != null) {
                ParamsHelper.addParam("Video url", VideoRecorder.getInstance().getVideoPath());
                VideoRecorder.getInstance().resetVideoRecorder();
            }
        }

        if (PageFactory.getEnvironment() == Environment.WEB && TagWebDriver.isWebDriverShared()) {
            return;
        } else {
            PageFactory.dispose();
        }
    }
}
