    @Test
    void shouldIgnoreSavedErrorPageAfterLogin() throws Exception {
        MockHttpServletRequest original = new MockHttpServletRequest("GET", "/error");
        new HttpSessionRequestCache().saveRequest(original, new MockHttpServletResponse());

        MockHttpServletRequest loginRequest = new MockHttpServletRequest("POST", "/login");
        loginRequest.setSession(original.getSession());
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "u@example.com", null, AuthorityUtils.createAuthorityList("ROLE_CUSTOMER"));

        handler.onAuthenticationSuccess(loginRequest, response, auth);

        assertEquals("/", response.getRedirectedUrl());
    }