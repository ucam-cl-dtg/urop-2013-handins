package uk.ac.cam.sup.controllers;


public class BinControllerTest {
    /*
    final String testOwner = "at666";
    final String testQuestionSet = "/questionSet/666013";
    final String randomUser ="at667";
    final String perm1u = "fp628";
    final String perm2u = "sg235";

    Dispatcher dispatcher;
    Session session;

    Bin testBin;
    BinAccessPermission perm1, perm2;

    MockHttpRequest request;
    MockHttpResponse response;
    ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        dispatcher = MockDispatcherFactory.createDispatcher();
        dispatcher.getRegistry().addPerRequestResource(BinController.class);

        testBin = new Bin(testOwner,testQuestionSet);

        session = HibernateUtil.getTransaction();
        session.save(testBin);

        session.save(perm1 = new BinAccessPermission(testBin, perm1u));
        session.save(perm2 = new BinAccessPermission(testBin, perm2u));
        session.getTransaction().commit();

        session = HibernateUtil.getTransaction();

        response = new MockHttpResponse();
        mapper = new ObjectMapper();
    }

    @After
    public void tearDown() throws Exception {
        if (session.getTransaction().isActive())
            session.getTransaction().commit();

        session = HibernateUtil.getTransaction();
        session.createQuery("delete from BinAccessPermission").executeUpdate();
        session.createQuery("delete from Bin").executeUpdate();
        session.getTransaction().commit();
    }

    @Test
    public void testCreateBin() throws Exception {
        request = MockHttpRequest.post("/bin");
        request.addFormHeader("owner", randomUser);
        request.addFormHeader("questionSet", "/question/33");

        dispatcher.invoke(request, response);

        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());

        JsonNode resp = mapper.readValue(response.getContentAsString(), JsonNode.class);

        Assert.assertNotNull(resp.get("id"));
        Assert.assertNotNull(resp.get("token"));
    }

    @Test
    public void testDeleteBin() throws Exception {
        // At the moment bins shouldn't be deleted;

        request = MockHttpRequest.delete("/bin/" + testBin.getId() + "/?token=" + testBin.getToken());

        dispatcher.invoke(request, response);

        Assert.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    private List<String> getPermissions(long id) throws IOException, URISyntaxException {

        request = MockHttpRequest.get("/bin/" + testBin.getId() + "/permission");

        dispatcher.invoke(request, response);

        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());

        JsonNode resp = mapper.readValue(response.getContentAsString(), JsonNode.class);

        List<String> permissions = new LinkedList<String>();
        for (JsonNode perm : resp) {
            permissions.add(perm.asText());
        }
        return permissions;
    }


    @Test
    public void testListPermissions() throws Exception {

        List<String> permissions = getPermissions(testBin.getId());
        Assert.assertTrue(permissions.contains(perm1u));
        Assert.assertTrue(permissions.contains(perm2u));
    }

    @Test
    public void testAddPermissionsWithAuthorization () throws Exception {
        request = MockHttpRequest.post("/bin/" + testBin.getId() + "/permission");
        request.addFormHeader("token", testBin.getToken());
        request.addFormHeader("users[]", randomUser);

        dispatcher.invoke(request, response);
        session.getTransaction().commit();
        session = HibernateUtil.getTransaction();

        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());

        List<String> permissions = getPermissions(testBin.getId());
        Assert.assertTrue(permissions.contains(perm1u));
        Assert.assertTrue(permissions.contains(perm2u));
        Assert.assertTrue(permissions.contains(randomUser));

    }

    @Test
    public void testAddPermissionsWithOutAuthorization () throws Exception {
        request = MockHttpRequest.post("/bins/" + testBin.getId() + "/permissions");
        request.addFormHeader("token", "bAfsadaDas");
        request.addFormHeader("users", randomUser);

        dispatcher.invoke(request, response);
        session.getTransaction().commit();
        session = HibernateUtil.getTransaction();

        Assert.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());

        List<String> permissions = getPermissions(testBin.getId());
        Assert.assertTrue(permissions.contains(perm1u));
        Assert.assertTrue(permissions.contains(perm2u));
        Assert.assertFalse(permissions.contains(randomUser));
    }

    // Last two tests crash like hell

    @Test
    public void testDeletePermissionsWithAuthorization () throws Exception {
        request = MockHttpRequest.delete("/bins/" + testBin.getId() + "/permissions"
                + "?token=" + testBin.getToken()
                + "&users[]=" + perm1u);


        dispatcher.invoke(request, response);
        session.getTransaction().commit();
        session = HibernateUtil.getTransaction();

        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());

        List<String> permissions = getPermissions(testBin.getId());
        Assert.assertFalse(permissions.contains(perm1u));
        Assert.assertTrue(permissions.contains(perm2u));
    }

    @Test
    public void testDeletePermissionsWithOutAuthorization () throws Exception {
        request = MockHttpRequest.delete("/bin/" + testBin.getId() + "/permission"
                + "?token=" + "asdasdad"
                + "&users[]=" + perm1u);

        dispatcher.invoke(request, response);
        session.getTransaction().commit();
        session = HibernateUtil.getTransaction();

        Assert.assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());

        List<String> permissions = getPermissions(testBin.getId());
        Assert.assertTrue(permissions.contains(perm1u));
        Assert.assertTrue(permissions.contains(perm2u));
    }

    */
}
