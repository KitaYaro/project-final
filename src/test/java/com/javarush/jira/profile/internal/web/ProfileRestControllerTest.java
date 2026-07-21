package com.javarush.jira.profile.internal.web;

import com.javarush.jira.AbstractControllerTest;
import com.javarush.jira.common.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.security.test.context.support.WithUserDetails;
import static com.javarush.jira.login.internal.web.UserTestData.USER_MAIL;
import org.springframework.http.MediaType;

class ProfileRestControllerTest extends AbstractControllerTest {


    @Test
    void getUnAuth() throws Exception {
        perform(MockMvcRequestBuilders.get(ProfileRestController.REST_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateUnAuth() throws Exception {
        perform(MockMvcRequestBuilders.put(ProfileRestController.REST_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void getProfileIsOk() throws Exception{
        perform(MockMvcRequestBuilders.get(ProfileRestController.REST_URL))
                .andExpect(status().isOk());
    }
    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateProfileIsNoContent() throws Exception{
        perform(MockMvcRequestBuilders.put(ProfileRestController.REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(ProfileTestData.getUpdatedTo())))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void updateProfileInvalidUnprocessableEntity() throws Exception{
        perform(MockMvcRequestBuilders.put(ProfileRestController.REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(ProfileTestData.getInvalidTo())))
                .andExpect(status().isUnprocessableEntity());
    }
}