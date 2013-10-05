Track customers directly in the J2EE Web Framework using Woopra's J2EE SDK

The SDK can be used both for front-end and back-end tracking. In either cases, the first step is to setup the tracker SDK in your Servlets. For example, if you want to set up tracking with Woopra on your homepage, the Servlet should look like:

``` java
public class Homepage extends HttpServlet {
   public void doGet(HttpServletRequest request, HttpServletResponse response) {
      WoopraTracker woopra = new WoopraTrackerEE(request);
      woopra.config(WoopraTracker.DOMAIN, "mybusiness.com");

      // Your code here...

      // When you're done setting up your WoopraTracker object, set an attribute containing the
      // value of woopra.jsCode() among all the other attributes you are passing to the jsp.
      request.setAttribute("jsCode", woopra.jsCode());
      this.getServletContext().getRequestDispatcher("/WEB-INF/homepage.jsp").forward(request, response);
```
You can also customize all the properties of the tracker by repeating that step with different parameters. For example, to also update your idle timeout (default: 30 seconds):
``` java
woopra.config(WoopraTracker.IDLE_TIMEOUT, 15000); // in milliseconds
```
Configuration can also be done in one single step, by adding all the properties you wish to configure to a 2D Array:
``` java
woopra.config(new Object[][] {
   {WoopraTracker.DOMAIN, "4ltrophy.campus.ecp.fr"},
   {WoopraTracker.IDLE_TIMEOUT, 15000},
   {WoopraTracker.PING, false},
});
```
To add custom visitor properties, you should pass a 2D Array to the <code>identify(String[][] user)</code> function:
``` java
woopra.identify(new String[][] {
   {"name", "User Name"},
   {"email", "user@company.com"},
   {"company", "User Business"}
});
```
If you wish to identify a user without any tracking event, don't forget to push() the update to Woopra:
``` java
woopra.identify(user).push();
//or, to push through back-end:
woopra.identify(user).push(true);
```
If you wish to track page views, just call <code>track()</code>:
``` java
woopra.track();
//Or, for back-end tracking, just add the optional parameter true:
woopra.track(true);
```
You can also track custom events through the front-end or the back-end. With all the previous steps done at once, your Servlet should look like:
``` java
public class Homepage extends HttpServlet {
   public void doGet(HttpServletRequest request, HttpServletResponse response) {
      WoopraTracker woopra = new WoopraTrackerEE(request);
      woopra.config(WoopraTracker.DOMAIN, "mybusiness.com").identify(user).track();
      // Create an event
      myEvent = new WoopraEvent("play");
      myEvent.setProperty("artist", "Dave Brubeck");
      myEvent.setProperty("song", "Take Five");
      myEvent.setProperty("genre", "Jazz");
      woopra.track(myEvent);
      // Track it through the front end...
      woopra.track(myEvent)
      // ... or through the back end by passing the optional param True
      woopra.track(myEvent, true)

      //Your code here...

      request.setAttribute("jsCode", woopra.jsCode());
      this.getServletContext().getRequestDispatcher("/WEB-INF/homepage.jsp").forward(request, response);
```
and print the jsCode in your jsp's header:
``` jsp
<head>
   ...
   <%= request.getAttribute("jsCode") %>
</head>
```
If you prefer, you can also track an event without even having to create a WoopraEvent Object:
``` java
woopra.track("play", new Object[][] {
   {"artist", "Dave Brubeck"},
   {"song", "Take Five"},
   {"genre", "Jazz"},
}, true);
```
Finally, if you wish to track your users only through the back-end, you should set the cookie on your user's browser. However, if you are planning to also use front-end tracking, don't even bother with that step, the JavaScript tracker will handle it for you.
``` java
woopra.setWoopraCookie(response)
// where response is the instance of HttpServletResponse
```
If you are using another Java Web Framework than J2EE, you should use the WoopraTracker class instead of the WoopraTrackerEE class. The constructor of WoopraTracker doesn't require an instance of HttpServletRequest. However, for the tracker to work properly, you should configure manually the domain, the cookieDomain, the cookieValue, and the ipAddress of the user being tracked:
``` python
woopra.config({WoopraTracker.DOMAIN:"mywebsite.com", WoopraTracker.COOKIE_DOMAIN:"mywebsite.com", WoopraTracker.COOKIE_VALUE:"COOKIEVALUE", WoopraTracker.IP_ADDRESS:"0.0.0.0"})
```
Instead of calling the setWoopraCookie(response) method to set the Woopra cookie on the user's browser, you should set it manually (this step depends on the Java-based Web Framework you are using).
