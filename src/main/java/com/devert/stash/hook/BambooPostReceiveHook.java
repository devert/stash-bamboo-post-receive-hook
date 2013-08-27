package com.devert.stash.hook;

import com.atlassian.stash.hook.repository.*;
import com.atlassian.stash.repository.*;
import com.atlassian.stash.setting.*;
import java.net.URL;
import java.util.Collection;
import java.net.URI;
import java.awt.Desktop;

/**
 * Note that hooks can implement RepositorySettingsValidator directly.
 */
public class BambooPostReceiveHook implements AsyncPostReceiveRepositoryHook, RepositorySettingsValidator {

    private static final String REFS_HEADS = "refs/heads/";
    private static final String BUILD_ACTION_PATH = "/updateAndBuild.action?buildKey=";

    /**
     * Connects to a configured URL to notify of all changes.
     */
    @Override
    public void postReceive(RepositoryHookContext context, Collection<RefChange> refChanges) {
        String bambooUrl = context.getSettings().getString("bambooUrl");
        String prodBranchName = REFS_HEADS + context.getSettings().getString("prodBranchName");
        String prodBuildKey = context.getSettings().getString("prodBuildKey");
        String devBranchName = REFS_HEADS + context.getSettings().getString("devBranchName");
        String devBuildKey = context.getSettings().getString("devBuildKey");
        String prodTriggerUrl = "";
        String devTriggerUrl = "";

        for (RefChange refChange : refChanges) {
            if(prodBranchName.equals(refChange.getRefId())) {
                prodTriggerUrl = bambooUrl + BUILD_ACTION_PATH + prodBuildKey;
            } else if (devBranchName.equals(refChange.getRefId())) {
                devTriggerUrl = bambooUrl + BUILD_ACTION_PATH + devBuildKey;
            }
        }

        if(prodTriggerUrl != "") {
            try {
                new URL(prodTriggerUrl).openConnection().getInputStream().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (devTriggerUrl != "") {
            try {
                new URL(devTriggerUrl).openConnection().getInputStream().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void validate(Settings settings, SettingsValidationErrors errors, Repository repository) {
        String bambooUrl = settings.getString("bambooUrl", "");
        String prodBranchName = settings.getString("prodBranchName", "");
        String prodBuildKey = settings.getString("prodBuildKey", "");
        String devBranchName = settings.getString("devBranchName", "");
        String devBuildKey = settings.getString("devBuildKey", "");

        if (bambooUrl.isEmpty()) {
            errors.addFieldError("bambooUrl", "Bamboo URL field is blank, please supply one");
        }
        if (prodBranchName.isEmpty() && devBranchName.isEmpty()) {
            errors.addFieldError("prodBranchName", "Must supply at least one branch name");
        }
        if (!prodBranchName.isEmpty()) {
            if(prodBuildKey.isEmpty()) {
                errors.addFieldError("prodBuildKey", "Must supply build key for production branch");
            }
        }
        if (!devBranchName.isEmpty()) {
            if(devBuildKey.isEmpty()) {
                errors.addFieldError("devBuildKey", "Must supply build key for development branch");
            }
        }
    }
}