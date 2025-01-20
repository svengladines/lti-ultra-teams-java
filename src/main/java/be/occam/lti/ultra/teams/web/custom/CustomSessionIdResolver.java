package be.occam.lti.ultra.teams.web.custom;

public class CustomSessionIdResolver /*implements HttpSessionIdResolver*/ {

    /*
    protected final CookieHttpSessionIdResolver cookieSessionIdResolver;
    protected final LTIService ltiService;
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public CustomSessionIdResolver(LTIService ltiService) {
        this.cookieSessionIdResolver = new CookieHttpSessionIdResolver();
        this.ltiService = ltiService;
    }

    @Override
    public List<String> resolveSessionIds(HttpServletRequest request) {
        List<String> ids = new ArrayList<>();
        ids.addAll(this.cookieSessionIdResolver.resolveSessionIds(request));
        logger.info("Session ids found by cookie: {}", ids);
        if (ids.isEmpty()) {
            Arrays.stream(request.getParameterValues("fiz")).findFirst().ifPresent(fiz -> {
                ids.add(fiz);
                logger.info("Session id found by fiz parameter: {}", fiz);
            });
        }
        return ids;
    }

    @Override
    public void setSessionId(HttpServletRequest request, HttpServletResponse response, String sessionId) {

    }

    @Override
    public void expireSession(HttpServletRequest request, HttpServletResponse response) {

    }
     */
}
