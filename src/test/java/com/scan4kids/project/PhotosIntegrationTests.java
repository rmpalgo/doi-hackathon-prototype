package com.scan4kids.project;

import com.scan4kids.project.daos.AlbumsRepository;
import com.scan4kids.project.daos.PhotosRepository;
import com.scan4kids.project.daos.UsersRepository;
import com.scan4kids.project.models.Album;
import com.scan4kids.project.models.Photo;
import com.scan4kids.project.models.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.HttpSession;


import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectApplication.class)
@AutoConfigureMockMvc
public class PhotosIntegrationTests {

    private User testUser;
    private Album testAlbum;
    private HttpSession httpSession;

    @Autowired
    private MockMvc mvc;

    @Autowired
    UsersRepository usersDao;

    @Autowired
    AlbumsRepository albumsDao;

    @Autowired
    PhotosRepository photosDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Before
    public void setup() throws Exception {

        testUser = usersDao.findByUsername("testUser");

        //Creates the test user if it does not exist
        if(testUser == null) {
            User newUser = new User();
            newUser.setUsername("testUser");
            newUser.setPassword(passwordEncoder.encode("pass"));
            newUser.setPasswordToConfirm(passwordEncoder.encode("pass"));
            newUser.setEmail("testUser@codeup.com");
            testUser = usersDao.save(newUser);
        }

        //Throws a POST request to /login and expect a redirection to the dashboard page after being logged in
        httpSession = this.mvc.perform(post("/login").with(csrf())
                .param("username", "testUser")
                .param("password", "pass"))
                .andExpect(status().is(HttpStatus.FOUND.value()))
                .andExpect(redirectedUrl("/dashboard"))
                .andReturn()
                .getRequest()
                .getSession();

    }

    @Test
    public void contextLoads() {
        //sanity test, to make sure the MVC bean is working
        assertNotNull(mvc);
    }

    @Test
    public void testIfUserSessionIsActive() throws Exception {
        //make sure the returned the user session is active and not null
        assertNotNull(httpSession);
    }

    @Test
    public void testCreatePhoto() throws Exception {

        setup();
        contextLoads();
        testIfUserSessionIsActive();

        //check to see if there's an existing album
        testAlbum = albumsDao.findByTitle("testAlbumTitle");
        //if not, create an album
        if(testAlbum == null) {
            Album newAlbum = new Album();
            newAlbum.setTitle("testAlbumTitle");
            newAlbum.setDescription("testAlbumDesc");
            testAlbum = albumsDao.save(newAlbum);
        }
        //Create a photo. make a POST request to /albums/{albumid}/photos/create and expect a redirection
        String someLink = "https://images.pexels.com/photos/761295/pexels-photo-761295.jpeg?auto=compress&cs=tinysrgb&dpr=2&w=500";
        this.mvc.perform(
                post("/albums/" + testAlbum.getId() + "/photos/create").with(csrf())
                        .session((MockHttpSession) httpSession)
                        //add all the required parameters to the request
                        .param("description", "genericDescription")
                        .param("link", someLink))
                .andExpect(status().is3xxRedirection());

    }
}