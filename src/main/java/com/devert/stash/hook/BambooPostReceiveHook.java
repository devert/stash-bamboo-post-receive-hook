package com.devert.stash.hook;

import com.atlassian.stash.hook.repository.*;
import com.atlassian.stash.repository.*;
import com.atlassian.stash.setting.*;
import java.net.URL;
import java.util.Collection;

/**
 * Note that hooks can implement RepositorySettingsValidator directly.
 */
public class BambooPostReceiveHook implements AsyncPostReceiveRepositoryHook, RepositorySettingsValidator {

    /**
     * Connects to a configured URL to notify of all changes.
     */
    @Override
    public void postReceive(RepositoryHookContext context, Collection<RefChange> refChanges) {
        String bambooUrl = context.getSettings().getString("bambooUrl");
        String buildKey = context.getSettings().getString("bambooBuildKey");
        String triggerUrl = bambooUrl + "/updateAndBuild.action?buildKey=" + buildKey;
        
        if (bambooUrl != null && buildKey != null) {
            try {
                new URL(triggerUrl).openConnection().getInputStream().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void validate(Settings settings, SettingsValidationErrors errors, Repository repository) {
        if (settings.getString("bambooUrl", "").isEmpty()) {
            errors.addFieldError("bambooUrl", "Bamboo URL field is blank, please supply one");
        }
        if (settings.getString("bambooBuildKey", "").isEmpty()) {
            errors.addFieldError("bambooBuildKey", "Bamboo Build Key field is blank, please supply one");
        }
    }
}