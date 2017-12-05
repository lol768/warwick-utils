package uk.ac.warwick.util.mywarwick;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import play.api.Configuration;
import uk.ac.warwick.sso.client.core.OnCampusService;
import uk.ac.warwick.userlookup.*;
import uk.ac.warwick.userlookup.webgroups.GroupInfo;
import uk.ac.warwick.userlookup.webgroups.GroupNotFoundException;
import uk.ac.warwick.userlookup.webgroups.GroupServiceException;
import uk.ac.warwick.util.cache.Cache;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MyWarwickModuleTest {
    public static class TestModule extends AbstractModule {
        protected void configure() {
            // This emulates what Play does, providing a Configuration.
            bind(Configuration.class).toInstance(Configuration.apply(ConfigFactory.empty()));
            bind(GroupService.class).toInstance(new FakeGroupService());
            bind(UserLookupInterface.class).toInstance(new FakeUserLookupService());
        }
    }

    public static class EmptyConfigModule extends AbstractModule {
        protected void configure() {

        }

        // a rogue Config provider! Make sure it doesn't clash with the one in the module.
        @Provides
        public Config newConfig() {
            return ConfigFactory.empty();
        }
    }


    @Test
    public void noConflicts() {
        Injector injector = Guice.createInjector(new TestModule(), new MyWarwickModule());
    }

    @Test
    public void conflicts() {
        Injector injector = Guice.createInjector(new EmptyConfigModule(), new TestModule(), new MyWarwickModule());
    }

}

class FakeUserLookupService implements UserLookupInterface {

    @Override
    public List<User> getUsersInDepartment(String s) {
        return null;
    }

    @Override
    public List<User> getUsersInDepartmentCode(String s) {
        return null;
    }

    @Override
    public User getUserByToken(String s) {
        return null;
    }

    @Override
    public Map<String, User> getUsersByUserIds(List<String> list) {
        return null;
    }

    @Override
    public User getUserByWarwickUniId(String s) {
        return null;
    }

    @Override
    public User getUserByWarwickUniId(String s, boolean b) {
        return null;
    }

    @Override
    public List<User> findUsersWithFilter(Map<String, String> map) {
        return null;
    }

    @Override
    public List<User> findUsersWithFilter(Map<String, String> map, boolean b) {
        return null;
    }

    @Override
    public GroupService getGroupService() {
        return null;
    }

    @Override
    public OnCampusService getOnCampusService() {
        return null;
    }

    @Override
    public Map<String, Set<Cache<?, ?>>> getCaches() {
        return null;
    }

    @Override
    public void clearCaches() {

    }

    @Override
    public User getUserByIdAndPassNonLoggingIn(String s, String s1) throws UserLookupException {
        return null;
    }

    @Override
    public void requestClearWebGroup(String s) throws UserLookupException {

    }

    @Override
    public User getUserByUserId(String s) {
        return null;
    }
}

class FakeGroupService implements GroupService {

    @Override
    public List<Group> getGroupsForUser(String s) throws GroupServiceException {
        return null;
    }

    @Override
    public List<String> getGroupsNamesForUser(String s) throws GroupServiceException {
        return null;
    }

    @Override
    public boolean isUserInGroup(String s, String s1) throws GroupServiceException {
        return false;
    }

    @Override
    public List<String> getUserCodesInGroup(String s) throws GroupServiceException {
        return null;
    }

    @Override
    public List<Group> getRelatedGroups(String s) throws GroupServiceException {
        return null;
    }

    @Override
    public Group getGroupByName(String s) throws GroupNotFoundException, GroupServiceException {
        return null;
    }

    @Override
    public List<Group> getGroupsForDeptCode(String s) throws GroupServiceException {
        return null;
    }

    @Override
    public List<Group> getGroupsForQuery(String s) throws GroupServiceException {
        return null;
    }

    @Override
    public GroupInfo getGroupInfo(String s) throws GroupNotFoundException, GroupServiceException {
        return null;
    }

    @Override
    public Map<String, Set<Cache<?, ?>>> getCaches() {
        return null;
    }

    @Override
    public void clearCaches() {

    }

    @Override
    public void setTimeoutConfig(WebServiceTimeoutConfig webServiceTimeoutConfig) {

    }
}